package app.services;

import app.daos.CategoryDAO;
import app.daos.WeddingDAO;
import app.entities.Category;
import app.entities.Wedding;
import app.exceptions.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.javalin.http.Context;

import java.util.*;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

class CategoryServiceTest {

    private FakeCategoryDAO categoryDAO;
    private FakeWeddingDAO weddingDAO;
    private CategoryService categoryService;

    private UUID ownerId;
    private Wedding wedding;
    private Category category;

    // ________________________________________________________


    @BeforeEach
    void setUp() {
        categoryDAO = new FakeCategoryDAO();
        weddingDAO = new FakeWeddingDAO();

        categoryService = new CategoryService(categoryDAO, weddingDAO);

        ownerId = UUID.randomUUID();

        wedding = Wedding.builder()
                .id(UUID.randomUUID())
                .budget(1000)
                .build();

        category = Category.builder()
                .id(UUID.randomUUID())
                .title("Venue")
                .categoryBudget(200)
                .build();

        wedding.addCategory(category);

        categoryDAO.categoryToReturn = category;
        weddingDAO.weddingToReturn = wedding;
    }

    // ________________________________________________________


    @Test
    void updateBudgetAcceptsValidBudget() {
        Category result = categoryService.updateBudget(
                wedding.getId(), category.getId(), ownerId, "500"
        );

        assertSame(category, result);
        assertEquals(500, result.getCategoryBudget(), 0.001);
        assertTrue(categoryDAO.updateCalled);
    }

    // ________________________________________________________


    @Test
    void updateBudgetRejectsBudgetAboveWeddingBudget() {
        assertThrows(ApiException.class, () ->
                categoryService.updateBudget(
                        wedding.getId(), category.getId(), ownerId, "1001"
                )
        );

        assertEquals(200, category.getCategoryBudget(), 0.001);
        assertFalse(categoryDAO.updateCalled);
    }

    // ________________________________________________________

    @Test
    void updateBudgetRejectsNonNumericBudget() {

        assertThrows(ApiException.class, () ->
                categoryService.updateBudget(
                        wedding.getId(),
                        category.getId(),
                        ownerId,
                        "abc"
                )
        );

        assertEquals(200, category.getCategoryBudget(), 0.001);
        assertFalse(categoryDAO.updateCalled);
    }


    // ________________________________________________________


    @Test
    void updateBudgetAcceptsBudgetEqualToWeddingBudget() {


        Category result = categoryService.updateBudget(
                wedding.getId(),
                category.getId(),
                ownerId,
                "1000"
        );


        assertEquals(1000, result.getCategoryBudget(), 0.001);
        assertTrue(categoryDAO.updateCalled);
    }

    // ________________________________________________________

    @Test
    void updateBudgetRejectsNegativeBudget() {
        assertThrows(ApiException.class, () ->
                categoryService.updateBudget(
                        wedding.getId(),
                        category.getId(),
                        ownerId,
                        "-100"
                )
        );

        assertEquals(200, category.getCategoryBudget(), 0.001);
        assertFalse(categoryDAO.updateCalled);
    }
    // ________________________________________________________


    @Test
    void updateBudgetRejectsCategoryFromAnotherWedding() {
        UUID anotherWeddingId = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () ->
                categoryService.updateBudget(
                        anotherWeddingId,
                        category.getId(),
                        ownerId,
                        "500"
                )
        );

