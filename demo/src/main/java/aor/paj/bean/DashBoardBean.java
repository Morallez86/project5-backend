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
}
