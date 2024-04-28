package aor.paj.bean;

import aor.paj.dao.NotificationDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.MessageDto;
import aor.paj.dto.NotificationDto;
import aor.paj.entity.NotificationEntity;
import aor.paj.entity.UserEntity;
import aor.paj.mapper.NotificationMapper;
import aor.paj.websocket.NotificationSocket;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@ApplicationScoped
public class NotificationBean {

    @Inject
    TokenBean tokenBean;

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

    public NotificationDto addNotificationMessage(NotificationDto notificationDto) {
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
            notificationEntity.setMessage("New message from: " + userEntity2.getUsername());
            notificationEntity.setNotification_type("message");

            // Persist the notificationEntity to the database
            notificationDao.persist(notificationEntity);

            return notificationDto;
        } catch (Exception e) {
            e.printStackTrace(); // Log or handle the exception appropriately
            return null;
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

    public boolean markNotificationsAsRead(int userId) {
        try {
            // Retrieve all notifications for the specified user
            List<NotificationEntity> notifications = notificationDao.findNotificationsByUserId(userId);

            // Update the read status of each notification to indicate they have been read
            for (NotificationEntity notification : notifications) {
                notification.setNotification_read(true);
                notificationDao.merge(notification); // Persist the updated notification
            }

            return true; // Notifications successfully marked as read
        } catch (Exception e) {
            e.printStackTrace(); // Log or handle the exception appropriately
            return false;
        }
    }
    public void sendNotificationToRecipient(NotificationDto notificationDto) throws IOException {

        String activeTokenReceiver = tokenBean.displayTokenValueForUser(notificationDto.getRecipientId());
        NotificationSocket.sendNotification(activeTokenReceiver, notificationDto);
    }

    public void sendMessageToRecipient(MessageDto messageDto) throws IOException {

        String activeTokenReceiver = tokenBean.displayTokenValueForUser(messageDto.getRecipient());
        NotificationSocket.sendSocketMessage(activeTokenReceiver, messageDto);
    }

    public NotificationDto socketLastNotification(){
        NotificationEntity socketNotificationEntity = notificationDao.findLastNotification();
        return NotificationMapper.convertNotificationEntityToNotificationDto(socketNotificationEntity);
    }

}
