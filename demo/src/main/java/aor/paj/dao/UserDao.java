package aor.paj.dao;

import aor.paj.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

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
}

