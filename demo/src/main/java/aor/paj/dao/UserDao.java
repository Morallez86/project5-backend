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
public class UserDao extends AbstractDao<UserEntity> {

    private static final long serialVersionUID = 1L;

    public UserDao() {
        super(UserEntity.class);
    }

    public UserEntity findUserByEmail(String email) {
        try {
            return (UserEntity) em.createNamedQuery("User.findUserByEmail").setParameter("email", email)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public UserEntity findUserByUsername(String username) {
        try {
            return (UserEntity) em.createNamedQuery("User.findUserByUsername").setParameter("username", username)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public UserEntity findUserById(int id) {
        try {
            return (UserEntity) em.createNamedQuery("User.findUserById").setParameter("id", id)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public List<UserEntity> getActiveUsersContainingString(String searchQuery) {
        try {
            return em.createNamedQuery("User.findUsersBySearch", UserEntity.class)
                    .setParameter("query", "%" + searchQuery.toLowerCase() + "%")
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving active users containing string", e);
        }
    }

    public List<UserEntity> findAllUsers() {
        try{
            return em.createNamedQuery("User.findAllUsers", UserEntity.class).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<UserEntity> findAllActiveUsers() {
        try {
            return em.createQuery("SELECT u FROM UserEntity u WHERE u.active = true", UserEntity.class)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public UserEntity findByEmailValidationToken(String emailValidationToken) {
        try {
            return em.createNamedQuery("User.findUserByEmailValidationToken", UserEntity.class)
                    .setParameter("emailValidationToken", emailValidationToken)
                    .getSingleResult();
        } catch (Exception e) {
            return null; // Handle appropriately (e.g., log the exception)
        }
    }

    public List<UserEntity> searchUsers(String query) {
        try {
            return em.createNamedQuery("User.findUsersBySearch", UserEntity.class)
                    .setParameter("query", "%" + query.toLowerCase() + "%")
                    .getResultList();
        } catch (NoResultException e) {
            return null; // Handle appropriately (e.g., log the exception)
        }
    }

    public List<UserEntity> findUnvalidUsersForDeletion(LocalDateTime cutoffTime) {
        return em.createNamedQuery("User.findUnvalidUsersForDeletion", UserEntity.class)
                .setParameter("cutoffTime", cutoffTime)
                .getResultList();
    }

    public long countTotalUsers() {
        try {
            return (long) em.createNamedQuery("User.countTotalUsers")
                    .getSingleResult();
        } catch (NoResultException e) {
            return 0; // Return 0 if no users found
        }
    }

    public long countPendingUsers() {
        try {
            return (long) em.createNamedQuery("User.countPendingUsers")
                    .getSingleResult();
        } catch (NoResultException e) {
            return 0; // Return 0 if no pending users found
        }
    }

    public List<UserEntity> findAllUsersWithNonNullPasswordStamps(LocalDateTime cutoffTime) {
        try {
            return em.createNamedQuery("User.findAllUsersWithNonNullPasswordStamps", UserEntity.class)
                    .setParameter("cutoffTime", cutoffTime)
                    .getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList(); // Return an empty list if no results
        }
    }
}

