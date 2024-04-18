package aor.paj.service;

import aor.paj.bean.NotificationBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.MessageDto;
import aor.paj.dto.NotificationDto;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/notifications")
public class NotificationService {

    @Inject
    private NotificationBean notificationBean;

    @Inject
    private UserBean userbean;

    private Response unauthorizedResponse() {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized")))
                .build();
    }

    private Response handleTokenValidation(String token) {
        if (token == null || token.isEmpty() || !userbean.isValidUserByToken(token)) {
            return unauthorizedResponse();
        }
        return null;
    }

    @GET
    @Path("/all/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllNotifications(@HeaderParam("token") String token, @PathParam("userId") int userId) {
        Response tokenValidationResponse = handleTokenValidation(token);
        if (tokenValidationResponse != null) {
            return tokenValidationResponse;
        }

        List<NotificationDto> allNotifications = notificationBean.getNotificationsForUser(userId);

        if (allNotifications == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Error retrieving all notifications")))
                    .build();
        }

        return Response.ok(allNotifications).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addNotification(@HeaderParam("token") String token, NotificationDto notificationDto) {
        Response tokenValidationResponse = handleTokenValidation(token);
        if (tokenValidationResponse != null) {
            return tokenValidationResponse;
        }

        NotificationDto notificationSaved = notificationBean.addNotificationMessage(notificationDto);

        if (notificationSaved != null) {
            return Response.status(Response.Status.CREATED)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Notification created successfully")))
                    .build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to create notification")))
                    .build();
        }
    }

    @GET
    @Path("/unread/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUnreadNotifications(@HeaderParam("token") String token, @PathParam("userId") int userId) {
        Response tokenValidationResponse = handleTokenValidation(token);
        if (tokenValidationResponse != null) {
            return tokenValidationResponse;
        }

        List<NotificationDto> unreadNotifications = notificationBean.getUnreadNotificationsForUser(userId);

        if (unreadNotifications == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Error retrieving notifications")))
                    .build();
        }

        return Response.ok(unreadNotifications).build();
    }

    @PUT
    @Path("/read/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response markNotificationsAsRead(
            @HeaderParam("token") String token,
            @PathParam("userId") int userId) {

        // Validate token
        Response tokenValidationResponse = handleTokenValidation(token);
        if (tokenValidationResponse != null) {
            return tokenValidationResponse;
        }

        // Update notifications status for the specified user
        boolean updated = notificationBean.markNotificationsAsRead(userId);

        if (updated) {
            return Response.ok()
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Notifications marked as read")))
                    .build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to mark notifications as read")))
                    .build();
        }
    }
}
