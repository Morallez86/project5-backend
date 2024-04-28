package aor.paj.bean;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
public class DashBoardBean {

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

    public DashboardGeneralStatsDto mapToDashboardGeneralStatsDto() {
        long totalUsers = userDao.countTotalUsers();
        long totalPendingUsers = userDao.countPendingUsers();
        long totalActiveUsers = totalUsers - totalPendingUsers; // Calculate active users
        long totalTasks = taskDao.countAllTasks(); // Total tasks in the system

        // Retrieve task counts by status for all tasks
        List<Object[]> taskStatusCounts = taskDao.countTotalTasksByStatus();
        Map<Integer, Long> taskCountsByStatus = new HashMap<>();

        // Populate the map with task counts by status
        for (Object[] result : taskStatusCounts) {
            int status = (int) result[0];
            long count = (long) result[1];
            taskCountsByStatus.put(status, count);
        }

        return new DashboardGeneralStatsDto(totalUsers, totalPendingUsers, totalActiveUsers, totalTasks, taskCountsByStatus);
    }

    public List<CategoryTaskCountDto> displayTaskCountsByCategory() {
        // Retrieve the category counts from taskDao
        List<Object[]> categoryCounts = taskDao.countTasksByCategory();
        List<CategoryTaskCountDto> categoryTaskCounts = new ArrayList<>();

        // Sort the categoryCounts list by task count (descending order)
        categoryCounts.sort(Comparator.comparingLong((Object[] result) -> (Long) result[1]).reversed());

        // Create CategoryTaskCountDto objects from the sorted categoryCounts
        for (Object[] result : categoryCounts) {
            CategoryEntity category = (CategoryEntity) result[0];
            Long taskCount = (Long) result[1];

            CategoryTaskCountDto dto = new CategoryTaskCountDto();
            dto.setCategoryTitle(category.getTitle());
            dto.setTaskCount(taskCount);

            categoryTaskCounts.add(dto);
        }

        return categoryTaskCounts;
    }

    public List<DashboardLineChartDto> convertUserEntityToDashboardLineChartDto() {
        try {
            List<UserEntity> allUsers = userDao.findAllUsers();
            List<DashboardLineChartDto> linechartDto = new ArrayList<>();

            for (UserEntity user : allUsers) {
                if (user.getRegistTime() != null) {
                    DashboardLineChartDto dashboardLineChartDto = new DashboardLineChartDto(user.getRegistTime());
                    linechartDto.add(dashboardLineChartDto);
                } else {
                }
            }
            return linechartDto;
        } catch (Exception e) {
            return Collections.emptyList(); // Return empty list on error
        }
    }

    public List<DashboardTaskLineChartDto> convertTaskEntityToDashboardLineChartDto() {
        try {
            List<TaskEntity> taskDone = taskDao.findTasksByStatus(300);
            List<DashboardTaskLineChartDto> taskLinechartDto = new ArrayList<>();

            for (TaskEntity task : taskDone) {
                if (task.getFinalDate() != null) {
                    DashboardTaskLineChartDto dashboardTaskLineChartDto = new DashboardTaskLineChartDto(task.getFinalDate(), task.getInitialDate());
                    taskLinechartDto.add(dashboardTaskLineChartDto);
                } else {
                }
            }
            return taskLinechartDto;
        } catch (Exception e) {
            return Collections.emptyList(); // Return empty list on error
        }
    }

}
