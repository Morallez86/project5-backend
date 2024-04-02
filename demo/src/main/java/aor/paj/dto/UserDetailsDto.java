package aor.paj.dto;

import java.util.Map;

public class UserDetailsDto {
    private String username;
    private String firstname;
    private String lastname;
    private String email;
    private String photoURL;
    private String phone;
    private String role;
    private Map<String, Integer> taskCounts;
    private long totalTasks;

    public UserDetailsDto() {
    }

    public UserDetailsDto(String username, String firstname, String lastname, String email, String photoURL, String phone, String role, Map<String, Integer> taskCounts, long totalTasks) {
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.photoURL = photoURL;
        this.phone = phone;
        this.role = role;
        this.taskCounts = taskCounts;
        this.totalTasks = totalTasks;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
    }

    public Map<String, Integer> getTaskCounts() {
        return taskCounts;
    }

    public void setTaskCounts(Map<String, Integer> taskCounts) {
        this.taskCounts = taskCounts;
    }
}
