package aor.paj.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ConfigurationDto {

    private int id;
    private int tokenExpirationTime;

    public ConfigurationDto() {
    }

    // Constructor with fields
    public ConfigurationDto(int id, int tokenExpirationTime) {
        this.id = id;
        this.tokenExpirationTime = tokenExpirationTime;
    }

    // Getter and setter methods
    @XmlElement
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @XmlElement
    public int getTokenExpirationTime() {
        return tokenExpirationTime;
    }

    public void setTokenExpirationTime(int tokenExpirationTime) {
        this.tokenExpirationTime = tokenExpirationTime;
    }

    @Override
    public String toString() {
        return "ConfigurationDto{" +
                "id=" + id +
                ", tokenExpirationTime=" + tokenExpirationTime +
                '}';
    }
}
