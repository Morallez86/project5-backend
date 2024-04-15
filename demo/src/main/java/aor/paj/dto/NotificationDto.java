package aor.paj.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.LocalDateTime;

@XmlRootElement
public class NotificationDto {
    private int id;
    private int userId;
    private String message;
    private LocalDateTime timestamp;
    private boolean read;

    // Constructors
    public NotificationDto() {
    }

    public NotificationDto(int id, int userDto, String message, LocalDateTime timestamp, boolean read) {
        this.id = id;
        this.userId = userDto;
        this.message = message;
        this.timestamp = timestamp;
        this.read = read;
    }

    // Getters and Setters
    @XmlElement
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @XmlElement
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @XmlElement
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @XmlElement
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @XmlElement
    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "NotificationDto{" +
                "id=" + id +
                ", userDto=" + userId +
                ", messageDto=" + message +
                ", timestamp=" + timestamp +
                ", read=" + read +
                '}';
    }
}
