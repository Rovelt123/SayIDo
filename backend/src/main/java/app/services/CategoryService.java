package app.services;

import app.daos.CategoryDAO;
import app.daos.WeddingDAO;
import app.entities.Category;
import app.entities.Wedding;
import app.enums.Categories;
import app.enums.Notifications;
import app.exceptions.ApiException;
import app.server.Setup;
import app.utils.ErrorHandler;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CategoryService {

    private final CategoryDAO categoryDAO;
    private final WeddingDAO weddingDAO;
    private final MessageService messageService = new MessageService();

    // ________________________________________________________

    public CategoryService() {
        this(new CategoryDAO(Setup.em), new WeddingDAO(Setup.em));
    }

    // ________________________________________________________

    public CategoryService(CategoryDAO categoryDAO, WeddingDAO weddingDAO) {
        this.categoryDAO = categoryDAO;
        this.weddingDAO = weddingDAO;
    }

    // ________________________________________________________

    public Category createCategory(UUID weddingId, UUID ownerId, Context ctx) {
        double categoryBudget = 0;

        Map<String, String> body = ErrorHandler.tryBodyMap(ctx, Notifications.BODY_EMPTY.getDisplayName());
        String title = ErrorHandler.tryString(body.get("title"), Notifications.CATEGORY_TITLE_REQUIRED.getDisplayName()).strip();
        if (body.get("categoryBudget") != null) {
            categoryBudget = ErrorHandler.tryParseDouble(body.get("categoryBudget"), "Category budget is invalid");
        }

        if (isUncategorized(title)) {
            throw new ApiException(400, Notifications.CATEGORY_NAME_RESERVED.getDisplayName());
        }

        Wedding wedding = ErrorHandler.tryEntity(weddingDAO.getByIdAndOwnerId(weddingId, ownerId), Notifications.WEDDING_NOT_FOUND.getDisplayName());
        int position = wedding.getCategories().stream().mapToInt(Category::getPosition).max().orElse(-1) + 1;
        Category category = Category.builder().title(title).position(position).categoryBudget(categoryBudget).build();
        wedding.addCategory(category);
        return categoryDAO.create(category);

    }

    // ________________________________________________________

    public Category updateCategory(UUID id, UUID ownerId, Context ctx) {

        Map<String, String> body = ErrorHandler.tryBodyMap(ctx, Notifications.BODY_EMPTY.getDisplayName());
        String title = ErrorHandler.tryString(body.get("title"), Notifications.CATEGORY_TITLE_REQUIRED.getDisplayName()).strip();

        Category category = ErrorHandler.tryEntity(categoryDAO.getByIdAndOwnerId(id, ownerId), Notifications.CATEGORY_NOT_FOUND.getDisplayName());
        if (isUncategorized(category.getTitle()) || isUncategorized(title)) {
            throw new ApiException(400, Notifications.CATEGORY_UNCATEGORIZED_RENAME.getDisplayName());
        }
        category.setTitle(title);

        if (body.get("categoryBudget") != null) {
            double categoryBudget = ErrorHandler.tryParseDouble(body.get("categoryBudget"), Notifications.CATEGORY_BUDGET_INVALID.getDisplayName());

            category.setCategoryBudget(categoryBudget);
        }
        return categoryDAO.update(category);
    }

    // ________________________________________________________

    public void deleteCategory(UUID id, UUID ownerId) {
        Category category = ErrorHandler.tryEntity(categoryDAO.getByIdAndOwnerId(id, ownerId), Notifications.CATEGORY_NOT_FOUND.getDisplayName());
        if (isUncategorized(category.getTitle())) {
            throw new ApiException(400, Notifications.CATEGORY_UNCATEGORIZED_DELETE.getDisplayName());
        }

        Category uncategorized = categoryDAO.getUncategorized(category.getWedding().getId(), ownerId);
        if (uncategorized == null) {
            uncategorized = Category.builder().title(Categories.UNCATEGORIZED.getDisplayName()).position(0).build();
        }
        categoryDAO.deleteAndMoveTasks(category, uncategorized);
    }

    // ________________________________________________________

    public Category moveCategory(UUID id, UUID ownerId, Context ctx) {

        Map<String, String> body = ErrorHandler.tryBodyMap(ctx, Notifications.BODY_EMPTY.getDisplayName());

        int position = ErrorHandler.tryParseInt(body.get("position"), Notifications.POSITION_INVALID.getDisplayName());

        Category category = ErrorHandler.tryEntity(categoryDAO.getByIdAndOwnerId(id, ownerId), Notifications.CATEGORY_NOT_FOUND.getDisplayName());
        List<Category> categories = orderedCategories(category.getWedding());
        if (position < 0 || position >= categories.size()) {
            throw new ApiException(400, messageService.buildMessage(Notifications.POSITION_RANGE, String.valueOf(categories.size() - 1)));
        }
        categories.remove(category);
        categories.add(position, category);
        categoryDAO.updatePositions(categories);
        return category;
    }

    // ________________________________________________________

    private List<Category> orderedCategories(Wedding wedding) {
        return new ArrayList<>(wedding.getCategories().stream()
                .sorted(Comparator.comparingInt(Category::getPosition).thenComparing(Category::getId)).toList());
    }

    // ________________________________________________________

    private boolean isUncategorized(String title) {
        return Categories.UNCATEGORIZED.getDisplayName().equalsIgnoreCase(title.strip());
    }
}
