package aor.paj.mapper;

import aor.paj.dto.MessageDto;
import aor.paj.dto.NotificationDto;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.NotificationEntity;

import java.time.LocalDateTime;

public class NotificationMapper {

    private NotificationMapper() {
    }

    public static NotificationDto convertNotificationEntityToNotificationDto(NotificationEntity notificationEntity) {
        if (notificationEntity == null) {
            return null;
        }

        NotificationDto notificationDto = new NotificationDto();

        notificationDto.setId(notificationEntity.getId());
        notificationDto.setRecipientId(notificationEntity.getRecipient().getId());
        notificationDto.setSenderId(notificationEntity.getSender().getId());
        notificationDto.setMessage(notificationEntity.getMessage());
        notificationDto.setTimestamp(notificationEntity.getTimestamp());
        notificationDto.setRead(notificationEntity.isNotification_read());
        notificationDto.setNotificationType(notificationEntity.getNotification_type());

        return notificationDto;
    }

    public static NotificationEntity convertNotificationDtoToNotificationEntity(NotificationDto notificationDto) {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setMessage(notificationDto.getMessage());
        notificationEntity.setNotification_type(notificationDto.getNotificationType());
        if (notificationDto.getTimestamp() != null) {
            notificationDto.setTimestamp(notificationDto.getTimestamp());
        } else {
            notificationEntity.setTimestamp(LocalDateTime.now()); // Set current timestamp if not provided
        }

        return notificationEntity;
    }
}
