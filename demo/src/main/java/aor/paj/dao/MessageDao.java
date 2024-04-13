package aor.paj.dao;

import aor.paj.entity.CategoryEntity;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class MessageDao extends AbstractDao<MessageEntity> {

    private static final long serialVersionUID = 1L;

    public MessageDao() {
        super(MessageEntity.class);
    }

    public List<MessageEntity> findMessagesBySenderId(int senderId) {
        try {
            return em.createNamedQuery("Message.findSentMessagesByUserId", MessageEntity.class)
                    .setParameter("userId", senderId)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<MessageEntity> findMessagesByRecipientId(int recipientId) {
        try {
            return em.createNamedQuery("Message.findReceivedMessagesByUserId", MessageEntity.class)
                    .setParameter("userId", recipientId)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<MessageEntity> findUnreadMessagesByRecipientId(int recipientId) {
        try {
            return em.createNamedQuery("Message.findUnreadMessagesByUserId", MessageEntity.class)
                    .setParameter("userId", recipientId)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<MessageEntity> findMessagesExchangedBetweenUsers(int userId1, int userId2) {
        try {
            return em.createNamedQuery("Message.findMessagesBetweenUsers", MessageEntity.class)
                    .setParameter("userId1", userId1)
                    .setParameter("userId2", userId2)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<UserEntity> findUsersCommunicatedWith(int userId) {
        System.out.println("1");
        try {
            return em.createNamedQuery("MessageEntity.findUsersCommunicatedWith", UserEntity.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } catch (NoResultException e) {
            return new ArrayList<>(); // Return an empty list if no results found
        }
    }


    public MessageEntity findMessageById(int id) {
        try {
            return em.createNamedQuery("Message.findMessageById", MessageEntity.class)
                    .setParameter("id", id)
                    .getSingleResult();

        } catch (Exception e) {
            return null;
        }
    }
    public List<MessageEntity> findMessagesBeforeTimestampForUsers(int userId1, int userId2, LocalDateTime timestamp) {
        try {
            return em.createNamedQuery("MessageEntity.findMessagesBeforeTimestampForUsers", MessageEntity.class)
                    .setParameter("userId1", userId1)
                    .setParameter("userId2", userId2)
                    .setParameter("timestamp", timestamp)
                    .getResultList();
        } catch (NoResultException e) {
            return new ArrayList<>(); // Return an empty list if no results found
        }
    }

}
