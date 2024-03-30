package aor.paj.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "auth_token")
@NamedQueries({
        @NamedQuery(name = "Token.findTokenByValue", query = "SELECT t FROM TokenEntity t WHERE t.tokenValue = :tokenValue"),
        @NamedQuery(name = "Token.findTokensByUser", query = "SELECT t FROM TokenEntity t WHERE t.user.id = :userId"),
        @NamedQuery(name = "Token.findExpiredTokens", query = "SELECT t FROM TokenEntity t WHERE t.expirationTime < :currentDateTime"),
        @NamedQuery(name = "Token.countTokensByUser", query = "SELECT COUNT(t) FROM TokenEntity t WHERE t.user.id = :userId"),
        @NamedQuery(name = "Token.deleteTokensByUser", query = "DELETE FROM TokenEntity t WHERE t.user.id = :userId"),
        @NamedQuery(name = "Token.countTokensByUserId", query = "SELECT COUNT(t) FROM TokenEntity t WHERE t.user.id = :userId")
})
public class TokenEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "token_value", nullable = false, unique = true)
    private String tokenValue;

    @Column(name = "expiration_time", nullable = false)
    private LocalDateTime expirationTime;

    @ManyToOne
    private UserEntity user;

    public TokenEntity() {
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "TokenEntity{" +
                "id=" + id +
                ", tokenValue='" + tokenValue + '\'' +
                ", expirationTime=" + expirationTime +
                ", user=" + user +
                '}';
    }
}
