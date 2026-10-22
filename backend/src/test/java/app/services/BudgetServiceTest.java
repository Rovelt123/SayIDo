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
    void totalSpentReturnsZeroWhenWeddingIsNull() {
        double result = BudgetService.totalSpent(null);

        assertEquals(0, result, 0.001);
    }

    // ________________________________________________________

    @Test
    void totalSpentSumsPricesAcrossMultipleCategories() {
        Wedding wedding = weddingWith(10000, 1000, 2000);

        Category catering = categoryWith(1500);
        wedding.addCategory(catering);

        double result = BudgetService.totalSpent(wedding);

        assertEquals(4500, result, 0.001);
    }

    // ________________________________________________________

    @Test
    void totalSpentCategoryReturnsZeroWhenCategoryIsNull() {
        double result = BudgetService.totalSpentCategory(null);

        assertEquals(0, result, 0.001);
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
    void remainingBudgetReturnsZeroWhenWeddingIsNull() {
        double result = BudgetService.remainingBudget(null);

        assertEquals(0, result, 0.001);
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

    // --------------------------------------remainingCategoryBudget----------------

    @Test
    void remainingCategoryBudgetSubtractsSpending() {
        Category category = categoryWith(100, 200);
        category.setCategoryBudget(1000);
        double result = BudgetService.remainingCategoryBudget(category);
        assertEquals(700, result, 0.001);
    }

    // ------------------------------------------------------

    @Test
    void categoryBudgetUsedPercentCalculatesCorrectly() {

        Category category = categoryWith(100, 200);
        category.setCategoryBudget(1000);
        double result = BudgetService.categoryBudgetUsedPercent(category);
        assertEquals(30, result, 0.001);
    }

    // ------------------------------------------------------

    @Test
    void categoryBudgetUsedPercentReturnsZeroWhenBudgetIsZero(){

        Category category = categoryWith(100, 200);
        category.setCategoryBudget(0);
        double result = BudgetService.categoryBudgetUsedPercent(category);
        assertEquals(0, result, 0.001);
    }

    // ------------------------------------------------------

    @Test
    void budgetUsedPercentReturnsZeroWhenBudgetIsNegative() {
        Wedding wedding = weddingWith(-1000, 500);

        double result = BudgetService.budgetUsedPercent(wedding);

        assertEquals(0, result, 0.001);
    }

    // ------------------------------------------------------

    @Test
    void categoryBudgetOverrunReturnsZeroWhenSpendingIsWithinBudget() {
        Category category = categoryWith(100, 200);
        category.setCategoryBudget(500);
        double result = BudgetService.categoryBudgetOverrun(category);
        assertEquals(0, result, 0.001);
    }

    // ------------------------------------------------------

    @Test
    void categoryBudgetOverrunReturnsAmountAboveBudget() {
        Category category = categoryWith(100, 200, 300);
        category.setCategoryBudget(500);
        double result = BudgetService.categoryBudgetOverrun(category);
        assertEquals(100, result, 0.001);
    }

    // ------------------------------------------------------

    @Test
    void percentageOfTotalSpentCalculatesCorrectly() {

        Wedding wedding = weddingWith(20000, 4000);
        Category venue = wedding.getCategories().iterator().next();
        Category catering = categoryWith(6000);
        wedding.addCategory(catering);
        double result = BudgetService.percentageOfTotalSpent(venue);
        assertEquals(40, result, 0.001);
    }

    // ------------------------------------------------------

    @Test
    void percentageOfTotalSpentReturnsZeroWhenNothingSpent() {

        Wedding wedding = weddingWith(0);
        Category venue = wedding.getCategories().iterator().next();
        double result = BudgetService.percentageOfTotalSpent(venue);
        assertEquals(0, result, 0.001);
    }

    // ------------------------------------------------------
}