        assertEquals(404, exception.getStatus());
        assertEquals(200, category.getCategoryBudget(), 0.001);
        assertFalse(categoryDAO.updateCalled);
    }

    // ________________________________________________________


    @Test
    void updateBudgetRejectsNonexistentCategory() {
        categoryDAO.categoryToReturn = null;

        ApiException exception = assertThrows(ApiException.class, () ->
                categoryService.updateBudget(
                        wedding.getId(),
                        UUID.randomUUID(),
                        ownerId,
                        "500"
                )
        );

        assertEquals(404, exception.getStatus());
        assertFalse(categoryDAO.updateCalled);
    }

    // ________________________________________________________


    @Test
    void updateBudgetRejectsNonFiniteBudget() {
        assertThrows(ApiException.class, () ->
                categoryService.updateBudget(
                        wedding.getId(),
                        category.getId(),
                        ownerId,
                        "NaN"
                )
        );

        assertEquals(200, category.getCategoryBudget(), 0.001);
        assertFalse(categoryDAO.updateCalled);
    }

    // ________________________________________________________


    @Test
    void updateBudgetAcceptsZeroBudget() {
        Category result = categoryService.updateBudget(
                wedding.getId(),
                category.getId(),
                ownerId,
                "0"
        );

        assertEquals(0, result.getCategoryBudget(), 0.001);
        assertTrue(categoryDAO.updateCalled);
    }

    // ________________________________________________________


    @Test
    void createCategoryAcceptsValidCategory() {

        Context ctx = mockContext(Map.of(
                "title", "Venue",
                "categoryBudget", "500"
        ));

        int expectedPosition = wedding.getCategories().stream()
                .mapToInt(Category::getPosition)
                .max().orElse(-1) + 1;

        Category result = categoryService.createCategory(
                wedding.getId(), ownerId, ctx
        );

        assertNotNull(result);
        assertEquals("Venue", result.getTitle());
        assertEquals(500, result.getCategoryBudget(), 0.001);
        assertSame(wedding, result.getWedding());
        assertEquals(expectedPosition, result.getPosition());
        assertTrue(wedding.getCategories().contains(result));
        assertTrue(categoryDAO.createCalled);
    }

    // ________________________________________________________

    @Test
    void createCategoryRejectsMissingTitle(){
        Context ctx = mockContext(Map.of(
                "categoryBudget", "500"
        ));
        ApiException exception = assertThrows(ApiException.class, () ->
                categoryService.createCategory(
                        wedding.getId(), ownerId, ctx
                )
        );

        assertEquals(400, exception.getStatus());
        assertFalse(categoryDAO.createCalled);
    }

    // ________________________________________________________

    @Test
    void createCategoryRejectsBlankTitle(){
        Context ctx = mockContext(Map.of(
                "title", "   ",
                "categoryBudget", "500"
        ));
        ApiException exception = assertThrows(ApiException.class, () ->
                categoryService.createCategory(
                        wedding.getId(), ownerId, ctx
                )
        );
        assertEquals(400, exception.getStatus());
        assertFalse(categoryDAO.createCalled);
    }

    // ________________________________________________________

    @Test
    void createCategoryRejectsReservedName(){
        Context ctx = mockContext(Map.of(
                "title", "Uncategorized",
                "categoryBudget", "500"
        ));
        ApiException exception = assertThrows(ApiException.class, () ->
                categoryService.createCategory(
                        wedding.getId(), ownerId, ctx
                )
        );
        assertEquals(400, exception.getStatus());
        assertFalse(categoryDAO.createCalled);
    }

    // ________________________________________________________

    @Test
    void createCategoryRejectIfCategoryBudgetLargerThanWeddingBudget(){

        Context ctx = mockContext(Map.of(
                "title", "Test",
                "categoryBudget", "1500"
        ));
        ApiException exception = assertThrows(ApiException.class, () ->
                categoryService.createCategory(
                        wedding.getId(), ownerId, ctx
                )
        );
        assertEquals(400, exception.getStatus());
        assertFalse(categoryDAO.createCalled);
    }

    // ________________________________________________________

    @Test
    void createCategoryIfCategoryBudgetSmallerThanWeddingBudget(){

        Context ctx = mockContext(Map.of(
                "title", "Test",
                "categoryBudget", "500"
        ));

        Category result = categoryService.createCategory(
                wedding.getId(), ownerId, ctx
        );

        assertEquals("Test",result.getTitle());
        assertEquals(500,result.getCategoryBudget());
        assertTrue(categoryDAO.createCalled);

    }
    // ________________________________________________________

    @Test
    void createCategoryDefaultsBudgetToZero(){
        Context ctx = mockContext(Map.of(
                "title", "Test"
        ));
        Category result = categoryService.createCategory(
                wedding.getId(), ownerId, ctx
        );
        assertEquals(0,result.getCategoryBudget());
        assertTrue(categoryDAO.createCalled);
    }
    // ________________________________________________________

    @Test
    void createCategoryStripsWhitespaceFromTitle(){
        Context ctx = mockContext(Map.of(
                "title", "    Test     "
        ));
        Category result = categoryService.createCategory(
                wedding.getId(), ownerId, ctx
        );
        assertEquals("Test",result.getTitle());
        assertTrue(categoryDAO.createCalled);
    }

    // ________________________________________________________

    @Test
    void createCategoryRejectsNonexistentWedding(){
        Context ctx = mockContext(Map.of(
                "title", "    Test     "
        ));
        weddingDAO.weddingToReturn = null;
        ApiException exception = assertThrows(ApiException.class, () ->
                categoryService.createCategory(
                        wedding.getId(), ownerId, ctx
                )
        );
        assertEquals(404, exception.getStatus());
        assertFalse(categoryDAO.createCalled);
    }
    // ________________________________________________________

    @Test
    void createCategoryUsesNextAvailablePosition() {

        wedding.getCategories().clear();

        Category first = Category.builder()
                .title("Food")
                .position(0)
                .build();

        Category second = Category.builder()
                .title("Music")
                .position(1)
                .build();

        Category third = Category.builder()
                .title("Venue")
                .position(2)
                .build();

        wedding.addCategory(first);
        wedding.addCategory(second);
        wedding.addCategory(third);

        Context ctx = mockContext(Map.of(
                "title", "Flowers",
                "categoryBudget", "200"
        ));

        Category result = categoryService.createCategory(
                wedding.getId(), ownerId, ctx
        );

        assertEquals(3, result.getPosition());
        assertSame(wedding, result.getWedding());
        assertTrue(wedding.getCategories().contains(result));
        assertTrue(categoryDAO.createCalled);
    }

    // ________________________________________________________

    @Test
    void updateCategoryAcceptsValidChanges() {
        Context ctx = mockContext(Map.of(
                "title", "Flowers",
                "categoryBudget", "500"
        ));

        Category result = categoryService.updateCategory(
                category.getId(), ownerId, ctx
        );

        assertSame(category, result);
        assertEquals("Flowers", result.getTitle());
        assertEquals(500, result.getCategoryBudget(), 0.001);
        assertTrue(categoryDAO.updateCalled);
    }

    @Test
    void updateCategoryRejectsMissingTitle() {
        Context ctx = mockContext(Map.of(
                "categoryBudget", "500"
        ));

        ApiException exception = assertThrows(ApiException.class, () ->
                categoryService.updateCategory(
                        category.getId(), ownerId, ctx
                )
        );

        assertEquals(400, exception.getStatus());
        assertFalse(categoryDAO.updateCalled);
    }

    @Test
    void updateCategoryRejectsBlankTitle() {
        Context ctx = mockContext(Map.of(
                "title", "   ",
                "categoryBudget", "500"
        ));

        ApiException exception = assertThrows(ApiException.class, () ->
                categoryService.updateCategory(
                        category.getId(), ownerId, ctx
                )
        );

        assertEquals(400, exception.getStatus());
        assertFalse(categoryDAO.updateCalled);
    }

    @Test
    void updateCategoryRejectsNonexistentCategory() {
        categoryDAO.categoryToReturn = null;

        Context ctx = mockContext(Map.of(
                "title", "Flowers",
                "categoryBudget", "500"
        ));

        ApiException exception = assertThrows(ApiException.class, () ->
                categoryService.updateCategory(
                        UUID.randomUUID(), ownerId, ctx
                )
        );

        assertEquals(404, exception.getStatus());
        assertFalse(categoryDAO.updateCalled);
    }

    @Test
    void updateCategoryRejectsRenamingUncategorized() {
        category.setTitle("Uncategorized");

        Context ctx = mockContext(Map.of(
                "title", "Flowers",
                "categoryBudget", "500"
        ));

        ApiException exception = assertThrows(ApiException.class, () ->
                categoryService.updateCategory(
                        category.getId(), ownerId, ctx
                )
        );

        assertEquals(400, exception.getStatus());
        assertEquals("Uncategorized", category.getTitle());
        assertFalse(categoryDAO.updateCalled);
    }

    @Test
    void updateCategoryRejectsReservedName() {
        Context ctx = mockContext(Map.of(
                "title", "Uncategorized",
                "categoryBudget", "500"
        ));

        ApiException exception = assertThrows(ApiException.class, () ->
                categoryService.updateCategory(
                        category.getId(), ownerId, ctx
                )
        );

        assertEquals(400, exception.getStatus());
        assertEquals("Venue", category.getTitle());
        assertFalse(categoryDAO.updateCalled);
    }

    @Test
    void updateCategoryKeepsBudgetWhenOmitted() {
        Context ctx = mockContext(Map.of(
                "title", "Flowers"
        ));

        Category result = categoryService.updateCategory(
                category.getId(), ownerId, ctx
        );

        assertEquals("Flowers", result.getTitle());
        assertEquals(200, result.getCategoryBudget(), 0.001);
        assertTrue(categoryDAO.updateCalled);
    }

    @Test
    void updateCategoryRejectsBudgetAboveWeddingBudget() {
        Context ctx = mockContext(Map.of(
                "title", "Flowers",
                "categoryBudget", "1500"
        ));

        ApiException exception = assertThrows(ApiException.class, () ->
                categoryService.updateCategory(
                        category.getId(), ownerId, ctx
                )
        );

        assertEquals(400, exception.getStatus());

        assertEquals("Venue", category.getTitle());
        assertEquals(200, category.getCategoryBudget(), 0.001);
        assertFalse(categoryDAO.updateCalled);
    }

    @Test
    void updateCategoryRejectsNegativeBudget() {
        Context ctx = mockContext(Map.of(
                "title", "Flowers",
                "categoryBudget", "-100"
        ));

        ApiException exception = assertThrows(ApiException.class, () ->
                categoryService.updateCategory(
                        category.getId(), ownerId, ctx
                )
        );

        assertEquals(400, exception.getStatus());
        assertEquals("Venue", category.getTitle());
        assertEquals(200, category.getCategoryBudget(), 0.001);
        assertFalse(categoryDAO.updateCalled);
    }

    @Test
    void updateCategoryRejectsInvalidBudget() {
        Context ctx = mockContext(Map.of(
                "title", "Flowers",
                "categoryBudget", "abc"
        ));

        ApiException exception = assertThrows(ApiException.class, () ->
                categoryService.updateCategory(
                        category.getId(), ownerId, ctx
                )
        );

        assertEquals(400, exception.getStatus());
        assertEquals("Venue", category.getTitle());
        assertEquals(200, category.getCategoryBudget(), 0.001);
        assertFalse(categoryDAO.updateCalled);
    }

    // ________________________________________________________


    @Test
    void deleteCategoryUsesExistingUncategorized() {

        Category uncategorized = Category.builder()
                .id(UUID.randomUUID())
                .title("Uncategorized")
                .position(0)
                .build();

        categoryDAO.uncategorizedToReturn = uncategorized;

        categoryService.deleteCategory(category.getId(), ownerId);

        assertTrue(categoryDAO.deleteAndMoveTasksCalled);
        assertSame(category, categoryDAO.deletedCategory);
        assertSame(uncategorized, categoryDAO.targetCategory);
    }

    // ________________________________________________________

    @Test
    void deleteCategoryCreatesUncategorizedWhenMissing() {

        categoryDAO.uncategorizedToReturn = null;

        categoryService.deleteCategory(category.getId(), ownerId);

        assertTrue(categoryDAO.deleteAndMoveTasksCalled);
        assertSame(category, categoryDAO.deletedCategory);

        assertNotNull(categoryDAO.targetCategory);
        assertEquals("Uncategorized",
                categoryDAO.targetCategory.getTitle());
        assertEquals(0,
                categoryDAO.targetCategory.getPosition());
    }

    // ________________________________________________________

    @Test
    void deleteCategoryRejectsDeletingUncategorized() {

        category.setTitle("Uncategorized");

        ApiException exception = assertThrows(ApiException.class, () ->
                categoryService.deleteCategory(
                        category.getId(), ownerId
                )
        );

        assertEquals(400, exception.getStatus());
        assertFalse(categoryDAO.deleteAndMoveTasksCalled);
    }

    // ________________________________________________________

    @Test
    void deleteCategoryRejectsNonexistentCategory() {

        categoryDAO.categoryToReturn = null;

        ApiException exception = assertThrows(ApiException.class, () ->
                categoryService.deleteCategory(
                        UUID.randomUUID(), ownerId
                )
        );

        assertEquals(404, exception.getStatus());
        assertFalse(categoryDAO.deleteAndMoveTasksCalled);
    }

    // ________________________________________________________


    @Test
    void moveCategoryToFirstPosition() {
        setupCategoriesForMoving();

        Context ctx = mockContext(Map.of("position", "0"));

        Category result = categoryService.moveCategory(
                category.getId(), ownerId, ctx
        );

        assertSame(category, result);
        assertTrue(categoryDAO.updatePositionsCalled);

        assertEquals("Venue", categoryDAO.updatedPositions.get(0).getTitle());
        assertEquals("Food", categoryDAO.updatedPositions.get(1).getTitle());
        assertEquals("Music", categoryDAO.updatedPositions.get(2).getTitle());

        assertEquals(0, result.getPosition());
    }

    // ________________________________________________________

    @Test
    void moveCategoryToLastPosition() {
        setupCategoriesForMoving();

        Category food = wedding.getCategories().stream()
                .filter(c -> c.getTitle().equals("Food"))
                .findFirst()
                .orElseThrow();

        categoryDAO.categoryToReturn = food;

        Context ctx = mockContext(Map.of("position", "2"));

        Category result = categoryService.moveCategory(
                food.getId(), ownerId, ctx
        );

        assertSame(food, result);
        assertTrue(categoryDAO.updatePositionsCalled);

        assertEquals("Music", categoryDAO.updatedPositions.get(0).getTitle());
        assertEquals("Venue", categoryDAO.updatedPositions.get(1).getTitle());
        assertEquals("Food", categoryDAO.updatedPositions.get(2).getTitle());

        assertEquals(2, result.getPosition());
    }

    // ________________________________________________________

    @Test
    void moveCategoryRejectsNegativePosition() {
        setupCategoriesForMoving();

        Context ctx = mockContext(Map.of("position", "-1"));

        ApiException exception = assertThrows(ApiException.class, () ->
                categoryService.moveCategory(
                        category.getId(), ownerId, ctx
                )
        );

        assertEquals(400, exception.getStatus());
        assertFalse(categoryDAO.updatePositionsCalled);
    }

    // ________________________________________________________

    @Test
    void moveCategoryRejectsPositionOutOfRange() {
        setupCategoriesForMoving();

        Context ctx = mockContext(Map.of("position", "3"));

        ApiException exception = assertThrows(ApiException.class, () ->
                categoryService.moveCategory(
                        category.getId(), ownerId, ctx
                )
        );

        assertEquals(400, exception.getStatus());
        assertFalse(categoryDAO.updatePositionsCalled);
    }

    // ________________________________________________________

    @Test
    void moveCategoryRejectsInvalidPosition() {
        setupCategoriesForMoving();

        Context ctx = mockContext(Map.of("position", "abc"));

        ApiException exception = assertThrows(ApiException.class, () ->
                categoryService.moveCategory(
                        category.getId(), ownerId, ctx
                )
        );

        assertEquals(400, exception.getStatus());
        assertFalse(categoryDAO.updatePositionsCalled);
    }

    // ________________________________________________________

    @Test
    void moveCategoryRejectsNonexistentCategory() {
        setupCategoriesForMoving();

        categoryDAO.categoryToReturn = null;

        Context ctx = mockContext(Map.of("position", "1"));

        ApiException exception = assertThrows(ApiException.class, () ->
                categoryService.moveCategory(
                        UUID.randomUUID(), ownerId, ctx
                )
        );

        assertEquals(404, exception.getStatus());
        assertFalse(categoryDAO.updatePositionsCalled);
    }

    // ________________________________________________________

    @Test
    void moveCategoryKeepsPositionWhenUnchanged() {
        setupCategoriesForMoving();

        Context ctx = mockContext(Map.of("position", "2"));

        Category result = categoryService.moveCategory(
                category.getId(), ownerId, ctx
        );

        assertSame(category, result);
        assertTrue(categoryDAO.updatePositionsCalled);

        assertEquals("Food", categoryDAO.updatedPositions.get(0).getTitle());
        assertEquals("Music", categoryDAO.updatedPositions.get(1).getTitle());
        assertEquals("Venue", categoryDAO.updatedPositions.get(2).getTitle());

        assertEquals(2, result.getPosition());
    }


    // ________________________________________________________
    // ________________________________________________________
    // ________________________________________________________

    private static class FakeCategoryDAO extends CategoryDAO {

        Category categoryToReturn;
        boolean updateCalled;
        boolean createCalled;
        Category uncategorizedToReturn;
        boolean deleteAndMoveTasksCalled;
        Category deletedCategory;
        Category targetCategory;
        boolean updatePositionsCalled;
        List<Category> updatedPositions;

        FakeCategoryDAO() {
            super(null);
        }
        // ________________________________________________________


        @Override
        public Category getByIdAndOwnerId(UUID id, UUID ownerId) {
            return categoryToReturn;
        }
        // ________________________________________________________


        @Override
        public Category update(Category category) {
            updateCalled = true;
            return category;
        }
        // ________________________________________________________
        @Override
        public Category create(Category category) {
            createCalled = true;
            return category;
        }
        // ________________________________________________________
        @Override
        public void deleteAndMoveTasks(Category category, Category uncategorized) {
            deleteAndMoveTasksCalled = true;
            deletedCategory = category;
            targetCategory = uncategorized;
        }
        // ________________________________________________________
        @Override
        public Category getUncategorized(UUID weddingId, UUID ownerId) {
            return uncategorizedToReturn;
        }
        // ________________________________________________________
        @Override
        public void updatePositions(List<Category> categories) {
            updatePositionsCalled = true;
            updatedPositions = new ArrayList<>(categories);

            for (int i = 0; i < categories.size(); i++) {
                categories.get(i).setPosition(i);
            }
        }
    }

    private static class FakeWeddingDAO extends WeddingDAO {

        Wedding weddingToReturn;

        FakeWeddingDAO() {
            super(null);
        }
        // ________________________________________________________


        @Override
        public Wedding getByIdAndOwnerId(UUID id, UUID ownerId) {
            return weddingToReturn;
        }
    }

    private Context mockContext(Map<String, String> body) {
        Context ctx = mock(Context.class);
        when(ctx.bodyAsClass(Map.class)).thenReturn(body);
        return ctx;
    }

    private void setupCategoriesForMoving() {
        wedding.getCategories().clear();

        Category food = Category.builder()
                .id(UUID.randomUUID())
                .title("Food")
                .position(0)
                .build();

        Category music = Category.builder()
                .id(UUID.randomUUID())
                .title("Music")
                .position(1)
                .build();

        Category venue = Category.builder()
                .id(UUID.randomUUID())
                .title("Venue")
                .position(2)
                .build();

        wedding.addCategory(food);
        wedding.addCategory(music);
        wedding.addCategory(venue);

        category = venue;
        categoryDAO.categoryToReturn = venue;
    }

}