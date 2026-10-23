package app.controllers;

import app.SetupServerIT;
import app.mappers.UserMapper;
import app.security.SecurityService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BudgetOverviewIT extends SetupServerIT {

    String token;
    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setupBudgetTestData(){

        // Set an initial budget and task
        wedding.setBudget(1000);
        task.setPrice(500);

        em.persist(testUser);
        em.persist(wedding);
        em.persist(category);
        em.persist(task);

        em.flush();
        em.clear();

        SecurityService securityService = new SecurityService();
        UserMapper userMapper = new UserMapper();

        token = securityService.createToken(
                userMapper.toDTO(testUser)
        );
    }

    // ________________________________________________________

    @Test
    void changeTaskPriceSucces() throws Exception{
        //Arrange
        String json = """
                {
                    "title": "find catering company",
                    "price": "750",
                    "estimatedHours": "0"
                }
                """;

        HttpRequest updateTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:9393/api/tasks/" + task.getId()
                ))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
//Act
        HttpResponse<String> changeResponse = HttpClient.newHttpClient()
                .send(updateTaskRequest, HttpResponse.BodyHandlers.ofString());

        // Fetch updated budget overview
        HttpRequest budgetRequest = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:9393/api/weddings/" + wedding.getId()
                ))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> budgetResponse = HttpClient.newHttpClient()
                .send(budgetRequest, HttpResponse.BodyHandlers.ofString());

        JsonNode root = mapper.readTree(budgetResponse.body());

        JsonNode budgetOverview = root.path("data").path("data");

        // Assert
        assertEquals(1000, budgetOverview.path("budget").asDouble(), 0.001);
        assertEquals(750, budgetOverview.path("totalSpent").asDouble(), 0.001);
        assertEquals(250, budgetOverview.path("remainingBudget").asDouble(), 0.001);
        assertEquals(75, budgetOverview.path("budgetUsedPercent").asDouble(), 0.001);
        assertEquals(0, budgetOverview.path("budgetOverrun").asDouble(), 0.001);

        assertEquals(200, changeResponse.statusCode());
    }

    // ________________________________________________________

    @Test
    void createTaskUpdateOverview() throws Exception{
        //Arrange
        String json = """
                {
                    "title": "new task",
                    "price": "250",
                    "estimatedHours": "0"
                }
                """;

        HttpRequest createTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:9393/api/categories/" + category.getId() + "/tasks"
                ))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
//Act
        HttpResponse<String> createResponse = HttpClient.newHttpClient()
                .send(createTaskRequest, HttpResponse.BodyHandlers.ofString());

        // Fetch updated budget overview
        HttpRequest budgetRequest = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:9393/api/weddings/" + wedding.getId()
                ))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> budgetResponse = HttpClient.newHttpClient()
                .send(budgetRequest, HttpResponse.BodyHandlers.ofString());

        JsonNode root = mapper.readTree(budgetResponse.body());

        JsonNode budgetOverview = root.path("data").path("data");

        // Assert
        assertEquals(201, createResponse.statusCode());

        assertEquals(750, budgetOverview.path("totalSpent").asDouble(), 0.001);
        assertEquals(250, budgetOverview.path("remainingBudget").asDouble(), 0.001);

    }

    // ________________________________________________________

    @Test
    void deleteTaskUpdateOverview() throws Exception{
        //Arrange
        HttpRequest deleteTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9393/api/tasks/" + task.getId()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build();
//Act
        HttpResponse<String> deleteResponse = HttpClient.newHttpClient()
                .send(deleteTaskRequest, HttpResponse.BodyHandlers.ofString());

        // Fetch updated budget overview
        HttpRequest budgetRequest = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:9393/api/weddings/" + wedding.getId()
                ))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> budgetResponse = HttpClient.newHttpClient()
                .send(budgetRequest, HttpResponse.BodyHandlers.ofString());

        JsonNode root = mapper.readTree(budgetResponse.body());

        JsonNode budgetOverview = root.path("data").path("data");

        // Assert
        assertEquals(200, deleteResponse.statusCode());

        assertEquals(0, budgetOverview.path("totalSpent").asDouble(), 0.001);
        assertEquals(1000, budgetOverview.path("remainingBudget").asDouble(), 0.001);

    }

    // ________________________________________________________

    @Test
    void moveTaskBetweenCategoriesUpdatesOverview() throws Exception{
        //Arrange
        category2.setWedding(wedding);
        em.persist(category2);

        em.flush();
        em.clear();

        String categoryId = category2.getId().toString();

        String json = """
        {
            "position": "0",
            "categoryId": "%s"
        }
        """.formatted(categoryId);

        HttpRequest moveTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9393/api/tasks/" + task.getId() + "/position"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                .build();
//Act
        HttpResponse<String> moveResponse = HttpClient.newHttpClient()
                .send(moveTaskRequest, HttpResponse.BodyHandlers.ofString());

        // Fetch updated budget overview
        HttpRequest budgetRequest = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:9393/api/weddings/" + wedding.getId()
                ))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> budgetResponse = HttpClient.newHttpClient()
                .send(budgetRequest, HttpResponse.BodyHandlers.ofString());

        JsonNode root = mapper.readTree(budgetResponse.body());

        JsonNode budgetOverview = root.path("data").path("data");

        JsonNode categories = budgetOverview.path("categories");
        // Assert
        assertEquals(200, moveResponse.statusCode());

        assertEquals(500, budgetOverview.path("totalSpent").asDouble(), 0.001);
        assertEquals(500, budgetOverview.path("remainingBudget").asDouble(), 0.001);

        // get 0 is the category task is moved from and get 1 is the category task is moved to
        assertEquals(0, categories.get(0).path("totalTasksPrice").asDouble(), 0.001);
        assertEquals(500, categories.get(1).path("totalTasksPrice").asDouble(), 0.001);
    }

}
