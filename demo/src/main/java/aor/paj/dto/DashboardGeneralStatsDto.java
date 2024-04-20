package aor.paj.dto;

import java.util.Map;

public class DashboardGeneralStatsDto {
    private long totalUsers;
    private long totalPendingUsers;
    private long totalActiveUsers; // Total users excluding pending users
    private long totalTasks;
    private Map<Integer, Long> taskCountsByStatus; // Map of task counts by status (100, 200, 300)

    // Constructor
    public DashboardGeneralStatsDto(long totalUsers, long totalPendingUsers, long totalActiveUsers, long totalTasks, Map<Integer, Long> taskCountsByStatus) {
        this.totalUsers = totalUsers;
        this.totalPendingUsers = totalPendingUsers;
        this.totalActiveUsers = totalActiveUsers;
        this.totalTasks = totalTasks;
        this.taskCountsByStatus = taskCountsByStatus;
    }

    // Getters and Setters
    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalPendingUsers() {
        return totalPendingUsers;
    }

    public void setTotalPendingUsers(long totalPendingUsers) {
        this.totalPendingUsers = totalPendingUsers;
    }

    public long getTotalActiveUsers() {
        return totalActiveUsers;
    }

    public void setTotalActiveUsers(long totalActiveUsers) {
        this.totalActiveUsers = totalActiveUsers;
    }

    public long getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(long totalTasks) {
        this.totalTasks = totalTasks;
    }

    public Map<Integer, Long> getTaskCountsByStatus() {
        return taskCountsByStatus;
    }

    public void setTaskCountsByStatus(Map<Integer, Long> taskCountsByStatus) {
        this.taskCountsByStatus = taskCountsByStatus;
    }
}

