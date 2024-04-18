package aor.paj.websocket;

import aor.paj.bean.TaskBean;
import aor.paj.bean.TokenBean;
import aor.paj.dto.ManagingTaskDto;
import aor.paj.dto.MessageDto;
import aor.paj.dto.NotificationDto;
import aor.paj.dto.TaskDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@ServerEndpoint("/websocket/application/{token}")
public class NotificationSocket {
    @Inject
    TokenBean tokenBean;

    @Inject
    TaskBean taskBean;

    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        // Register JavaTimeModule to handle Java 8 date/time types
        mapper.registerModule(new JavaTimeModule());
    }
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        // Perform token validation/authentication before allowing the session to be added
        if (tokenBean.isValidToken(token)) {
            sessions.put(token, session);
            System.out.println("New WebSocket session opened for token: " + token);
        } else {
            System.out.println("Invalid token. WebSocket connection rejected for token: " + token);
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Invalid token"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason, @PathParam("token") String token) {
        sessions.remove(token);
        System.out.println("WebSocket session closed for token: " + token +
                " Reason: " + reason.getCloseCode() + ": " + reason.getReasonPhrase());
    }

    @OnMessage
    public void toDoOnMessage(Session session, String msg) {
        System.out.println("A new message is received: " + msg);
        try {
            session.getBasicRemote().sendText("ack");
        } catch (IOException e) {
            System.out.println("Something went wrong!");
        }
    }

    public static void sendNotification(String token, NotificationDto notification) {
        try {
            String notificationJson = mapper.writeValueAsString(notification);
            Session session = sessions.get(token);
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(notificationJson);
                System.out.println(notificationJson + "cmsapovnpvnoavopsa");
            } else {
                System.out.println("Session not found or closed for token: " + token);
            }
        } catch (IOException e) {
            System.out.println("Error sending notification to token: " + token);
            e.printStackTrace();
        }
    }

    public static void sendSocketMessage(String token, MessageDto message) {
        try {
            String notificationJson = mapper.writeValueAsString(message);
            Session session = sessions.get(token);
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(notificationJson);
                System.out.println(notificationJson + "cmsapovnpvnoavopsa");
            } else {
                System.out.println("Session not found or closed for token: " + token);
            }
        } catch (IOException e) {
            System.out.println("Error sending notification to token: " + token);
            e.printStackTrace();
        }
    }

    public static void sendTaskToAll(List<TaskDto> listOfAllTasks) {
        for (Map.Entry<String, Session> entry : sessions.entrySet()) {
            String token = entry.getKey();
            Session session = entry.getValue();
            try {
                if (session != null && session.isOpen()) {
                    // Convert the list of tasks to JSON
                    String tasksJson = mapper.writeValueAsString(listOfAllTasks);

                    // Send the JSON string over WebSocket
                    session.getBasicRemote().sendText(tasksJson);
                    System.out.println("Tasks sent to token: " + token);
                } else {
                    System.out.println("Session not found or closed for token: " + token);
                }
            } catch (IOException e) {
                System.out.println("Error sending message to token: " + token);
                e.printStackTrace();
            }
        }
    }
}


