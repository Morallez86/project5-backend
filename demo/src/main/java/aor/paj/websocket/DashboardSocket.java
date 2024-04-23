package aor.paj.websocket;

import aor.paj.bean.TokenBean;
import aor.paj.dto.CategoryTaskCountDto;
import aor.paj.dto.DashboardGeneralStatsDto;
import aor.paj.dto.DashboardLineChartDto;
import aor.paj.dto.DashboardTaskLineChartDto;

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
@ServerEndpoint("/websocket/dashboard/{token}")
public class DashboardSocket {

    @Inject
    TokenBean tokenBean;

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
            System.out.println("New WebSocket session opened for dashboard with token: " + token);
        } else {
            System.out.println("Invalid token. WebSocket connection rejected for dashboard with token: " + token);
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
        System.out.println("WebSocket session closed for dashboard with token: " + token +
                " Reason: " + reason.getCloseCode() + ": " + reason.getReasonPhrase());
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        // Handle incoming messages from the dashboard if needed
        System.out.println("Received message from dashboard with token: " + session.getPathParameters().get("token") + ", Message: " + message);
    }

    public static void sendDashboardGeneralStatsDtoToAll (DashboardGeneralStatsDto dashboardGeneralStatsDto) {
        for (Map.Entry<String, Session> entry : sessions.entrySet()) {
            String token = entry.getKey();
            Session session = entry.getValue();
            try {
                if (session != null && session.isOpen()) {
                    // Convert the list of tasks to JSON
                    String tasksJson = mapper.writeValueAsString(dashboardGeneralStatsDto);

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

    public static void sendDashboardLineChartDtoToAll (List<DashboardLineChartDto> dashboardLineChartDto) {
        for (Map.Entry<String, Session> entry : sessions.entrySet()) {
            String token = entry.getKey();
            Session session = entry.getValue();
            try {
                if (session != null && session.isOpen()) {
                    // Convert the list of tasks to JSON
                    String tasksJson = mapper.writeValueAsString(dashboardLineChartDto);

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
    public static void sendDashboardTaskLineChartDtoToAll (List<DashboardTaskLineChartDto> DashboardTaskLineChartDto) {
        for (Map.Entry<String, Session> entry : sessions.entrySet()) {
            String token = entry.getKey();
            Session session = entry.getValue();
            try {
                if (session != null && session.isOpen()) {
                    // Convert the list of tasks to JSON
                    String tasksJson = mapper.writeValueAsString(DashboardTaskLineChartDto);

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

    public static void sendCategoryTaskCountDtoToAll(List<CategoryTaskCountDto> categoryTaskCountDto) {
        for (Map.Entry<String, Session> entry : sessions.entrySet()) {
            String token = entry.getKey();
            Session session = entry.getValue();
            try {
                if (session != null && session.isOpen()) {
                    // Convert the DTO object to JSON
                    String dtoJson = mapper.writeValueAsString(categoryTaskCountDto);

                    // Send the JSON string over WebSocket
                    session.getBasicRemote().sendText(dtoJson);
                    System.out.println("CategoryTaskCountDto sent to token: " + token);
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
