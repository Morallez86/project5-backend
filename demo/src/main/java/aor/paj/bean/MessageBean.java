package aor.paj.bean;

import aor.paj.dao.MessageDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.MessageDto;
import aor.paj.dto.UserDto;
import aor.paj.dto.UserPartialDto;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.UserEntity;
import aor.paj.mapper.MessageMapper;
import aor.paj.mapper.UserMapper;
import aor.paj.utils.EmailUtil;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class MessageBean {

    @EJB
    MessageDao messageDao;

    @EJB
    UserDao userDao;

    public int generateIdDataBase() {
        int id = 1;
        boolean idAlreadyExists;

        do {
            idAlreadyExists = false;
            MessageEntity messageEntity = messageDao.findMessageById(id);
            if (messageEntity != null) {
                id++;
                idAlreadyExists = true;
            }
        } while (idAlreadyExists);
        return id;
    }

    public List<MessageDto> getMessagesForUser(int userId, int recipientId) {
        List<MessageEntity> messageEntities = messageDao.findMessagesExchangedBetweenUsers(userId, recipientId);
        List<MessageDto> messageDtos = new ArrayList<>();
        for(MessageEntity me : messageEntities){
            messageDtos.add(MessageMapper.convertMessageEntityToMessageDto(me));
        }
        return messageDtos;
    }

    //Mock
    public boolean addMessage(MessageDto message) {
        try {
            // Fetch sender and recipient from database (assuming userDao is available)
            UserEntity sender = userDao.findUserById(message.getSender());
            UserEntity recipient = userDao.findUserById(message.getRecipient());

            MessageEntity messageEntity = MessageMapper.convertMessageDtoToMessageEntity(message);
            messageEntity.setSender(sender);
            messageEntity.setRecipient(recipient);
            messageEntity.setId(generateIdDataBase());
            messageEntity.setRead(false);
            messageEntity.setTimestamp(LocalDateTime.now());

            messageDao.persist(messageEntity);

            return true;
        } catch (Exception e) {
            e.printStackTrace(); // Log or handle the exception appropriately
            return false;
        }
    }

    //Mock
    public List<UserPartialDto> getAllUsersCommunicatedWith(int userId) {
        List<UserEntity> usersCommunicatedWith = messageDao.findUsersCommunicatedWith(userId);
        List<UserPartialDto> userPartialDtos = new ArrayList<>();

        for (UserEntity userEntity : usersCommunicatedWith) {
            UserPartialDto userPartialDto = UserMapper.convertUserEntityToUserPartialDto(userEntity);
            userPartialDtos.add(userPartialDto);
        }

        return userPartialDtos;
    }

    public MessageDto addMessageChat(MessageDto message) {
        try {
            // Fetch sender and recipient from database (assuming userDao is available)
            UserEntity sender = userDao.findUserById(message.getSender());
            UserEntity recipient = userDao.findUserById(message.getRecipient());

            if (sender == null || recipient == null) {
                throw new IllegalArgumentException("Sender or recipient not found");
            }

            // Create a new MessageEntity from the MessageDto
            MessageEntity messageEntity = MessageMapper.convertMessageDtoToMessageEntity(message);
            messageEntity.setSender(sender);
            messageEntity.setRecipient(recipient);
            messageEntity.setRead(false);
            messageEntity.setTimestamp(LocalDateTime.now());
            messageEntity.setId(generateIdDataBase());

            // Persist the messageEntity to the database
            messageDao.persist(messageEntity);

            // Convert the persisted MessageEntity back to MessageDto

            return MessageMapper.convertMessageEntityToMessageDto(messageEntity);
        } catch (Exception e) {
            e.printStackTrace(); // Log or handle the exception appropriately
            return null; // Return null or throw a custom exception if desired
        }
    }

    //mock
    public boolean markMessagesAsSeenBefore(int messageId) {
        try {
            // Retrieve the message by its ID to get its timestamp
            MessageEntity targetMessage = messageDao.findMessageById(messageId);

            if (targetMessage == null) {
                // Target message not found
                return false;
            }

            int receiverId = targetMessage.getRecipient().getId();
            int senderId = targetMessage.getSender().getId();
            LocalDateTime targetTimestamp = targetMessage.getTimestamp();

            // Retrieve messages with timestamps earlier than the target message for specific users
            List<MessageEntity> messagesToUpdate = messageDao.findMessagesBeforeTimestampForUsers(senderId, receiverId, targetTimestamp);
            if (messagesToUpdate.isEmpty()) {
                // No messages to update
                return false;
            }

            // Update the read status of each message to indicate it has been seen
            for (MessageEntity message : messagesToUpdate) {
                message.setRead(true);
                messageDao.merge(message); // Persist the updated message
            }

            return true; // Messages successfully marked as seen
        } catch (Exception e) {
            // Handle any exceptions or errors
            e.printStackTrace(); // Log the exception for debugging
            return false;
        }
    }

    //mock
    public List<MessageDto> getUnreadMessagesForUser(int userId) {
        List<MessageEntity> unreadMessageEntities = messageDao.findUnreadMessagesByRecipientId(userId);
        List<MessageDto> unreadMessages = new ArrayList<>();

        for (MessageEntity messageEntity : unreadMessageEntities) {
            // Convert MessageEntity to MessageDto using mapper
            MessageDto messageDto = MessageMapper.convertMessageEntityToMessageDto(messageEntity);
            unreadMessages.add(messageDto);
        }

        return unreadMessages;
    }
}
