package aor.paj.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "notifications")
public class NotificationEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private UserEntity userId;

    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false, updatable = false)
    private MessageEntity messageId;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "read")
    private boolean read;

    public NotificationEntity() {
    }

    public NotificationEntity(UserEntity userId, MessageEntity messageId) {
        this.userId = userId;
        this.messageId = messageId;
        this.timestamp = LocalDateTime.now();
        this.read = false; // By default, notifications are marked as unread
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUserId() {
        return userId;
    }

    public void setUserId(UserEntity userId) {
        this.userId = userId;
    }

    public MessageEntity getMessageId() {
        return messageId;
    }

    public void setMessageId(MessageEntity messageId) {
        this.messageId = messageId;
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
}
