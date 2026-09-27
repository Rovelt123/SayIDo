package app.controllers;

import app.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class AdminController {

    // ________________________________________________________

    public static EndpointGroup registerRoutes() {
        UserController userController = new UserController();
        WeddingController weddingController = new WeddingController();
        CategoryController categoryController = new CategoryController();
        TaskController taskController = new TaskController();
        return () -> {

            get("/admin/users", userController::getAll, Role.ADMIN);
            get("/admin/users/{id}", userController::getByID, Role.ADMIN);

            get("/admin/weddings", weddingController::getAll, Role.ADMIN);
            get("/admin/weddings/{id}", weddingController::getByID, Role.ADMIN);
            post("/admin/users/{userId}/weddings", weddingController::createWedding, Role.ADMIN);
            put("/admin/weddings/{id}", weddingController::updateWedding, Role.ADMIN);
            delete("/admin/weddings/{id}", weddingController::deleteWedding, Role.ADMIN);

            get("/admin/categories", categoryController::getAll, Role.ADMIN);
            get("/admin/categories/{id}", categoryController::getByID, Role.ADMIN);
            get("/admin/weddings/{weddingId}/categories", categoryController::getAllCategories, Role.ADMIN);
            post("/admin/weddings/{weddingId}/categories", categoryController::createCategory, Role.ADMIN);
            put("/admin/categories/{id}", categoryController::updateCategory, Role.ADMIN);
            delete("/admin/categories/{id}", categoryController::deleteCategory, Role.ADMIN);
            patch("/admin/categories/{id}/position", categoryController::moveCategory, Role.ADMIN);

            get("/admin/tasks", taskController::getAll, Role.ADMIN);
            get("/admin/tasks/{id}", taskController::getByID, Role.ADMIN);
            get("/admin/categories/{categoryId}/tasks", taskController::getAllTasks, Role.ADMIN);
            post("/admin/categories/{categoryId}/tasks", taskController::createTask, Role.ADMIN);
            put("/admin/tasks/{id}", taskController::updateTask, Role.ADMIN);
            delete("/admin/tasks/{id}", taskController::deleteTask, Role.ADMIN);
            patch("/admin/tasks/{id}/position", taskController::moveTask, Role.ADMIN);
            patch("/admin/tasks/{id}/status", taskController::setStatus, Role.ADMIN);

        };
    }
}
