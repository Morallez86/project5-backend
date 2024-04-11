package aor.paj.dao;

import aor.paj.entity.CategoryEntity;
import aor.paj.entity.MessageEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class MessageDao extends AbstractDao<MessageEntity> {

    private static final long serialVersionUID = 1L;

    public MessageDao() {
        super(MessageEntity.class);
    }

    public List<MessageEntity> findMessagesBySenderId(Long senderId) {
        try {
            return em.createNamedQuery("Message.findSentMessagesByUserId", MessageEntity.class)
                    .setParameter("userId", senderId)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<MessageEntity> findMessagesByRecipientId(Long recipientId) {
        try {
            return em.createNamedQuery("Message.findReceivedMessagesByUserId", MessageEntity.class)
                    .setParameter("userId", recipientId)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<MessageEntity> findUnreadMessagesByRecipientId(Long recipientId) {
        try {
            return em.createNamedQuery("Message.findUnreadMessagesByUserId", MessageEntity.class)
                    .setParameter("userId", recipientId)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<MessageEntity> findMessagesExchangedByUserId(Long userId) {
        try {
            return em.createNamedQuery("Message.findMessagesExchangedByUserId", MessageEntity.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public MessageEntity findMessageById(long id) {
        try {
            return em.createNamedQuery("Message.findMessageById", MessageEntity.class)
                    .setParameter("id", id)
                    .getSingleResult();

        } catch (Exception e) {
            return null;
        }
    }

    // Additional methods can be added based on your application's requirements
}
