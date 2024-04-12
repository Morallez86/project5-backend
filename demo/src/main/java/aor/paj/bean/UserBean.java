package aor.paj.bean;

import java.time.LocalDateTime;
import java.util.*;

import aor.paj.dao.CategoryDao;
import aor.paj.dao.TaskDao;
import aor.paj.dao.TokenDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.*;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.TokenEntity;
import aor.paj.entity.UserEntity;
import aor.paj.mapper.UserMapper;
import aor.paj.utils.EmailUtil;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;
import org.apache.logging.log4j.*;

@ApplicationScoped
public class UserBean {
    private ArrayList<UserDto> userDtos;
    private static final Logger logger  = LogManager.getLogger(UserBean.class);

    @EJB
    UserDao userDao;

    @EJB
    TaskDao taskDao;

    @EJB
    CategoryDao categoryDao;

    @EJB
    TokenDao tokenDao;

    @Inject
    TokenBean tokenBean;


    //Function that generates a unique id for new user checking in database mysql if the id already exists
    public int generateIdDataBase() {
        int id = 1;
        boolean idAlreadyExists;

        do {
            idAlreadyExists = false;
            UserEntity userEntity = userDao.findUserById(id);
            if (userEntity != null) {
                id++;
                idAlreadyExists = true;
            }
        } while (idAlreadyExists);
        return id;
    }

    //Add a user to the database mysql, encrypting the password, role to "dev" and generating a id
    public boolean addUser(UserDto user) {

            UserEntity userEntity = UserMapper.convertUserDtoToUserEntity(user);
            //Encrypt the password
            userEntity.setPassword(BCrypt.hashpw(userEntity.getPassword(), BCrypt.gensalt()));
            userEntity.setId(generateIdDataBase());
            if(userEntity.getUsername().equals("admin") || userEntity.getUsername().equals("User deleted")){
                userEntity.setRole("po");
            }else {
                userEntity.setRole("dev");
                userEntity.setActive(true);
                userEntity.setPending(false);
                userEntity.setRegistTime(LocalDateTime.now());
            }
            userDao.persist(userEntity);

            return true;
    }
    public boolean addUserPO(UserDto user) {
        try {
            UserEntity userEntity = UserMapper.convertUserDtoToUserEntity(user);
            // Encrypt the password
            userEntity.setPassword(BCrypt.hashpw(userEntity.getPassword(), BCrypt.gensalt()));
            userEntity.setId(generateIdDataBase());
            userEntity.setActive(false);
            userEntity.setPending(true);
            userEntity.setEmailValidation(tokenBean.generateNewToken());
            userEntity.setRegistTime(LocalDateTime.now());
            userDao.persist(userEntity);

            // Send verification email
            String verificationLink = "http://localhost:3000/verify-account/" + userEntity.getEmailValidation();
            EmailUtil.sendVerificationEmail(userEntity.getEmail(), userEntity.getUsername(), verificationLink);

            return true;
        } catch (Exception e) {
            // Handle any exceptions that occur during user creation or email sending
            e.printStackTrace(); // Log the exception or handle it appropriately
            return false;
        }
    }


    //Function that validates a user in database by token
    public boolean isValidUserByToken(String token) {
        TokenEntity tokenEntity = tokenDao.findTokenByValue(token);
        if (tokenEntity != null && tokenEntity.getExpirationTime().isAfter(LocalDateTime.now())) {
            UserEntity userEntity = tokenEntity.getUser();
            if (userEntity != null && userEntity.isActive()) {
                return true;
            }
        }
        return false;
    }


    //Function that receives a UserDto and checks in database mysql if the username and email already exists
    public boolean userExists(UserDto user) {
        UserEntity userEntity = userDao.findUserByUsername(user.getUsername());
        if (userEntity != null) {
            return true;
        }
        userEntity = userDao.findUserByEmail(user.getEmail());
        if (userEntity != null) {
            return true;
        }
        return false;
    }

