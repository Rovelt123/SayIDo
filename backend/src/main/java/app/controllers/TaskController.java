package app.controllers;

import app.controllers.generic.BaseController;
import app.daos.TaskDAO;
import app.daos.CategoryDAO;
import app.dtos.TaskDTO;
import app.entities.Task;
import app.enums.Notifications;
import app.enums.Role;
import app.mappers.TaskMapper;
import app.server.Setup;
import app.services.TaskService;
import app.services.UserService;
import app.utils.ErrorHandler;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.javalin.apibuilder.ApiBuilder.*;

public class TaskController extends BaseController<Task, TaskDTO> {

    private final TaskDAO taskDAO = new TaskDAO(Setup.em);
    private final CategoryDAO categoryDAO = new CategoryDAO(Setup.em);
    private final TaskMapper taskMapper = new TaskMapper();
    private final TaskService taskService = new TaskService();
    private final UserService userService = new UserService();

    // ________________________________________________________

    public TaskController() {
        super(Task.class, new TaskMapper());
    }

    // ________________________________________________________

    public static EndpointGroup registerRoutes() {

        TaskController controller = new TaskController();

        return () -> {
            get("/categories/{categoryId}/tasks", controller::getAllTasks, Role.USER);
            get("/tasks/{id}", controller::getMyTaskByID, Role.USER);
            post("/categories/{categoryId}/tasks", controller::createTask, Role.USER);
            put("/tasks/{id}", controller::updateTask, Role.USER);
            patch("/tasks/{id}/status", controller::setStatus, Role.USER);
            patch("/tasks/{id}/position", controller::moveTask, Role.USER);
            delete("/tasks/{id}", controller::deleteTask, Role.USER);
        };
    }

    // ________________________________________________________

    @Override
    protected List<Task> getAllEntities() {
        return taskDAO.getAll();
    }

    // ________________________________________________________

    @Override
    protected Task getEntityById(UUID id) {
        return taskDAO.getById(id);
    }

    // ________________________________________________________

    public void getMyTaskByID(Context ctx) {
        UUID taskId = ErrorHandler.tryParseUUID(ctx.pathParam("id"), Notifications.TASK_ID_INVALID.getDisplayName());

        Task task = ErrorHandler.tryEntity(taskDAO.getByIdAndOwnerId(taskId, userService.getOwnerId(ctx)),
                Notifications.TASK_NOT_FOUND.getDisplayName());
        TaskDTO taskDTO = taskMapper.toDTO(task);
        respond(ctx, 200, messageService.buildMessage(Notifications.GET_BY_ID, "task", taskId.toString()), Map.of("data", taskDTO));
    }

    // ________________________________________________________


    public void getAllTasks(Context ctx) {

        UUID categoryId = ErrorHandler.tryParseUUID(ctx.pathParam("categoryId"), Notifications.CATEGORY_ID_INVALID.getDisplayName());
        ErrorHandler.tryEntity(categoryDAO.getByIdAndOwnerId(categoryId, userService.getOwnerId(ctx)), Notifications.CATEGORY_NOT_FOUND.getDisplayName());
        List<Task> entities = taskDAO.getAllByCategoryIdAndOwnerId(categoryId, userService.getOwnerId(ctx));
        List<TaskDTO> tasks = entities.stream().map(taskMapper::toDTO).toList();

        respond(ctx, 200, Notifications.TASK_GET_ALL.getDisplayName(), Map.of("data", tasks));
    }

    // ________________________________________________________

    public void createTask(Context ctx) {

        UUID categoryId = ErrorHandler.tryParseUUID(ctx.pathParam("categoryId"), Notifications.CATEGORY_ID_INVALID.getDisplayName());


        Task task = taskService.createTask(categoryId, userService.getOwnerId(ctx), ctx);

        respond(ctx, 201, Notifications.TASK_CREATED.getDisplayName(), Map.of("data", taskMapper.toDTO(task)));
    }

    // ________________________________________________________

    public void updateTask(Context ctx) {

        UUID id = ErrorHandler.tryParseUUID(ctx.pathParam("id"), Notifications.TASK_ID_INVALID.getDisplayName());

        Task task = taskService.updateTask(id, userService.getOwnerId(ctx), ctx);

        respond(ctx, 200, Notifications.TASK_UPDATED.getDisplayName(), Map.of("data", taskMapper.toDTO(task)));
    }

    // ________________________________________________________

    public void setStatus(Context ctx) {

        UUID id = ErrorHandler.tryParseUUID(ctx.pathParam("id"), Notifications.TASK_ID_INVALID.getDisplayName());

        Task task = taskService.setStatus(id, userService.getOwnerId(ctx), ctx);

        respond(ctx, 200, Notifications.TASK_STATUS_UPDATED.getDisplayName(), Map.of("data", taskMapper.toDTO(task)));
    }

    // ________________________________________________________

    public void deleteTask(Context ctx) {

        UUID id = ErrorHandler.tryParseUUID(ctx.pathParam("id"), Notifications.TASK_ID_INVALID.getDisplayName());

        taskService.deleteTask(id, userService.getOwnerId(ctx));

        respond(ctx, 200, Notifications.TASK_DELETED.getDisplayName(), null);
    }

    // ________________________________________________________

    public void moveTask(Context ctx) {

        UUID id = ErrorHandler.tryParseUUID(ctx.pathParam("id"), Notifications.TASK_ID_INVALID.getDisplayName());
        Task task = taskService.moveTask(id, userService.getOwnerId(ctx), ctx);
        respond(ctx, 200, Notifications.TASK_UPDATED.getDisplayName(), Map.of("data", taskMapper.toDTO(task)));

    }
}
