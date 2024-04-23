package aor.paj.bean;

import aor.paj.dao.CategoryDao;
import aor.paj.dao.TaskDao;
import aor.paj.dao.TokenDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.TaskDto;
import aor.paj.dto.UserDto;
import aor.paj.dto.CategoryDto;
import aor.paj.dto.ManagingTaskDto;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.TokenEntity;
import aor.paj.entity.UserEntity;
import aor.paj.mapper.TaskMapper;
import aor.paj.utils.JsonUtils;
import aor.paj.utils.State;
import aor.paj.websocket.DashboardSocket;
import aor.paj.websocket.NotificationSocket;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TaskBean {

    @EJB
    UserDao userDao;

    @EJB
    TaskDao taskDao;

    @EJB
    CategoryDao categoryDao;

    @EJB
    TokenDao tokenDao;

    @Inject
    DashBoardBean dashBoardBean;



   //Function that receives a token and a taskdto and creates a task with the user token as owner and adds the task to the database mysql
   public boolean addTask(String token, TaskDto taskDto) {
       TokenEntity tokenEntity = tokenDao.findTokenByValue(token);
       if (tokenEntity != null && tokenEntity.getUser() != null) {
           UserEntity userEntity = tokenEntity.getUser();
           CategoryEntity categoryEntity = categoryDao.findCategoryByTitle(taskDto.getCategory());
           if (categoryEntity != null) {
               TaskEntity taskEntity = TaskMapper.convertTaskDtoToTaskEntity(taskDto);

               taskEntity.setOwner(userEntity);
               taskEntity.setActive(true);
               taskEntity.setId(generateTaskId());
               taskEntity.setCategory(categoryEntity);
               if (taskEntity.getInitialDate() == null) {
                   taskEntity.setInitialDate(LocalDate.now());
               }
               taskDao.persist(taskEntity);

               NotificationSocket.sendTaskToAll(getActiveTasksOrderedByPriority());
               DashboardSocket.sendDashboardGeneralStatsDtoToAll(dashBoardBean.mapToDashboardGeneralStatsDto());
               return true;
           }
       }
       return false;
   }


    //Function that receives a taskdto and checks in database mysql if a task with the same title already exists
    public boolean taskTitleExists(TaskDto taskDto) {
        TaskEntity taskEntity = taskDao.findTaskByTitle(taskDto.getTitle());
        if (taskEntity != null) {
            return true;
        }
        return false;
    }

    //Function that generates a unique id for new task checking in database mysql if the id already exists
    public int generateTaskId() {
        int id = 1;
        boolean idAlreadyExists;

        do {
            idAlreadyExists = false;
            TaskEntity taskEntity = taskDao.findTaskById(id);
            if (taskEntity != null) {
                id++;
                idAlreadyExists = true;
            }
        } while (idAlreadyExists);

        return id;
    }

    //Function that returns all tasks from the database mysql
    public List<TaskDto> getActiveTasks() {
        List<TaskEntity> taskEntities = taskDao.getAllTasks();
        List<TaskDto> activeTasks = new ArrayList<>();
        for (TaskEntity taskEntity : taskEntities) {
            if (taskEntity.getActive()) { // Check if the task is active
                activeTasks.add(TaskMapper.convertTaskEntityToTaskDto(taskEntity));
            }
        }
        return activeTasks;
    }

    //Function that receives a task id and a new task status and updates the task status in the database mysql
    public void updateTaskStatus(int id, int status) {
        TaskEntity taskEntity = taskDao.findTaskById(id);
        taskEntity.setStatus(status);
        taskDao.merge(taskEntity);
    }
    
    //Function that receives a task id and sets the task active to false in the database mysql
    public boolean desactivateTask(int id) {
        TaskEntity taskEntity = taskDao.findTaskById(id);
        taskEntity.setActive(false);
        taskDao.merge(taskEntity);

        NotificationSocket.sendTaskToAll(getActiveTasksOrderedByPriority());
        return true;
    }
    
    //Function that receives a task id and a token and checks if the user its the owner of task with that id
    public boolean taskBelongsToUser(String token, int id) {
        TokenEntity tokenEntity = tokenDao.findTokenByValue(token);
        if (tokenEntity != null && tokenEntity.getUser() != null) {
            UserEntity userEntity = tokenEntity.getUser();
            TaskEntity taskEntity = taskDao.findTaskById(id);
            if (taskEntity != null && taskEntity.getOwner() != null && taskEntity.getOwner().getId() == userEntity.getId()) {
                return true;
            }
        }
        return false;
    }

    //Function that receives a task id and returns the task from the database mysql
    public TaskDto getTaskById(int id) {
        TaskEntity taskEntity = taskDao.findTaskById(id);
        return TaskMapper.convertTaskEntityToTaskDto(taskEntity);
    }

    public void updateTask(TaskDto taskDto, int id) {
        TaskEntity taskEntity = taskDao.findTaskById(id);

        if (taskEntity == null) {
            return;
        }

        taskEntity.setTitle(taskDto.getTitle());
        taskEntity.setDescription(taskDto.getDescription());
        taskEntity.setStatus(taskDto.getStatus());
        taskEntity.setPriority(taskDto.getPriority());
        taskEntity.setCategory(categoryDao.findCategoryByTitle(taskDto.getCategory()));

        LocalDate initialDate = taskDto.getInitialDate();
        LocalDate finalDate = taskDto.getFinalDate();

        // Check if status is 300, then set FinalDate to today's date
        if (taskDto.getStatus() == 300) {
            taskEntity.setFinalDate(LocalDate.now());
            if (initialDate.isAfter(finalDate)) {
                taskEntity.setInitialDate(LocalDate.now()); // Set InitialDate to today's date
            } else {
                taskEntity.setInitialDate(initialDate); // Set InitialDate from taskDto
            }
        }else if (taskDto.getStatus() == 200 ){
            initialDate = LocalDate.now();
            taskEntity.setInitialDate(initialDate);
            if (initialDate.isAfter(finalDate)) {
                taskEntity.setFinalDate(LocalDate.now()); // Set InitialDate to today's date
            } else {
                taskEntity.setFinalDate(finalDate);
            }
        } else {
            taskEntity.setFinalDate(finalDate); // Set FinalDate from taskDto
            taskEntity.setInitialDate(initialDate);
        }

        // Check and update InitialDate if necessary


        taskDao.merge(taskEntity);

        NotificationSocket.sendTaskToAll(getActiveTasksOrderedByPriority());
        DashboardSocket.sendDashboardGeneralStatsDtoToAll(dashBoardBean.mapToDashboardGeneralStatsDto());
        if (taskDto.getStatus() == 300){
            DashboardSocket.sendDashboardTaskLineChartDtoToAll(dashBoardBean.convertTaskEntityToDashboardLineChartDto());
        }
        DashboardSocket.sendCategoryTaskCountDtoToAll(dashBoardBean.displayTaskCountsByCategory());
    }



    //Function that receives a task name and deletes the task from the database mysql
    public boolean deleteTask(String title) {
        TaskEntity taskEntity = taskDao.findTaskByTitle(title);
        taskDao.remove(taskEntity);

        NotificationSocket.sendTaskToAll(getActiveTasksOrderedByPriority());
        DashboardSocket.sendDashboardGeneralStatsDtoToAll(dashBoardBean.mapToDashboardGeneralStatsDto());
        return true;
    }

    //Function that checks all tasks active = false and sets them to true
    public boolean restoreAllTasks() {
        List<TaskEntity> taskEntities = taskDao.getAllTasks();
        for (TaskEntity taskEntity : taskEntities) {
            if (!taskEntity.getActive()) {
                taskEntity.setActive(true);
                taskDao.merge(taskEntity);
            }
        }
        return true;
    }

    //Function that deletes all tasks from the database mysql that are active = false, returns true if all tasks were deleted
    public boolean deleteAllTasks() {
        List<TaskEntity> taskEntities = taskDao.getAllTasks();
        for (TaskEntity taskEntity : taskEntities) {
            if (!taskEntity.getActive()) {
                taskDao.remove(taskEntity);
            }
        }
        return true;
    }

    //Function that returns list of tasks filtered by category and owner from the database mysql
    public List<TaskDto> getTasksByCategoryAndOwner(String category, String owner){
        UserEntity userEntity = userDao.findUserByUsername(owner);
        CategoryEntity categoryEntity = categoryDao.findCategoryByTitle(category);
        List<TaskEntity> taskEntities = taskDao.getTasksByCategoryAndOwner(userEntity, categoryEntity);
        ArrayList<TaskDto> taskDtos = new ArrayList<>();
        for (TaskEntity taskEntity : taskEntities) {
            taskDtos.add(TaskMapper.convertTaskEntityToTaskDto(taskEntity));
        }
        return taskDtos;
    }

    //Function that returns list of tasks filtered by category from the database mysql
    public List<TaskDto> getTasksByCategory(String category){
        CategoryEntity categoryEntity = categoryDao.findCategoryByTitle(category);
        List<TaskEntity> taskEntities = taskDao.findTasksByCategory(categoryEntity);
        ArrayList<TaskDto> taskDtos = new ArrayList<>();
        for (TaskEntity taskEntity : taskEntities) {
            taskDtos.add(TaskMapper.convertTaskEntityToTaskDto(taskEntity));
        }
        return taskDtos;
    }

    //Function that returns list of tasks filtered by owner from the database mysql
    public List<TaskDto> getTasksByOwner(String owner){
        UserEntity userEntity = userDao.findUserByUsername(owner);
        List<TaskEntity> taskEntities = taskDao.findTaskByOwnerId(userEntity.getId());
        ArrayList<TaskDto> taskDtos = new ArrayList<>();
        for (TaskEntity taskEntity : taskEntities) {
            taskDtos.add(TaskMapper.convertTaskEntityToTaskDto(taskEntity));
        }
        return taskDtos;
    }

    // Function that returns the title of the task by id
    public String getStringTaskById(int idtask) {
        TaskEntity taskEntity = taskDao.findTaskById(idtask);
        if (taskEntity != null) {
            return taskEntity.getTitle();
        }
        return null;
    }


    public List<ManagingTaskDto> getManagingTasksByCategoryAndOwner(String category, String owner) {
        UserEntity userEntity = userDao.findUserByUsername(owner);
        CategoryEntity categoryEntity = categoryDao.findCategoryByTitle(category);
        List<TaskEntity> taskEntities = taskDao.getTasksByCategoryAndOwner(userEntity, categoryEntity);
        List<ManagingTaskDto> managingTaskDtos = new ArrayList<>();

        for (TaskEntity taskEntity : taskEntities) {
            ManagingTaskDto managingTaskDto = TaskMapper.convertTaskEntityToManagingTaskDto(taskEntity);

            // Add user information to the DTO
            UserEntity taskOwner = taskEntity.getOwner();
            UserDto userDto = new UserDto();
            userDto.setId(taskOwner.getId());
            userDto.setUsername(taskOwner.getUsername());
            userDto.setFirstname(taskOwner.getFirstname());
            userDto.setLastname(taskOwner.getLastname());
            userDto.setEmail(taskOwner.getEmail());
            userDto.setPhone(taskOwner.getPhone());
            userDto.setPhotoURL(taskOwner.getPhotoURL());
            userDto.setRole(taskOwner.getRole());
            managingTaskDto.setOwner(userDto);

            // Add category information to the DTO
            CategoryEntity taskCategory = taskEntity.getCategory();
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setId(taskCategory.getId());
            categoryDto.setTitle(taskCategory.getTitle());
            categoryDto.setDescription(taskCategory.getDescription());
            managingTaskDto.setCategory(categoryDto);

            managingTaskDtos.add(managingTaskDto);
        }

        return managingTaskDtos;
    }

    public List<ManagingTaskDto> getManagingTasksByCategory(String category) {
        CategoryEntity categoryEntity = categoryDao.findCategoryByTitle(category);
        List<TaskEntity> taskEntities = taskDao.findTasksByCategory(categoryEntity);
        List<ManagingTaskDto> managingTaskDtos = new ArrayList<>();

        for (TaskEntity taskEntity : taskEntities) {
            ManagingTaskDto managingTaskDto = TaskMapper.convertTaskEntityToManagingTaskDto(taskEntity);

            // Add user information to the DTO
            UserEntity taskOwner = taskEntity.getOwner();
            UserDto userDto = new UserDto();
            userDto.setId(taskOwner.getId());
            userDto.setUsername(taskOwner.getUsername());
            userDto.setFirstname(taskOwner.getFirstname());
            userDto.setLastname(taskOwner.getLastname());
            userDto.setEmail(taskOwner.getEmail());
            userDto.setPhone(taskOwner.getPhone());
            userDto.setPhotoURL(taskOwner.getPhotoURL());
            userDto.setRole(taskOwner.getRole());
            managingTaskDto.setOwner(userDto);

            // Add category information to the DTO
            CategoryEntity taskCategory = taskEntity.getCategory();
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setId(taskCategory.getId());
            categoryDto.setTitle(taskCategory.getTitle());
            categoryDto.setDescription(taskCategory.getDescription());
            managingTaskDto.setCategory(categoryDto);

            managingTaskDtos.add(managingTaskDto);
        }

        return managingTaskDtos;
    }

    public List<ManagingTaskDto> getManagingTasksByOwner(String owner) {
        UserEntity userEntity = userDao.findUserByUsername(owner);
        List<TaskEntity> taskEntities = taskDao.findTaskByOwnerId(userEntity.getId());
        List<ManagingTaskDto> managingTaskDtos = new ArrayList<>();

        for (TaskEntity taskEntity : taskEntities) {
            ManagingTaskDto managingTaskDto = TaskMapper.convertTaskEntityToManagingTaskDto(taskEntity);

            // Add user information to the DTO
            UserEntity taskOwner = taskEntity.getOwner();
            UserDto userDto = new UserDto();
            userDto.setId(taskOwner.getId());
            userDto.setUsername(taskOwner.getUsername());
            userDto.setFirstname(taskOwner.getFirstname());
            userDto.setLastname(taskOwner.getLastname());
            userDto.setEmail(taskOwner.getEmail());
            userDto.setPhone(taskOwner.getPhone());
            userDto.setPhotoURL(taskOwner.getPhotoURL());
            userDto.setRole(taskOwner.getRole());
            managingTaskDto.setOwner(userDto);

            // Add category information to the DTO
            CategoryEntity taskCategory = taskEntity.getCategory();
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setId(taskCategory.getId());
            categoryDto.setTitle(taskCategory.getTitle());
            categoryDto.setDescription(taskCategory.getDescription());
            managingTaskDto.setCategory(categoryDto);

            managingTaskDtos.add(managingTaskDto);
        }

        return managingTaskDtos;
    }

    // Function that return all tasks for tables
    public List<ManagingTaskDto> getAllManagingTasks() {
        List<TaskEntity> taskEntities = taskDao.getAllTasks();
        List<ManagingTaskDto> managingTaskDtos = new ArrayList<>();

        for (TaskEntity taskEntity : taskEntities) {
            ManagingTaskDto managingTaskDto = TaskMapper.convertTaskEntityToManagingTaskDto(taskEntity);

            // Add user information to the DTO
            UserEntity taskOwner = taskEntity.getOwner();
            UserDto userDto = new UserDto();
            userDto.setId(taskOwner.getId());
            userDto.setUsername(taskOwner.getUsername());
            userDto.setFirstname(taskOwner.getFirstname());
            userDto.setLastname(taskOwner.getLastname());
            userDto.setEmail(taskOwner.getEmail());
            userDto.setPhone(taskOwner.getPhone());
            userDto.setPhotoURL(taskOwner.getPhotoURL());
            userDto.setRole(taskOwner.getRole());
            managingTaskDto.setOwner(userDto);

            // Add category information to the DTO
            CategoryEntity taskCategory = taskEntity.getCategory();
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setId(taskCategory.getId());
            categoryDto.setTitle(taskCategory.getTitle());
            categoryDto.setDescription(taskCategory.getDescription());
            managingTaskDto.setCategory(categoryDto);

            managingTaskDtos.add(managingTaskDto);
        }

        return managingTaskDtos;
    }

    // Function that return all active tasks for tables
    public List<ManagingTaskDto> getAllActiveManagingTasks() {
        List<TaskEntity> taskEntities = taskDao.getAllTasks();
        List<ManagingTaskDto> activeManagingTaskDtos = new ArrayList<>();

        for (TaskEntity taskEntity : taskEntities) {
            if (taskEntity.getActive()) {
                ManagingTaskDto managingTaskDto = TaskMapper.convertTaskEntityToManagingTaskDto(taskEntity);

                // Add user information to the DTO
                UserEntity taskOwner = taskEntity.getOwner();
                UserDto userDto = new UserDto();
                userDto.setId(taskOwner.getId());
                userDto.setUsername(taskOwner.getUsername());
                userDto.setFirstname(taskOwner.getFirstname());
                userDto.setLastname(taskOwner.getLastname());
                userDto.setEmail(taskOwner.getEmail());
                userDto.setPhone(taskOwner.getPhone());
                userDto.setPhotoURL(taskOwner.getPhotoURL());
                userDto.setRole(taskOwner.getRole());
                managingTaskDto.setOwner(userDto);

                // Add category information to the DTO
                CategoryEntity taskCategory = taskEntity.getCategory();
                CategoryDto categoryDto = new CategoryDto();
                categoryDto.setId(taskCategory.getId());
                categoryDto.setTitle(taskCategory.getTitle());
                categoryDto.setDescription(taskCategory.getDescription());
                managingTaskDto.setCategory(categoryDto);

                activeManagingTaskDtos.add(managingTaskDto);
            }
        }

        return activeManagingTaskDtos;
    }

    //Confirms that the task exists by id
    public boolean taskExists(int id) {
        TaskEntity taskEntity = taskDao.findTaskById(id);
        return taskEntity != null;
    }

    //Confirms that task is inactive by id
    public boolean isTaskInactive(int id) {
        TaskEntity taskEntity = taskDao.findTaskById(id);
        return taskEntity != null && !taskEntity.getActive();
    }

    //Confirms that task is inactive by id
    public boolean isTaskActive(int taskId) {
        TaskEntity task = taskDao.findTaskById(taskId);
        if (task != null) {
            return task.getActive();
        }
        return false;
    }

    //Function so set Task active
    public boolean activateTask(int taskId) {
        TaskEntity taskEntity = taskDao.findTaskById(taskId);
        if (taskEntity != null) {
            taskEntity.setActive(true);
            taskDao.merge(taskEntity);

            NotificationSocket.sendTaskToAll(getActiveTasksOrderedByPriority());
            return true;
        }
        return false;
    }

    public List<ManagingTaskDto> getInactiveManagingTasks() {
        List<TaskEntity> inactiveTaskEntities = taskDao.getInactiveTasks();
        List<ManagingTaskDto> inactiveManagingTaskDtos = new ArrayList<>();

        for (TaskEntity taskEntity : inactiveTaskEntities) {
            ManagingTaskDto managingTaskDto = TaskMapper.convertTaskEntityToManagingTaskDto(taskEntity);

            // Add user information to the DTO
            UserEntity taskOwner = taskEntity.getOwner();
            UserDto userDto = new UserDto();
            userDto.setId(taskOwner.getId());
            userDto.setUsername(taskOwner.getUsername());
            userDto.setFirstname(taskOwner.getFirstname());
            userDto.setLastname(taskOwner.getLastname());
            userDto.setEmail(taskOwner.getEmail());
            userDto.setPhone(taskOwner.getPhone());
            userDto.setPhotoURL(taskOwner.getPhotoURL());
            userDto.setRole(taskOwner.getRole());
            managingTaskDto.setOwner(userDto);

            // Add category information to the DTO
            CategoryEntity taskCategory = taskEntity.getCategory();
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setId(taskCategory.getId());
            categoryDto.setTitle(taskCategory.getTitle());
            categoryDto.setDescription(taskCategory.getDescription());
            managingTaskDto.setCategory(categoryDto);

            inactiveManagingTaskDtos.add(managingTaskDto);
        }

        return inactiveManagingTaskDtos;
    }
    public List<TaskDto> getActiveTasksOrderedByPriority() {
        List<TaskEntity> taskEntities = taskDao.getActiveTasksOrderedByPriorityDesc();
        List<TaskDto> taskDtos = new ArrayList<>();
        for (TaskEntity taskEntity : taskEntities) {
            taskDtos.add(TaskMapper.convertTaskEntityToTaskDto(taskEntity));
        }
        return taskDtos;
    }

}
