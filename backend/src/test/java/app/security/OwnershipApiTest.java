package app.security;

import app.SetupTest;
import app.configs.TestHibernateConfig;
import app.controllers.TaskController;
import app.controllers.WeddingController;
import app.entities.Category;
import app.entities.Task;
import app.entities.Wedding;
import app.enums.Role;
import app.mappers.UserMapper;
import app.server.Setup;

import app.entities.User;
import app.controllers.UserController;
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
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

//TestInstance makes it such that one does not need to make methods like @BeforeAll and @AfterAll static
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OwnershipApiTest {

    private UUID taskBId;

    private EntityManagerFactory emf;
    private EntityManager serverEm;

    private Setup testServerSetup;
    private final int testPort = 9393;

    private String tokenA;
    private String tokenB;

    private final SecurityService securityService = new SecurityService();
    private final UserMapper userMapper = new UserMapper();

    @BeforeAll
    void serverSetup() {

        emf = TestHibernateConfig.getTestEmf();
        serverEm = emf.createEntityManager();

        testServerSetup = new Setup(serverEm, testPort);
        testServerSetup.initialize();
    }

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

            taskBId = taskB.getId();

            //Create or get tokens for both users
            tokenA = securityService.createToken(userMapper.toDTO(userA));
            tokenB = securityService.createToken(userMapper.toDTO(userB));

            em.getTransaction().commit();
        }
    }

    // ________________________________________________________

    @AfterEach
    void cleanTestData() {

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            em.createQuery("DELETE FROM Task").executeUpdate();
            em.createQuery("DELETE FROM Category").executeUpdate();
            em.createQuery("DELETE FROM Wedding").executeUpdate();

            //Important: Do not change unless it is important, since user has @ElementCollection with rolls, which can give foreign key issues
            em.createQuery("DELETE FROM User").executeUpdate();

            em.getTransaction().commit();
        }
    }

    // ________________________________________________________

    @AfterAll
    void stopServer() {
        testServerSetup.getApp().stop();

        serverEm.close();
        emf.close();
    }

    // ________________________________________________________

    @Test
    void userCannotReadOtherUsersTask() throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9393/tasks/" + taskBId))
                .header("Authorization", "Bearer " + tokenA)
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(404, response.statusCode());
    }

    // ________________________________________________________

    @Test
    void userCannotUpdateOtherUsersTask() {


    }

    // ________________________________________________________

    @Test
    void userCannotDeleteOtherUsersTask() {


    }

    // ________________________________________________________

    @Test
    void userCannotMoveOtherUsersTask() {


    }

    // ________________________________________________________

    @Test
    void userCanAccessOwnTask() {


    }
}
