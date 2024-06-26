package aor.paj.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

@Entity
@Table(name="user")
@NamedQuery(name = "User.findUserByUsername", query = "SELECT u FROM UserEntity u WHERE u.username = :username")
@NamedQuery(name = "User.findUserByEmail", query = "SELECT u FROM UserEntity u WHERE u.email = :email")
@NamedQuery(name = "User.findUserById", query = "SELECT u FROM UserEntity u WHERE u.id = :id")
@NamedQuery(name = "User.findAllUsers", query = "SELECT u FROM UserEntity u")
@NamedQuery(name = "User.findAllActiveUsers", query = "SELECT u FROM UserEntity u WHERE u.active = true")
@NamedQuery(name = "User.findUserByEmailValidationToken", query = "SELECT u FROM UserEntity u WHERE u.emailValidation = :emailValidationToken")
@NamedQuery(name= "User.findUsersBySearch", query = "SELECT u FROM UserEntity u WHERE LOWER(u.username) LIKE :query")
@NamedQuery(name = "User.findUnvalidUsersForDeletion", query = "SELECT u FROM UserEntity u WHERE u.pending = true AND u.registTime <= :cutoffTime")
@NamedQuery(name = "User.countTotalUsers", query = "SELECT COUNT(u) FROM UserEntity u")
@NamedQuery(name = "User.countPendingUsers", query = "SELECT COUNT(u) FROM UserEntity u WHERE u.pending = true")
@NamedQuery(
        name = "User.findAllUsersWithNonNullPasswordStamps",
        query = "SELECT u FROM UserEntity u WHERE u.passwordRetrieveTime IS NOT NULL AND u.passwordRetrieveTime <= :cutoffTime"
)

public class UserEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="id", nullable = false, unique = true, updatable = false)
    private int id;

    @Column(name="username", nullable = false, unique = true, updatable = false)
    private String username;

    @Column(name="password", nullable = false, unique = false, updatable = true)
    private String password;

    @Column(name="email", nullable = false, unique = true, updatable = true)
    private String email;

    @Column(name="firstname", nullable = false, unique = false, updatable = true)
    private String firstname;

    @Column(name="lastname", nullable = false, unique = false, updatable = true)
    private String lastname;

    @Column(name="phone", nullable = false, unique = false, updatable = true)
    private String phone;

    @Column(name="photoURL", nullable = false, unique = false, updatable = true)
    private String photoURL;

    @Column(name="role", nullable = false, unique = false, updatable = true)
    private String role;

    @OneToMany(mappedBy = "user")
    private List<TokenEntity> tokens = new ArrayList<>();

    @Column(name="active", nullable = false, unique = false, updatable = true)
    private Boolean active;

    @Column(name="pending", nullable = false, unique = false, updatable = true)
    private Boolean pending;

    @Column(name="email_validation", nullable = true, unique = false, updatable = true)
    private String emailValidation;

    @Column(name="regist_time", nullable = true, unique = false, updatable = true)
    private LocalDateTime registTime;

    @Column(name = "password_stamp")
    private LocalDateTime passwordRetrieveTime;


    public UserEntity() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;//BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDateTime getPasswordRetrieveTime() {
        return passwordRetrieveTime;
    }

    public void setPasswordRetrieveTime(LocalDateTime passwordRetrieveTime) {
        this.passwordRetrieveTime = passwordRetrieveTime;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getPending() {
        return pending;
    }

    public void setPending(Boolean pending) {
        this.pending = pending;
    }

    public String getEmailValidation() {
        return emailValidation;
    }

    public void setEmailValidation(String emailValidation) {
        this.emailValidation = emailValidation;
    }

    public LocalDateTime getRegistTime() {
        return registTime;
    }

    public void setRegistTime(LocalDateTime registTime) {
        this.registTime = registTime;
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", phone='" + phone + '\'' +
                ", photoURL='" + photoURL + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                ", pending=" + pending +
                ", emailValidation" + emailValidation +
                ", registerTime" + registTime +
                ", passwordRetrievalTime" + passwordRetrieveTime +
                '}';
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
