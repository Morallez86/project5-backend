package aor.paj.mapper;

import aor.paj.dto.MessageDto;
import aor.paj.entity.MessageEntity;
import java.time.LocalDateTime;

public class MessageMapper {


    private MessageMapper() {
        // Private constructor to prevent instantiation
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

    public static MessageDto convertMessageEntityToMessageDto(MessageEntity messageEntity) {
        MessageDto messageDto = new MessageDto();

        messageDto.setContent(messageEntity.getContent());
        messageDto.setRead(messageEntity.isRead());
        messageDto.setTimestamp(messageEntity.getTimestamp());

        return messageDto;
    }
}
