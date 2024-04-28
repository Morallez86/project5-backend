package aor.paj.service;

import aor.paj.bean.MessageBean;
import aor.paj.bean.TokenBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.MessageDto;
import aor.paj.dto.UserPartialDto;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;

import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.Logger;

@Path("/messages")
public class MessageService {

    @Inject
    private UserBean userBean;

    @Inject
    private MessageBean messageBean;
    @Inject
    private TokenBean tokenBean;

    private static final Logger logger = LogManager.getLogger(MessageService.class);

    private Response unauthorizedResponse() {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized")))
                .build();
    }

    private Response invalidTokenResponse() {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid token")))
                .build();
    }

    private Response handleTokenValidation(String token, String clientIP) {
        logger.warn("Invalid token received for retrieving users - IP: {}", clientIP);
        if (token == null || token.isEmpty() || !userBean.isValidUserByToken(token)) {
            return unauthorizedResponse();
        }
        return null;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessages(@HeaderParam("token") String token, @QueryParam("recipientId") int recipientId, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to get messages. Token: {}, RecipientId: {}, IP: {}", token, recipientId, clientIP);

        Response tokenValidationResponse = handleTokenValidation(token, clientIP);
        if (tokenValidationResponse != null) {
            return tokenValidationResponse;
        }

        try {
            int userId = userBean.getUserByToken(token).getId();
            List<MessageDto> messages = messageBean.getMessagesForUser(userId, recipientId);

            // Always return an empty list instead of 404 Not Found
            if (messages == null) {
                messages = Collections.emptyList(); // Initialize to empty list if null
            }

            logger.info("Retrieved {} messages for user {} with recipientId {} from IP: {}", messages.size(), userId, recipientId, clientIP);
            return Response.ok().entity(JsonUtils.convertObjectToJson(messages)).build();
        } catch (Exception e) {
            logger.error("Error occurred while retrieving messages from IP: {}", clientIP, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Error retrieving messages")))
                    .build();
        }
    }



    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addMessage(@HeaderParam("token") String token, MessageDto messageDto, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to add message. Token: {}, IP: {}", token, clientIP);

        Response tokenValidationResponse = handleTokenValidation(token, clientIP);
        if (tokenValidationResponse != null) {
            return tokenValidationResponse;
        }

        try {
            boolean messageSaved = messageBean.addMessage(messageDto);

            if (messageSaved) {
                tokenBean.renewToken(token);
                logger.info("Message created successfully from IP: {}", clientIP);
                return Response.status(Response.Status.CREATED)
                        .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Message created successfully")))
                        .build();
            } else {
                logger.error("Failed to create message from IP: {}", clientIP);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to create message")))
                        .build();
            }
        } catch (Exception e) {
            logger.error("Error occurred while adding message from IP: {}", clientIP, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Error adding message")))
                    .build();
        }
    }

    @GET
    @Path("/chat")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsersChat(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to fetch all users chat. Token: {}, IP: {}", token, clientIP);

        Response tokenValidationResponse = handleTokenValidation(token, clientIP);
        if (tokenValidationResponse != null) {
            return tokenValidationResponse;
        }

        try {
            int userId = userBean.getUserByToken(token).getId();
            logger.debug("User ID retrieved for token {}: {}", token, userId);

            List<UserPartialDto> userPartialDtos = messageBean.getAllUsersCommunicatedWith(userId);
            logger.debug("Retrieved {} users communicated with user ID {}", userPartialDtos.size(), userId);

            if (userPartialDtos == null || userPartialDtos.isEmpty()) {
                logger.warn("No users found for user ID {}", userId);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(JsonUtils.convertObjectToJson(new ResponseMessage("No users found")))
                        .build();
            }

            logger.info("Successfully fetched all users chat for user ID {}", userId);
            return Response.ok(userPartialDtos).build();
        } catch (Exception e) {
            logger.error("Error occurred while fetching all users chat for token {}: {}", token, e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Error fetching users chat")))
                    .build();
        }
    }

    @PUT
    @Path("/seen/{messageId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response markMessageAsSeen(@HeaderParam("token") String token, @PathParam("messageId") int messageId, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to mark message as seen. Token: {}, Message ID: {}, IP: {}", token, messageId, clientIP);

        Response tokenValidationResponse = handleTokenValidation(token, clientIP);
        if (tokenValidationResponse != null) {
            return tokenValidationResponse;
        }

        try {
            boolean messageSeen = messageBean.markMessagesAsSeenBefore(messageId);

            if (messageSeen) {
                tokenBean.renewToken(token);
                logger.info("Successfully marked message ID {} as seen for user with token {}", messageId, token);
                return Response.ok()
                        .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Message marked as seen")))
                        .build();
            } else {
                logger.error("Failed to mark message ID {} as seen for user with token {}", messageId, token);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to mark message as seen")))
                        .build();
            }
        } catch (Exception e) {
            logger.error("Error occurred while marking message ID {} as seen for user with token {}: {}", messageId, token, e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Error marking message as seen")))
                    .build();
        }
    }

    @GET
    @Path("/unread/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUnreadMessages(@HeaderParam("token") String token, @PathParam("userId") int userId, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to get unread messages for user ID: {} from IP: {}", userId, clientIP);

        Response tokenValidationResponse = handleTokenValidation(token, clientIP);
        if (tokenValidationResponse != null) {
            return tokenValidationResponse;
        }

        try {
            List<MessageDto> unreadMessages = messageBean.getUnreadMessagesForUser(userId);

            if (unreadMessages == null) {
                logger.error("Error retrieving unread messages for user ID: {}", userId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Error retrieving messages")))
                        .build();
            }

            logger.info("Retrieved {} unread messages for user ID: {}", unreadMessages.size(), userId);
            return Response.ok(unreadMessages).build();
        } catch (Exception e) {
            logger.error("Error occurred while fetching unread messages for user ID {}: {}", userId, e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Error retrieving messages")))
                    .build();
        }
    }
}