    //Function that receives username, retrieves the user from the database, and returns the token generated
    public String login(String username, String password) {
        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity != null && BCrypt.checkpw(password, userEntity.getPassword())) {
            logger.info("User: " + userEntity.getUsername() + " logged in");
            // Call generateToken method from TokenBean
            return tokenBean.generateToken(userEntity).getTokenValue();
        }
        return null;
    }


    //Function that receives username, retrieves the user from the database and returns the userDto object
    public UserDto getUserByUsername(String username) {
        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity != null) {
            return UserMapper.convertUserEntityToUserDto(userEntity);
        }
        return null;
    }

    //Function that receives id, retrieves the user from the database and returns the userDto object
    public String getUserByIdString(int id) {
        UserEntity userEntity = userDao.findUserById(id);
        if (userEntity != null) {
            return userEntity.getUsername();
        }
        return null;
    }


    //Function that receives id, retrieves the user from the database and returns the userDto object
    public UserDto getUserById(int userId) {
        UserEntity userEntity = userDao.findUserById(userId);
        if (userEntity != null) {
            return UserMapper.convertUserEntityToUserDto(userEntity);
        }
        return null;
    }

    //Function that receives the token and retrieves the user from the database and returns the userDto object
    public UserDto getUserByToken(String token) {
        TokenEntity tokenEntity = tokenDao.findTokenByValue(token);
        if (tokenEntity != null && tokenEntity.getExpirationTime().isAfter(LocalDateTime.now())) {
            UserEntity userEntity = tokenEntity.getUser();
            if (userEntity != null) {
                return UserMapper.convertUserEntityToUserDto(userEntity);
            }
        }
        return null;
    }


    //Function that receives a token and a task id and checks if the user has permission to access the task, to edit he must be role sm or po, or the be owner of the task
    public boolean hasPermissionToEdit(String token, int taskId) {
        TokenEntity tokenEntity = tokenDao.findTokenByValue(token);
        if (tokenEntity != null && tokenEntity.getExpirationTime().isAfter(LocalDateTime.now())) {
            UserEntity userEntity = tokenEntity.getUser();
            if (userEntity != null) {
                // Check if the user has the role of 'sm' or 'po'
                if (userEntity.getRole().equals("sm") || userEntity.getRole().equals("po")) {
                    return true;
                }
                // Check if the user owns the task
                List<TaskEntity> userTasks = taskDao.findTaskByOwnerId(userEntity.getId());
                for (TaskEntity task : userTasks) {
                    if (task.getId() == taskId) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //Return the list of users in the json file
    public List<UserDto> getAllUsersDB() {
        List<UserEntity> userEntities = userDao.findAllUsers();
        //cria um arraylist de userentity para devolver
        List<UserDto> userDtos = new ArrayList<>();
        //adiciona os users à lista
        for(UserEntity ue : userEntities){
            userDtos.add(UserMapper.convertUserEntityToUserDto(ue));
        }
        return userDtos;
    }

    //Function that receives a UserUpdateDto and updates the corresponding user
    public void updateUser(UserUpdateDto userUpdateDto) {
        UserEntity userEntity = userDao.findUserByUsername(userUpdateDto.getUsername());

        if (userEntity != null) {
            userEntity.setFirstname(userUpdateDto.getFirstname());
            userEntity.setLastname(userUpdateDto.getLastname());
            userEntity.setEmail(userUpdateDto.getEmail());
            userEntity.setPhone(userUpdateDto.getPhone());
            userEntity.setPhotoURL(userUpdateDto.getPhotoURL());
            userEntity.setRole(userUpdateDto.getRole());

            userDao.merge(userEntity);
        }
    }

    //Function that receives a UserUpdateDto and updates the corresponding user by id
    public void updateUserById(int userId, UserUpdateDto userUpdateDto) {
        UserEntity userEntity = userDao.findUserById(userId);

        if (userEntity != null) {
            userEntity.setFirstname(userUpdateDto.getFirstname());
            userEntity.setLastname(userUpdateDto.getLastname());
            userEntity.setEmail(userUpdateDto.getEmail());
            userEntity.setPhone(userUpdateDto.getPhone());
            userEntity.setPhotoURL(userUpdateDto.getPhotoURL());
            userEntity.setRole(userUpdateDto.getRole());

            userDao.merge(userEntity);
        }
    }

    //Function that receives a UserPasswordUpdateDto and updates the corresponding user
    public boolean updatePassword(UserPasswordUpdateDto userPasswordUpdateDto, String token) {
        TokenEntity tokenEntity = tokenDao.findTokenByValue(token);
        UserEntity userEntity = userDao.findUserById(tokenEntity.getUser().getId());
        if (userEntity != null) {
            if (BCrypt.checkpw(userPasswordUpdateDto.getOldPassword(), userEntity.getPassword())) {
                String encryptedPassword = BCrypt.hashpw(userPasswordUpdateDto.getNewPassword(), BCrypt.gensalt());
                userEntity.setPassword(encryptedPassword);
                userDao.merge(userEntity);
                return true;
            } else {
                // If the old password provided does not match the user's current password
                return false;
            }
        }
        // If the user associated with the token is not found
        return false;
    }

    //Function that receives a token and returns the user role
    public String getUserRole(String token) {
        TokenEntity tokenEntity = tokenDao.findTokenByValue(token);
        if (tokenEntity != null && tokenEntity.getUser() != null) {
            return tokenEntity.getUser().getRole();
        }
        return null;
    }

    public boolean changeStatus(String username, boolean status){
        if(username.equals("admin")){
            return false;
        }
        UserEntity userEntity = userDao.findUserByUsername(username);
        if(userEntity != null){
            userEntity.setActive(status);
            userDao.merge(userEntity);
            return true;
        }
        return false;
    }


    public boolean deleteUser(String username) {
        if(username.equals("admin") || username.equals("User deleted")){
            return false;
        }
        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity != null) {
            changeTaskOwner(username,"User deleted");
            changeCategoryOwner(username,"User deleted");
            userDao.remove(userEntity);

            return true;
            }
        return false;
    }

    public boolean changeCategoryOwner(String oldUsername, String newUsername){
        UserEntity oldUserEntity = userDao.findUserByUsername(oldUsername);
        UserEntity newUserEntity = userDao.findUserByUsername(newUsername);
        if(oldUserEntity != null && newUserEntity != null) {
            List<CategoryEntity> categories = categoryDao.findCategoryByOwnerID(oldUserEntity.getId());
            for (CategoryEntity category : categories) {
                category.setOwner(newUserEntity);
                categoryDao.merge(category);
                return true;
            }
        }
        return false;
    }

    public boolean changeTaskOwner(String oldUsername, String newUsername){
        UserEntity oldUserEntity = userDao.findUserByUsername(oldUsername);
        UserEntity newUserEntity = userDao.findUserByUsername(newUsername);
        if(oldUserEntity != null && newUserEntity != null){
            List<TaskEntity> tasks = taskDao.findTaskByOwnerId(oldUserEntity.getId());
            for(TaskEntity task : tasks){
                task.setOwner(newUserEntity);
                taskDao.merge(task);
            }
            return true;
        }
        return false;
    }

    public boolean deleteTasks(String username) {
        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity != null) {
            List<TaskEntity> tasks = taskDao.findTaskByOwnerId(userEntity.getId());
            for(TaskEntity task : tasks){
                task.setActive(false);
                taskDao.merge(task);
            }
            return true;
        }
        return false;
    }

    public void createDefaultUsersIfNotExistent() {
        if (userDao.findUserByUsername("admin") == null) {
            UserDto userDto = new UserDto();
            userDto.setUsername("admin");
            userDto.setPassword("admin");
            userDto.setFirstname("Admin");
            userDto.setLastname("Admin");
            userDto.setEmail("admin@admin");
            userDto.setPhone("000000000");
            userDto.setPending(false);
            userDto.setActive(true);
            userDto.setPhotoURL("https://t4.ftcdn.net/jpg/04/75/00/99/360_F_475009987_zwsk4c77x3cTpcI3W1C1LU4pOSyPKaqi.jpg");
            addUser(userDto);
        }

        if (userDao.findUserByUsername("User deleted") == null) {
            UserDto userDto = new UserDto();
            userDto.setUsername("User deleted");
            userDto.setPassword("deleted");
            userDto.setFirstname("User deleted");
            userDto.setLastname("User deleted");
            userDto.setEmail("deleted@deleted");
            userDto.setPhone("000000000");
            userDto.setPhotoURL("https://www.shutterstock.com/image-vector/trash-can-icon-symbol-delete-600nw-1454137346.jpg");
            userDto.setPending(false);
            userDto.setActive(false);
            addUser(userDto);
            changeStatus("deleted",false);
        }

    }

    // Function that returns if the user is active or not
    public boolean isUserActive(int userId) {
        UserEntity userEntity = userDao.findUserById(userId);
        if (userEntity != null) {
            return userEntity.isActive();
        }
        return false;
    }

    public List<UserDto> getAllActiveUsers() {
        List<UserEntity> activeUsers = userDao.findAllActiveUsers();
        List<UserDto> activeUserDtos = new ArrayList<>();
        for (UserEntity userEntity : activeUsers) {
            activeUserDtos.add(UserMapper.convertUserEntityToUserDto(userEntity));
        }
        return activeUserDtos;
    }

    public List<UserDto> searchUsers(String query) {
        List<UserEntity> users = userDao.searchUsers(query);

        // Map UserEntity objects to UserDto objects (DTO - Data Transfer Object)
        List<UserDto> userDtos = new ArrayList<>();
        for (UserEntity userEntity : users) {
            userDtos.add(UserMapper.convertUserEntityToUserDto(userEntity));
        }
        return userDtos;
    }

    public Map<String, Integer> extractTaskCounts(List<Object[]> taskCounts) {
        Map<String, Integer> countsMap = new HashMap<>();
        for (Object[] result : taskCounts) {
            String status = String.valueOf(result[0]);
            int count = ((Number) result[1]).intValue();
            countsMap.put(status, count);
        }
        return countsMap;
    }

    public void confirmRegistration (UserDto user, String password){
        // Update user's password and registration status
        UserEntity userEntity = userDao.findUserByUsername(user.getUsername());
        userEntity.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        userEntity.setPending(false);
        userEntity.setActive(true);
        userEntity.setEmailValidation(null);
        userDao.merge(userEntity);
    }

    public UserDto getUserByEmailValidationToken(String emailValidationToken) {
        UserEntity userEntity = userDao.findByEmailValidationToken(emailValidationToken);
        if (userEntity != null) {
            return UserMapper.convertUserEntityToUserDto(userEntity);
        }
        return null;
    }

    public List<UserPartialDto> getAllActiveUsersChat() {
        List<UserEntity> activeUsers = userDao.findAllActiveUsers();
        List<UserPartialDto> activeUserPartialDtos = new ArrayList<>();
        for (UserEntity userEntity : activeUsers) {
            activeUserPartialDtos.add(UserMapper.convertUserEntityToUserPartialDto(userEntity));
        }
        return activeUserPartialDtos;
    }

}
