package aor.paj.websocket;

import aor.paj.bean.NotificationBean;
import aor.paj.bean.TokenBean;
import aor.paj.dto.MessageDto;
import aor.paj.bean.MessageBean;
import aor.paj.dto.NotificationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@ServerEndpoint("/websocket/chat/{userId}")
public class Chat {

    @Inject
    MessageBean messageBean;

    @Inject
    NotificationBean notificationBean;

    @Inject
    TokenBean tokenBean;

    NotificationSocket aplicationWebSocket;

    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        // Register JavaTimeModule to handle Java 8 date/time types
        mapper.registerModule(new JavaTimeModule());
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        // Store the session associated with the user ID
        sessions.put(userId, session);
        System.out.println("New session opened for user ID: " + userId);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason, @PathParam("userId") String userId) {
        // Remove the session when it's closed
        sessions.remove(userId);
        System.out.println("Websocket session closed for user ID: " + userId +
                " Reason: " + reason.getCloseCode() + ": " + reason.getReasonPhrase());
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userId) {
        try {
            MessageDto messageDto = mapper.readValue(message, MessageDto.class);
            NotificationDto notificationDto = createNotificationFromMessage(messageDto);

            MessageDto addedMessage = messageBean.addMessageChat(messageDto);
            NotificationDto addedNotification = notificationBean.addNotificationMessage(notificationDto);
            NotificationDto sendNotificationDto = notificationBean.socketLastNotification();

            if (addedMessage != null && addedNotification != null && sendNotificationDto !=null) {
                sendUpdatedMessage(addedMessage);
                System.out.println("DTO NOTIFICATION   "+ sendNotificationDto);
                System.out.println("DTO MESSAGE   "+ addedMessage);
                notificationBean.sendNotificationToRecipient(sendNotificationDto);
                notificationBean.sendMessageToRecipient(addedMessage);
            } else {
                System.out.println("Failed to add message or notification to the database.");
            }
        } catch (IOException e) {
            System.out.println("Error parsing message JSON: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error processing message: " + e.getMessage());
        }
    }

    private NotificationDto createNotificationFromMessage(MessageDto messageDto) {
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setRecipientId(messageDto.getRecipient());
        notificationDto.setSenderId(messageDto.getSender());

        // Check if the recipient is currently active in the chat (has an open session)
        String recipientUserId = String.valueOf(messageDto.getRecipient());
        boolean isRecipientActive = sessions.containsKey(recipientUserId) && sessions.get(recipientUserId).isOpen();

        // Set notification_read based on recipient's activity status
        notificationDto.setRead(isRecipientActive);

        return notificationDto;
    }


    private void sendUpdatedMessage(MessageDto messageDto) throws IOException {
        String updatedMessageJson = mapper.writeValueAsString(messageDto);
        System.out.println(updatedMessageJson);
        sendObject(updatedMessageJson, String.valueOf(messageDto.getRecipient()));
        sendObject(updatedMessageJson, String.valueOf(messageDto.getSender()));
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
