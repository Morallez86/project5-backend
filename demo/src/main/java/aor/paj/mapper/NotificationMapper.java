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
        notificationDto.setUserId(notificationEntity.getUserEntity().getId());
        notificationDto.setMessage(notificationEntity.getMessage());
        notificationDto.setTimestamp(notificationEntity.getTimestamp());
        notificationDto.setRead(notificationEntity.isRead());

        return notificationDto;
    }

    public static MessageEntity convertMessageDtoToMessageEntity(MessageDto messageDto) {
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setContent(messageDto.getContent());

        if (messageDto.getTimestamp() != null) {
            messageEntity.setTimestamp(messageDto.getTimestamp());
        } else {
            messageEntity.setTimestamp(LocalDateTime.now()); // Set current timestamp if not provided
        }

        return messageEntity;
    }
    public static NotificationEntity convertNotificationDtoToNotificationEntity(NotificationDto notificationDto) {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setMessage(notificationDto.getMessage());
        if (notificationDto.getTimestamp() != null) {
            notificationDto.setTimestamp(notificationDto.getTimestamp());
        } else {
            notificationEntity.setTimestamp(LocalDateTime.now()); // Set current timestamp if not provided
        }

        return notificationEntity;
    }
}
