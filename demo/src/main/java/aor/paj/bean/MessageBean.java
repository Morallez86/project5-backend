package aor.paj.bean;

import aor.paj.dao.MessageDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.MessageDto;
import aor.paj.dto.UserDto;
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

    public long generateIdDataBase() {
        long id = 1;
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

    public List<MessageDto> getMessagesForUser(Long userId) {
        List<MessageEntity> messageEntities = messageDao.findMessagesByRecipientId(userId);
        List<MessageDto> messageDtos = new ArrayList<>();
        for(MessageEntity me : messageEntities){
            messageDtos.add(MessageMapper.convertMessageEntityToMessageDto(me));
        }
        return messageDtos;
    }

    public boolean addMessage(MessageDto message) {
        try {
            // Fetch sender and recipient from database (assuming userDao is available)
            UserEntity sender = userDao.findUserById(message.getSender());
            UserEntity recipient = userDao.findUserById(message.getRecipient());
            System.out.println(sender);
            System.out.println(recipient);

            MessageEntity messageEntity = MessageMapper.convertMessageDtoToMessageEntity(message);
            messageEntity.setSender(sender);
            messageEntity.setRecipient(recipient);
            messageEntity.setId(generateIdDataBase());
            messageEntity.setRead(false);
            messageEntity.setTimestamp(LocalDateTime.now());
            System.out.println(messageEntity);

            messageDao.persist(messageEntity);

            return true;
        } catch (Exception e) {
            e.printStackTrace(); // Log or handle the exception appropriately
            return false;
        }
    }

}
