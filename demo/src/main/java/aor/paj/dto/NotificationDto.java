package aor.paj.dto;

import java.time.LocalDateTime;

public class NotificationDto {
    private int id;
    private int senderId;
    private int recipientId;
    private String message;
    private LocalDateTime timestamp;
    private boolean read;
    private String notificationType;

    // Constructors
    public NotificationDto() {
    }

    public NotificationDto(int id, int senderId, int recipientId, String message, LocalDateTime timestamp, boolean read, String notificationType) {
        this.id = id;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.message = message;
        this.timestamp = timestamp;
        this.read = read;
        this.notificationType = notificationType;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(int recipientId) {
        this.recipientId = recipientId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "NotificationDto{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", recipientId=" + recipientId +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", read=" + read +
                ", notificationType='" + notificationType + '\'' +
                '}';
    }
}
