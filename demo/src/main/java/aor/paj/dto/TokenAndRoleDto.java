package aor.paj.dto;

public class TokenAndRoleDto {
    private String token;
    private String role;
    private String username;
    private int userId;

    public TokenAndRoleDto() {
    }

    public TokenAndRoleDto(String token, String role, String username, int userId) {
        this.token = token;
        this.role = role;
        this.username = username;
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
