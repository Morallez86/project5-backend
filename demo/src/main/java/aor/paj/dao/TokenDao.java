package aor.paj.dao;

import aor.paj.entity.TokenEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Stateless
public class TokenDao extends AbstractDao<TokenEntity> {

    private static final long serialVersionUID = 1L;

    public TokenDao() {
        super(TokenEntity.class);
    }

    public TokenEntity findTokenByValue(String tokenValue) {
        try {
            return em.createNamedQuery("Token.findTokenByValue", TokenEntity.class)
                    .setParameter("tokenValue", tokenValue)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<TokenEntity> findExpiredTokens(LocalDateTime currentDateTime) {
        return em.createNamedQuery("Token.findExpiredTokens", TokenEntity.class)
                .setParameter("currentDateTime", currentDateTime)
                .getResultList();
    }

    public Long countTokensByUserId(int userId) {
        return em.createNamedQuery("Token.countTokensByUser", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    public void deleteTokensByUserId(int userId) {
        em.createNamedQuery("Token.deleteTokensByUser")
                .setParameter("userId", userId)
                .executeUpdate();
    }


    public List<TokenEntity> findTokensByUserId(int userId) {
        return em.createNamedQuery("Token.findTokensByUser", TokenEntity.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public String getTokenValueByUserId(int userId) {
        try {
            return em.createNamedQuery("Token.findTokenValueByUserId", String.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
        } catch (NoResultException e) {
            // Handle the case where no token is found for the specified user ID
            return null;
        }
    }

}
