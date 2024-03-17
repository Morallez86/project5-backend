package aor.paj.mapper;

import aor.paj.dao.CategoryDao;
import aor.paj.dto.TaskDto;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import aor.paj.entity.CategoryEntity;
import aor.paj.dto.ManagingTaskDto;
import aor.paj.dto.UserDto;
import aor.paj.dto.CategoryDto;

public class TaskMapper {

    public TaskMapper() {
    }

    public static TaskEntity convertTaskDtoToTaskEntity(TaskDto taskDto) {

        CategoryDao categoryDao = new CategoryDao();

        TaskEntity taskEntity = new TaskEntity();

        taskEntity.setTitle(taskDto.getTitle());
        taskEntity.setDescription(taskDto.getDescription());
        taskEntity.setInitialDate(taskDto.getInitialDate());
        taskEntity.setFinalDate(taskDto.getFinalDate());
        taskEntity.setStatus(taskDto.getStatus());
        taskEntity.setPriority(taskDto.getPriority());

        return taskEntity;
    }

    public static TaskDto convertTaskEntityToTaskDto(TaskEntity taskEntity) {
        TaskDto taskDto = new TaskDto();

        taskDto.setId(taskEntity.getId());
        taskDto.setTitle(taskEntity.getTitle());
        taskDto.setDescription(taskEntity.getDescription());
        taskDto.setInitialDate(taskEntity.getInitialDate());
        taskDto.setFinalDate(taskEntity.getFinalDate());
        taskDto.setStatus(taskEntity.getStatus());
        taskDto.setPriority(taskEntity.getPriority());
        if(taskEntity.getActive() != null) {
            taskDto.setActive(taskEntity.getActive());
        }
        if(taskEntity.getCategory() != null) {
            taskDto.setCategory(taskEntity.getCategory().getTitle());
        }
        if(taskEntity.getOwner() != null) {
            taskDto.setOwner(taskEntity.getOwner().getUsername());
        }

        return taskDto;
    }

    public static ManagingTaskDto convertTaskEntityToManagingTaskDto(TaskEntity taskEntity) {
        ManagingTaskDto managingTaskDto = new ManagingTaskDto();

        managingTaskDto.setId(taskEntity.getId());
        managingTaskDto.setTitle(taskEntity.getTitle());
        managingTaskDto.setDescription(taskEntity.getDescription());
        managingTaskDto.setInitialDate(taskEntity.getInitialDate());
        managingTaskDto.setFinalDate(taskEntity.getFinalDate());
        managingTaskDto.setStatus(taskEntity.getStatus());
        managingTaskDto.setPriority(taskEntity.getPriority());
        managingTaskDto.setActive(taskEntity.getActive());

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

        return managingTaskDto;
    }
}
