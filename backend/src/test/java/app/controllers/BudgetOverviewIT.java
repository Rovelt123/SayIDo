package app.controllers;

import app.SetupServerIT;
import app.mappers.UserMapper;
import app.security.SecurityService;
import app.services.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BudgetOverviewIT extends SetupServerIT {

    String token;

    @BeforeEach
    void setupUserAndWeddingWithTTasksAndBudget(){

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

// Assert
        assertEquals(200, changeResponse.statusCode());

        System.out.println(budgetResponse.body());

     //   assertEquals(750, budgetResponse.body().)

    }

}
