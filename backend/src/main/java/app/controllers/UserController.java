package app.controllers;

import app.controllers.generic.BaseController;
import app.daos.UserDAO;
import app.dtos.UserDTO;
import app.entities.User;
import app.enums.Notifications;
import app.enums.Role;
import app.mappers.UserMapper;
import app.security.SecurityService;
import app.server.Setup;
import app.services.EmailService;
import app.services.PasswordService;
import app.services.TokenGenerator;
import app.services.UserService;
import app.services.mail.BrevoMailSender;
import app.services.mail.MailSender;
import app.utils.ErrorHandler;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.javalin.apibuilder.ApiBuilder.*;

public class UserController extends BaseController<User, UserDTO> {

    private final UserDAO userDAO = new UserDAO(Setup.em);
    private final UserMapper userMapper = new UserMapper();
    private final SecurityService securityService = new SecurityService();
    private final UserService userService = new UserService();
    private final TokenGenerator tokenGenerator = new TokenGenerator();

    private final MailSender mailSender = new BrevoMailSender();
    private final EmailService emailService = new EmailService(mailSender);

    // ________________________________________________________

    public UserController() {
        super(User.class, new UserMapper());
    }

    // ________________________________________________________

    public static EndpointGroup registerRoutes() {
        UserController controller = new UserController();
        return ()->{

            post("users/auth/register", controller::registerUser, Role.ANYONE);
            post("users/auth/login", controller::login, Role.ANYONE);
            get("users/auth/confirm-email", controller::confirmMail);
            post("users/auth/forgot-password/request", controller::requestForgotPassword, Role.ANYONE);
            post("users/auth/forgot-password", controller::forgotPassword, Role.ANYONE);
            patch("users/me/password", controller::changePassword, Role.USER);
            post("users/auth/resend-confirmation", controller::resendConfirmationEmail, Role.ANYONE);

        };
    }
    // ________________________________________________________
    @Override
    protected List<User> getAllEntities() {
        return userDAO.getAll();
    }

    // ________________________________________________________

    @Override
    protected User getEntityById(UUID id) {
        return userDAO.getById(id);
    }

    // ________________________________________________________

    private void registerUser(Context ctx) {
        Map<String, String> body = ErrorHandler.tryBodyMap(ctx, Notifications.BODY_EMPTY.getDisplayName());

        String firstname = ErrorHandler.tryString(body.get("first_name"), Notifications.REGISTER_NO_FIRSTNAME.getDisplayName());
        String lastname = ErrorHandler.tryString(body.get("last_name"), Notifications.REGISTER_NO_LASTNAME.getDisplayName());
        String email = ErrorHandler.tryString(body.get("email"), Notifications.REGISTER_NO_EMAIL.getDisplayName());
        String password = ErrorHandler.tryString(body.get("password"), Notifications.REGISTER_NO_PASSWORD.getDisplayName());
        String password_repeat = ErrorHandler.tryString(body.get("repeat_password"), Notifications.REGISTER_NO_PASSWORD_REPEAT.getDisplayName());

        PasswordService.passwordValidation(password);

        if(!password.equals(password_repeat)){
            ctx.status(400).json(Notifications.REGISTER_PASSWORD_MISMATCH.getDisplayName());
            return;
        }

        Role role = Role.USER;

        if (userDAO.existByColumn(email, "email") || !email.contains("@")) {
            String message = messageService.buildMessage(Notifications.EMAIL_EXISTS, email);
            ctx.status(400).json(message);
            return;
        }

        String token = tokenGenerator.generateToken();

        User user = ErrorHandler.tryEntity(
                userDAO.create(User.builder()
                        .firstname(firstname)
                        .lastname(lastname)
                        .roles(Set.of(role))
                        .email(email)
                        .password(PasswordService.hashHelper(password))
                        .emailConfirmationToken(token)
                        .emailConfirmationExpiresAt(LocalDateTime.now().plusHours(24))
                        .build()),
                messageService.buildMessage(Notifications.EMAIL_EXISTS, email)
        );

        emailService.sendConfirmationEmail(user);

        UserDTO dto = userMapper.toDTO(user);

        String jwtToken = securityService.createToken(dto);

        String message = messageService.buildMessage(Notifications.REGISTER_SUCCESS, user.getFirstname());

        respond(ctx, 201, message, Map.of(
                "token", jwtToken,
                "data", dto
        ));
    }

