package app.services;

import app.entities.Category;
import app.entities.Task;
import app.entities.Wedding;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BudgetServiceTest {

    private Wedding weddingWith(float budget, float... prices) {

        Wedding wedding = Wedding.builder().budget(budget).build();
        Category category = Category.builder().title("Venue").position(0).build();
        wedding.addCategory(category);

        for (float price : prices) {
            category.addTask(Task.builder().title("Task").price(price).build());
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

    @Test
    void totalSpentSumsAllTaskPrices() {
        assertEquals(3500.5, BudgetService.totalSpent(weddingWith(10000, 1000, 2500.5f)), 0.001);
    }

    // ________________________________________________________

    @Test
    void totalSpentIsZeroWhenThereAreNoTasks() {
        assertEquals(0, BudgetService.totalSpent(weddingWith(10000)), 0.001);
    }

    // ________________________________________________________

    @Test
    void remainingBudgetIsBudgetMinusSpent() {
        assertEquals(6500, BudgetService.remainingBudget(weddingWith(10000, 3500)), 0.001);
    }

    // ________________________________________________________

    @Test
    void remainingBudgetIsNegativeWhenSpendingExceedsBudget() {
        assertEquals(-500, BudgetService.remainingBudget(weddingWith(1000, 1500)), 0.001);
    }

    // ________________________________________________________

    @Test
    void budgetUsedPercentIsTheShareOfTheBudgetSpent() {
        assertEquals(25, BudgetService.budgetUsedPercent(weddingWith(2000, 500)), 0.001);
    }

    // ________________________________________________________

    @Test
    void budgetUsedPercentIsZeroWhenTheBudgetIsZero() {
        assertEquals(0, BudgetService.budgetUsedPercent(weddingWith(0, 500)), 0.001);
    }

    // ________________________________________________________

    @Test
    void budgetOverrunIsZeroWhenSpendingIsWithinTheBudget() {
        assertEquals(0, BudgetService.budgetOverrun(weddingWith(2000, 500)), 0.001);
    }

    // ________________________________________________________

    @Test
    void budgetOverrunIsTheAmountAboveTheBudget() {
        assertEquals(500, BudgetService.budgetOverrun(weddingWith(1000, 1500)), 0.001);
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