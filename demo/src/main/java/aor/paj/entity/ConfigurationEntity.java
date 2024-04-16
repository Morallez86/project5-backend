package aor.paj.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@NamedQuery(name = "Configuration.findById", query = "SELECT c FROM ConfigurationEntity c WHERE c.id = :id")
@Table(name = "configuration")
public class ConfigurationEntity implements Serializable{

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "token_expiration_time")
    private int tokenExpirationTime;

    // Default constructor
    public ConfigurationEntity() {
    }

    // Constructor with token expiration time
    public ConfigurationEntity(int tokenExpirationTime) {
        this.tokenExpirationTime = tokenExpirationTime;
    }

    // Getter and setter methods
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTokenExpirationTime() {
        return tokenExpirationTime;
    }

    public void setTokenExpirationTime(int tokenExpirationTime) {
        this.tokenExpirationTime = tokenExpirationTime;
    }

    @Override
    public String toString() {
        return "ConfigurationEntity{" +
                "id=" + id +
                ", tokenExpirationTime=" + tokenExpirationTime +
                '}';
    }
}
