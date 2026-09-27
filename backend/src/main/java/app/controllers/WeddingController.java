package app.controllers;

import app.controllers.generic.BaseController;
import app.daos.WeddingDAO;
import app.dtos.WeddingDTO;
import app.entities.Wedding;
import app.enums.Notifications;
import app.enums.Role;
import app.mappers.WeddingMapper;
import app.server.Setup;
import app.services.UserService;
import app.services.WeddingService;
import app.utils.ErrorHandler;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.javalin.apibuilder.ApiBuilder.*;

public class WeddingController extends BaseController<Wedding, WeddingDTO> {

    private final WeddingDAO weddingDAO = new WeddingDAO(Setup.em);
    private final WeddingMapper weddingMapper = new WeddingMapper();
    private final WeddingService weddingService = new WeddingService();
    private final UserService userService = new UserService();

    // ________________________________________________________

    public WeddingController() {
        super(Wedding.class, new WeddingMapper());
    }

    // ________________________________________________________

    public static EndpointGroup registerRoutes() {
        WeddingController controller = new WeddingController();
        return () -> {
            AdminController.registerRoutes().addEndpoints();

            get("/weddings", controller::getMyWeddings, Role.USER);
            get("/weddings/{id}", controller::getMyWeddingByID, Role.USER);
            post("/weddings", controller::createWedding, Role.USER);
            put("/weddings/{id}", controller::updateWedding, Role.USER);
            delete("/weddings/{id}", controller::deleteWedding, Role.USER);
        };
    }

    // ________________________________________________________

    @Override
    protected List<Wedding> getAllEntities() {
        return weddingDAO.getAll();
    }

    // ________________________________________________________

    @Override
    protected Wedding getEntityById(UUID id) {
        return weddingDAO.getById(id);
    }

    // ________________________________________________________

    public void getMyWeddingByID(Context ctx) {
        UUID weddingId = ErrorHandler.tryParseUUID(ctx.pathParam("id"), Notifications.WEDDING_ID_INVALID.getDisplayName());
        Wedding wedding = ErrorHandler.tryEntity(weddingDAO.getByIdAndOwnerId(weddingId, userService.getOwnerId(ctx)), Notifications.WEDDING_NOT_FOUND.getDisplayName());
        WeddingDTO weddingDTO = weddingMapper.toDTO(wedding);
        respond(ctx, 200, messageService.buildMessage(Notifications.GET_BY_ID, "wedding", weddingId.toString()), Map.of("data", weddingDTO));
    }

    // ________________________________________________________

    public void getMyWeddings(Context ctx) {
        List<Wedding> entities = weddingDAO.getAllByOwnerId(userService.getOwnerId(ctx));
        List<WeddingDTO> weddings = entities.stream().map(weddingMapper::toDTO).toList();
        respond(ctx, 200, messageService.buildMessage(Notifications.GET_ALL, String.valueOf(weddings.size()), "wedding"), Map.of("data", weddings));
    }

    // ________________________________________________________

    public void createWedding(Context ctx) {


        Wedding wedding = weddingService.createWedding(userService.getOwnerId(ctx), ctx);
        WeddingDTO dto = weddingMapper.toDTO(wedding);

        respond(ctx, 201, Notifications.WEDDING_CREATED.getDisplayName(), Map.of("data", dto));
    }

    // ________________________________________________________

    public void updateWedding(Context ctx) {
        UUID id = ErrorHandler.tryParseUUID(ctx.pathParam("id"), Notifications.WEDDING_ID_INVALID.getDisplayName());

        Wedding wedding = weddingService.updateWedding(id, userService.getOwnerId(ctx), ctx);
        WeddingDTO dto = weddingMapper.toDTO(wedding);

        respond(ctx, 200, Notifications.WEDDING_UPDATED.getDisplayName(), Map.of("data", dto));
    }

    // ________________________________________________________

    public void deleteWedding(Context ctx) {
        UUID id = ErrorHandler.tryParseUUID(ctx.pathParam("id"), Notifications.WEDDING_ID_INVALID.getDisplayName());

        weddingService.deleteWedding(id, userService.getOwnerId(ctx));

        respond(ctx, 200, Notifications.WEDDING_DELETED.getDisplayName(), null);
    }
}
