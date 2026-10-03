package app.services;

import app.SetupTest;
import app.entities.Category;
import app.exceptions.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.*;

class CategoryServiceTest extends SetupTest {

    private CategoryService categoryService;

    // ________________________________________________________

    @BeforeEach
    void setupCategoryServiceTest() {
        userDAO.create(testUser);
        weddingDAO.create(wedding);
        categoryDAO.create(category);

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
}
