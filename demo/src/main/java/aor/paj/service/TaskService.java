package aor.paj.service;

import aor.paj.bean.TaskBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.TaskDto;
import aor.paj.dto.ManagingTaskDto;
import aor.paj.dto.UserDto;
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

    //Service that receives a task id, a token and a role, and checks if the user has the role to deactivate the task, and if its owner of task to desactivate it
    @PUT
    @Path("/deactivate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response desactivateTasks(@HeaderParam("token") String token, List<Integer> ids) {
        if (!userBean.isValidUserByToken(token)) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        UserDto user = userBean.getUserByToken(token);
        if (!user.getRole().equals("po") && !user.getRole().equals("sm")) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
        }

        boolean allTasksDeactivated = true;
        for (int id : ids) {
            if (!taskBean.taskExists(id)) {
                continue;
            }
            if (taskBean.isTaskInactive(id)) {
                continue;
            }
            if (!taskBean.desactivateTask(id)) {
                allTasksDeactivated = false;
                break;
            }
        }

        if (allTasksDeactivated) {
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("All tasks are deactivated successfully"))).build();
        } else {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to deactivate all tasks"))).build();
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
    @Path("/activate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response activateTasks(@HeaderParam("token") String token, List<Integer> ids) {
        if (!userBean.isValidUserByToken(token)) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        UserDto user = userBean.getUserByToken(token);
        if (!user.getRole().equals("po") && !user.getRole().equals("sm")) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
        }

        boolean allTasksActivated = true;
        for (int id : ids) {
            if (!taskBean.taskExists(id)) {
                continue;
            }
            if (taskBean.isTaskActive(id)) {
                continue;
            }
            if (!taskBean.activateTask(id)) {
                allTasksActivated = false;
                break;
            }
        }

        if (allTasksActivated) {
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("All tasks are activated successfully"))).build();
        } else {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to activate all tasks"))).build();
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

    @GET
    @Path("/allManagingTasks")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getManagingTasks(@HeaderParam("token") String token, @QueryParam("category") String category, @QueryParam("owner") String owner) {
        if (userBean.isValidUserByToken(token)) {
            List<ManagingTaskDto> managingTasks;
            if (category != null && !category.isEmpty() && owner != null && !owner.isEmpty()) {
                managingTasks = taskBean.getManagingTasksByCategoryAndOwner(category, owner);
            } else if (category != null && !category.isEmpty()) {
                managingTasks = taskBean.getManagingTasksByCategory(category);
            } else if (owner != null && !owner.isEmpty()) {
                managingTasks = taskBean.getManagingTasksByOwner(owner);
            } else {
                managingTasks = taskBean.getAllManagingTasks();
            }
            return Response.status(200).entity(managingTasks).build();
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }

}
