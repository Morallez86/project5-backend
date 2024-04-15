package aor.paj.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "notifications")
@NamedQuery(name = "Notification.findNotificationById", query = "SELECT n FROM NotificationEntity n WHERE n.id = :id")
@NamedQuery(name = "Notification.findNotificationsByUserId", query = "SELECT n FROM NotificationEntity n WHERE n.userEntity.id = :userId")
@NamedQuery(name = "Notification.findUnreadNotificationsByUserId", query = "SELECT n FROM NotificationEntity n WHERE n.userEntity.id = :userId AND n.notification_read = false")
public class NotificationEntity implements Serializable {
    @Id
    @Column(name="id", nullable = false, unique = true, updatable = false)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private UserEntity userEntity;

    @Column(name = "message", nullable = false, updatable = false)
    private String message;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(name = "notification_read", nullable = false)
    private boolean notification_read;

    public NotificationEntity() {
    }

    public NotificationEntity(UserEntity userEntity, String message) {
        this.userEntity = userEntity;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.notification_read = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UserEntity getUserEntity() {
        return userEntity;
    }

    public void setUserEntity(UserEntity userEntity) {
        this.userEntity = userEntity;
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
        return notification_read;
    }

    public void setRead(boolean notification_read) {
        this.notification_read = notification_read;
    }
}
