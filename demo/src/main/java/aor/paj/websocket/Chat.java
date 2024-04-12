package aor.paj.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ejb.Singleton;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@ServerEndpoint("/websocket/chat/{userId}")
public class Chat {

    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        // Store the session associated with the user ID
        sessions.put(userId, session);
        System.out.println("New session opened for user ID: " + userId);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        // Remove the session when it's closed
        String closedSessionUserId = null;
        for (Map.Entry<String, Session> entry : sessions.entrySet()) {
            if (entry.getValue().equals(session)) {
                closedSessionUserId = entry.getKey();
                break;
            }
        }
        if (closedSessionUserId != null) {
            sessions.remove(closedSessionUserId);
            System.out.println("Websocket session closed for user ID: " + closedSessionUserId +
                    " Reason: " + reason.getCloseCode() + ": " + reason.getReasonPhrase());
        }
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userId) {
        System.out.println("ola");
        Session userSession = sessions.get(userId);
        if (userSession != null && userSession.isOpen()) {
            try {
                // Echo the received message back to the client
                userSession.getBasicRemote().sendText(message);
            } catch (IOException e) {
                System.out.println("Error sending message to user ID: " + userId);
            }
        }
    }

    public static void send(String message, String userId) {
        Session session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                System.out.println("Error sending message to user ID: " + userId);
            }
        }
    }

    public static void sendObject(Object object, String userId) {
        Session session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(object);
                session.getBasicRemote().sendText(json);
            } catch (IOException e) {
                System.out.println("Error sending object to user ID: " + userId);
            }
        }
    }
}
