package aor.paj.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import aor.paj.bean.TokenBean;
import aor.paj.bean.UserBean;
import aor.paj.dao.TaskDao;
import aor.paj.dto.*;
import aor.paj.entity.UserEntity;
import aor.paj.pojo.ConfirmationRequest;
import aor.paj.pojo.LoginRequest;
import aor.paj.pojo.LogoutRequest;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
import aor.paj.validator.UserValidator;
import aor.paj.websocket.DashboardSocket;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Objects;

@Path("/users")
public class UserService {

    @Inject
    UserBean userBean;

    @Inject
    TokenBean tokenBean;

    @Inject
    TaskDao taskDao;

    private static final Logger logger  = LogManager.getLogger(UserService.class);

    //Service that manages the login of the user, sets the token for the user and sends the token and the role of the user
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest loginRequest, @Context HttpServletRequest request) {
        // Retrieve client's IP address
        String clientIP = request.getRemoteAddr();
        try {
            String username = loginRequest.getUsername();
            String password = loginRequest.getPassword();

            // Authenticate user and get token
            String token = userBean.login(username, password);

            if (token != null) {
                // Check if the user is active
                UserDto userDto = userBean.getUserByUsername(username);
                if (userDto != null) {
                    if (userDto.isActive()) {
                        // User is active, return token and user role
                        TokenAndRoleDto tokenAndRoleDto = new TokenAndRoleDto(token, userDto.getRole(), userDto.getUsername(), userDto.getId());

                        // Log successful login with IP address
                        logger.info("User logged in successfully: " + username + " from IP: " + clientIP);

                        return Response.status(200).entity(tokenAndRoleDto).build();
                    } else {
                        // User is not active, return forbidden status
                        logger.warn("User login failed - account not active: " + username);
                        return Response.status(403).entity(new ResponseMessage("User is not active")).build();
                    }
                } else {
                    // User not found, return unauthorized status
                    logger.warn("User login failed - user not found: " + username);
                    return Response.status(401).entity(new ResponseMessage("Login Failed")).build();
                }
            } else {
                UserDto userDtoPending = userBean.getUserByUsername(username);
                // Check if the user is pending
                if(userDtoPending!=null){
                    if (userDtoPending.isPending()) {
                        // User is pending, return unauthorized status
                        logger.warn("User login failed - account pending registration: " + username);
                        return Response.status(401).entity(new ResponseMessage("User registration pending")).build();
                    }
                }
                // Log failed login attempt with IP address
                logger.warn("Failed login attempt for user: " + username + " from IP: " + clientIP);
                return Response.status(401).entity(new ResponseMessage("Login Failed")).build();
            }
        } catch (Exception e) {
            // Log internal server error with IP address
            logger.warn("Internal Server Error from IP: " + clientIP);
            e.printStackTrace(); // Log the stack trace for detailed error information

            return Response.status(500).entity(new ResponseMessage("Internal Server Error")).build();
        }
    }


    @POST
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(LogoutRequest logoutRequest, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        String token = logoutRequest.getToken();
        String username = logoutRequest.getUsername();

        if (token == null || username == null) {
            logger.warn("Logout failed: Token or username missing - Token: {}, Username: {}, from IP: {}", token, username, clientIP);
            return Response.status(400).entity(new ResponseMessage("Token or username missing")).build();
        }

        if (!tokenBean.isValidToken(token)) {
            logger.warn("Logout failed: Invalid token - Token: {}, Username: {}, from IP: {}", token, username, clientIP);
            return Response.status(401).entity(new ResponseMessage("Invalid token")).build();
        }

        if (!tokenBean.isValidTokenForUser(token, username)) {
            logger.warn("Logout failed: Token does not correspond to the provided username - Token: {}, Username: {}, from IP: {}", token, username, clientIP);
            return Response.status(401).entity(new ResponseMessage("Token does not correspond to the provided username")).build();
        }

        tokenBean.deleteToken(token);

        // Log successful logout
        logger.info("User logged out successfully - Username: {}, from IP: {}", username, clientIP);

        return Response.status(200).entity(new ResponseMessage("User is logged out")).build();
    }

    //Service that receives a user object and adds it to the list of users
    @POST
    @Path("/new")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(UserDto u, @HeaderParam("token") String token, @HeaderParam("role") String roleNewUser, @Context HttpServletRequest request ) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to add new user from IP: {}", clientIP);
        if (UserValidator.isNullorBlank(u)) {
            logger.warn("Failed to add new user: One or more parameters are null or blank - IP: {}", clientIP);
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("One or more parameters are null or blank"))).build();
        }

        // Validate email format
        if (!UserValidator.isValidEmail(u.getEmail())) {
            logger.warn("Failed to add new user: Invalid email format - Email: {}, IP: {}", u.getEmail(), clientIP);
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid email format"))).build();
        }

        // Validate phone number format
        if (!UserValidator.isValidPhoneNumber(u.getPhone())) {
            logger.warn("Failed to add new user: Invalid phone number format - Phone: {}, IP: {}", u.getPhone(), clientIP);
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid phone number format"))).build();
        }

        // Validate URL format
        if (!UserValidator.isValidURL(u.getPhotoURL())) {
            logger.warn("Failed to add new user: Invalid URL format - URL: {}, IP: {}", u.getPhotoURL(), clientIP);
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid URL format"))).build();
        }

        // Check if username or email already exists
        if (userBean.userExists(u)) {
            logger.warn("Failed to add new user: Username or Email already exists - Username: {}, Email: {}, IP: {}", u.getUsername(), u.getEmail(), clientIP);
            return Response.status(409).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid Username or Email"))).build();
        }

        // Check if the user is a PO & if the token is valid and create the new user
        String userToken="";
        userToken = token;
        if(userToken!=null && roleNewUser != null && userBean.isValidUserByToken(userToken)){
            String role = userBean.getUserByToken(userToken).getRole();
            if(role.equals("po")){
                userBean.addUserPO(u);
                logger.info("New user added by PO - Username: {}, IP: {}", u.getUsername(), clientIP);
                return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("A new user is created")).toString()).build();
            }

            }else{
                // If all checks pass, add the user
                userBean.addUser(u);
                logger.info("New user added - Username: {}, IP: {}", u.getUsername(), clientIP);
                return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("A new user is created"))).build();
        }
        logger.error("Failed to add new user due to internal server error - IP: {}", clientIP);
        return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized")).toString()).build();
    }

    @POST
    @Path("/confirmRegistration")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response confirmRegistration(ConfirmationRequest request, @Context HttpServletRequest requestHttp) {
        String clientIP = requestHttp.getRemoteAddr();
        String emailValidationToken = request.getToken();
        String password = request.getPassword();

        UserDto user = userBean.getUserByEmailValidationToken(emailValidationToken);
        logger.info("Received confirmation registration request from IP: {} of user: {}", clientIP, user.getUsername());
        if (user != null) {
            try {
                userBean.confirmRegistration(user, password);
                logger.info("User registration confirmed successfully - Email: {}, IP: {}", user.getEmail(), clientIP);
                return Response.status(200).entity(new ResponseMessage("Registration confirmed")).build();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Failed to confirm registration for user - Email: {}, IP: {}", user.getEmail(), clientIP, e);
                return Response.status(500).entity(new ResponseMessage("Failed to confirm registration")).build();
            }
        } else {
            logger.warn("User not found for registration confirmation - Email Validation Token: {}, IP: {}", emailValidationToken, clientIP);
            return Response.status(404).entity(new ResponseMessage("User not found")).build();
        }
    }


    // Function that returns the list of all users if role is PO or active users if SM
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to get all users from IP: {}", clientIP);
        if (token == null || token.isEmpty()) {
            logger.warn("Invalid token received for retrieving users - IP: {}", clientIP);
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid token"))).build();
        }
        if (!userBean.isValidUserByToken(token)) {
            logger.warn("Unauthorized access to retrieve users - IP: {}", clientIP);
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
        String userRole = userBean.getUserRole(token);
        if (userRole == null) {
            logger.error("User role not found for token - IP: {}", clientIP);
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User role not found"))).build();
        }

        List<UserDto> userDtos;

        if (userRole.equals("po")) {
            userDtos = userBean.getAllUsersDB();
        } else if (userRole.equals("sm")) {
            userDtos = userBean.getAllActiveUsers();
        } else {
            logger.warn("Unauthorized user role to retrieve users - User Role: {}, IP: {}", userRole, clientIP);
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        if (userDtos == null || userDtos.isEmpty()) {
            logger.info("No users found for the requested role - User Role: {}, IP: {}", userRole, clientIP);
            return Response.status(404).entity(JsonUtils.convertObjectToJson(new ResponseMessage("No users found"))).build();
        }
        logger.info("Retrieved {} users for role: {} - IP: {}", userDtos.size(), userRole, clientIP);
        return Response.status(200).entity(userDtos).build();
    }


    //Service that receives a token and a username and sends the photoURL of the usersame
    @GET
    @Path("/photo")
    @Produces({MediaType.APPLICATION_JSON, })
    public Response getPhoto(@HeaderParam("token") String token, @HeaderParam("username") String username, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to get photo for user '{}' from IP: {}", username, clientIP);
        if (userBean.isValidUserByToken(token)) {
            logger.info("Photo retrieved successfully for user '{}' - IP: {}", username, clientIP);
            UserDto userDto = userBean.getUserByUsername(username);
            return Response.status(200).entity(JsonUtils.convertObjectToJson((userDto.getPhotoURL()))).build();
        } else {
            logger.warn("Unauthorized access to get photo for user '{}' - IP: {}", username, clientIP);
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }


    @GET
    @Path("/profileDetails/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserProfileDetails(@HeaderParam("token") String token, @PathParam("userId") int userId, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        // If userId is 0, retrieve the user ID from the token and allow access regardless of role
        logger.info("Received request to get profile details for user ID: {} from IP: {}", userId, clientIP);
        if (userId == 0) {
            // Retrieve user ID from the token
            userId = userBean.getUserByToken(token).getId();
            if (userId == 0) {
                logger.warn("Unauthorized access to profile details - Invalid user ID retrieved from token - IP: {}", clientIP);
                return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
            }
        } else {
            logger.warn("Unauthorized access to profile details - Developer role not allowed - IP: {}", clientIP);
            // If userId is not 0, verify the role of the token user
            String userRole = userBean.getUserRole(token);
            if (Objects.equals(userRole, "dev")) {
                return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
            }
        }

        // Retrieve user details by ID
        UserDto userDto = userBean.getUserById(userId);
        if (userDto != null) {
            List<Object[]> taskCounts = taskDao.countTasksByStatus(userId);
            long totalTasks = taskDao.countTotalTasksByUser(userId);

            // Extract task counts map from the result
            Map<String, Integer> taskCountsMap = userBean.extractTaskCounts(taskCounts);

            // Create UserDetailsDto object with user details, task counts, and total tasks
            UserDetailsDto userDetails = new UserDetailsDto(
                    userDto.getUsername(),
                    userDto.getFirstname(),
                    userDto.getLastname(),
                    userDto.getEmail(),
                    userDto.getPhotoURL(),
                    userDto.getPhone(),
                    userDto.getRole(),
                    taskCountsMap,
                    totalTasks
            );
            logger.info("Profile details retrieved successfully for user ID: {} - IP: {}", userId, clientIP);
            return Response.status(200).entity(userDetails).build();
        } else {
            // User with the specified ID not found
            logger.warn("User not found for user ID: {} - IP: {}", userId, clientIP);
            return Response.status(404).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User not found"))).build();
        }
    }

    @PUT
    @Path("/updateProfile/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProfile(UserUpdateDto u, @HeaderParam("token") String token, @PathParam("userId") int userId, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to update profile for user ID: {} from IP: {}", userId, clientIP);
        if (userId == 0) {
            // Retrieve user ID from the token
            userId = userBean.getUserByToken(token).getId();
            if (userId == 0) {
                logger.warn("Unauthorized access to update profile - Invalid user ID retrieved from token - IP: {}", clientIP);
                return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
            }
        }

        if (!userBean.isValidUserByToken(token)) {
            logger.warn("Unauthorized access to update profile - Invalid token - IP: {}", clientIP);
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        } else {
            // Retrieve the authenticated user
            UserDto authenticatedUser = userBean.getUserByToken(token);
            System.out.println(authenticatedUser.getEmail());

            // Check if the authenticated user has the required permissions
            if (authenticatedUser.getRole().equals("po") || authenticatedUser.getId() == userId) {
                // Retrieve the user being updated
                UserDto userToUpdate = userBean.getUserById(userId);
                System.out.println(userToUpdate.getEmail());

                if (!UserValidator.isValidEmail(u.getEmail())) {
                    logger.warn("Invalid email format for update - User ID: {} - IP: {}", userId, clientIP);
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid email format"))).build();
                } else if (!u.getEmail().equals(userToUpdate.getEmail()) && UserValidator.emailExists(userBean.getAllUsersDB(), u.getEmail())
                        && (!userToUpdate.getEmail().equals(u.getEmail()))) {
                    logger.warn("Email already exists for update - User ID: {} - IP: {}", userId, clientIP);
                    return Response.status(409).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Email already exists"))).build();
                } else if (!UserValidator.isValidPhoneNumber(u.getPhone())) {
                    logger.warn("Invalid phone number format for update - User ID: {} - IP: {}", userId, clientIP);
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid phone number format"))).build();
                } else if (!UserValidator.isValidURL(u.getPhotoURL())) {
                    logger.warn("Invalid URL format for update - User ID: {} - IP: {}", userId, clientIP);
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid URL format"))).build();
                } else {
                    userBean.updateUserById(userId, u);
                    logger.info("User profile updated successfully - User ID: {} - IP: {}", userId, clientIP);
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User is updated")).toString()).build();
                }
            } else {
                // Unauthorized access
                logger.warn("Unauthorized access to update profile - User ID: {} - IP: {}", userId, clientIP);
                return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
            }
        }
    }

    //Services tha receives a UserPasswordDto object, authenticates the user, sees if the user that is logged is the same as the one that is being updated and updates the user password
    @PUT
    @Path("/updatePassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePassword(UserPasswordUpdateDto u, @HeaderParam("token") String token, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to update password from IP: {}", clientIP);
        if (!userBean.isValidUserByToken(token)) {
            logger.warn("Unauthorized access to update password - Invalid token - IP: {}", clientIP);
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        // Check if the new password is the same as the old password
        if (u.getOldPassword().equals(u.getNewPassword())) {
            logger.warn("Password update failed - New password must be different from the old password - IP: {}", clientIP);
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("New password must be different from the old password"))).build();
        }

        boolean updateTry = userBean.updatePassword(u, token);
        if (!updateTry) {
            logger.warn("Password update failed - Old password is incorrect - IP: {}", clientIP);
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Old password is incorrect"))).build();
        } else {
            logger.info("Password updated successfully - IP: {}", clientIP);
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Password is updated")).toString()).build();
        }
    }


    @POST
    @Path("/updateInactive")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response desactivateTasks(@HeaderParam("token") String token, List<Integer> ids, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to deactivate users from IP: {}", clientIP);
        if (!userBean.isValidUserByToken(token)) {
            logger.warn("Unauthorized access to deactivate users - Invalid token - IP: {}", clientIP);
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        UserDto user = userBean.getUserByToken(token);
        if (!user.getRole().equals("po")) {
            logger.warn("Forbidden access to deactivate users - User is not a Product Owner - IP: {}", clientIP);
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
        }

        boolean allUsersActivated = true;
        for (int id : ids) {
            if (userBean.getUserById(id) == null) {
                allUsersActivated = false;
            }
            if (!userBean.getUserById(id).isActive()) {
                allUsersActivated = false;
            }
        }

        if (!allUsersActivated) {
            logger.warn("Failed to deactivate all users - Some users are not active or do not exist - IP: {}", clientIP);
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to deactivate all users"))).build();
        } else {
            for (int id : ids) {
                userBean.changeStatus(userBean.getUserById(id).getUsername(), false);
            }
            logger.info("All users are deactivated successfully - IP: {}", clientIP);
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("All users are deactivated successfully"))).build();
        }
    }

    @POST
    @Path("/updateActive")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response ActivateTasks(@HeaderParam("token") String token, List<Integer> ids, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to activate users from IP: {}", clientIP);
        if (!userBean.isValidUserByToken(token)) {
            logger.warn("Unauthorized access to activate users - Invalid token - IP: {}", clientIP);
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        UserDto user = userBean.getUserByToken(token);
        if (!user.getRole().equals("po")) {
            logger.warn("Forbidden access to activate users - User is not a Product Owner - IP: {}", clientIP);
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
        }

        boolean allUsersActivated = false;
        for (int id : ids) {
            if (userBean.getUserById(id) == null) {
                allUsersActivated = true;
            }
            if (userBean.getUserById(id).isActive()) {
                allUsersActivated = true;
            }
        }

        if (allUsersActivated) {
            logger.warn("Failed to activate all users - Some users are already active or do not exist - IP: {}", clientIP);
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to deactivate all users"))).build();
        } else {
            for (int id : ids) {
                userBean.changeStatus(userBean.getUserById(id).getUsername(), true);
            }
            logger.info("All users are activated successfully - IP: {}", clientIP);
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("All users are activated successfully"))).build();
        }
    }


    @DELETE
    @Path("/deleteUsers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUsers(@HeaderParam("token") String token, List<Integer> selectedUsers, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to delete users from IP: {}", clientIP);
        if (!userBean.isValidUserByToken(token) && !userBean.getUserByToken(token).getRole().equals("po")) {
            logger.warn("Unauthorized access to delete users - Invalid token or not a Product Owner - IP: {}", clientIP);
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        } else if (userBean.getUserByToken(token).getRole().equals("po")) {
            if (selectedUsers != null && !selectedUsers.isEmpty()) {
                // Check if all selected users are inactive
                boolean allInactive = true;
                for (Integer userId : selectedUsers) {
                    // Check if the user is inactive
                    if (userBean.isUserActive(userId)) {
                        allInactive = false;
                        break;
                    }
                }
                if (allInactive) {
                    // Iterate through the list of user IDs and delete each user
                    for (Integer userId : selectedUsers) {
                        if (!userBean.deleteUser(userBean.getUserByIdString(userId))) {
                            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("One or more users not deleted"))).build();
                        }
                    }
                    logger.info("All users are deleted successfully - IP: {}", clientIP);
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Users deleted"))).build();
                } else {
                    logger.warn("One or more selected users are active - IP: {}", clientIP);
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("One or more selected users are active"))).build();
                }
            } else {
                logger.warn("No user IDs provided to delete - IP: {}", clientIP);
                return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("No user IDs provided"))).build();
            }
        }
        logger.warn("Invalid parameters - IP: {}", clientIP);
        return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid Parameters"))).build();
    }

    @GET
    @Path("/shareProfileDetails/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserShareProfileDetails(@PathParam("username") String username, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        // Retrieve user details by username
        logger.info("Received request to retrieve profile details for username: {} from IP: {}", username, clientIP);
        UserDto userDto = userBean.getUserByUsername(username);
        if (!userDto.isActive()){
            userDto = null;
        }
        if (userDto != null) {
            List<Object[]> taskCounts = taskDao.countTasksByStatus(userDto.getId());
            long totalTasks = taskDao.countTotalTasksByUser(userDto.getId());

            // Extract task counts map from the result
            Map<String, Integer> taskCountsMap = userBean.extractTaskCounts(taskCounts);

            // Create UserDetailsDto object with user details, task counts, and total tasks
            UserDetailsDto userDetails = new UserDetailsDto(
                    userDto.getUsername(),
                    userDto.getFirstname(),
                    userDto.getLastname(),
                    userDto.getEmail(),
                    userDto.getPhotoURL(),
                    userDto.getPhone(),
                    userDto.getRole(),
                    taskCountsMap,
                    totalTasks
            );
            logger.info("Profile details retrieved successfully for username: {} - IP: {}", username, clientIP);
            return Response.status(200).entity(userDetails).build();
        } else {
            logger.warn("User not found for username: {} - IP: {}", username, clientIP);
            // User with the specified username not found
            return Response.status(404).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User not found"))).build();
        }
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchUsers(
            @HeaderParam("token") String token,
            @QueryParam("query") String query,
            @Context HttpServletRequest request
    ) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received user search request with query '{}' from IP: {}", query, clientIP);
        if (token == null || token.isEmpty()) {
            logger.warn("Invalid token provided for user search - IP: {}", clientIP);
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid token"))).build();
        }

        if (!userBean.isValidUserByToken(token)) {
            logger.warn("Unauthorized user search request - Invalid token - IP: {}", clientIP);
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        // Perform user search based on the query string
        List<UserDto> searchResults = userBean.searchUsers(query);

        if (searchResults == null || searchResults.isEmpty()) {
            logger.info("No matching users found for query '{}' - IP: {}", query, clientIP);
            return Response.status(404).entity(JsonUtils.convertObjectToJson(new ResponseMessage("No matching users found"))).build();
        }
        logger.info("User search completed successfully for query '{}' - IP: {}", query, clientIP);
        return Response.status(200).entity(searchResults).build();
    }

    @PUT
    @Path("/checkEmail")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response checkEmailExists(String email, @Context HttpServletRequest request) {
        String clientIP = request.getRemoteAddr();
        logger.info("Received request to check email '{}' from IP: {}", email, clientIP);
        try {
            // Parse the JSON payload to retrieve the email
            UserEntity userEntity = userBean.userExistsByEmail(email);
            System.out.println(email);
            if (userEntity !=null) {
                userBean.passwordRetrievalStamp(userEntity);
                logger.info("Email '{}' found in the database - IP: {}", email, clientIP);
                return Response.ok().build(); // Email exists, return HTTP 200 OK
            } else {
                logger.info("Email '{}' not found in the database - IP: {}", email, clientIP);
                return Response.status(Response.Status.NOT_FOUND).build(); // Email does not exist, return HTTP 404 Not Found
            }
        } catch (Exception e) {
            logger.error("Internal Server Error while checking email '{}' - IP: {}", email, clientIP, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); // Error occurred, return HTTP 500 Internal Server Error
        }
    }

    @PUT
    @Path("/forgotPassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response forgotPassword(ConfirmationRequest request, @Context HttpServletRequest requestHttp) {
        String clientIP = requestHttp.getRemoteAddr();
        String emailValidationToken = request.getToken();
        String password = request.getPassword();
        UserDto user = userBean.getUserByEmailValidationToken(emailValidationToken);
        logger.info("Received request to reset password for IP: {}", clientIP);
        logger.debug("Email validation token: {}", emailValidationToken);
        if (user != null) {
            try {
                userBean.forgotPassword(user, password);
                logger.info("Password reset successful for user '{}'", user.getUsername());
                return Response.status(200).entity(new ResponseMessage("New Password updated")).build();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Failed to update password for user '{}'", user.getUsername(), e);
                return Response.status(500).entity(new ResponseMessage("Failed to update Password")).build();
            }
        } else {
            logger.warn("User not found for email validation token: {}", emailValidationToken);
            return Response.status(404).entity(new ResponseMessage("User not found")).build();
        }
    }

}



