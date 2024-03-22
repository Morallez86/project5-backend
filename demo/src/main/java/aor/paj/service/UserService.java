package aor.paj.service;

import java.util.List;

import aor.paj.bean.UserBean;
import aor.paj.dto.*;
import aor.paj.entity.UserEntity;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
import aor.paj.validator.UserValidator;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Objects;
import java.util.stream.Collectors;

@Path("/users")
public class UserService {

    @Inject
    UserBean userBean;

    //Service that receives a user object and adds it to the list of users
    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(UserDto u, @HeaderParam("token") String token, @HeaderParam("role") String roleNewUser) {
        // Check if any parameter is null or blank
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
                userBean.addUserPO(u, roleNewUser);
                return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("A new user is created")).toString()).build();
            }

        }else{
        // If all checks pass, add the user
            userBean.addUser(u);
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("A new user is created"))).build();
        }
        return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized")).toString()).build();
    }


    //Service that manages the login of the user, sets the token for the user and sends the token and the role of the user
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@HeaderParam("username") String username, @HeaderParam("password") String password) {
        String token = userBean.login(username, password);
        if (token != null) {
            if(userBean.getUserByUsername(username).isActive()){
                return Response.status(200).entity(JsonUtils.convertObjectToJson(new TokenAndRoleDto(token, userBean.getUserByUsername(username).getRole(), userBean.getUserByToken(token).getUsername()))).build();
            }else{
                return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User is not active")).toString()).build();
            }
        }
        return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Login Failed"))).build();
    }

    @POST
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@HeaderParam("token") String token) {
        if (userBean.isValidUserByToken(token)) {
            userBean.logout(token);
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User is logged out")).toString()).build();
        }
        return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized")).toString()).build();
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
    @Path("/getPhoto")
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
            UserDetailsDto userDetails = new UserDetailsDto(
                    userDto.getUsername(),
                    userDto.getFirstname(),
                    userDto.getLastname(),
                    userDto.getEmail(),
                    userDto.getPhotoURL(),
                    userDto.getPhone(),
                    userDto.getRole()
            );
            return Response.status(200).entity(userDetails).build();
        } else {
            // User with the specified ID not found
            return Response.status(404).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User not found"))).build();
        }
    }

    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(UserUpdateDto u, @HeaderParam("token") String token, @HeaderParam("selectedUser") String selectedUser) {
        if (!userBean.isValidUserByToken(token)) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        } else if (userBean.isValidUserByToken(token) && userBean.getUserByToken(token).getRole().equals("po") || userBean.getUserByToken(token).getUsername().equals(selectedUser)) {
            if (!UserValidator.isValidEmail(u.getEmail())) {
                return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid email format"))).build();
            } else if (!u.getEmail().equals(userBean.getUserByUsername(selectedUser).getEmail()) && UserValidator.emailExists(userBean.getAllUsersDB(),u.getEmail())
                    && (!userBean.getUserByToken(token).getEmail().equals(u.getEmail()) || !userBean.getUserByUsername(selectedUser).getEmail().equals(u.getEmail()))) {
                return Response.status(409).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Email already exists"))).build();
            } else if (!UserValidator.isValidPhoneNumber(u.getPhone())) {
                return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid phone number format"))).build();
            } else if (!UserValidator.isValidURL(u.getPhotoURL())) {
                return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid URL format"))).build();
            } else {
                userBean.updateUser(u);
                return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User is updated")).toString()).build();
            }
        }
        return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
    }

    @PUT
    @Path("/updateById")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(UserUpdateDto u, @HeaderParam("token") String token, @HeaderParam("selectedUserId") int selectedUserId) {
        if (!userBean.isValidUserByToken(token)) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        } else {
            // Retrieve the authenticated user
            UserDto authenticatedUser = userBean.getUserByToken(token);
            System.out.println(authenticatedUser.getEmail());

            // Check if the authenticated user has the required permissions
            if (authenticatedUser.getRole().equals("po")) {
                // Retrieve the user being updated
                UserDto userToUpdate = userBean.getUserById(selectedUserId);
                System.out.println(userToUpdate.getEmail());

                if (!UserValidator.isValidEmail(u.getEmail())) {
                    System.out.println("1");
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid email format"))).build();
                } else if (!u.getEmail().equals(userToUpdate.getEmail()) && UserValidator.emailExists(userBean.getAllUsersDB(), u.getEmail())
                        && (!userToUpdate.getEmail().equals(u.getEmail()))) {
                    return Response.status(409).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Email already exists"))).build();
                } else if (!UserValidator.isValidPhoneNumber(u.getPhone())) {
                    System.out.println("2");
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid phone number format"))).build();
                } else if (!UserValidator.isValidURL(u.getPhotoURL())) {
                    System.out.println("3");
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid URL format"))).build();
                } else {
                    userBean.updateUserById(selectedUserId, u);
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
        }else if(userBean.isValidUserByToken(token)){
            boolean updateTry = userBean.updatePassword(u, token);
            if(!updateTry){
                return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Old password is incorrect"))).build();
            }else{
                return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Password is updated")).toString()).build();
            }
        }
        return Response .status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid Parameters")).toString()).build();
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
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@HeaderParam("token") String token, @HeaderParam("selectedUser") String selectedUser) {
        if(!userBean.isValidUserByToken(token) && !userBean.getUserByToken(token).getRole().equals("po")){
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }else if(userBean.getUserByToken(token).getRole().equals("po")){
            if(userBean.deleteUser(selectedUser)){
                return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User deleted")).toString()).build();
            }else{
                return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User not deleted")).toString()).build();
            }
        }
        return Response .status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid Parameters")).toString()).build();
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



    //Delete all tasks of a user
    @DELETE
    @Path("/deleteTasks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTasks(@HeaderParam("token") String token, @HeaderParam("selectedUser") String selectedUser) {
        if(!userBean.isValidUserByToken(token) && !userBean.getUserByToken(token).getRole().equals("po")){
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }else if(userBean.getUserByToken(token).getRole().equals("po")){
            if(userBean.deleteTasks(selectedUser)){
                return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Tasks deleted")).toString()).build();
            }else{
                return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Tasks not deleted")).toString()).build();
            }
        }
        return Response .status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid Parameters")).toString()).build();
    }

}



