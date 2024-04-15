package aor.paj.service;

import aor.paj.bean.MessageBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.MessageDto;
import aor.paj.dto.UserPartialDto;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/messages")
public class MessageService {

    @Inject
    private UserBean userBean;

    @Inject
    private MessageBean messageBean;

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

    private Response handleTokenValidation(String token) {
        if (token == null || token.isEmpty() || !userBean.isValidUserByToken(token)) {
            return unauthorizedResponse();
        }
        return null;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessages(@HeaderParam("token") String token, @QueryParam("recipientId") int recipientId) {
        Response tokenValidationResponse = handleTokenValidation(token);
        if (tokenValidationResponse != null) {
            return tokenValidationResponse;
        }

        int userId = userBean.getUserByToken(token).getId();
        List<MessageDto> messages = messageBean.getMessagesForUser(userId, recipientId);

        if (messages == null || messages.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("No messages found")))
                    .build();
        }

        return Response.ok().entity(JsonUtils.convertObjectToJson(messages)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addMessage(@HeaderParam("token") String token, MessageDto messageDto) {
        Response tokenValidationResponse = handleTokenValidation(token);
        if (tokenValidationResponse != null) {
            return tokenValidationResponse;
        }

        boolean messageSaved = messageBean.addMessage(messageDto);

        if (messageSaved) {
            return Response.status(Response.Status.CREATED)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Message created successfully")))
                    .build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to create message")))
                    .build();
        }
    }

    @GET
    @Path("/chat")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsersChat(@HeaderParam("token") String token) {
        Response tokenValidationResponse = handleTokenValidation(token);
        if (tokenValidationResponse != null) {
            return tokenValidationResponse;
        }

        int userId = userBean.getUserByToken(token).getId();
        List<UserPartialDto> userPartialDtos = messageBean.getAllUsersCommunicatedWith(userId);

        if (userPartialDtos == null || userPartialDtos.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("No users found")))
                    .build();
        }

        return Response.ok(userPartialDtos).build();
    }

    @PUT
    @Path("/seen/{messageId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response markMessageAsSeen(@HeaderParam("token") String token, @PathParam("messageId") int messageId) {
        Response tokenValidationResponse = handleTokenValidation(token);
        if (tokenValidationResponse != null) {
            return tokenValidationResponse;
        }

        boolean messageSeen = messageBean.markMessagesAsSeenBefore(messageId);

        if (messageSeen) {
            return Response.ok()
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Message marked as seen")))
                    .build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to mark message as seen")))
                    .build();
        }
    }

    @GET
    @Path("/unread/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUnreadMessages(@HeaderParam("token") String token, @PathParam("userId") int userId) {
        Response tokenValidationResponse = handleTokenValidation(token);
        if (tokenValidationResponse != null) {
            return tokenValidationResponse;
        }

        List<MessageDto> unreadMessages = messageBean.getUnreadMessagesForUser(userId);

        if (unreadMessages == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Error retrieving messages")))
                    .build();
        }

        return Response.ok(unreadMessages).build();
    }
}
