package aor.paj.bean;

import aor.paj.dao.MessageDao;
import aor.paj.dao.NotificationDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.MessageDto;
import aor.paj.dto.UserPartialDto;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.NotificationEntity;
import aor.paj.entity.UserEntity;
import aor.paj.mapper.MessageMapper;
import aor.paj.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageBeanTest {

    @Mock
    private UserDao userDao;

    @Mock
    private NotificationDao notificationDao;

    @Mock
    private MessageDao messageDao;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private MessageBean messageBean;

    @Test
    void givenValidMessageDto_whenAddMessage_thenReturnTrue() {
        // Given
        int senderId = 1;
        int recipientId = 2;
        String messageContent = "Message Content";

        // Mock UserEntities for sender and recipient
        UserEntity sender = new UserEntity();
        sender.setId(senderId);

        UserEntity recipient = new UserEntity();
        recipient.setId(recipientId);

        MessageDto messageDto = new MessageDto();
        messageDto.setSender(senderId);
        messageDto.setRecipient(recipientId);
        messageDto.setContent(messageContent);

        // Mock userDao to return sender and recipient when findById is called
        when(userDao.findUserById(senderId)).thenReturn(sender);
        when(userDao.findUserById(recipientId)).thenReturn(recipient);

        // When
        boolean messageAdded = messageBean.addMessage(messageDto);

        // Then
        assertTrue(messageAdded); // Message should be successfully added

        // Capture the argument passed to messageDao.persist
        ArgumentCaptor<MessageEntity> messageEntityCaptor = ArgumentCaptor.forClass(MessageEntity.class);
        verify(messageDao).persist(messageEntityCaptor.capture());

        // Retrieve the captured MessageEntity
        MessageEntity capturedMessageEntity = messageEntityCaptor.getValue();
        assertNotNull(capturedMessageEntity);

        // Assert fields of the captured MessageEntity
        assertEquals(sender, capturedMessageEntity.getSender());
        assertEquals(recipient, capturedMessageEntity.getRecipient());
        assertEquals(messageContent, capturedMessageEntity.getContent());
        assertFalse(capturedMessageEntity.isRead()); // Assuming default is unread
        assertNotNull(capturedMessageEntity.getTimestamp()); // Timestamp should be set
    }

    @Test
    public void testGetAllUsersCommunicatedWith() {
        // Given
        int userId = 1;

        // Mock the list of UserEntity objects returned by messageDao
        UserEntity user1 = new UserEntity();
        user1.setId(2);
        user1.setUsername("Alice");
        user1.setPhotoURL("x");

        UserEntity user2 = new UserEntity();
        user2.setId(3);
        user2.setUsername("Bob");
        user2.setPhotoURL("y");

        List<UserEntity> usersCommunicatedWith = Arrays.asList(user1, user2);

        // Manually create UserPartialDto objects
        UserPartialDto userPartialDto1 = new UserPartialDto();
        userPartialDto1.setUserId(2);
        userPartialDto1.setUsername("Alice");
        userPartialDto1.setPhotoUrl("x");

        UserPartialDto userPartialDto2 = new UserPartialDto();
        userPartialDto2.setUserId(3);
        userPartialDto2.setUsername("Bob");
        userPartialDto2.setPhotoUrl("y");

        List<UserPartialDto> expectedUserPartialDtos = Arrays.asList(userPartialDto1, userPartialDto2);

        // Stub messageDao method to return the list of users communicated with
        when(messageDao.findUsersCommunicatedWith(userId)).thenReturn(usersCommunicatedWith);

        // When
        List<UserPartialDto> actualUserPartialDtos = messageBean.getAllUsersCommunicatedWith(userId);

        // Then
        assertNotNull(actualUserPartialDtos);
        assertEquals(expectedUserPartialDtos.size(), actualUserPartialDtos.size());
        for (int i = 0; i < expectedUserPartialDtos.size(); i++) {
            assertEquals(expectedUserPartialDtos.get(i), actualUserPartialDtos.get(i));
        }

        // Verify that messageDao method was called with the correct argument
        verify(messageDao).findUsersCommunicatedWith(userId);
    }
}


