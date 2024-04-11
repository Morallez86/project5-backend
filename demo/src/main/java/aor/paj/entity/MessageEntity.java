package aor.paj.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "messages")
@NamedQuery(name = "Message.findMessageById", query = "SELECT m FROM MessageEntity m WHERE m.id = :id")
@NamedQuery(name = "Message.findSentMessagesByUserId", query = "SELECT m FROM MessageEntity m WHERE m.sender = :userId")
@NamedQuery(name = "Message.findReceivedMessagesByUserId", query = "SELECT m FROM MessageEntity m WHERE m.recipient = :userId")
@NamedQuery(name = "Message.findUnreadMessagesByUserId", query = "SELECT m FROM MessageEntity m WHERE m.recipient = :userId AND m.read = false")
@NamedQuery(name = "Message.findMessagesExchangedByUserId", query = "SELECT m FROM MessageEntity m WHERE m.sender = :userId OR m.recipient = :userId")

public class MessageEntity implements Serializable {

    @Id
    @Column(name="id", nullable = false, unique = true, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false, updatable = false)
    private UserEntity sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false, updatable = false)
    private UserEntity recipient;

    @Column(name = "content", nullable = false, updatable = false)
    private String content;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    public MessageEntity() {
    }

    public MessageEntity(UserEntity sender, UserEntity recipient, String content) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.read = false; // By default, messages are marked as unread
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    @Override
    public String toString() {
        return "MessageEntity{" +
                "id=" + id +
                ", sender=" + sender +
                ", recipient=" + recipient +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", read=" + read +
                '}';
    }
}
