package aor.paj.service;

import aor.paj.bean.CategoryBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.CategoryDto;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/categories")
public class CategoryService {

    @Inject
    UserBean userBean;

    @Inject
    CategoryBean categoryBean;

    //Service that gets all categories from database
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCategories(@HeaderParam("token") String token, @QueryParam("active") boolean active) {
        if (userBean.isValidUserByToken(token)) {
            if (active) {
                return Response.status(200).entity(categoryBean.getActiveCategories()).build();
            } else {
                return Response.status(200).entity(categoryBean.getAllCategories()).build();
            }
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }

    //Service that receives a token and a category title, validates, checks if user is role po, checks if there is any task with the category, if not, deletes the category
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCategory(@HeaderParam("token") String token, @QueryParam("title") String title) {
        if (userBean.isValidUserByToken(token)) {
            if (userBean.getUserRole(token).equals("po")) {
                if (categoryBean.deleteCategory(title)) {
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Category deleted"))).build();
                } else {
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("There are tasks with this category. Delete these tasks before deleting the category."))).build();
                }
            } else {
                return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
            }
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }

    //Service that receives a token and a category dto, validates the token, checks if toke user role its po, checks if its a valid category and if so, adds the category to the database
    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addCategory(@HeaderParam("token") String token, CategoryDto category) {
        if (userBean.isValidUserByToken(token)) {
            if (userBean.getUserRole(token).equals("po")) {
                if (categoryBean.isValidCategory(category)) {
                    if (categoryBean.addCategory(category)) {
                        return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Category added"))).build();
                    }
                } else
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid category"))).build();
            } else {
                return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
            }
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
        return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
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
            @QueryParam("title") String newTitle
    ) {
        // Check if the user is authenticated
        if (!userBean.isValidUserByToken(token)) {
            return Response.status(401).entity(new ResponseMessage("Unauthorized")).build();
        }

        // Check if the user has permission to perform this action
        if (!userBean.getUserRole(token).equals("po")) {
            return Response.status(403).entity(new ResponseMessage("Forbidden")).build();
        }

        // Check if the provided category ID is valid
        if (categoryId == null || categoryId.isEmpty()) {
            return Response.status(400).entity(new ResponseMessage("Invalid category ID")).build();
        }

        // Check if the category DTO has correct data
        if (!categoryBean.isValidCategory(categoryDto)) {
            return Response.status(400).entity(new ResponseMessage("Invalid category data")).build();
        }

        // Update the category
        if (categoryBean.updateCategory(categoryDto, newTitle)) {
            return Response.status(200).entity(new ResponseMessage("Category updated")).build();
        } else {
            return Response.status(500).entity(new ResponseMessage("Failed to update category")).build();
        }
    }
}