    // ________________________________________________________

    private void confirmMail(Context ctx){

        String token = ctx.queryParam("token");

        if (token == null || token.isBlank()) {
            String message = Notifications.TOKEN_MISSING.getDisplayName();
            respond(ctx, 400, message, null);
            return;
        }

        boolean confirmed = userService.confirmEmail(token);

        if (!confirmed) {
            String message = Notifications.LINK_EXPIRED.getDisplayName();
            respond(ctx, 400, message, null);
            return;
        }

        String message = Notifications.EMAIL_CONFIRMED.getDisplayName();
        respond(ctx, 200, message, null);
    }

    // ________________________________________________________

    private void login(Context ctx) {
        Map<String, String> body = ErrorHandler.tryBodyMap(ctx, Notifications.BODY_EMPTY.getDisplayName());
        User user = ErrorHandler.tryEntity(
                userDAO.getByEmail(body.get("email")),
                Notifications.WRONG_CREDENTIALS.getDisplayName()
        );

        if (!PasswordService.passwordEquals(body.get("password"), user.getPassword())) {
            respond(ctx, 401, Notifications.WRONG_CREDENTIALS.getDisplayName(), null);
            return;
        }

        UserDTO dto = userMapper.toDTO(user);

        String token = securityService.createToken(dto);

        String message = messageService.buildMessage(
                Notifications.LOGGED_IN,
                user.getFirstname()
        );

        respond(ctx, 200, message, Map.of(
                "token", token,
                "data", dto
        ));
    }

    // ________________________________________________________

    private void changePassword(Context ctx) {
        UserDTO user = ctx.attribute("user");
        if (user == null) {
            throw new UnauthorizedResponse("Authentication required.");
        }
        Map<String, String> body = ErrorHandler.tryBodyMap(ctx, Notifications.BODY_EMPTY.getDisplayName());

        String newPassword = ErrorHandler.tryString(
                body.get("new_password"),
                Notifications.PASSWORD_NEW_MISSING.getDisplayName()
        );

        PasswordService.passwordValidation(newPassword);

        userService.changePassword(
                user.getId(),
                body.get("current_password"),
                body.get("new_password"),
                body.get("repeat_new_password")
        );

        respond(ctx, 200, Notifications.PASSWORD_CHANGED.getDisplayName(), null);
    }

    // ________________________________________________________

    private void requestForgotPassword(Context ctx) {
        Map<String, String> body = ErrorHandler.tryBodyMap(ctx, Notifications.BODY_EMPTY.getDisplayName());

        userService.requestForgotPassword(body.get("email"));

        respond(ctx, 200, Notifications.PASSWORD_RESET_REQUESTED.getDisplayName(), null);
    }

    // ________________________________________________________

    private void forgotPassword(Context ctx) {
        Map<String, String> body = ErrorHandler.tryBodyMap(ctx, Notifications.BODY_EMPTY.getDisplayName());

        String newPassword = ErrorHandler.tryString(
                body.get("new_password"),
                Notifications.PASSWORD_NEW_MISSING.getDisplayName()
        );

        PasswordService.passwordValidation(newPassword);

        userService.forgotPassword(
                body.get("token"),
                body.get("new_password"),
                body.get("repeat_new_password")
        );

        respond(ctx, 200, Notifications.PASSWORD_RESET_SUCCESS.getDisplayName(), null);
    }

    // ________________________________________________________

    private void resendConfirmationEmail(Context ctx) {

        Map<String, String> body = ErrorHandler.tryBodyMap(ctx, Notifications.BODY_EMPTY.getDisplayName());

        String email = ErrorHandler.tryString(body.get("email"), Notifications.REGISTER_NO_EMAIL.getDisplayName());

        userService.resendConfirmationEmail(email);

        respond(ctx, 200, Notifications.EMAIL_CONFIRMATION_RESENT.getDisplayName(), null);
    }
}
