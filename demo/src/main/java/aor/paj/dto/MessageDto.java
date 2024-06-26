package aor.paj.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.LocalDateTime;
import java.util.Objects;

@XmlRootElement
public class MessageDto {
    private int id;
    private int sender;
    private int recipient;
    private String content;
    private LocalDateTime timestamp;
    private boolean read;

    // Constructors
    public MessageDto() {
    }

    public MessageDto(int id, int sender, int recipient, String content, LocalDateTime timestamp, boolean read) {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.timestamp = timestamp;
        this.read = read;
    }

    @XmlElement
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @XmlElement
    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    @XmlElement
    public int getRecipient() {
        return recipient;
    }

    public void setRecipient(int recipient) {
        this.recipient = recipient;
    }

    @XmlElement
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
        return "MessageDto{" +
                "id=" + id +
                ", sender=" + sender +
                ", recipient=" + recipient +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", read=" + read +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageDto that = (MessageDto) o;
        return id == that.id &&
                sender == that.sender &&
                recipient == that.recipient &&
                Objects.equals(content, that.content) &&
                Objects.equals(timestamp, that.timestamp) &&
                read == that.read;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sender, recipient, content, timestamp, read);
    }

}
