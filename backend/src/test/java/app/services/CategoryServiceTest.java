package app.services;

import app.SetupTest;
import app.entities.Category;
import app.enums.Categories;
import app.exceptions.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.*;

class CategoryServiceTest extends SetupTest {

    private CategoryService categoryService;
    private Category uncategorized;

    // ________________________________________________________

    @BeforeEach
    void setupCategoryServiceTest() {

        uncategorized = Category.builder()
                .title(Categories.UNCATEGORIZED.getDisplayName())
                .position(0)
                .wedding(wedding)
                .build();

        userDAO.create(testUser);
        weddingDAO.create(wedding);
        categoryDAO.create(category);
        categoryDAO.create(uncategorized);
        taskDAO.create(task);
        taskDAO.create(task2);

        em.flush();
        em.clear();

        categoryService = new CategoryService(categoryDAO, weddingDAO);
    }

    // ________________________________________________________

    @ParameterizedTest(name = "[{index}] {3} - {2}")
    @CsvFileSource(resources = "/category-budget-test-cases.csv", numLinesToSkip = 1)
    void categoryBudgetRulesFollowTheTestCaseTable(String value, boolean accepted, String reason, String story) {

        if (accepted) {
            Category updated = categoryService.updateBudget(
                    wedding.getId(), category.getId(), testUser.getId(), value);

            assertEquals(Double.parseDouble(value), updated.getCategoryBudget(), 0.001);
            return;
        }

        assertThrows(
                ApiException.class,
                () -> categoryService.updateBudget(
                        wedding.getId(), category.getId(), testUser.getId(), value)
        );
    }

    // ________________________________________________________

    @Test
    void theUncategorizedCategoryCannotBeDeleted() {

        assertThrows(
                ApiException.class,
                () -> categoryService.deleteCategory(uncategorized.getId(), testUser.getId())
        );
    }

    // ________________________________________________________

    @Test
    void deletingACategoryMovesItsTasksToUncategorized() {

        categoryService.deleteCategory(category.getId(), testUser.getId());

        em.flush();
        em.clear();

        Category moved = categoryDAO.getUncategorized(wedding.getId(), testUser.getId());

        assertEquals(2, moved.getTasks().size());
        assertTrue(moved.getTasks().stream()
                .anyMatch(movedTask -> movedTask.getTitle().equals(task.getTitle())));
    }
}
