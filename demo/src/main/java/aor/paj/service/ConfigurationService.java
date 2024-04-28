package aor.paj.service;

import aor.paj.bean.CategoryBean;
import aor.paj.bean.ConfigurationBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.CategoryDto;
import aor.paj.dto.ConfigurationDto;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("/configurations")
public class ConfigurationService {

    @Inject
    ConfigurationBean configurationBean;

    @Inject
    UserBean userBean;

    private static final Logger logger = LogManager.getLogger(ConfigurationService.class);

    @PUT
    @Path("/updateTokenExpirationTime")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateTokenExpirationTime(@HeaderParam("token") String token, int newTokenExpirationTime, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to update token expiration time. New Expiration Time: {}, IP: {}", newTokenExpirationTime, clientIP);

        if (userBean.isValidUserByToken(token)) {
            if (userBean.getUserRole(token).equals("po")) {
                try {
                    configurationBean.updateTokenExpirationTime(newTokenExpirationTime);
                    logger.info("Token expiration time updated successfully. New Expiration Time: {}, IP: {}", newTokenExpirationTime, clientIP);
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Timeout updated"))).build();
                } catch (Exception e) {
                    logger.error("Failed to update token expiration time. New Expiration Time: {}, IP: {}", newTokenExpirationTime, clientIP, e);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ResponseMessage("Failed to update token expiration time")).build();
                }
            } else {
                logger.warn("Unauthorized access to update token expiration time. Token: {}, IP: {}", token, clientIP);
                return Response.status(Response.Status.UNAUTHORIZED).entity(new ResponseMessage("Unauthorized")).build();
            }
        } else {
            logger.warn("Unauthorized access to update token expiration time. IP: {}", clientIP);
            return Response.status(Response.Status.UNAUTHORIZED).entity(new ResponseMessage("Unauthorized")).build();
        }
    }


    @GET
    @Path("/currentTokenExpirationTime")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCurrentConfiguration(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to get current token expiration time. IP: {}", clientIP);

        if (userBean.isValidUserByToken(token)) {
            if (userBean.getUserRole(token).equals("po")) {
                try {
                    ConfigurationDto configurationDto = configurationBean.getTokenExpirationTime();
                    if (configurationDto != null) {
                        logger.info("Retrieved current token expiration time successfully. IP: {}", clientIP);
                        return Response.ok(configurationDto).build();
                    } else {
                        logger.warn("Configuration not found. IP: {}", clientIP);
                        return Response.status(Response.Status.NOT_FOUND).entity(new ResponseMessage("Configuration not found")).build();
                    }
                } catch (Exception e) {
                    logger.error("Failed to retrieve current token expiration time. IP: {}", clientIP, e);
                    return Response.serverError().entity(new ResponseMessage("Failed to retrieve configuration")).build();
                }
            } else {
                logger.warn("Unauthorized access to retrieve current token expiration time. IP: {}", clientIP);
                return Response.status(Response.Status.UNAUTHORIZED).entity(new ResponseMessage("Unauthorized")).build();
            }
        } else {
            logger.warn("Unauthorized access to retrieve current token expiration time. IP: {}", clientIP);
            return Response.status(Response.Status.UNAUTHORIZED).entity(new ResponseMessage("Unauthorized")).build();
        }
    }
}
