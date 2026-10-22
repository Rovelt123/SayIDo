
package app.services;

import app.SetupTest;
import app.entities.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.javalin.http.Context;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceIntegrationTest extends SetupTest {

    private CategoryService categoryService;

    @BeforeEach
    void setupIntegrationTest() {
        categoryService = new CategoryService(
                categoryDAO,
                weddingDAO
        );

        // Save our test entities
        em.persist(testUser);
        em.persist(wedding);
        em.persist(category);

        em.flush();
        em.clear();
    }

    // ________________________________________________________

    @Test
    void updateBudgetPersistChanges() {
        double newBudget = 50;

        categoryService.updateBudget(
                wedding.getId(),
                category.getId(),
                testUser.getId(),
                String.valueOf(newBudget));

        em.flush();
        em.clear();

        Category updatedCategory = em.find(
                Category.class,
                category.getId()
        );
        assertNotNull(updatedCategory);
        assertEquals(
                newBudget,
                updatedCategory.getCategoryBudget(),
                0.001
        );
    }

    // ________________________________________________________

    @Test
    void createCategoryPersistsToDatabase(){
        Context ctx = mockContext(Map.of(
                "title", "Venue",
                "categoryBudget", "50"
        ));
        Category result= categoryService.createCategory(
                wedding.getId(),
                testUser.getId(),
                ctx);

        em.flush();
        em.clear();

        Category savedCategory = em.find(
                Category.class,
                result.getId()
        );

        assertNotNull(savedCategory);
        assertEquals("Venue", savedCategory.getTitle());
        assertEquals(50, savedCategory.getCategoryBudget(), 0.001);
        assertEquals(wedding.getId(), savedCategory.getWedding().getId());
    }

    // ________________________________________________________


    @Test
    void updateCategoryPersistsToDatabase() {

        Context ctx = mockContext(Map.of(
                "title", "VenueChanged",
                "categoryBudget", "50"
        ));

        categoryService.updateCategory(
                category.getId(),
                testUser.getId(),
                ctx
        );

        em.flush();
        em.clear();

        Category updatedCategory = em.find(
                Category.class,
                category.getId()
        );

        assertNotNull(updatedCategory);
        assertEquals("VenueChanged", updatedCategory.getTitle());
        assertEquals(50, updatedCategory.getCategoryBudget(), 0.001);
        assertEquals(wedding.getId(), updatedCategory.getWedding().getId());
    }

    // ________________________________________________________


    @Test
    void deleteCategoryMovesTasksToUncategorized() {


        UUID categoryId = category.getId();
        UUID weddingId = wedding.getId();
        UUID ownerId = testUser.getId();

        Category managedCategory = em.find(Category.class, categoryId);


        managedCategory.addTask(task);
        managedCategory.addTask(task2);

        em.flush();
        em.clear();


        categoryService.deleteCategory(categoryId, ownerId);

        em.flush();
        em.clear();


        Category deletedCategory = em.find(Category.class, categoryId);
        assertNull(deletedCategory);


        Category uncategorized = categoryDAO.getUncategorized(
                weddingId, ownerId
        );

        assertNotNull(uncategorized);
        assertEquals("Uncategorized", uncategorized.getTitle());

        assertEquals(2, uncategorized.getTasks().size());

        assertTrue(uncategorized.getTasks().stream()
                .anyMatch(t -> t.getTitle().equals("find catering company")));

        assertTrue(uncategorized.getTasks().stream()
                .anyMatch(t -> t.getTitle().equals("Decide menu")));

        assertEquals(weddingId, uncategorized.getWedding().getId());
    }



    private Context mockContext(Map<String, String> body) {
        Context ctx = mock(Context.class);
        when(ctx.bodyAsClass(Map.class)).thenReturn(body);
        return ctx;
    }
}


