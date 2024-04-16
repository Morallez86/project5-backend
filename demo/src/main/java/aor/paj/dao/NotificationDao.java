package aor.paj.dao;

import aor.paj.entity.NotificationEntity;
import jakarta.ejb.Stateless;
import java.util.List;

@Stateless
public class NotificationDao extends AbstractDao<NotificationEntity> {

    private static final long serialVersionUID = 1L;

    public NotificationDao() {
        super(NotificationEntity.class);
    }

    public NotificationEntity findNotificationById(int id) {
        try {
            return em.createNamedQuery("Notification.findNotificationById", NotificationEntity.class)
                    .setParameter("id", id)
                    .getSingleResult();

        } catch (Exception e) {
            return null;
        }
    }

    public List<NotificationEntity> findNotificationsByUserId(int userId) {
        return em.createNamedQuery("Notification.findNotificationsByUserId", NotificationEntity.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<NotificationEntity> findUnreadNotificationsByUserId(int userId) {
        return em.createNamedQuery("Notification.findUnreadNotificationsByUserId", NotificationEntity.class)
                .setParameter("userId", userId)
                .getResultList();
    }

}
