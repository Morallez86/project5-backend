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
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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

    //Service that manages the login of the user, sets the token for the user and sends the token and the role of the user
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest loginRequest) {
        try {
            String username = loginRequest.getUsername();
            String password = loginRequest.getPassword();

            // Authenticate user and get token
            String token = userBean.login(username, password);

            if (token != null) {
                // Check if the user is active
                UserDto userDto = userBean.getUserByUsername(username);
                if (userDto != null && userDto.isActive()) {
                    // Return token and user role
                    TokenAndRoleDto tokenAndRoleDto = new TokenAndRoleDto(token, userDto.getRole(), userDto.getUsername(), userDto.getId());
                    return Response.status(200).entity(tokenAndRoleDto).build();
                } else {
                    return Response.status(403).entity(new ResponseMessage("User is not active")).build();
                }
            } else {
                return Response.status(401).entity(new ResponseMessage("Login Failed")).build();
            }
        } catch (Exception e) {
            // Handle exceptions
            return Response.status(500).entity(new ResponseMessage("Internal Server Error")).build();
        }
    }


    @POST
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(LogoutRequest logoutRequest) {
        String token = logoutRequest.getToken();
        String username = logoutRequest.getUsername();

        if (token == null || username == null) {
            return Response.status(400).entity(new ResponseMessage("Token or username missing")).build();
        }

        if (!tokenBean.isValidToken(token)) {
            return Response.status(401).entity(new ResponseMessage("Invalid token")).build();
        }

        if (!tokenBean.isValidTokenForUser(token, username)) {
            return Response.status(401).entity(new ResponseMessage("Token does not correspond to the provided username")).build();
        }

        tokenBean.deleteToken(token);
        return Response.status(200).entity(new ResponseMessage("User is logged out")).build();
    }


    //Service that receives a user object and adds it to the list of users
    @POST
    @Path("/new")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(UserDto u, @HeaderParam("token") String token, @HeaderParam("role") String roleNewUser) {

        if (UserValidator.isNullorBlank(u)) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("One or more parameters are null or blank"))).build();
        }

        // Validate email format
        if (!UserValidator.isValidEmail(u.getEmail())) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid email format"))).build();
        }

        // Validate phone number format
        if (!UserValidator.isValidPhoneNumber(u.getPhone())) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid phone number format"))).build();
        }

        // Validate URL format
        if (!UserValidator.isValidURL(u.getPhotoURL())) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid URL format"))).build();
        }

        // Check if username or email already exists
        if (userBean.userExists(u)) {
            return Response.status(409).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid Username or Email"))).build();
        }

        // Check if the user is a PO & if the token is valid and create the new user
        String userToken="";
        userToken = token;
        if(userToken!=null && roleNewUser != null && userBean.isValidUserByToken(userToken)){
            String role = userBean.getUserByToken(userToken).getRole();
            if(role.equals("po")){
                userBean.addUserPO(u);
                return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("A new user is created")).toString()).build();
            }

            }else{
                // If all checks pass, add the user
                userBean.addUser(u);

                return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("A new user is created"))).build();
        }
        return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized")).toString()).build();
    }

    @POST
    @Path("/confirmRegistration")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response confirmRegistration(ConfirmationRequest request) {
        String emailValidationToken = request.getToken();
        String password = request.getPassword();

        UserDto user = userBean.getUserByEmailValidationToken(emailValidationToken);
        if (user != null) {
            try {
                userBean.confirmRegistration(user, password);
                return Response.status(200).entity(new ResponseMessage("Registration confirmed")).build();
            } catch (Exception e) {
                e.printStackTrace();
                return Response.status(500).entity(new ResponseMessage("Failed to confirm registration")).build();
            }
        } else {
            return Response.status(404).entity(new ResponseMessage("User not found")).build();
        }
    }


    // Function that returns the list of all users if role is PO or active users if SM
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers(@HeaderParam("token") String token) {
        if (token == null || token.isEmpty()) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid token"))).build();
        }

        if (!userBean.isValidUserByToken(token)) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        String userRole = userBean.getUserRole(token);

        if (userRole == null) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User role not found"))).build();
        }

        List<UserDto> userDtos;

        if (userRole.equals("po")) {
            userDtos = userBean.getAllUsersDB();
        } else if (userRole.equals("sm")) {
            userDtos = userBean.getAllActiveUsers();
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        if (userDtos == null || userDtos.isEmpty()) {
            return Response.status(404).entity(JsonUtils.convertObjectToJson(new ResponseMessage("No users found"))).build();
        }

        return Response.status(200).entity(userDtos).build();
    }


    //Service that receives a token and a username and sends the photoURL of the usersame
    @GET
    @Path("/photo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPhoto(@HeaderParam("token") String token, @HeaderParam("username") String username) {
        if (userBean.isValidUserByToken(token)) {
            UserDto userDto = userBean.getUserByUsername(username);
            return Response.status(200).entity(JsonUtils.convertObjectToJson((userDto.getPhotoURL()))).build();
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }


    @GET
    @Path("/profileDetails/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserProfileDetails(@HeaderParam("token") String token, @PathParam("userId") int userId) {
        // If userId is 0, retrieve the user ID from the token and allow access regardless of role
        if (userId == 0) {
            // Retrieve user ID from the token
            userId = userBean.getUserByToken(token).getId();
            if (userId == 0) {
                return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
            }
        } else {
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

            return Response.status(200).entity(userDetails).build();
        } else {
            // User with the specified ID not found
            return Response.status(404).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User not found"))).build();
        }
    }

    @PUT
    @Path("/updateProfile/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProfile(UserUpdateDto u, @HeaderParam("token") String token, @PathParam("userId") int userId) {
        if (userId == 0) {
            // Retrieve user ID from the token
            userId = userBean.getUserByToken(token).getId();
            if (userId == 0) {
                return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
            }
        }

        if (!userBean.isValidUserByToken(token)) {
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
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid email format"))).build();
                } else if (!u.getEmail().equals(userToUpdate.getEmail()) && UserValidator.emailExists(userBean.getAllUsersDB(), u.getEmail())
                        && (!userToUpdate.getEmail().equals(u.getEmail()))) {
                    return Response.status(409).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Email already exists"))).build();
                } else if (!UserValidator.isValidPhoneNumber(u.getPhone())) {
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid phone number format"))).build();
                } else if (!UserValidator.isValidURL(u.getPhotoURL())) {
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid URL format"))).build();
                } else {
                    userBean.updateUserById(userId, u);
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User is updated")).toString()).build();
                }
            } else {
                // Unauthorized access
                return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
            }
        }
    }

    //Services tha receives a UserPasswordDto object, authenticates the user, sees if the user that is logged is the same as the one that is being updated and updates the user password
    @PUT
    @Path("/updatePassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePassword(UserPasswordUpdateDto u, @HeaderParam("token") String token) {
        if (!userBean.isValidUserByToken(token)) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        // Check if the new password is the same as the old password
        if (u.getOldPassword().equals(u.getNewPassword())) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("New password must be different from the old password"))).build();
        }

        boolean updateTry = userBean.updatePassword(u, token);
        if (!updateTry) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Old password is incorrect"))).build();
        } else {
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Password is updated")).toString()).build();
        }
    }


    @POST
    @Path("/updateInactive")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response desactivateTasks(@HeaderParam("token") String token, List<Integer> ids) {
        if (!userBean.isValidUserByToken(token)) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        UserDto user = userBean.getUserByToken(token);
        if (!user.getRole().equals("po")) {
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
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to deactivate all users"))).build();
        } else {
            for (int id : ids) {
                userBean.changeStatus(userBean.getUserById(id).getUsername(), false);
            }
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("All users are deactivated successfully"))).build();
        }
    }

    @POST
    @Path("/updateActive")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response ActivateTasks(@HeaderParam("token") String token, List<Integer> ids) {
        if (!userBean.isValidUserByToken(token)) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        UserDto user = userBean.getUserByToken(token);
        if (!user.getRole().equals("po")) {
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
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to deactivate all users"))).build();
        } else {
            for (int id : ids) {
                userBean.changeStatus(userBean.getUserById(id).getUsername(), true);
            }
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("All users are activated successfully"))).build();
        }
    }


    @DELETE
    @Path("/deleteUsers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUsers(@HeaderParam("token") String token, List<Integer> selectedUsers) {
        if (!userBean.isValidUserByToken(token) && !userBean.getUserByToken(token).getRole().equals("po")) {
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
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Users deleted"))).build();
                } else {
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("One or more selected users are active"))).build();
                }
            } else {
                return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("No user IDs provided"))).build();
            }
        }
        return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid Parameters"))).build();
    }

    @GET
    @Path("/shareProfileDetails/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserShareProfileDetails(@PathParam("username") String username) {
        // Retrieve user details by username
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

            return Response.status(200).entity(userDetails).build();
        } else {
            // User with the specified username not found
            return Response.status(404).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User not found"))).build();
        }
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchUsers(
            @HeaderParam("token") String token,
            @QueryParam("query") String query
    ) {
        if (token == null || token.isEmpty()) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid token"))).build();
        }

        if (!userBean.isValidUserByToken(token)) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        // Perform user search based on the query string
        List<UserDto> searchResults = userBean.searchUsers(query);

        if (searchResults == null || searchResults.isEmpty()) {
            return Response.status(404).entity(JsonUtils.convertObjectToJson(new ResponseMessage("No matching users found"))).build();
        }

        return Response.status(200).entity(searchResults).build();
    }

    @PUT
    @Path("/checkEmail")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response checkEmailExists(String email) {
        try {
            // Parse the JSON payload to retrieve the email
            UserEntity userEntity = userBean.userExistsByEmail(email);
            System.out.println(email);
            if (userEntity !=null) {
                userBean.passwordRetrievalStamp(userEntity);
                return Response.ok().build(); // Email exists, return HTTP 200 OK
            } else {
                return Response.status(Response.Status.NOT_FOUND).build(); // Email does not exist, return HTTP 404 Not Found
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); // Error occurred, return HTTP 500 Internal Server Error
        }
    }

    @PUT
    @Path("/forgotPassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response forgotPassword(ConfirmationRequest request) {
        String emailValidationToken = request.getToken();
        String password = request.getPassword();
        System.out.println(emailValidationToken);
        System.out.println(password);

        UserDto user = userBean.getUserByEmailValidationToken(emailValidationToken);
        System.out.println(user);
        if (user != null) {
            try {
                userBean.forgotPassword(user, password);
                return Response.status(200).entity(new ResponseMessage("New Password updated")).build();
            } catch (Exception e) {
                e.printStackTrace();
                return Response.status(500).entity(new ResponseMessage("Failed to update Password")).build();
            }
        } else {
            return Response.status(404).entity(new ResponseMessage("User not found")).build();
        }
    }

}



