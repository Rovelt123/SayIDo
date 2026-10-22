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
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserResetPasswordIT extends SetupServerIT {

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
                .passwordResetToken("reset-token-123")
                .passwordResetExpiresAt(LocalDateTime.now().plusHours(1))
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
    void userResetPasswordSuccesReturn200() throws Exception{
        //Arrange
        String json = """
                {
                    "token": "reset-token-123",
                    "new_password": "NewPassword123!",
                    "repeat_new_password": "NewPassword123!"
                }
                """;

        HttpRequest forgotPWRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9393/api/users/auth/forgot-password"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

//Act
        HttpResponse<String> changeResponse = HttpClient.newHttpClient()
                .send(forgotPWRequest, HttpResponse.BodyHandlers.ofString());

// Assert
        assertEquals(200, changeResponse.statusCode());

    }

    // ________________________________________________________

    @Test
    void userResetPasswordWrongResetTokenReturn400() throws Exception{
        //Arrange
        String json = """
                {
                    "token": "wrong-token",
                    "new_password": "NewPassword123!",
                    "repeat_new_password": "NewPassword123!"
                }
                """;

        HttpRequest resetRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9393/api/users/auth/forgot-password"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

//Act
        HttpResponse<String> resetResponse = HttpClient.newHttpClient()
                .send(resetRequest, HttpResponse.BodyHandlers.ofString());

// Assert
        assertEquals(400, resetResponse.statusCode());
        assertTrue(resetResponse.body().contains("Authorization header missing"));

    }

    // ________________________________________________________

    @Test
    void userResetPasswordExpiredResetTokenReturn400() throws Exception{
        //Arrange -
       User testUserPWExpiredToken = User.builder()
                .firstname("John")
                .lastname("Doe")
                .roles(Set.of(Role.USER))
                .email("otherJohn123@test.dk")
                .password(PasswordService.hashHelper("Password123!"))
                .passwordResetToken("other-reset-token-123")
                .passwordResetExpiresAt(LocalDateTime.now().minusDays(10))
                .build();

       userDAO.create(testUserPWExpiredToken);

        em.flush();
        em.clear();

        String json = """
                {
                    "token": "other-reset-token-123",
                    "new_password": "NewPassword123!",
                    "repeat_new_password": "NewPassword123!"
                }
                """;

        HttpRequest resetRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9393/api/users/auth/forgot-password"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

//Act
        HttpResponse<String> resetResponse = HttpClient.newHttpClient()
                .send(resetRequest, HttpResponse.BodyHandlers.ofString());

// Assert
        assertEquals(400, resetResponse.statusCode());
        assertTrue(resetResponse.body().contains("Link is expired or invalid"));

    }

    // ________________________________________________________

    @Test
    void userResetPasswordNewAndRepeatMismatchReturn400() throws Exception{
        //Arrange
        String json = """
                {
                    "token": "reset-token-123",
                    "new_password": "NewPassword123!",
                    "repeat_new_password": "WrongNewPassword123!"
                }
                """;

        HttpRequest forgotPWRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9393/api/users/auth/forgot-password"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

//Act
        HttpResponse<String> changeResponse = HttpClient.newHttpClient()
                .send(forgotPWRequest, HttpResponse.BodyHandlers.ofString());

// Assert
        assertEquals(400, changeResponse.statusCode());
        assertTrue(changeResponse.body().contains("Password confirmation does not match"));

    }

    // ________________________________________________________

    @Test
    void userResetPasswordNewIsIdenticalToOldReturn400() throws Exception{
        //Arrange
        String json = """
                {
                    "token": "reset-token-123",
                    "new_password": "Password123!",
                    "repeat_new_password": "Password123!"
                }
                """;

        HttpRequest forgotPWRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9393/api/users/auth/forgot-password"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

//Act
        HttpResponse<String> changeResponse = HttpClient.newHttpClient()
                .send(forgotPWRequest, HttpResponse.BodyHandlers.ofString());

// Assert
        assertEquals(400, changeResponse.statusCode());
        assertTrue(changeResponse.body().contains("New password must be different from current password"));

    }

    // ________________________________________________________

    @Test
    void userResetPasswordPWCriteriaNotFollowedReturn400() throws Exception{
        //Arrange
        String json = """
                {
                    "token": "reset-token-123",
                    "new_password": "no-capital-letter",
                    "repeat_new_password": "no-capital-letter"
                }
                """;

        HttpRequest forgotPWRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9393/api/users/auth/forgot-password"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

//Act
        HttpResponse<String> changeResponse = HttpClient.newHttpClient()
                .send(forgotPWRequest, HttpResponse.BodyHandlers.ofString());

// Assert
        assertEquals(400, changeResponse.statusCode());
        assertTrue(changeResponse.body().contains("The password must contain an uppercase letter"));

    }

    // ________________________________________________________

    @Test
    void userResetPasswordMissingResetTokenReturn400() throws Exception{
        //Arrange
        String json = """
                {
                    "token": "",
                    "new_password": "NewPassword123!",
                    "repeat_new_password": "NewPassword123!"
                }
                """;

        HttpRequest forgotPWRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9393/api/users/auth/forgot-password"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

//Act
        HttpResponse<String> changeResponse = HttpClient.newHttpClient()
                .send(forgotPWRequest, HttpResponse.BodyHandlers.ofString());

// Assert
        assertEquals(400, changeResponse.statusCode());
        assertTrue(changeResponse.body().contains("Token is missing"));

    }


}

