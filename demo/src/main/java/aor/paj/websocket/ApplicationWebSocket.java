package aor.paj.websocket;

import aor.paj.dto.MessageDto;
import aor.paj.dto.NotificationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@ServerEndpoint("/websocket/application/{token}")
public class ApplicationWebSocket {

    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        sessions.put(token, session);
        System.out.println("New WebSocket session opened for token: " + token);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason, @PathParam("token") String token) {
        sessions.remove(token);
        System.out.println("WebSocket session closed for token: " + token +
                " Reason: " + reason.getCloseCode() + ": " + reason.getReasonPhrase());
    }

    public void sendNotification(String token, NotificationDto notification) {
        try {
            String notificationJson = mapper.writeValueAsString(notification);
            Session session = sessions.get(token);
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(notificationJson);
            } else {
                System.out.println("Session not found or closed for token: " + token);
            }
        } catch (IOException e) {
            System.out.println("Error sending notification to token: " + token);
            e.printStackTrace();
        }
    }

    public void sendMessage(String token, MessageDto message) {
        try {
            String messageJson = mapper.writeValueAsString(message);
            Session session = sessions.get(token);
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(messageJson);
            } else {
                System.out.println("Session not found or closed for token: " + token);
            }
        } catch (IOException e) {
            System.out.println("Error sending message to token: " + token);
            e.printStackTrace();
        }
    }
}


