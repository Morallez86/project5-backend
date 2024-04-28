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

    private MessageEntity messageEntity1;
    private MessageEntity messageEntity2;
    private UserEntity alice;
    private UserEntity bob;

    @BeforeEach
    void setUp() {
        // Initialize shared MessageEntity objects
        alice = new UserEntity();
        alice.setId(2);
        alice.setUsername("Alice");
        alice.setPhotoURL("x");

        bob = new UserEntity();
        bob.setId(3);
        bob.setUsername("Bob");
        bob.setPhotoURL("y");

        messageEntity1 = new MessageEntity();
        messageEntity1.setId(2);
        messageEntity1.setSender(bob);
        messageEntity1.setRecipient(alice);
        messageEntity1.setContent("Hi Alice!");
        messageEntity1.setTimestamp(LocalDateTime.now());
        messageEntity1.setRead(false);

        messageEntity2 = new MessageEntity();
        messageEntity2.setId(3);
        messageEntity2.setSender(alice);
        messageEntity2.setRecipient(bob);
        messageEntity2.setContent("Howdy!");
        messageEntity2.setTimestamp(LocalDateTime.now());
        messageEntity2.setRead(false);
    }

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

        List<UserEntity> usersCommunicatedWith = Arrays.asList(alice, bob);

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

    @Test
    public void testGetUnreadMessagesForUser() {
        // Given
        int userId = 1;



        UserEntity user3 = new UserEntity();
        user3.setId(userId);
        user3.setUsername("Charlie");
        user3.setPhotoURL("z");

        // Mock the list of MessageEntity objects returned by messageDao
        MessageEntity message1 = new MessageEntity();
        message1.setId(1);
        message1.setSender(alice);
        message1.setRecipient(bob);
        message1.setContent("Hello Bob!");
        message1.setTimestamp(LocalDateTime.now());
        message1.setRead(false);

        MessageEntity message2 = new MessageEntity();
        message2.setId(2);
        message2.setSender(user3);
        message2.setRecipient(bob);
        message2.setContent("How are you, Bob?");
        message2.setTimestamp(LocalDateTime.now());
        message2.setRead(false);

        List<MessageEntity> unreadMessageEntities = Arrays.asList(message1, message2);

        // Manually create MessageDto objects
        MessageDto messageDto1 = new MessageDto();
        messageDto1.setId(1);
        messageDto1.setSender(alice.getId());
        messageDto1.setContent("Hello Bob!");
        messageDto1.setRecipient(bob.getId());
        messageDto1.setTimestamp(LocalDateTime.now());
        messageDto1.setRead(false);
        MessageDto messageDto2 = new MessageDto();
        messageDto2.setId(2);
        messageDto2.setSender(user3.getId());
        messageDto2.setContent("How are you, Bob?");
        messageDto2.setRecipient(bob.getId());
        messageDto2.setTimestamp(LocalDateTime.now());
        messageDto2.setRead(false);

        List<MessageDto> expectedUnreadMessages = Arrays.asList(messageDto1, messageDto2);

        // Stub messageDao method to return the list of unread messages
        when(messageDao.findUnreadMessagesByRecipientId(userId)).thenReturn(unreadMessageEntities);

        // When
        List<MessageDto> actualUnreadMessages = messageBean.getUnreadMessagesForUser(userId);

        // Then
        assertNotNull(actualUnreadMessages);
        assertEquals(expectedUnreadMessages.size(), actualUnreadMessages.size());
        for (int i = 0; i < expectedUnreadMessages.size(); i++) {
            assertEquals(expectedUnreadMessages.get(i), actualUnreadMessages.get(i));
        }

        // Verify that messageDao method was called with the correct argument
        verify(messageDao).findUnreadMessagesByRecipientId(userId);
    }

    @Test
    public void testMarkMessagesAsSeenBefore() {
        // Given
        int messageId = 1;
        LocalDateTime targetTimestamp = LocalDateTime.now();

        MessageEntity targetMessage = new MessageEntity();
        targetMessage.setId(messageId);

        // Create sender and recipient using setters on empty constructors
        targetMessage.setSender(alice);
        targetMessage.setRecipient(bob);

        targetMessage.setTimestamp(targetTimestamp);

        // Mock findMessageById to return the target message
        when(messageDao.findMessageById(messageId)).thenReturn(targetMessage);

        List<MessageEntity> messagesToUpdate = Arrays.asList( messageEntity1, messageEntity2);
        when(messageDao.findMessagesBeforeTimestampForUsers(eq(alice.getId()), eq(bob.getId()), any(LocalDateTime.class)))
                .thenReturn(messagesToUpdate);

        // When
        boolean result = messageBean.markMessagesAsSeenBefore(messageId);

        // Then
        assertTrue(result); // Expect marking as seen to be successful

        // Verify that findMessageById was called with the correct argument
        verify(messageDao).findMessageById(messageId);

        // Verify that findMessagesBeforeTimestampForUsers was called with the correct arguments
        verify(messageDao).findMessagesBeforeTimestampForUsers(alice.getId(), bob.getId(), targetTimestamp);

        // Verify that merge was called for each message to update read status
        for (MessageEntity message : messagesToUpdate) {
            assertTrue(message.isRead()); // Assert that the message is now marked as read
            verify(messageDao).merge(message); // Verify that merge was called for each message
        }
    }

}


