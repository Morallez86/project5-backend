package aor.paj.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "notifications")
@NamedQuery(name = "Notification.findNotificationById", query = "SELECT n FROM NotificationEntity n WHERE n.id = :id")
@NamedQuery(name = "Notification.findNotificationsByUserId", query = "SELECT n FROM NotificationEntity n WHERE n.recipient.id = :userId ORDER BY n.timestamp DESC")
@NamedQuery(name = "Notification.findUnreadNotificationsByUserId", query = "SELECT n FROM NotificationEntity n WHERE n.recipient.id = :userId AND n.notification_read = false")
@NamedQuery(name = "Notification.findLastNotification", query = "SELECT n FROM NotificationEntity n ORDER BY n.timestamp DESC")
public class NotificationEntity implements Serializable {
    @Id
    @Column(name="id", nullable = false, unique = true, updatable = false)
    private int id;

    @ManyToOne
    @JoinColumn(name = "sender", nullable = false, updatable = false)
    private UserEntity sender;

    @ManyToOne
    @JoinColumn(name = "recipient", nullable = false, updatable = false)
    private UserEntity recipient;

    @Column(name = "message", nullable = false, updatable = false)
    private String message;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(name = "notification_read", nullable = false)
    private boolean notification_read;

    @Column(name = "notification_type", nullable = false, updatable = false)
    private String notification_type;

    public NotificationEntity() {
    }

    public NotificationEntity(UserEntity sender, UserEntity recipient, String message, LocalDateTime timestamp, boolean notification_read, String notification_type) {
        this.sender = sender;
        this.recipient = recipient;
        this.message = message;
        this.timestamp = timestamp;
        this.notification_read = notification_read;
        this.notification_type = notification_type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public UserEntity getSender() {
        return sender;
    }

    public void setSender(UserEntity sender) {
        this.sender = sender;
    }

    public UserEntity getRecipient() {
        return recipient;
    }

    public void setRecipient(UserEntity recipient) {
        this.recipient = recipient;
    }

    public boolean isNotification_read() {
        return notification_read;
    }

    public void setNotification_read(boolean notification_read) {
        this.notification_read = notification_read;
    }

    public String getNotification_type() {
        return notification_type;
    }

    public void setNotification_type(String notification_type) {
        this.notification_type = notification_type;
    }
}
