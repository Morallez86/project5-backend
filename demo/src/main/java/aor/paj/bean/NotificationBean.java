package aor.paj.bean;

import aor.paj.dao.NotificationDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.MessageDto;
import aor.paj.dto.NotificationDto;
import aor.paj.dto.UserDto;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.NotificationEntity;
import aor.paj.entity.UserEntity;
import aor.paj.mapper.MessageMapper;
import aor.paj.mapper.NotificationMapper;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.NotificationOptions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class NotificationBean {

    @EJB
    private NotificationDao notificationDao;

    @EJB
    private UserDao userDao;

    public int generateIdDataBase() {
        int id = 1;
        boolean idAlreadyExists;

        do {
            idAlreadyExists = false;
            NotificationEntity notificationEntity = notificationDao.findNotificationById(id);
            if (notificationEntity != null) {
                id++;
                idAlreadyExists = true;
            }
        } while (idAlreadyExists);
        return id;
    }

    public List<NotificationDto> getNotificationsForUser(int userId) {
        List<NotificationEntity> notificationEntities = notificationDao.findNotificationsByUserId(userId);
        List<NotificationDto> notificationDtos = new ArrayList<>();

        for (NotificationEntity notificationEntity : notificationEntities) {
            NotificationDto notificationDto = NotificationMapper.convertNotificationEntityToNotificationDto(notificationEntity);
            notificationDtos.add(notificationDto);
        }

        return notificationDtos;
    }

    public boolean addNotification(NotificationDto notificationDto) {
        try {
            UserEntity userEntity = userDao.findUserById(notificationDto.getRecipientId());
            UserEntity userEntity2 = userDao.findUserById(notificationDto.getSenderId());

            if (userEntity == null) {
                throw new IllegalArgumentException("User not found");
            }

            // Create a new NotificationEntity from the NotificationDto
            NotificationEntity notificationEntity = NotificationMapper.convertNotificationDtoToNotificationEntity(notificationDto);
            notificationEntity.setRecipient(userEntity);
            notificationEntity.setSender(userEntity2);
            notificationEntity.setTimestamp(LocalDateTime.now());
            notificationEntity.setNotification_read(false);
            notificationEntity.setId(generateIdDataBase());

            // Persist the notificationEntity to the database
            notificationDao.persist(notificationEntity);

            return true;
        } catch (Exception e) {
            e.printStackTrace(); // Log or handle the exception appropriately
            return false;
        }
    }

    public boolean markNotificationAsRead(int notificationId) {
        try {
            NotificationEntity notificationEntity = notificationDao.findNotificationById(notificationId);

            if (notificationEntity == null) {
                return false;
            }

            // Update the read status of the notification to indicate it has been read
            notificationEntity.setNotification_read(true);
            notificationDao.merge(notificationEntity); // Persist the updated notification

            return true; // Notification successfully marked as read
        } catch (Exception e) {
            e.printStackTrace(); // Log or handle the exception appropriately
            return false;
        }
    }

    public List<NotificationDto> getUnreadNotificationsForUser(int userId) {
        List<NotificationEntity> unreadNotificationEntities = notificationDao.findUnreadNotificationsByUserId(userId);
        List<NotificationDto> unreadNotifications = new ArrayList<>();
        for (NotificationEntity notificationEntity : unreadNotificationEntities) {
            // Convert MessageEntity to MessageDto using mapper
            NotificationDto notificationDto = NotificationMapper.convertNotificationEntityToNotificationDto(notificationEntity);
            unreadNotifications.add(notificationDto);
        }

        return unreadNotifications;
    }

}
