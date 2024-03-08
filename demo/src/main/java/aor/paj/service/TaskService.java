package aor.paj.service;

import aor.paj.bean.TaskBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.TaskDto;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
import aor.paj.validator.TaskValidator;
import aor.paj.validator.UserValidator;
import jakarta.inject.Inject;
import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.Path;

import java.util.List;
import java.util.Objects;

@Path("/task")
public class TaskService {
    //
    @Inject
    TaskBean taskBean;

    @Inject
    UserBean userBean;

    //Service that receives a taskdto and a token and creates a new task with the user in token and adds the task to the task table in the database mysql
    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTask(@HeaderParam("token") String token, TaskDto t) {
        if (userBean.isValidUserByToken(token)) {
            if (TaskValidator.isValidTask(t) && !taskBean.taskTitleExists(t)) {
                if (taskBean.addTask(token, t)) {
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task is added"))).build();
                } else {
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Cannot add task"))).build();
                }
            } else {
                return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid task"))).build();
            }
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }

    @GET
    @Path("/all")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTasks(@HeaderParam("token") String token, @QueryParam("category") String category, @QueryParam("owner") String owner) {
        if (userBean.isValidUserByToken(token)) {
            List<TaskDto> tasks;
            if (category != null && !category.isEmpty() && owner != null && !owner.isEmpty()) {
                tasks = taskBean.getTasksByCategoryAndOwner(category, owner);
            } else if (category != null && !category.isEmpty()) {
                tasks = taskBean.getTasksByCategory(category);
            } else if (owner != null && !owner.isEmpty()) {
                tasks = taskBean.getTasksByOwner(owner);
            } else {
                tasks = taskBean.getAllTasks();
            }
            return Response.status(200).entity(tasks).build();
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }

    //Service that updates the task status
    @PUT
    @Path("/updateStatus")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateTask(@HeaderParam("token") String token, @QueryParam("id") int id, @QueryParam("status") int status) {
        if (userBean.isValidUserByToken(token) && TaskValidator.isValidStatus(status)) {
            taskBean.updateTaskStatus(id, status);
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task is updated"))).build();
        } else {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid status"))).build();
        }
    }

    //Service that receives a task id, a token and a role, and checls if the user has the role to desactivate the task, and if its owner of task to desactivate it
    @PUT
    @Path("/desactivate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response desactivateTask(@HeaderParam("token") String token, @QueryParam("id") int id, @QueryParam("role") String role) {
        if (userBean.isValidUserByToken(token)) {
            if (taskBean.taskBelongsToUser(token, id)) {
                if (taskBean.desactivateTask(id)) {
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task is desactivated"))).build();
                } else {
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Cannot desactivate task"))).build();
                }
            } else if (!Objects.equals(role, "dev")) {
                if (taskBean.desactivateTask(id)) {
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task is desactivated"))).build();
                } else {
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Cannot desactivate task"))).build();
                }
            } else {
                return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
            }
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }

    //Service that receives a token and a task id and sends the task object that has the id that is received
    @GET
    @Path("/get")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTask(@HeaderParam("token") String token, @QueryParam("id") int id) {
        if (userBean.isValidUserByToken(token)) {
            return Response.status(200).entity(taskBean.getTaskById(id)).build();
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }

    //Service that receives a token of a user, validates


    //Service that receives a token, a taskdto and a task id and updates the task with the id that is received
    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateTask(@HeaderParam("token") String token, TaskDto t, @QueryParam("id") int id) {
        if (userBean.isValidUserByToken(token)) {
            if(userBean.hasPermissionToEdit(token, id)){
                if (TaskValidator.isValidTaskEdit(t)) {
                    taskBean.updateTask(t, id);
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task is updated"))).build();
                } else {
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid task"))).build();
                }
            } else {
                return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
            }
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }

    //Service that receives a token and a task name, validates the token and sets the active of that task to true
    @PUT
    @Path("/restore")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response restoreTask(@HeaderParam("token") String token, @QueryParam("name") String name) {
        if (userBean.isValidUserByToken(token)) {
            String role = userBean.getUserRole(token);
            if (role.equals("sm") || role.equals("po")) {
                if (taskBean.restoreTask(name)) {
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task is restored"))).build();
                } else {
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Cannot restore task"))).build();
                }
            } else {
                return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
            }
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }

    //Service that receives a token and a task name, validates the token, checks if user = po, and deletes the task from the database
    @DELETE
    @Path("/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeTask(@HeaderParam("token") String token, @QueryParam("name") String name) {
        if (userBean.isValidUserByToken(token)) {
            String role = userBean.getUserRole(token);
            if (role.equals("po")) {
                if (taskBean.deleteTask(name)) {
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task is deleted"))).build();
                } else {
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Cannot delete task"))).build();
                }
            } else {
                return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
            }
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }

    //Service that receives a token, checks if the user is valid, checks if user role = sm or po, and restore all tasks
    @PUT
    @Path("/restoreAll")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response restoreAllTasks(@HeaderParam("token") String token) {
        if (userBean.isValidUserByToken(token)) {
            String role = userBean.getUserRole(token);
            if (role.equals("sm") || role.equals("po")) {
                if (taskBean.restoreAllTasks()) {
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("All tasks are restored"))).build();
                } else {
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Cannot restore all tasks"))).build();
                }
            } else {
                return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
            }
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }

    //Service that receives a token, checks if the user is valid, checks if user role = po, and deletes all tasks
    @DELETE
    @Path("/deleteAll")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAllTasks(@HeaderParam("token") String token) {
        if (userBean.isValidUserByToken(token)) {
            String role = userBean.getUserRole(token);
            if (role.equals("po")) {
                if (taskBean.deleteAllTasks()) {
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("All tasks are deleted"))).build();
                } else {
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Cannot delete all tasks"))).build();
                }
            } else {
                return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
            }
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }
}
