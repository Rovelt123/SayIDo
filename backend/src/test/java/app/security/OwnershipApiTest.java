package app.security;

import app.configs.TestHibernateConfig;
import app.entities.Category;
import app.entities.Task;
import app.entities.Wedding;
import app.enums.Role;
import app.enums.Notifications;
import app.mappers.TaskMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.mappers.UserMapper;
import app.server.Setup;

import app.entities.User;
import app.services.PasswordService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.Duration;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

//TestInstance makes it such that one does not need to make methods like @BeforeAll and @AfterAll static
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OwnershipApiTest {

    private UUID taskAId;
    private UUID taskBId;

    private UUID weddingAId;
    private UUID weddingBId;

    private EntityManagerFactory emf;
    private EntityManager serverEm;

    private Setup testServerSetup;
    private String baseUrl;
    private HttpClient client;
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final TaskMapper taskMapper = new TaskMapper();
    private UUID categoryAId;
    private UUID categoryBId;
    private UUID siblingAId;
    private UUID siblingBId;

    private static final String TASK_DETAILS = """
            {"title":"Updated catering","description":"Updated description",
             "price":"125.5","estimatedHours":"3.5","deadline":"2026-12-01",
             "link":"https://example.com/catering","priority":"HIGH","status":"IN_PROGRESS"}
            """;

    private String tokenA;
    private String tokenB;

    private final SecurityService securityService = new SecurityService();
    private final UserMapper userMapper = new UserMapper();

    // ________________________________________________________

    @BeforeAll
    void serverSetup() {

        emf = TestHibernateConfig.getTestEmf();
        serverEm = emf.createEntityManager();

        testServerSetup = new Setup(serverEm, 0);
        testServerSetup.initialize();
        baseUrl = "http://localhost:" + testServerSetup.getApp().port() + "/api";
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    // ________________________________________________________

    @BeforeEach
    void setupTestData() {

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            //Setup of user A and B
            User userA = User.builder()
                    .firstname("John")
                    .lastname("Doe")
                    .email("john123@test.dk")
                    .password(PasswordService.hashHelper("123"))
                    .roles(Set.of(Role.USER))
                    .build();

            User userB = User.builder()
                    .firstname("Gert")
                    .lastname("Hansen")
                    .email("gert123@test.dk")
                    .password(PasswordService.hashHelper("123"))
                    .roles(Set.of(Role.USER))
                    .build();

            em.persist(userA);
            em.persist(userB);

            //Create wedding, categories and tasks and give them to user A and B
            Wedding weddingA = Wedding.builder()
                    .title("John's Wedding")
                    .date(LocalDate.of(2026, 12, 24))
                    .location("Lyngby")
                    .budget(1000)
                    .build();

            weddingA.setOwner(userA);

            Wedding weddingB = Wedding.builder()
                    .title("Gert's Wedding")
                    .date(LocalDate.of(2026, 11, 11))
                    .location("Virum")
                    .budget(2000)
                    .build();

            weddingB.setOwner(userB);

            em.persist(weddingA);
            em.persist(weddingB);

            weddingAId = weddingA.getId();
            weddingBId = weddingB.getId();

            Category categoryA = Category.builder()
                    .title("Food and Drink")
                    .position(1)
                    .wedding(weddingA)
                    .build();

            Category categoryB = Category.builder()
                    .title("Food and Drink")
                    .position(1)
                    .wedding(weddingB)
                    .build();

            em.persist(categoryA);
            em.persist(categoryB);

            Task taskA = Task.builder()
                    .title("Find catering")
                    .position(0)
                    .category(categoryA)
                    .build();

            Task taskB = Task.builder()
                    .title("Decide menu")
                    .position(0)
                    .category(categoryB)
                    .build();

            em.persist(taskA);
            em.persist(taskB);

            Task siblingA = Task.builder().title("A second task").position(1).category(categoryA).build();
            Task siblingB = Task.builder().title("B second task").position(1).category(categoryB).build();

            em.persist(siblingA);
            em.persist(siblingB);

            siblingAId = siblingA.getId();
            siblingBId = siblingB.getId();

            categoryAId = categoryA.getId();
            categoryBId = categoryB.getId();

            taskAId = taskA.getId();
            taskBId = taskB.getId();

            //Create tokens
            tokenA = securityService.createToken(userMapper.toDTO(userA));
            tokenB = securityService.createToken(userMapper.toDTO(userB));

            em.getTransaction().commit();
        }
    }

    // ________________________________________________________

    @AfterEach
    void cleanTestData() {

        if (serverEm.getTransaction().isActive()) {
            serverEm.getTransaction().rollback();
        }
        serverEm.clear();

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            em.createQuery("DELETE FROM Task").executeUpdate();
            em.createQuery("DELETE FROM Category").executeUpdate();
            em.createQuery("DELETE FROM Wedding").executeUpdate();

            // Entity removal also cleans up the roles element collection.
            em.createQuery("SELECT u FROM User u", User.class).getResultList().forEach(em::remove);

            em.getTransaction().commit();
        }
    }

    // ________________________________________________________

    @AfterAll
    void stopServer() {
        testServerSetup.getApp().stop();

        if (client != null) client.close();
        serverEm.close();
        emf.close();
    }

    // ________________________________________________________

    @Test
    void userCannotReadOtherUsersTask() throws IOException, InterruptedException {

        assertDenied(send("GET", "/tasks/" + taskBId, tokenA, null), Notifications.TASK_NOT_FOUND);

        assertTaskAccessible(taskBId, tokenB, "Decide menu");

        assertDenied(send("GET", "/tasks/" + taskAId, tokenB, null), Notifications.TASK_NOT_FOUND);

        assertTaskAccessible(taskAId, tokenA, "Find catering");
    }

    // ________________________________________________________

    @Test
    void userCannotUpdateOtherUsersTask() throws IOException, InterruptedException {

        Map<UUID, JsonNode> before = taskState();

        assertDenied(send("PUT", "/tasks/" + taskBId, tokenA, TASK_DETAILS), Notifications.TASK_NOT_FOUND);
        assertEquals(before, taskState(), "Rejected update must not change persisted tasks");

        assertTaskAccessible(taskBId, tokenB, "Decide menu");

        assertStatus(200, send("PUT", "/tasks/" + taskAId, tokenA, TASK_DETAILS));
        JsonNode updated = taskState().get(taskAId);

        assertEquals("Updated catering", updated.path("title").asText());
        assertEquals(125.5, updated.path("price").asDouble());
        assertEquals(3.5, updated.path("estimatedHours").asDouble());
        assertEquals("IN_PROGRESS", updated.path("status").asText());
        assertEquals(before.get(taskBId), taskState().get(taskBId));

        assertTaskAccessible(taskAId, tokenA, "Updated catering");
    }

    // ________________________________________________________

    @Test
    void userCannotDeleteOtherUsersTask() throws IOException, InterruptedException {

        Map<UUID, JsonNode> before = taskState();

        assertDenied(send("DELETE", "/tasks/" + taskBId, tokenA, null), Notifications.TASK_NOT_FOUND);
        assertEquals(before, taskState(), "Rejected delete must preserve tasks and sibling positions");

        assertTaskAccessible(taskBId, tokenB, "Decide menu");

        assertStatus(200, send("DELETE", "/tasks/" + taskAId, tokenA, null));

        Map<UUID, JsonNode> after = taskState();

        assertFalse(after.containsKey(taskAId));
        assertEquals(0, after.get(siblingAId).path("position").asInt());
        assertEquals(before.get(taskBId), after.get(taskBId));
        assertEquals(before.get(siblingBId), after.get(siblingBId));
    }

    // ________________________________________________________

    @Test
    void userCannotMoveOtherUsersTask() throws IOException, InterruptedException {

        Map<UUID, JsonNode> before = taskState();

        String body = """
                {"position":"1"}
                """;

        assertDenied(send("PATCH", "/tasks/" + taskBId + "/position", tokenA, body), Notifications.TASK_NOT_FOUND);
        assertEquals(before, taskState(), "Rejected move must preserve all positions and categories");

        // Also reject taking B's task into A's category.
        assertDenied(send("PATCH", "/tasks/" + taskBId + "/position", tokenA, moveBody(categoryAId)), Notifications.TASK_NOT_FOUND);
        assertEquals(before, taskState());
        assertTaskAccessible(taskBId, tokenB, "Decide menu");

        assertStatus(200, send("PATCH", "/tasks/" + taskAId + "/position", tokenA, body));

        Map<UUID, JsonNode> after = taskState();

        assertEquals(1, after.get(taskAId).path("position").asInt());
        assertEquals(0, after.get(siblingAId).path("position").asInt());
        assertEquals(categoryAId.toString(), after.get(taskAId).path("categoryId").asText());
        assertEquals(before.get(taskBId), after.get(taskBId));
        assertEquals(before.get(siblingBId), after.get(siblingBId));
    }

    // ________________________________________________________

    @Test
    void userCanAccessOwnTask() throws IOException, InterruptedException {

        assertTaskAccessible(taskAId, tokenA, "Find catering");
        assertTaskAccessible(taskBId, tokenB, "Decide menu");
    }

    // ________________________________________________________

    @Test
    void userCannotChangeOtherUsersTaskStatus() throws IOException, InterruptedException {

        Map<UUID, JsonNode> before = taskState();
        String body = """
                {"status":"DONE"}
                """;

        assertDenied(send("PATCH", "/tasks/" + taskBId + "/status", tokenA, body), Notifications.TASK_NOT_FOUND);
        assertEquals(before, taskState(), "Rejected status change must preserve persisted tasks");
        assertTaskAccessible(taskBId, tokenB, "Decide menu");

        assertStatus(200, send("PATCH", "/tasks/" + taskAId + "/status", tokenA, body));

        assertEquals("DONE", taskState().get(taskAId).path("status").asText());
        assertEquals(before.get(taskBId), taskState().get(taskBId));
    }

    // ________________________________________________________

    @Test
    void userCannotReadTasksInOtherUsersCategory() throws IOException, InterruptedException {

        assertDenied(send("GET", "/categories/" + categoryBId + "/tasks", tokenA, null), Notifications.CATEGORY_NOT_FOUND);

        assertCategoryTasks(categoryAId, tokenA, Set.of(taskAId, siblingAId));
        assertCategoryTasks(categoryBId, tokenB, Set.of(taskBId, siblingBId));
    }

    // ________________________________________________________

    @Test
    void userCannotCreateTaskInOtherUsersCategory() throws IOException, InterruptedException {

        Map<UUID, JsonNode> before = taskState();
        assertDenied(send("POST", "/categories/" + categoryBId + "/tasks", tokenA, TASK_DETAILS), Notifications.CATEGORY_NOT_FOUND);
        assertEquals(before, taskState(), "Rejected create must not insert or change tasks");

        assertCategoryTasks(categoryBId, tokenB, Set.of(taskBId, siblingBId));

        HttpResponse<String> response = send("POST", "/categories/" + categoryAId + "/tasks", tokenA, TASK_DETAILS);
        assertStatus(201, response);

        UUID createdId = UUID.fromString(data(response).path("id").asText());
        Map<UUID, JsonNode> after = taskState();

        assertEquals(before.size() + 1, after.size());
        assertEquals(categoryAId.toString(), after.get(createdId).path("categoryId").asText());
        assertEquals(before.get(taskBId), after.get(taskBId));

        assertTaskAccessible(createdId, tokenA, "Updated catering");
    }

    // ________________________________________________________

    @Test
    void userCannotMoveOwnTaskIntoOtherUsersCategory() throws IOException, InterruptedException {

        Map<UUID, JsonNode> before = taskState();

        assertDenied(send("PATCH", "/tasks/" + taskAId + "/position", tokenA, moveBody(categoryBId)), Notifications.CATEGORY_NOT_FOUND);
        assertEquals(before, taskState(), "Rejected destination must preserve both users' tasks");

        assertTaskAccessible(taskAId, tokenA, "Find catering");
        assertTaskAccessible(taskBId, tokenB, "Decide menu");
    }

    // ________________________________________________________

    @Test
    void userCannotReadOtherUsersWedding() throws IOException, InterruptedException {

        // User A cannot read User B's wedding
        assertDenied(send("GET", "/weddings/" + weddingBId, tokenA, null), Notifications.WEDDING_NOT_FOUND);

        // User B can read their own wedding
        assertStatus(200, send("GET", "/weddings/" + weddingBId, tokenB, null));

        // User B cannot read User A's wedding
        assertDenied(send("GET", "/weddings/" + weddingAId, tokenB, null), Notifications.WEDDING_NOT_FOUND);

        // User A can read their own wedding
        assertStatus(200, send("GET", "/weddings/" + weddingAId, tokenA, null));
    }

    // ________________________________________________________

    @Test
    void userCannotReadOtherUsersCategory() throws IOException, InterruptedException {

        // User A cannot read User B's category
        assertDenied(send("GET", "/categories/" + categoryBId, tokenA, null), Notifications.CATEGORY_NOT_FOUND);

        // User B can read their own category
        assertStatus(200, send("GET", "/categories/" + categoryBId, tokenB, null));

        // User B cannot read User A's category
        assertDenied(send("GET", "/categories/" + categoryAId, tokenB, null), Notifications.CATEGORY_NOT_FOUND);

        // User A can read their own category
        assertStatus(200, send("GET", "/categories/" + categoryAId, tokenA, null));
    }

    // ________________________________________________________

    private String moveBody(UUID categoryId) {
        return "{\"position\":\"0\",\"categoryId\":\"" + categoryId + "\"}";
    }

    private HttpResponse<String> send(String method, String path, String token, String body) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // ________________________________________________________

    private void assertStatus(int expected, HttpResponse<String> response) {
        assertEquals(expected, response.statusCode(), () -> response.request().method() + " " + response.uri() + ": " + response.body());
    }

    // ________________________________________________________

    private void assertDenied(HttpResponse<String> response, Notifications notification) {

        assertStatus(404, response);

        // A router's "Endpoint ... not found" 404 must never satisfy an ownership assertion.
        assertEquals(notification.getDisplayName(), response.body());
    }

    // ________________________________________________________

    private JsonNode data(HttpResponse<String> response) throws IOException {

        // BaseController wraps the controller's data map inside another data object.
        return jsonMapper.readTree(response.body()).path("data").path("data");
    }

    // ________________________________________________________

    private void assertTaskAccessible(UUID taskId, String token, String title) throws IOException, InterruptedException {

        HttpResponse<String> response = send("GET", "/tasks/" + taskId, token, null);
        assertStatus(200, response);

        assertEquals(taskId.toString(), data(response).path("id").asText());
        assertEquals(title, data(response).path("title").asText());
    }

    // ________________________________________________________

    private void assertCategoryTasks(UUID categoryId, String token, Set<UUID> expected) throws IOException, InterruptedException {

        HttpResponse<String> response = send("GET", "/categories/" + categoryId + "/tasks", token, null);
        assertStatus(200, response);

        JsonNode tasks = data(response);
        assertTrue(tasks.isArray());

        Set<UUID> actual = new java.util.HashSet<>();

        for (JsonNode task : tasks) {
            actual.add(UUID.fromString(task.path("id").asText()));
            assertEquals(categoryId.toString(), task.path("categoryId").asText());
        }

        assertEquals(expected.size(), tasks.size());
        assertEquals(expected, actual);
    }

    // ________________________________________________________

    private Map<UUID, JsonNode> taskState() {

        // A fresh persistence context observes committed database state, not server-cached entities.
        try (EntityManager em = emf.createEntityManager()) {

            Map<UUID, JsonNode> state = new HashMap<>();

            for (Task task : em.createQuery("SELECT t FROM Task t", Task.class).getResultList()) {
                state.put(task.getId(), jsonMapper.valueToTree(taskMapper.toDTO(task)));
            }
            return state;
        }
    }
}

