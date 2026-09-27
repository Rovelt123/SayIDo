package app.controllers;

import app.controllers.generic.BaseController;
import app.daos.CategoryDAO;
import app.daos.WeddingDAO;
import app.dtos.CategoryDTO;
import app.dtos.CategoryBudgetDTO;
import app.exceptions.ApiException;
import app.entities.Category;
import app.enums.Notifications;
import app.enums.Role;
import app.mappers.CategoryMapper;
import app.server.Setup;
import app.services.CategoryService;
import app.services.UserService;
import app.utils.ErrorHandler;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.javalin.apibuilder.ApiBuilder.*;

public class CategoryController extends BaseController<Category, CategoryDTO> {

    private final CategoryDAO categoryDAO = new CategoryDAO(Setup.em);
    private final WeddingDAO weddingDAO = new WeddingDAO(Setup.em);
    private final CategoryMapper categoryMapper = new CategoryMapper();
    private final CategoryService categoryService = new CategoryService();
    private final UserService userService = new UserService();

    // ________________________________________________________

    public CategoryController() {
        super(Category.class, new CategoryMapper());
    }

    // ________________________________________________________

    public static EndpointGroup registerRoutes() {

        CategoryController controller = new CategoryController();

        return () -> {
            get("/weddings/{weddingId}/categories", controller::getAllCategories, Role.USER);
            get("/categories/{id}", controller::getMyCategoryByID, Role.USER);
            post("/weddings/{weddingId}/categories", controller::createCategory, Role.USER);
            put("/categories/{id}", controller::updateCategory, Role.USER);
            patch("/categories/{id}/position", controller::moveCategory, Role.USER);
            patch("/weddings/{weddingId}/categories/{id}/budget", controller::updateBudget, Role.USER);
            delete("/categories/{id}", controller::deleteCategory, Role.USER);
        };
    }

    // ________________________________________________________

    @Override
    protected List<Category> getAllEntities() {
        return categoryDAO.getAll();
    }

    // ________________________________________________________

    @Override
    protected Category getEntityById(UUID id) {
        return categoryDAO.getById(id);
    }

    // ________________________________________________________

    public void getMyCategoryByID(Context ctx) {
        UUID categoryId = ErrorHandler.tryParseUUID(ctx.pathParam("id"), Notifications.CATEGORY_ID_INVALID.getDisplayName());

        Category category = ErrorHandler.tryEntity(categoryDAO.getByIdAndOwnerId(categoryId, userService.getOwnerId(ctx)),
                Notifications.CATEGORY_NOT_FOUND.getDisplayName());
        CategoryDTO categoryDTO = categoryMapper.toDTO(category);
        respond(ctx, 200, messageService.buildMessage(Notifications.GET_BY_ID, "category", categoryId.toString()), Map.of("data", categoryDTO));
    }

    // ________________________________________________________

    public void getAllCategories(Context ctx) {

        UUID weddingId = ErrorHandler.tryParseUUID(ctx.pathParam("weddingId"), Notifications.WEDDING_ID_INVALID.getDisplayName());

        ErrorHandler.tryEntity(weddingDAO.getByIdAndOwnerId(weddingId, userService.getOwnerId(ctx)), Notifications.WEDDING_NOT_FOUND.getDisplayName());
        List<Category> entities = categoryDAO.getAllByWeddingIdAndOwnerId(weddingId, userService.getOwnerId(ctx));
        List<CategoryDTO> categories = entities.stream().map(categoryMapper::toDTO).toList();

        respond(ctx, 200, Notifications.CATEGORY_GET_ALL.getDisplayName(),
        Map.of("data", categories));
    }

    // ________________________________________________________

    public void createCategory(Context ctx) {

        UUID weddingId = ErrorHandler.tryParseUUID(ctx.pathParam("weddingId"), Notifications.WEDDING_ID_INVALID.getDisplayName());

        Category category = categoryService.createCategory(weddingId, userService.getOwnerId(ctx), ctx);

        respond(ctx, 201, Notifications.CATEGORY_CREATED.getDisplayName(), Map.of("data", categoryMapper.toDTO(category)));
    }

    // ________________________________________________________

    public void updateCategory(Context ctx) {

        UUID id = ErrorHandler.tryParseUUID(ctx.pathParam("id"), Notifications.CATEGORY_ID_INVALID.getDisplayName());

        Category category = categoryService.updateCategory(id, userService.getOwnerId(ctx), ctx);

        respond(ctx, 200, Notifications.CATEGORY_UPDATED.getDisplayName(), Map.of("data", categoryMapper.toDTO(category)));
    }

    // ________________________________________________________

    public void deleteCategory(Context ctx) {

        UUID id = ErrorHandler.tryParseUUID(ctx.pathParam("id"), Notifications.CATEGORY_ID_INVALID.getDisplayName());

        categoryService.deleteCategory(id, userService.getOwnerId(ctx));

        respond(ctx, 200, Notifications.CATEGORY_DELETED.getDisplayName(), null);
    }

    // ________________________________________________________

    public void moveCategory(Context ctx) {

        UUID id = ErrorHandler.tryParseUUID(ctx.pathParam("id"), Notifications.CATEGORY_ID_INVALID.getDisplayName());

        Category category = categoryService.moveCategory(id, userService.getOwnerId(ctx), ctx);
        respond(ctx, 200, Notifications.CATEGORY_UPDATED.getDisplayName(), Map.of("data", categoryMapper.toDTO(category)));
    }

    // ________________________________________________________

    public void updateBudget(Context ctx) {
        UUID weddingId = ErrorHandler.tryParseUUID(ctx.pathParam("weddingId"), Notifications.WEDDING_ID_INVALID.getDisplayName());
        UUID id = ErrorHandler.tryParseUUID(ctx.pathParam("id"), Notifications.CATEGORY_ID_INVALID.getDisplayName());

        CategoryBudgetDTO body = ErrorHandler.tryBody(ctx, CategoryBudgetDTO.class,
                Notifications.CATEGORY_BUDGET_INVALID.getDisplayName());
        if (body == null) {
            throw new ApiException(400, Notifications.BODY_EMPTY.getDisplayName());
        }
        Category category = categoryService.updateBudget(weddingId, id, userService.getOwnerId(ctx), body.getCategoryBudget());
        respond(ctx, 200, Notifications.CATEGORY_UPDATED.getDisplayName(), Map.of("data", categoryMapper.toDTO(category)));
    }
}
