package aor.paj.service;

import aor.paj.bean.NotificationBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.MessageDto;
import aor.paj.dto.NotificationDto;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;

@Path("/notifications")
public class NotificationService {

    @Inject
    private NotificationBean notificationBean;

    @Inject
    private UserBean userbean;

    private static final Logger logger = LogManager.getLogger(MessageService.class);

    private Response unauthorizedResponse() {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized")))
                .build();
    }

    private Response handleTokenValidation(String token, String cientIP) {
        logger.warn("Invalid token received for retrieving users - IP: {}", cientIP);
        if (token == null || token.isEmpty() || !userbean.isValidUserByToken(token)) {
            return unauthorizedResponse();
        }
        return null;
    }

    @GET
    @Path("/all/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllNotifications(@HeaderParam("token") String token, @PathParam("userId") int userId,  @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to get all notifications. Token: {}, UserId: {}, IP: {}", token, userId, clientIP);
        Response tokenValidationResponse = handleTokenValidation(token, clientIP);
        if (tokenValidationResponse != null) {
            return tokenValidationResponse;
        }

        List<NotificationDto> allNotifications = notificationBean.getNotificationsForUser(userId);

        if (allNotifications == null) {
            logger.error("Failed to create message from IP: {}", clientIP);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Error retrieving all notifications")))
                    .build();
        }
        logger.info("Retrieved {} notifications for userId {} from IP: {}", allNotifications.size(), userId, clientIP);
        return Response.ok(allNotifications).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addNotification(@HeaderParam("token") String token, NotificationDto notificationDto, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to add notification. Token: {}, IP: {}", token, clientIP);

        Response tokenValidationResponse = handleTokenValidation(token, clientIP);
        if (tokenValidationResponse != null) {
            return tokenValidationResponse;
        }

        try {
            NotificationDto notificationSaved = notificationBean.addNotificationMessage(notificationDto);

            if (notificationSaved != null) {
                logger.info("Notification created successfully for user with token {} from IP: {}", token, clientIP);
                return Response.status(Response.Status.CREATED)
                        .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Notification created successfully")))
                        .build();
            } else {
                logger.error("Failed to create notification for user with token {} from IP: {}", token, clientIP);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to create notification")))
                        .build();
            }
        } catch (Exception e) {
            logger.error("Error occurred while adding notification from IP: {}", clientIP, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Error adding notification")))
                    .build();
        }
    }


    @GET
    @Path("/unread/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUnreadNotifications(@HeaderParam("token") String token, @PathParam("userId") int userId,  @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        Response tokenValidationResponse = handleTokenValidation(token, clientIP);
        if (tokenValidationResponse != null) {
            return tokenValidationResponse;
        }

        List<NotificationDto> unreadNotifications = notificationBean.getUnreadNotificationsForUser(userId);

        if (unreadNotifications == null) {
            logger.error("Error occurred while getting notification from IP: {}", clientIP);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Error retrieving notifications")))
                    .build();
        }
        logger.info("Notifications successfully sent for user with token {} from IP: {}", token, clientIP);
        return Response.ok(unreadNotifications).build();
    }

    @PUT
    @Path("/read/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response markNotificationsAsRead(
            @HeaderParam("token") String token,
            @PathParam("userId") int userId,
            @Context HttpServletRequest request) {

        String clientIP = request.getRemoteAddr();
        logger.info("Received request to mark notifications as read. Token: {}, UserId: {}, IP: {}", token, userId, clientIP);

        // Validate token
        Response tokenValidationResponse = handleTokenValidation(token, clientIP);
        if (tokenValidationResponse != null) {
            return tokenValidationResponse;
        }

        // Update notifications status for the specified user
        boolean updated = notificationBean.markNotificationsAsRead(userId);

        if (updated) {
            logger.info("Notifications marked as read successfully for UserId: {} from IP: {}", userId, clientIP);
            return Response.ok()
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Notifications marked as read")))
                    .build();
        } else {
            logger.error("Failed to mark notifications as read for UserId: {} from IP: {}", userId, clientIP);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to mark notifications as read")))
                    .build();
        }
    }
}
