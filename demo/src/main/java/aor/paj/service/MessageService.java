package aor.paj.service;

import aor.paj.bean.MessageBean;
import aor.paj.dto.MessageDto;
import aor.paj.dto.UserDto;
import aor.paj.dto.UserPartialDto;
import jakarta.ws.rs.Path;
import aor.paj.bean.CategoryBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.CategoryDto;
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
    UserBean userBean;

    @Inject
    MessageBean messageBean;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessages(@HeaderParam("token") String token, @QueryParam("recipientId") int recipientId) {
        // Check if the user is valid based on the provided token
        if (!userBean.isValidUserByToken(token)) {
            return Response.status(401)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized")))
                    .build();
        }
        UserDto user = userBean.getUserByToken(token);
        int userId = user.getId();
        List<MessageDto> messages = messageBean.getMessagesForUser(userId, recipientId); // Retrieve messages for the user

        // Check if messages were retrieved successfully
        if (messages == null || messages.isEmpty()) {
            return Response.status(404)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("No messages found")))
                    .build();
        }

        return Response.ok().entity(JsonUtils.convertObjectToJson(messages)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addMessage(@HeaderParam("token") String token, MessageDto messageDto) {
        // Check if the user is valid based on the provided token
        if (!userBean.isValidUserByToken(token)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized")))
                    .build();
        }
        // Create the message
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
        if (token == null || token.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid token")))
                    .build();
        }

        if (!userBean.isValidUserByToken(token)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized")))
                    .build();
        }

        try {
            // Get the user ID from the token
            int userId = userBean.getUserByToken(token).getId();

            // Call the bean method to retrieve users communicated with
            List<UserPartialDto> userPartialDtos = messageBean.getAllUsersCommunicatedWith(userId);

            if (userPartialDtos == null || userPartialDtos.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(JsonUtils.convertObjectToJson(new ResponseMessage("No users found")))
                        .build();
            }

            return Response.ok(userPartialDtos).build();
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid user ID")))
                    .build();
        }
    }

    @PUT
    @Path("/seen/{messageId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response markMessageAsSeen(@HeaderParam("token") String token, @PathParam("messageId") int messageId) {
        System.out.println("ola");
        // Check if the user is valid based on the provided token
        if (token == null || token.isEmpty() || !userBean.isValidUserByToken(token)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized")))
                    .build();
        }

        // Call the bean method to mark the message as seen
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
}
