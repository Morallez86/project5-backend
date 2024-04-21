package aor.paj.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.Map;

@XmlRootElement
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
    @XmlElement
    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    @XmlElement
    public long getTotalPendingUsers() {
        return totalPendingUsers;
    }

    public void setTotalPendingUsers(long totalPendingUsers) {
        this.totalPendingUsers = totalPendingUsers;
    }

    @XmlElement
    public long getTotalActiveUsers() {
        return totalActiveUsers;
    }

    public void setTotalActiveUsers(long totalActiveUsers) {
        this.totalActiveUsers = totalActiveUsers;
    }

    @XmlElement
    public long getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(long totalTasks) {
        this.totalTasks = totalTasks;
    }

    @XmlElement
    public Map<Integer, Long> getTaskCountsByStatus() {
        return taskCountsByStatus;
    }

    public void setTaskCountsByStatus(Map<Integer, Long> taskCountsByStatus) {
        this.taskCountsByStatus = taskCountsByStatus;
    }
}

