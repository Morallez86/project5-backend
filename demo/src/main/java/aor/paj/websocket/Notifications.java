package aor.paj.websocket;

import aor.paj.dto.MessageDto;
import aor.paj.bean.MessageBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@ServerEndpoint("/websocket/notifications/{userId}")
public class Notifications {

    @Inject
    MessageBean messageBean; // Inject your message bean to interact with the database

    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();
    private LocalDateTime timeOfAccess;


    static {
        // Register JavaTimeModule to handle Java 8 date/time types
        mapper.registerModule(new JavaTimeModule());
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        // Store the session associated with the user ID
        sessions.put(userId, session);
        timeOfAccess = LocalDateTime.now();
        System.out.println("New session opened for user ID: " + userId);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason, @PathParam("userId") String userId) {
        // Remove the session when it's closed
        sessions.remove(userId);
        System.out.println("WebSocket session closed for user ID: " + userId +
                " Reason: " + reason.getCloseCode() + ": " + reason.getReasonPhrase());
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userId) {
        System.out.println("Received message: " + message);

        try {
            // Parse the incoming message JSON into MessageDto object
            MessageDto messageDto = mapper.readValue(message, MessageDto.class);

            // Add the message to the database
            MessageDto addedToDatabase = messageBean.addMessageChat(messageDto);
            if (addedToDatabase != null) {
                System.out.println("Message added to the database successfully.");

                // Convert the updated MessageDto to JSON
                String updatedMessageJson = mapper.writeValueAsString(addedToDatabase);

                // Send the updated message to relevant clients
                sendObject(updatedMessageJson, String.valueOf(addedToDatabase.getRecipient()));
                sendObject(updatedMessageJson, String.valueOf(addedToDatabase.getSender()));
            } else {
                System.out.println("Failed to add message to the database.");
            }
        } catch (IOException e) {
            System.out.println("Error parsing or sending message: " + e.getMessage());
        }
    }

    private void sendObject(String json, String userId) {
        Session session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(json);
            } catch (IOException e) {
                System.out.println("Error sending object to user ID: " + userId);
            }
        }
    }
}
