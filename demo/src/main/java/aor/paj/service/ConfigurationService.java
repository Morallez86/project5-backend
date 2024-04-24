package aor.paj.service;

import aor.paj.bean.CategoryBean;
import aor.paj.bean.ConfigurationBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.CategoryDto;
import aor.paj.dto.ConfigurationDto;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/configurations")
public class ConfigurationService {

    @Inject
    ConfigurationBean configurationBean;

    @Inject
    UserBean userBean;

    @PUT
    @Path("/updateTokenExpirationTime")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateTokenExpirationTime(@HeaderParam("token") String token, int newTokenExpirationTime) {
        System.out.println("***********");
        if (userBean.isValidUserByToken(token)) {
            if (userBean.getUserRole(token).equals("po")) {
                try {
                    System.out.println("***********");
                    configurationBean.updateTokenExpirationTime(newTokenExpirationTime);
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Timeout updated"))).build();
                } catch (Exception e) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ResponseMessage("Failed to update token expiration time")).build();

                }
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).entity(new ResponseMessage("Unauthorized")).build();
            }
        }else {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new ResponseMessage("Unauthorized")).build();
        }
    }

    @GET
    @Path("/currentTokenExpirationTime")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCurrentConfiguration(@HeaderParam("token") String token) {
        if (userBean.isValidUserByToken(token)) {
            if (userBean.getUserRole(token).equals("po")) {
                try {
                    ConfigurationDto configurationDto = configurationBean.getTokenExpirationTime();
                    if (configurationDto != null) {
                        return Response.ok(configurationDto).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).entity(new ResponseMessage("Configuration not found")).build();
                    }
                } catch (Exception e) {
                    return Response.serverError().entity(new ResponseMessage("Failed to retrieve configuration")).build();
                }
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).entity(new ResponseMessage("Unauthorized")).build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new ResponseMessage("Unauthorized")).build();
        }
    }
}
