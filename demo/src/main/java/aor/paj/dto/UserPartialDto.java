package aor.paj.dto;

public class UserPartialDto {
    private String username;
    private String photoUrl;

    private int userId;

    public UserPartialDto() {
    }

    public UserPartialDto(String username, String photoUrl, int userId) {
        this.username = username;
        this.photoUrl = photoUrl;
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photourl) {
        this.photoUrl = photourl;
    }

}
