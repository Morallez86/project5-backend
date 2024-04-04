package aor.paj.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.LocalDateTime;
import java.util.ArrayList;

@XmlRootElement
public class UserDto {
    private int id;
    private String username;
    private String password;
    private String email;
    private String firstname;
    private String lastname;
    private String phone;
    private String photoURL;
    private String role;
    private boolean active;

    private boolean pending;

    private String emailValidation;

    private LocalDateTime registTime;

    // Constructors
    public UserDto() {
    }

    public UserDto(String username, String password, String email, String firstname, String lastname, String phone, String photoURL) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.phone = phone;
        this.photoURL = photoURL;
    }

    // Getter methods
    @XmlElement
    public int getId() {
        return id;
    }

    @XmlElement
    public String getUsername() {
        return username;
    }

    @XmlElement
    public String getPassword() {
        return password;
    }

    @XmlElement
    public String getEmail() {
        return email;
    }

    @XmlElement
    public String getFirstname() {
        return firstname;
    }

    @XmlElement
    public String getLastname() {
        return lastname;
    }

    @XmlElement
    public String getPhone() {
        return phone;
    }

    @XmlElement
    public String getEmailValidation() {
        return emailValidation;
    }

    @XmlElement
    public LocalDateTime getRegistTime() {
        return registTime;
    }

    @XmlElement
    public String getPhotoURL() {
        return photoURL;
    }
    @XmlElement
    public String getRole() {
        return role;
    }

    @XmlElement
    public boolean isActive() {
        return active;
    }

    @XmlElement
    public boolean isPending() {
        return pending;
    }

    // Setter methods
    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public void setRegistTime(LocalDateTime registTime) {
        this.registTime = registTime;
    }

    public void setEmailValidation(String emailValidation) {
        this.emailValidation = emailValidation;
    }
}