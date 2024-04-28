package aor.paj.service;

import aor.paj.bean.TaskBean;
import aor.paj.bean.TokenBean;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

//testing
@Path("/tasks")
public class TaskService {
    //
    @Inject
    TaskBean taskBean;

    @Inject
    UserBean userBean;

    @Inject
    TokenBean tokenBean;

    private static final Logger logger = LogManager.getLogger(CategoryService.class);

    //Service that receives a taskdto and a token and creates a new task with the user in token and adds the task to the task table in the database mysql
    @Path("/new")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTask(@HeaderParam("token") String token, TaskDto t, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to add new task. Task Title: {}, IP: {}", t.getTitle(), clientIP);

        // Check if the user is authenticated
        if (!userBean.isValidUserByToken(token)) {
            logger.warn("Unauthorized access to add new task by invalid token. Task Title: {}, IP: {}", t.getTitle(), clientIP);
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        // Validate the task object and check for existing task title
        if (TaskValidator.isValidTask(t) && !taskBean.taskTitleExists(t)) {
            // Attempt to add the task
            if (taskBean.addTask(token, t)) {
                tokenBean.renewToken(token);
                logger.info("New task added successfully. Task Title: {}, IP: {}", t.getTitle(), clientIP);
                return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task added successfully"))).build();
            } else {
                logger.error("Failed to add new task. Task Title: {}, IP: {}", t.getTitle(), clientIP);
                return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to add task"))).build();
            }
        } else {
            logger.warn("Invalid task or existing task title provided. Task Title: {}, IP: {}", t.getTitle(), clientIP);
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid task"))).build();
        }
    }


    //Retrieves tasks based on various criteria: id, category, owner
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTasks(@HeaderParam("token") String token,
                             @QueryParam("id") int id,
                             @QueryParam("category") String category,
                             @QueryParam("owner") String owner,
                             @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to retrieve tasks. IP: {}", clientIP);

        // Check if the user is authenticated
        if (!userBean.isValidUserByToken(token)) {
            logger.warn("Unauthorized access to retrieve tasks. IP: {}", clientIP);
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        try {
            if (id > 0) {
                // Fetch task by ID
                TaskDto task = taskBean.getTaskById(id);
                logger.info("Retrieved task by ID: {} for IP: {}", id, clientIP);
                return Response.status(200).entity(task).build();
            } else {
                // Fetch tasks based on category and/or owner
                List<TaskDto> tasks;
                if (category != null && !category.isEmpty() && owner != null && !owner.isEmpty()) {
                    tasks = taskBean.getTasksByCategoryAndOwner(category, owner);
                    logger.info("Retrieved tasks by Category: {} and Owner: {} for IP: {}", category, owner, clientIP);
                } else if (category != null && !category.isEmpty()) {
                    tasks = taskBean.getTasksByCategory(category);
                    logger.info("Retrieved tasks by Category: {} for IP: {}", category, clientIP);
                } else if (owner != null && !owner.isEmpty()) {
                    tasks = taskBean.getTasksByOwner(owner);
                    logger.info("Retrieved tasks by Owner: {} for IP: {}", owner, clientIP);
                } else {
                    tasks = taskBean.getActiveTasksOrderedByPriority(); // Fetch only active tasks
                    logger.info("Retrieved active tasks ordered by priority for IP: {}", clientIP);
                }
                return Response.status(200).entity(tasks).build();
            }
        } catch (Exception e) {
            logger.error("Error occurred while retrieving tasks. IP: {}", clientIP, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonUtils.convertObjectToJson(new ResponseMessage("Error retrieving tasks")))
                    .build();
        }
    }


    //Service that receives a task id, a token and a role, and checks if the user has the role to deactivate the task, and if its owner of task to desactivate it
    @PUT
    @Path("/deactivate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deactivateTasks(@HeaderParam("token") String token, List<Integer> ids, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to deactivate tasks. IP: {}", clientIP);

        // Check if the user is authenticated
        if (!userBean.isValidUserByToken(token)) {
            logger.warn("Unauthorized access to deactivate tasks. IP: {}", clientIP);
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        // Check user role for permission
        UserDto user = userBean.getUserByToken(token);
        if (!user.getRole().equals("po") && !user.getRole().equals("sm")) {
            logger.warn("Forbidden access to deactivate tasks from user{}. IP: {}", user.getUsername(), clientIP);
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
        }

        boolean allTasksDeactivated = true;
        for (int id : ids) {
            if (!taskBean.taskExists(id)) {
                logger.warn("Task with ID {} does not exist. Skipping deactivation. IP: {}", id, clientIP);
                continue;
            }
            if (taskBean.isTaskInactive(id)) {
                logger.info("Task with ID {} is already inactive. Skipping deactivation. IP: {}", id, clientIP);
                continue;
            }
            if (!taskBean.desactivateTask(id)) {
                logger.error("Failed to deactivate task with ID {}. IP: {}", id, clientIP);
                allTasksDeactivated = false;
                break;
            }
            logger.info("Task with ID {} deactivated successfully. IP: {}", id, clientIP);
        }

        if (allTasksDeactivated) {
            tokenBean.renewToken(token);
            logger.info("All tasks are deactivated successfully from user {}. IP: {}", user.getUsername(), clientIP);
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("All tasks are deactivated successfully"))).build();
        } else {
            logger.error("Failed to deactivate all tasks from user {}. IP: {}", user.getUsername(), clientIP);
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to deactivate all tasks"))).build();
        }
    }


