package aor.paj.service;

import aor.paj.bean.MessageBean;
import aor.paj.dto.MessageDto;
import aor.paj.dto.UserDto;
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
    public Response getMessages(@HeaderParam("token") String token) {
        // Check if the user is valid based on the provided token
        if (!userBean.isValidUserByToken(token)) {
            return Response.status(401)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized")))
                    .build();
        }
        UserDto user = userBean.getUserByToken(token);
        long userId = user.getId();
        List<MessageDto> messages = messageBean.getMessagesForUser(userId); // Retrieve messages for the user

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
        System.out.println(messageDto + "***********************************************************");
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
}
