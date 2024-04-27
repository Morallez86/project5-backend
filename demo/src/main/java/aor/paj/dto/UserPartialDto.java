package aor.paj.dto;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPartialDto that = (UserPartialDto) o;
        return userId == that.userId &&
                Objects.equals(username, that.username) &&
                Objects.equals(photoUrl, that.photoUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, photoUrl, userId);
    }
}