    //Service that receives a token, a taskdto and a task id and updates the task with the id that is received
    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateTask(@HeaderParam("token") String token, TaskDto taskDto, @QueryParam("id") int id, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to update task with ID {}. IP: {}", id, clientIP);

        // Check if the user is authenticated
        if (!userBean.isValidUserByToken(token)) {
            logger.warn("Unauthorized access to update task with ID {}. IP: {}", id, clientIP);
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        // Check if the user has permission to edit the task
        if (!userBean.hasPermissionToEdit(token, id)) {
            logger.warn("User does not have permission to edit task with ID {}. IP: {}", id, clientIP);
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
        }

        // Validate the updated task data
        if (!TaskValidator.isValidTaskEdit(taskDto)) {
            logger.warn("Invalid task data provided for update. Task ID: {}. IP: {}", id, clientIP);
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid task"))).build();
        }

        // Update the task
        taskBean.updateTask(taskDto, id);
        tokenBean.renewToken(token);
        logger.info("Task with ID {} updated successfully. IP: {}", id, clientIP);
        return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task updated successfully"))).build();
    }


    //Service that receives a token and a task name, validates the token and sets the active of that task to true
    @PUT
    @Path("/activate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response activateTasks(@HeaderParam("token") String token, List<Integer> ids, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to activate tasks. IP: {}", clientIP);

        // Check if the user is authenticated
        if (!userBean.isValidUserByToken(token)) {
            logger.warn("Unauthorized access to activate tasks. IP: {}", clientIP);
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        // Retrieve user information to verify role
        UserDto user = userBean.getUserByToken(token);

        // Check if the user has appropriate role (e.g., "po" or "sm") to perform the action
        if (!user.getRole().equals("po") && !user.getRole().equals("sm")) {
            logger.warn("User does not have permission to activate tasks. IP: {}", clientIP);
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
        }

        // Process activation of tasks
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

        // Respond based on activation results
        if (allTasksActivated) {
            logger.info("All tasks activated successfully. IP: {}", clientIP);
            tokenBean.renewToken(token);
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("All tasks activated successfully"))).build();
        } else {
            logger.warn("Failed to activate all tasks. IP: {}", clientIP);
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to activate all tasks"))).build();
        }
    }

    @GET
    @Path("/allManagingTasks")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getManagingTasks(@HeaderParam("token") String token, @QueryParam("category") String category, @QueryParam("owner") String owner) {
        if (userBean.isValidUserByToken(token)) {
            List<ManagingTaskDto> managingTasks;
            String userRole = userBean.getUserRole(token);
            if (category != null && !category.isEmpty() && owner != null && !owner.isEmpty()) {
                managingTasks = taskBean.getManagingTasksByCategoryAndOwner(category, owner);
            } else if (category != null && !category.isEmpty()) {
                managingTasks = taskBean.getManagingTasksByCategory(category);
            } else if (owner != null && !owner.isEmpty()) {
                managingTasks = taskBean.getManagingTasksByOwner(owner);
            } else {
                if (userRole.equals("po")) {
                    managingTasks = taskBean.getAllManagingTasks();
                } else if(userRole.equals("sm")){
                    managingTasks = taskBean.getAllActiveManagingTasks();
                } else {
                    return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
                }
            }
            tokenBean.renewToken(token);
            return Response.status(200).entity(managingTasks).build();
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }

    //Service that receives a token, checks if the user is valid, checks if user role = sm or po, and restore all tasks

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTasks(@HeaderParam("token") String token, List<Integer> selectedTasks, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to delete tasks. IP: {}", clientIP);

        // Check if the user is authenticated
        if (!userBean.isValidUserByToken(token) || !"po".equals(userBean.getUserByToken(token).getRole())) {
            logger.warn("Unauthorized access to delete tasks. IP: {}", clientIP);
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        // Validate selectedTasks and ensure all are inactive before deletion
        if (selectedTasks == null || selectedTasks.isEmpty()) {
            logger.warn("No task IDs provided for deletion. IP: {}", clientIP);
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("No task IDs provided"))).build();
        }

        // Check if all selected tasks are inactive
        boolean allInactive = true;
        for (Integer taskId : selectedTasks) {
            if (taskBean.isTaskActive(taskId)) {
                allInactive = false;
                break;
            }
        }

        if (!allInactive) {
            logger.warn("One or more selected tasks are active. IP: {}", clientIP);
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("One or more selected tasks are active"))).build();
        }

        // Iterate through the list of task IDs and delete each task
        for (Integer taskId : selectedTasks) {
            if (!taskBean.deleteTask(taskBean.getStringTaskById(taskId))) {
                logger.error("Failed to delete task with ID {}. IP: {}", taskId, clientIP);
                return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("One or more tasks not deleted"))).build();
            }
            logger.info("Task with ID {} deleted successfully. IP: {}", taskId, clientIP);
        }

        logger.info("All selected tasks deleted successfully. IP: {}", clientIP);
        tokenBean.renewToken(token);
        return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Tasks deleted"))).build();
    }


    // Function to retrieve all inactive tasks
    @GET
    @Path("/inactive")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInactiveTasks(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to retrieve inactive tasks. IP: {}", clientIP);
        if (userBean.isValidUserByToken(token)) {

            String userRole = userBean.getUserRole(token);
            if (userRole.equals("po") || userRole.equals("sm")) {
                List<ManagingTaskDto> inactiveTasks = taskBean.getInactiveManagingTasks();
                tokenBean.renewToken(token);
                logger.info("All selected inactive tasks were sent. IP: {}", clientIP);
                return Response.status(200).entity(inactiveTasks).build();
            } else {
                logger.warn("User does not have permission to retrieve inactive tasks. IP: {}", clientIP);
                return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
            }
        } else {
            logger.warn("Unauthorized access to retrieve inactive tasks. IP: {}", clientIP);
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }
}
