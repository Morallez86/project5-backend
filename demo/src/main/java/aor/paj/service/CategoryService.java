package aor.paj.service;

import aor.paj.bean.CategoryBean;
import aor.paj.bean.TokenBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.CategoryDto;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("/categories")
public class CategoryService {

    @Inject
    UserBean userBean;

    @Inject
    CategoryBean categoryBean;

    @Inject
    TokenBean tokenBean;

    private static final Logger logger = LogManager.getLogger(CategoryService.class);

    //Service that gets all categories from database
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCategories(@HeaderParam("token") String token, @QueryParam("active") boolean active, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to get categories. Active: {}, IP: {}", active, clientIP);

        if (userBean.isValidUserByToken(token)) {
            if (active) {
                logger.info("Retrieving active categories. IP: {}", clientIP);
                return Response.status(Response.Status.OK).entity(categoryBean.getActiveCategories()).build();
            } else {
                logger.info("Retrieving all categories. IP: {}", clientIP);
                return Response.status(Response.Status.OK).entity(categoryBean.getAllCategories()).build();
            }
        } else {
            logger.warn("Unauthorized access to get categories. IP: {}", clientIP);
            return Response.status(Response.Status.UNAUTHORIZED).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }


    //Service that receives a token and a category title, validates, checks if user is role po, checks if there is any task with the category, if not, deletes the category
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCategory(@HeaderParam("token") String token, @QueryParam("title") String title, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to delete category '{}' with IP: {}", title, clientIP);

        if (userBean.isValidUserByToken(token)) {
            if (userBean.getUserRole(token).equals("po")) {
                if (categoryBean.deleteCategory(title)) {
                    tokenBean.renewToken(token);
                    logger.info("Category '{}' deleted successfully. IP: {}", title, clientIP);
                    return Response.status(Response.Status.OK).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Category deleted"))).build();
                } else {
                    logger.warn("Failed to delete category '{}' due to existing tasks. IP: {}", title, clientIP);
                    return Response.status(Response.Status.BAD_REQUEST).entity(JsonUtils.convertObjectToJson(new ResponseMessage("There are tasks with this category. Delete these tasks before deleting the category."))).build();
                }
            } else {
                logger.warn("Unauthorized access to delete category '{}' by non-PO user. IP: {}", title, clientIP);
                return Response.status(Response.Status.UNAUTHORIZED).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
            }
        } else {
            logger.warn("Unauthorized access to delete category '{}' with invalid token. IP: {}", title, clientIP);
            return Response.status(Response.Status.UNAUTHORIZED).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }


    //Service that receives a token and a category dto, validates the token, checks if toke user role its po, checks if its a valid category and if so, adds the category to the database
    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addCategory(@HeaderParam("token") String token, CategoryDto category, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to add category '{}' with IP: {}", category.getTitle(), clientIP);

        if (userBean.isValidUserByToken(token)) {
            if (userBean.getUserRole(token).equals("po")) {
                if (categoryBean.isValidCategory(category)) {
                    if (categoryBean.addCategory(category)) {
                        tokenBean.renewToken(token);
                        logger.info("Category '{}' added successfully. IP: {}", category.getTitle(), clientIP);
                        return Response.status(Response.Status.OK).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Category added"))).build();
                    }
                } else {
                    logger.warn("Invalid category provided for addition. IP: {}", clientIP);
                    return Response.status(Response.Status.BAD_REQUEST).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid category"))).build();
                }
            } else {
                logger.warn("Unauthorized access to add category by non-PO user. IP: {}", clientIP);
                return Response.status(Response.Status.UNAUTHORIZED).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
            }
        } else {
            logger.warn("Unauthorized access to add category with invalid token. IP: {}", clientIP);
            return Response.status(Response.Status.UNAUTHORIZED).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        logger.error("Unexpected error occurred while adding category '{}'. IP: {}", category.getTitle(), clientIP);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to add category"))).build();
    }



    //service that receives a token, category dto and a category title, validates the token, checks if toke user role its po, checks if it's a valid category and if so, updates the category in the database
    @PUT
    @Path("/{categoryId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCategory(
            @HeaderParam("token") String token,
            @PathParam("categoryId") String categoryId,
            CategoryDto categoryDto,
            @QueryParam("title") String newTitle,
            @Context HttpServletRequest request
    ) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to update category '{}'. Category ID: {}, New Title: {}, IP: {}", categoryDto.getTitle(), categoryId, newTitle, clientIP);

        // Check if the user is authenticated
        if (!userBean.isValidUserByToken(token)) {
            logger.warn("Unauthorized access to update category by invalid token. Category ID: {}, IP: {}", categoryId, clientIP);
            return Response.status(401).entity(new ResponseMessage("Unauthorized")).build();
        }

        // Check if the user has permission to perform this action
        if (!userBean.getUserRole(token).equals("po")) {
            logger.warn("Forbidden access to update category by non-PO user. Category ID: {}, IP: {}", categoryId, clientIP);
            return Response.status(403).entity(new ResponseMessage("Forbidden")).build();
        }

        // Check if the provided category ID is valid
        if (categoryId == null || categoryId.isEmpty()) {
            logger.warn("Invalid category ID provided for category update. IP: {}", clientIP);
            return Response.status(400).entity(new ResponseMessage("Invalid category ID")).build();
        }

        // Check if the category DTO has correct data
        if (!categoryBean.isValidCategory(categoryDto)) {
            logger.warn("Invalid category data provided for category update. Category ID: {}, IP: {}", categoryId, clientIP);
            return Response.status(400).entity(new ResponseMessage("Invalid category data")).build();
        }

        // Update the category
        if (categoryBean.updateCategory(categoryDto, newTitle)) {
            tokenBean.renewToken(token);
            logger.info("Category '{}' updated successfully. Category ID: {}, New Title: {}, IP: {}", categoryDto.getTitle(), categoryId, newTitle, clientIP);
            return Response.status(200).entity(new ResponseMessage("Category updated")).build();
        } else {
            logger.error("Failed to update category '{}'. Category ID: {}, New Title: {}, IP: {}", categoryDto.getTitle(), categoryId, newTitle, clientIP);
            return Response.status(500).entity(new ResponseMessage("Failed to update category")).build();
        }
    }
}
