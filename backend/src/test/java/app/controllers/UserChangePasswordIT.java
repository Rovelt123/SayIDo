package app.controllers;

import app.SetupServerIT;
import app.entities.User;
import app.enums.Role;
import app.mappers.UserMapper;
import app.security.SecurityService;
import app.services.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserChangePasswordIT extends SetupServerIT {

    private User testUserPW;
    String token;

    @BeforeEach
    void setupPasswordUser() {

        testUserPW = User.builder()
                .firstname("John")
                .lastname("Doe")
                .roles(Set.of(Role.USER))
                .email("john123@test.dk")
                .password(PasswordService.hashHelper("Password123!"))
                .build();

        testUserPW = userDAO.create(testUserPW);

        em.flush();
        em.clear();

        SecurityService securityService = new SecurityService();
        UserMapper userMapper = new UserMapper();

        token = securityService.createToken(
                userMapper.toDTO(testUserPW)
        );
    }

    // ________________________________________________________

    @Test
    void changePasswordSucces() throws Exception {
//Arrange
        String json = """
                {
                    "current_password": "Password123!",
                    "new_password": "NewPassword123!",
                    "repeat_new_password": "NewPassword123!"
                }
                """;

        HttpRequest changePWRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9393/api/users/me/password"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .method(
                        "PATCH",
                        HttpRequest.BodyPublishers.ofString(json)
                )
                .build();

//Act
        HttpResponse<String> changeResponse = HttpClient.newHttpClient()
                .send(changePWRequest, HttpResponse.BodyHandlers.ofString());

// Assert
        assertEquals(200, changeResponse.statusCode());

    }

    // ________________________________________________________

    @Test
    void changePasswordRepeatDifferentFromNewReturn400() throws Exception {
//Arrange
        String json = """
                {
                    "current_password": "Password123!",
                    "new_password": "NewPassword123!",
                    "repeat_new_password": "DifferentPassword123!"
                }
                """;

        HttpRequest changePWRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9393/api/users/me/password"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .method(
                        "PATCH",
                        HttpRequest.BodyPublishers.ofString(json)
                )
                .build();

//Act
        HttpResponse<String> changeResponse = HttpClient.newHttpClient()
                .send(changePWRequest, HttpResponse.BodyHandlers.ofString());

// Assert
        assertEquals(400, changeResponse.statusCode());
        assertTrue(changeResponse.body().contains("Password confirmation does not match"));

    }

    // ________________________________________________________

    @Test
    void changePasswordNewPasswordIdenticalWithOldReturn400() throws Exception {
//Arrange
        String json = """
                {
                    "current_password": "Password123!",
                    "new_password": "Password123!",
                    "repeat_new_password": "Password123!"
                }
                """;

        HttpRequest changePWRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9393/api/users/me/password"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .method(
                        "PATCH",
                        HttpRequest.BodyPublishers.ofString(json)
                )
                .build();

//Act
        HttpResponse<String> changeResponse = HttpClient.newHttpClient()
                .send(changePWRequest, HttpResponse.BodyHandlers.ofString());

// Assert
        assertEquals(400, changeResponse.statusCode());
        assertTrue(changeResponse.body().contains("New password must be different from current password"));

    }


    @Test
    void changePasswordWrongCurrentPasswordReturns401() throws Exception {
    //Arrange
        String json = """
                {
                    "current_password": "WrongPassword123!",
                    "new_password": "NewPassword123!",
                    "repeat_new_password": "NewPassword123!"
                }
                """;

        HttpRequest changePWRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9393/api/users/me/password"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .method(
                        "PATCH",
                        HttpRequest.BodyPublishers.ofString(json)
                )
                .build();

//Act
        HttpResponse<String> changeResponse = HttpClient.newHttpClient()
                .send(changePWRequest, HttpResponse.BodyHandlers.ofString());

// Assert
        assertEquals(401, changeResponse.statusCode());
        assertTrue(changeResponse.body().contains("Current password is wrong"));
    }

    // ________________________________________________________

    @Test
    void changePasswordInvalidNewPasswordReturns400() throws Exception{
        //Arrange
        String json = """
                {
                    "current_password": "Password123!",
                    "new_password": "dosntfollowPWrules",
                    "repeat_new_password": "dosntfollowPWrules"
                }
                """;

        HttpRequest changePWRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9393/api/users/me/password"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .method(
                        "PATCH",
                        HttpRequest.BodyPublishers.ofString(json)
                )
                .build();

//Act
        HttpResponse<String> changeResponse = HttpClient.newHttpClient()
                .send(changePWRequest, HttpResponse.BodyHandlers.ofString());

// Assert
        assertEquals(400, changeResponse.statusCode());
        assertTrue(changeResponse.body().contains("The password must contain a special character"));

    }

    // ________________________________________________________

    @Test
    void changePasswordMissingNewPasswordReturns400() throws Exception{
        //Arrange
        String json = """
                {
                    "current_password": "Password123!",
                    "new_password": "",
                    "repeat_new_password": ""
                }
                """;

        HttpRequest changePWRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9393/api/users/me/password"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .method(
                        "PATCH",
                        HttpRequest.BodyPublishers.ofString(json)
                )
                .build();

//Act
        HttpResponse<String> changeResponse = HttpClient.newHttpClient()
                .send(changePWRequest, HttpResponse.BodyHandlers.ofString());

// Assert
        assertEquals(400, changeResponse.statusCode());
        assertTrue(changeResponse.body().contains("You must enter a new password"));

    }

    // ________________________________________________________

    @Test
    void changePasswordWithoutTokenReturns401() throws Exception{
        //Arrange
        String json = """
                {
                    "current_password": "Password123!",
                    "new_password": "NewPassword123!",
                    "repeat_new_password": "NewPassword123!"
                }
                """;

        HttpRequest changePWRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9393/api/users/me/password"))
                .header("Content-Type", "application/json")
                .method(
                        "PATCH",
                        HttpRequest.BodyPublishers.ofString(json)
                )
                .build();

//Act
        HttpResponse<String> changeResponse = HttpClient.newHttpClient()
                .send(changePWRequest, HttpResponse.BodyHandlers.ofString());

// Assert
        assertEquals(401, changeResponse.statusCode());
        assertTrue(changeResponse.body().contains("Authorization header missing"));

    }
}

