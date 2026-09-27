package app.services;

import app.entities.Category;
import app.entities.Task;
import app.entities.Wedding;

public class BudgetService {

    public static double totalSpent(Wedding wedding) {
        if (wedding == null || wedding.getCategories() == null) {
            return 0;
        }
        return wedding.getCategories().stream()
                .filter(category -> category.getTasks() != null)
                .flatMap(category -> category.getTasks().stream())
                .mapToDouble(Task::getPrice)
                .sum();
    }

    // ________________________________________________________

    public static double totalSpentCategory(Category category){
        if (category == null || category.getTasks() == null){
            return  0;
        }
        return category.getTasks().stream()
                .mapToDouble(Task::getPrice)
                .sum();
        }



    // ________________________________________________________

    public static double remainingBudget(Wedding wedding) {
        return totalBudget(wedding) - totalSpent(wedding);
    }

    // ________________________________________________________

    public static double budgetUsedPercent(Wedding wedding) {
        double budget = totalBudget(wedding);
        if (budget <= 0) {
            return 0;
        }
        return totalSpent(wedding) / budget * 100;
    }

    // ________________________________________________________

    public static double budgetOverrun(Wedding wedding) {
        double over = totalSpent(wedding) - totalBudget(wedding);
        return over > 0 ? over : 0;
    }

    // ________________________________________________________

    private static double totalBudget(Wedding wedding) {
        return wedding == null ? 0 : wedding.getBudget();
    }
}
