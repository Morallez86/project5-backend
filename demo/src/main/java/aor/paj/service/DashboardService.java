package aor.paj.service;

import aor.paj.bean.DashBoardBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.CategoryTaskCountDto;
import aor.paj.dto.DashboardGeneralStatsDto;
import aor.paj.dto.DashboardLineChartDto;
import aor.paj.dto.DashboardTaskLineChartDto;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

@Path("dashboards")
public class DashboardService {

    @Inject
    DashBoardBean dashBoardBean;

    @Inject
    UserBean userBean;

    @GET
    @Path("/userTaskStats")
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

    @GET
    @Path("/categoryStats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDashboardCategoryStats(@HeaderParam("token") String token) {
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
            // Retrieve the task counts by category
            List<CategoryTaskCountDto> categoryCounts = dashBoardBean.displayTaskCountsByCategory();
            return Response.ok(categoryCounts).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to retrieve task counts by category")))
                    .build();
        }
    }

    @GET
    @Path("/lineChartStats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDashboardLineChartStats(@HeaderParam("token") String token) {
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
            // Retrieve the task counts by category
            List<DashboardLineChartDto> userRegistrationData = dashBoardBean.convertUserEntityToDashboardLineChartDto();

            // Retrieve the task line chart data
            List<DashboardTaskLineChartDto> taskLineChartData = dashBoardBean.convertTaskEntityToDashboardLineChartDto();

            // Merge or combine the data as needed into a single response entity
            // For simplicity, assume combining both into a single DTO list
            List<Object> responseData = new ArrayList<>();
            responseData.addAll(userRegistrationData);
            responseData.addAll(taskLineChartData);

            return Response.ok(responseData).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to retrieve line chart information")))
                    .build();
        }
    }
}
