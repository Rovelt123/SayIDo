package app.controllers;

import app.SetupTest;
import app.entities.User;
import app.enums.Role;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserControllerTest extends SetupTest {

    @Test
    void emailCanOnlyHaveOneRegistration() {

        userDAO.create(testUser);

        em.flush();
        em.clear();

        boolean exists = userDAO.existByColumn(
            testUser.getEmail(),
            "email"
    );

    assertTrue(exists);

        User testUser3 = User.builder()
                .firstname("newName")
                .lastname("newLastName")
                .roles(Set.of(Role.USER))
                .email("john123@test.dk")
                .password("Secret123!")
                .build();

        userDAO.create(testUser);

        em.flush();
        em.clear();


    }


}