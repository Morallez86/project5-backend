package aor.paj.service;

import aor.paj.bean.DashBoardBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.DashboardGeneralStatsDto;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("dashboards")
public class DashboardService {

    @Inject
    DashBoardBean dashBoardBean;

    @Inject
    UserBean userBean;

    @GET
    @Path("/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDashboardStats(@HeaderParam("token") String token) {
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

        String userRole = userBean.getUserRole(token);
        if (!"po".equals(userRole)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized")))
                    .build();
        }

        try {
            DashboardGeneralStatsDto dashboardStats = dashBoardBean.mapToDashboardGeneralStatsDto();
            return Response.ok(dashboardStats).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to retrieve dashboard statistics")))
                    .build();
        }
    }

}
