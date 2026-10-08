package app.controllers;

import app.SetupServerIT;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserControllerIT extends SetupServerIT {

    @Test
    void registerUserExistingEmailCorrectResponse() throws Exception {
//Arrange
        userDAO.create(testUser);

        em.flush();
        em.clear();

        String json = """
                {
                    "first_name": "newName",
                    "last_name": "newLastName",
                    "email": "john123@test.dk",
                    "password": "Secret123!",
                    "repeat_password": "Secret123!"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9393/api/users/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();


        // Act
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            // Assert
            assertEquals(400, response.statusCode());
            assertTrue(response.body().contains("john123@test.dk"));
    }
}
