package app.services;

import app.daos.CategoryDAO;
import app.daos.TaskDAO;
import app.entities.Category;
import app.entities.Task;
import app.enums.Notifications;
import app.enums.Priority;
import app.enums.Status;
import app.exceptions.ApiException;
import app.server.Setup;
import app.utils.ErrorHandler;
import io.javalin.http.Context;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

public class TaskService {

    private final TaskDAO taskDAO;
    private final CategoryDAO categoryDAO;
    private final MessageService messageService = new MessageService();

    // ________________________________________________________

    public TaskService() {
        this(new TaskDAO(Setup.em), new CategoryDAO(Setup.em));
    }

    // ________________________________________________________

    public TaskService(TaskDAO taskDAO, CategoryDAO categoryDAO) {
        this.taskDAO = taskDAO;
        this.categoryDAO = categoryDAO;
    }

    // ________________________________________________________

    public Task createTask(UUID categoryId, UUID ownerId, Context ctx) {

        Map<String, String> body = ErrorHandler.tryBodyMap(ctx, Notifications.BODY_EMPTY.getDisplayName());
        Category category = ErrorHandler.tryEntity(categoryDAO.getByIdAndOwnerId(categoryId, ownerId), Notifications.CATEGORY_NOT_FOUND.getDisplayName());
        Task task = new Task();
        applyDetails(task, body);
        int position = category.getTasks().stream().mapToInt(Task::getPosition).max().orElse(-1) + 1;
        task.setPosition(position);
        category.addTask(task);
        return taskDAO.create(task);

    }

    // ________________________________________________________

    public Task updateTask(UUID id, UUID ownerId, Context ctx) {

        Map<String, String> body = ErrorHandler.tryBodyMap(ctx, Notifications.BODY_EMPTY.getDisplayName());
        Task task = ErrorHandler.tryEntity(taskDAO.getByIdAndOwnerId(id, ownerId), Notifications.TASK_NOT_FOUND.getDisplayName());
        applyDetails(task, body);
        return taskDAO.update(task);
    }

    // ________________________________________________________

    public Task setStatus(UUID id, UUID ownerId, Context ctx) {
        Map<String, String> body = ErrorHandler.tryBodyMap(ctx, Notifications.BODY_EMPTY.getDisplayName());
        Task task = ErrorHandler.tryEntity(taskDAO.getByIdAndOwnerId(id, ownerId), Notifications.TASK_NOT_FOUND.getDisplayName());
        Status status = ErrorHandler.tryParseEnum(Status.class, body.get("status"), Notifications.TASK_STATUS_INVALID.getDisplayName());
        task.setStatus(status);


        return taskDAO.update(task);
    }

    // ________________________________________________________

    public void deleteTask(UUID id, UUID ownerId) {
        Task task = ErrorHandler.tryEntity(taskDAO.getByIdAndOwnerId(id, ownerId), Notifications.TASK_NOT_FOUND.getDisplayName());
        taskDAO.deleteAndUpdatePositions(task);
    }

    // ________________________________________________________

    public Task moveTask(UUID id, UUID ownerId, Context ctx) {


        Map<String, String> body = ErrorHandler.tryBodyMap(ctx, Notifications.BODY_EMPTY.getDisplayName());
        int position = ErrorHandler.tryParseInt(body.get("position"), Notifications.POSITION_INVALID.getDisplayName());

        UUID targetId = body.get("categoryId") == null ? null : ErrorHandler.tryParseUUID(body.get("categoryId"), Notifications.CATEGORY_ID_INVALID.getDisplayName());

        Task task = ErrorHandler.tryEntity(taskDAO.getByIdAndOwnerId(id, ownerId), Notifications.TASK_NOT_FOUND.getDisplayName());
        Category source = task.getCategory();
        Category target = targetId == null ? source : ErrorHandler.tryEntity(
                categoryDAO.getByIdAndOwnerId(targetId, ownerId),
                Notifications.CATEGORY_NOT_FOUND.getDisplayName()
        );

        if (!source.getWedding().getId().equals(target.getWedding().getId())) {
            throw new ApiException(400, Notifications.TASK_MOVE_DIFFERENT_WEDDING.getDisplayName());
        }

        List<Task> targetTasks = orderedTasks(target);

        targetTasks.remove(task);

        if (position < 0 || position > targetTasks.size()) {
            throw new ApiException(400, messageService.buildMessage(Notifications.POSITION_RANGE, String.valueOf(targetTasks.size())));
        }

        targetTasks.add(position, task);
        return taskDAO.moveTask(task, target, targetTasks);

    }

    // ________________________________________________________

    private List<Task> orderedTasks(Category category) {
        return new ArrayList<>(category.getTasks().stream()
                .sorted(Comparator.comparingInt(Task::getPosition).thenComparing(Task::getId)).toList());
    }

    // ________________________________________________________

    private void applyDetails(Task task, Map<String, String> body) {

        String title = ErrorHandler.tryString(body.get("title"), Notifications.TASK_TITLE_REQUIRED.getDisplayName());

        float price = ErrorHandler.tryParseFloat(body.get("price"), Notifications.MUST_BE_FLOAT.getDisplayName());

        LocalDate deadline = null;

        String deadlineInput = body.get("deadline");

        if (deadlineInput != null && !deadlineInput.isBlank()) {
            deadline = ErrorHandler.tryParseLocalDate(deadlineInput, Notifications.TASK_DEADLINE_INVALID.getDisplayName());
        }
        
        String link = body.get("link");

        float hours = ErrorHandler.tryParseFloat(body.get("estimatedHours"), Notifications.TASK_ESTIMATE_INVALID.getDisplayName());

        Priority priority = body.get("priority") == null ? task.getPriority() : ErrorHandler.tryParseEnum(Priority.class, body.get("priority"), Notifications.TASK_PRIORITY_INVALID.getDisplayName());

        Status status = body.get("status") == null ? task.getStatus() : ErrorHandler.tryParseEnum(Status.class, body.get("status"), Notifications.TASK_STATUS_INVALID.getDisplayName());

        task.setTitle(title);
        task.setDescription(body.get("description"));
        task.setLink(link);
        task.setPrice(price);
        task.setDeadline(deadline);
        task.setEstimatedHours(hours);
        task.setPriority(priority);
        task.setStatus(status);
    }
}
