package app.services;

import app.entities.Category;
import app.entities.Task;
import app.entities.Wedding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.*;

public class BudgetServiceTest {

    private Wedding weddingFrom(float budget, String categories) {

        Wedding wedding = Wedding.builder().budget(budget).build();

        if (categories.equals("-")) {
            return wedding;
        }

        String[] groups = categories.split(";", -1);

        for (int i = 0; i < groups.length; i++) {

            Category category = Category.builder().title("Category " + (i + 1)).position(i).build();
            wedding.addCategory(category);

            if (groups[i].isEmpty()) {
                continue;
            }

            for (String price : groups[i].split("\\|")) {
                category.addTask(Task.builder().title("Task").price(Float.parseFloat(price)).build());
            }
        }
        return wedding;
    }

    // ________________________________________________________

    private Category categoryWith(float... prices) {
        Category category = Category.builder()
                .title("Venue")
                .position(0)
                .build();

        for (int i = 0; i < prices.length; i++) {
            category.addTask(
                    Task.builder()
                            .title("Task " + (i + 1))
                            .price(prices[i])
                            .build()
            );
        }

        return category;
    }

    // ________________________________________________________

    @ParameterizedTest(name = "[{index}] {6}")
    @CsvFileSource(resources = "/budget-test-cases.csv", numLinesToSkip = 1)
    void budgetCalculationsFollowTheTestCaseTable(float budget, String categories, double totalSpent, double remaining, double usedPercent, double overrun, String reason) {

        Wedding wedding = weddingFrom(budget, categories);

        assertEquals(totalSpent, BudgetService.totalSpent(wedding), 0.001);
        assertEquals(remaining, BudgetService.remainingBudget(wedding), 0.001);
        assertEquals(usedPercent, BudgetService.budgetUsedPercent(wedding), 0.001);
        assertEquals(overrun, BudgetService.budgetOverrun(wedding), 0.001);
    }


// ______________________________Category budget test__________________________

    @Test
    void totalSpentCategoryAllTaskPrices() {
        assertEquals(300, BudgetService.totalSpentCategory(categoryWith(100, 200)), 0.001);

    }


    // ________________________________________________________

    @Test
    void totalSpentCategoryIsZeroWhenNoTasksHavePrice() {
        assertEquals(
                0,
                BudgetService.totalSpentCategory(categoryWith()),
                0.001
        );
    }
}
