
package app.services;

import app.daos.CategoryDAO;
import app.daos.TaskDAO;
import app.entities.Category;
import app.entities.Task;
import app.entities.Wedding;
import app.enums.Priority;
import app.enums.Status;
import app.exceptions.ApiException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    private TaskDAO taskDAO;
    private CategoryDAO categoryDAO;
    private TaskService taskService;

    private UUID ownerId;
    private Wedding wedding;
    private Category category;
    private Task task;

    @BeforeEach
    void setUp() {

        taskDAO = mock(TaskDAO.class);
        categoryDAO = mock(CategoryDAO.class);

        taskService = new TaskService(taskDAO, categoryDAO);

        ownerId = UUID.randomUUID();

        wedding = Wedding.builder()
                .id(UUID.randomUUID())
                .budget(1000)
                .build();

        category = Category.builder()
                .id(UUID.randomUUID())
                .title("Venue")
                .position(0)
                .build();

        wedding.addCategory(category);

        task = Task.builder()
                .id(UUID.randomUUID())
                .title("Book venue")
                .position(0)
                .price(500)
                .build();

        category.addTask(task);
    }

    // __________________________________________

    private Context mockContext(Map<String, String> body) {
        Context ctx = mock(Context.class);
        when(ctx.<Map<String, String>>bodyAsClass(new TypeReference<Map<String, String>>() {}.getType())).thenReturn(body);
        return ctx;
    }

    // __________________________________________

    @Test
    void createTaskAcceptsValidData() {

        Context ctx = mockContext(Map.of(
                "title", "Order flowers",
                "price", "250",
                "estimatedHours", "3"
        ));

        when(categoryDAO.getByIdAndOwnerId(
                category.getId(), ownerId
        )).thenReturn(category);

        when(taskDAO.create(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.createTask(
                category.getId(),
                ownerId,
                ctx
        );

        assertNotNull(result);
        assertEquals("Order flowers", result.getTitle());
        assertEquals(250, result.getPrice(), 0.001);
        assertEquals(3, result.getEstimatedHours(), 0.001);
        assertEquals(1, result.getPosition());
        assertSame(category, result.getCategory());

        verify(taskDAO).create(result);
    }

    // __________________________________________

    @Test
    void createTaskRejectsNonexistentCategory() {
        Context ctx = mockContext(Map.of(
                "title", "Order flowers",
                "price", "250",
                "estimatedHours", "3"
        ));

        ApiException exception = assertThrows(ApiException.class, () ->
                taskService.createTask(
                        category.getId(), ownerId, ctx
                )
        );

        assertEquals(404, exception.getStatus());
        verify(taskDAO, never()).create(any(Task.class));
    }

    // __________________________________________

    @Test
    void createTaskRejectsMissingTitle() {
        Context ctx = mockContext(Map.of(
                "price", "250",
                "estimatedHours", "3"
        ));

        when(categoryDAO.getByIdAndOwnerId(
                category.getId(), ownerId
        )).thenReturn(category);

        ApiException exception = assertThrows(ApiException.class, () ->
                taskService.createTask(
                        category.getId(), ownerId, ctx
                )
        );

        assertEquals(400, exception.getStatus());
        verify(taskDAO, never()).create(any(Task.class));
    }

    // __________________________________________

    @Test
    void createTaskRejectsBlankTitle() {
        Context ctx = mockContext(Map.of(
                "title", "   ",
                "price", "250",
                "estimatedHours", "3"
        ));

        when(categoryDAO.getByIdAndOwnerId(
                category.getId(), ownerId
        )).thenReturn(category);

        ApiException exception = assertThrows(ApiException.class, () ->
                taskService.createTask(
                        category.getId(), ownerId, ctx
                )
        );

        assertEquals(400, exception.getStatus());
        verify(taskDAO, never()).create(any(Task.class));
    }

    // __________________________________________

    @Test
    void createTaskRejectsInvalidPrice() {
        Context ctx = mockContext(Map.of(
                "title", "Order flowers",
                "price", "abc",
                "estimatedHours", "3"
        ));

        when(categoryDAO.getByIdAndOwnerId(
                category.getId(), ownerId
        )).thenReturn(category);

        ApiException exception = assertThrows(ApiException.class, () ->
                taskService.createTask(
                        category.getId(), ownerId, ctx
                )
        );

        assertEquals(400, exception.getStatus());
        verify(taskDAO, never()).create(any(Task.class));
    }

    // __________________________________________

    @Test
    void createTaskRejectsInvalidEstimatedHours() {
        Context ctx = mockContext(Map.of(
                "title", "Order flowers",
                "price", "250",
                "estimatedHours", "abc"
        ));

        when(categoryDAO.getByIdAndOwnerId(
                category.getId(), ownerId
        )).thenReturn(category);

        ApiException exception = assertThrows(ApiException.class, () ->
                taskService.createTask(
                        category.getId(), ownerId, ctx
                )
        );

        assertEquals(400, exception.getStatus());
        verify(taskDAO, never()).create(any(Task.class));
    }

    // __________________________________________

    @Test
    void createTaskRejectsInvalidDeadline() {
        Context ctx = mockContext(Map.of(
                "title", "Order flowers",
                "price", "250",
                "estimatedHours", "3",
                "deadline", "not-a-date"
        ));

        when(categoryDAO.getByIdAndOwnerId(
                category.getId(), ownerId
        )).thenReturn(category);

        ApiException exception = assertThrows(ApiException.class, () ->
                taskService.createTask(
                        category.getId(), ownerId, ctx
                )
        );

        assertEquals(400, exception.getStatus());
        verify(taskDAO, never()).create(any(Task.class));
    }

    // __________________________________________

    @Test
    void createTaskAcceptsValidOptionalDetails() {
        Context ctx = mockContext(Map.of(
                "title", "Order flowers",
                "price", "250",
                "estimatedHours", "3",
                "deadline", "2026-12-24",
                "priority", "HIGH",
                "status", "DONE",
                "description", "Order roses",
                "link", "https://example.com"
        ));

        when(categoryDAO.getByIdAndOwnerId(
                category.getId(), ownerId
        )).thenReturn(category);

        when(taskDAO.create(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.createTask(
                category.getId(), ownerId, ctx
        );

        assertEquals(LocalDate.of(2026, 12, 24), result.getDeadline());
        assertEquals(Priority.HIGH, result.getPriority());
        assertEquals(Status.DONE, result.getStatus());
        assertEquals("Order roses", result.getDescription());
        assertEquals("https://example.com", result.getLink());

        verify(taskDAO).create(result);
    }

    // __________________________________________

    @Test
    void createTaskUsesDefaultPriorityAndStatus() {
        Context ctx = mockContext(Map.of(
                "title", "Order flowers",
                "price", "250",
                "estimatedHours", "3"
        ));

        when(categoryDAO.getByIdAndOwnerId(
                category.getId(), ownerId
        )).thenReturn(category);

        when(taskDAO.create(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.createTask(
                category.getId(), ownerId, ctx
        );

        assertEquals(Priority.LOW, result.getPriority());
        assertEquals(Status.TODO, result.getStatus());

        verify(taskDAO).create(result);
    }

    // __________________________________________

    @Test
    void updateTaskAcceptsValidData() {

        Context ctx = mockContext(Map.of(
                "title", "Book new venue",
                "price", "750",
                "estimatedHours", "5"
        ));

        when(taskDAO.getByIdAndOwnerId(
                task.getId(), ownerId
        )).thenReturn(task);

        when(taskDAO.update(any(Task.class)))
                .thenReturn(task);

        Task result = taskService.updateTask(
                task.getId(),
                ownerId,
                ctx
        );

        assertNotNull(result);
        assertEquals("Book new venue", result.getTitle());
        assertEquals(750, result.getPrice(), 0.001);
        assertEquals(5, result.getEstimatedHours(), 0.001);

        verify(taskDAO).update(task);
    }

    // __________________________________________

    @Test
    void updateTaskRejectsNonexistentTask() {

        Context ctx = mockContext(Map.of(
                "title", "Book new venue",
                "price", "750",
                "estimatedHours", "5"
        ));

        ApiException exception = assertThrows(ApiException.class, () ->
                taskService.updateTask(
                        task.getId(),
                        ownerId,
                        ctx
                )
        );

        assertEquals(404, exception.getStatus());

        verify(taskDAO, never()).update(any(Task.class));
    }

    // __________________________________________

    @Test
    void updateTaskRejectsInvalidPrice() {

        Context ctx = mockContext(Map.of(
                "title", "Book new venue",
                "price", "invalid",
                "estimatedHours", "5"
        ));

        when(taskDAO.getByIdAndOwnerId(
                task.getId(), ownerId
        )).thenReturn(task);

        ApiException exception = assertThrows(ApiException.class, () ->
                taskService.updateTask(
                        task.getId(),
                        ownerId,
                        ctx
                )
        );

        assertEquals(400, exception.getStatus());

        assertEquals("Book venue", task.getTitle());
        assertEquals(500, task.getPrice(), 0.001);

        verify(taskDAO, never()).update(any(Task.class));
    }

    // __________________________________________

    @Test
    void updateTaskPreservesPriorityAndStatus() {

        task.setPriority(Priority.HIGH);
        task.setStatus(Status.DONE);

        Context ctx = mockContext(Map.of(
                "title", "Book new venue",
                "price", "750",
                "estimatedHours", "5"
        ));

        when(taskDAO.getByIdAndOwnerId(
                task.getId(), ownerId
        )).thenReturn(task);

        when(taskDAO.update(any(Task.class)))
                .thenReturn(task);

        Task result = taskService.updateTask(
                task.getId(),
                ownerId,
                ctx
        );

        assertEquals(Priority.HIGH, result.getPriority());
        assertEquals(Status.DONE, result.getStatus());

        verify(taskDAO).update(task);
    }

    // __________________________________________

    @Test
    void setStatusUpdatesTask() {
        Context ctx = mockContext(Map.of(
                "status", "DONE"
        ));

        when(taskDAO.getByIdAndOwnerId(
                task.getId(), ownerId
        )).thenReturn(task);

        when(taskDAO.update(task)).thenReturn(task);

        Task result = taskService.setStatus(
                task.getId(), ownerId, ctx
        );

        assertEquals(Status.DONE, result.getStatus());
        verify(taskDAO).update(task);
    }

    // __________________________________________

    @Test
    void setStatusRejectsInvalidStatus() {
        Context ctx = mockContext(Map.of(
                "status", "INVALID"
        ));

        when(taskDAO.getByIdAndOwnerId(
                task.getId(), ownerId
        )).thenReturn(task);

        ApiException exception = assertThrows(ApiException.class, () ->
                taskService.setStatus(task.getId(), ownerId, ctx)
        );

        assertEquals(400, exception.getStatus());

        verify(taskDAO, never()).update(any(Task.class));
    }

    // __________________________________________

    @Test
    void setStatusRejectsNonexistentTask() {
        Context ctx = mockContext(Map.of(
                "status", "DONE"
        ));

        ApiException exception = assertThrows(ApiException.class, () ->
                taskService.setStatus(task.getId(), ownerId, ctx)
        );

        assertEquals(404, exception.getStatus());

        verify(taskDAO, never()).update(any(Task.class));
    }

    // __________________________________________

    @Test
    void deleteTaskCallsDAO() {
        when(taskDAO.getByIdAndOwnerId(
                task.getId(), ownerId
        )).thenReturn(task);

        taskService.deleteTask(task.getId(), ownerId);

        verify(taskDAO).deleteAndUpdatePositions(task);
    }

    // __________________________________________

    @Test
    void deleteTaskRejectsNonexistentTask() {
        ApiException exception = assertThrows(ApiException.class, () ->
                taskService.deleteTask(task.getId(), ownerId)
        );

        assertEquals(404, exception.getStatus());

        verify(taskDAO, never())
                .deleteAndUpdatePositions(any(Task.class));
    }

    // __________________________________________

    @Test
    void moveTaskWithinSameCategory() {
        Task secondTask = Task.builder()
                .id(UUID.randomUUID())
                .title("Order decorations")
                .position(1)
                .build();

        category.addTask(secondTask);

        Context ctx = mockContext(Map.of(
                "position", "1"
        ));

        when(taskDAO.getByIdAndOwnerId(
                task.getId(), ownerId
        )).thenReturn(task);

        when(taskDAO.moveTask(
                eq(task), eq(category), anyList()
        )).thenReturn(task);

        Task result = taskService.moveTask(
                task.getId(), ownerId, ctx
        );

        assertSame(task, result);

        verify(taskDAO).moveTask(
                eq(task),
                eq(category),
                argThat(tasks ->
                        tasks.size() == 2
                                && tasks.get(0) == secondTask
                                && tasks.get(1) == task
                )
        );
    }

    // __________________________________________

    @Test
    void moveTaskRejectsNegativePosition() {
        Context ctx = mockContext(Map.of(
                "position", "-1"
        ));

        when(taskDAO.getByIdAndOwnerId(
                task.getId(), ownerId
        )).thenReturn(task);

        ApiException exception = assertThrows(ApiException.class, () ->
                taskService.moveTask(task.getId(), ownerId, ctx)
        );

        assertEquals(400, exception.getStatus());

        verify(taskDAO, never()).moveTask(
                any(Task.class),
                any(Category.class),
                anyList()
        );
    }

    // __________________________________________


    @Test
    void moveTaskRejectsDifferentWedding() {
        Wedding anotherWedding = Wedding.builder()
                .id(UUID.randomUUID())
                .budget(1000)
                .build();

        Category targetCategory = Category.builder()
                .id(UUID.randomUUID())
                .title("Flowers")
                .build();

        anotherWedding.addCategory(targetCategory);

        Context ctx = mockContext(Map.of(
                "position", "0",
                "categoryId", targetCategory.getId().toString()
        ));

        when(taskDAO.getByIdAndOwnerId(
                task.getId(), ownerId
        )).thenReturn(task);

        when(categoryDAO.getByIdAndOwnerId(
                targetCategory.getId(), ownerId
        )).thenReturn(targetCategory);

        ApiException exception = assertThrows(ApiException.class, () ->
                taskService.moveTask(task.getId(), ownerId, ctx)
        );

        assertEquals(400, exception.getStatus());

        verify(taskDAO, never()).moveTask(
                any(Task.class),
                any(Category.class),
                anyList()
        );
    }

    // __________________________________________

    @Test
    void moveTaskOrdersTasksByPosition() {
        Task secondTask = Task.builder()
                .id(UUID.randomUUID())
                .title("Second task")
                .position(2)
                .build();

        Task thirdTask = Task.builder()
                .id(UUID.randomUUID())
                .title("Third task")
                .position(1)
                .build();

        category.addTask(secondTask);
        category.addTask(thirdTask);

        Context ctx = mockContext(Map.of(
                "position", "0"
        ));

        when(taskDAO.getByIdAndOwnerId(
                task.getId(), ownerId
        )).thenReturn(task);

        taskService.moveTask(task.getId(), ownerId, ctx);

        verify(taskDAO).moveTask(
                eq(task),
                eq(category),
                argThat(tasks ->
                        tasks.size() == 3
                                && tasks.get(0) == task
                                && tasks.get(1) == thirdTask
                                && tasks.get(2) == secondTask
                )
        );
    }

}
