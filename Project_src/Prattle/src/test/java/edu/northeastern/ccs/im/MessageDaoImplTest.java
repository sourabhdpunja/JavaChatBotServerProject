package edu.northeastern.ccs.im;

import edu.northeastern.ccs.im.message.MessageDao;
import edu.northeastern.ccs.im.message.MessageDaoImpl;
import net.gpedro.integrations.slack.SlackApi;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * The type Message dao impl test.
 *
 * @author Sourabh Punja on 11/11/2018
 */
public class MessageDaoImplTest {
    private static final String SQL_MESSAGE_EXCEPTION = "SQL Exception in MessageDaoImpl";

    /**
     * Test create and delete messages for user and group.
     */
    @Test
    public void testCreateAndDeleteMessage() {
        MessageDao messageDaoImplTest = new MessageDaoImpl();

        /**
         * Clear anuy invalid messages from previous tests.
         */
        int clear = 1;
        while (clear != 0) {
            clear = messageDaoImplTest.deleteMessageByUsername("sender");
        }

        Message message =  Message.makeMessage("BCT", "sender", "Message Text");
        int result = messageDaoImplTest.createMessage(message);
        assertEquals( 1, result);

        message = Message.makeDirectMessage( "Rak", "Directed Message Text");
        message.setMsgReceiver("Oz");
        assertEquals( 1, messageDaoImplTest.createUserAndGroupMessage(message));

        String username = "sender";
        List<Message> actualMessages = messageDaoImplTest.findMessageByUsername(username);
        List<Message> actualMessagesForSender = messageDaoImplTest.findMessageByUsernameAndUnSeen(username);
        List<Message> expectedMessages = new ArrayList<>();
        assertEquals( expectedMessages, actualMessagesForSender);
        expectedMessages.add(Message.makeMessage("BCT", "sender", "Message Text"));
        result = messageDaoImplTest.updateMessagesToSeen(username);
        assertEquals( 0, result);
        assertEquals( expectedMessages, actualMessages);
        assertEquals( 1, messageDaoImplTest.deleteMessageByUsername("sender"));

        String[] tokens = new String[20];
        tokens[2] = "punjas";
        tokens[4] = "@punjas I am angiman";
        List<Message> actualMessagesReceiver = messageDaoImplTest.findMessagesByReceiverAndText(tokens,"Sou");
        assertTrue(actualMessagesReceiver.size() > 0);
    }

    /**
     * Test delete query exceptions.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testDeleteQueryExceptions() throws SQLException {
        MessageDao messageDaoImpl = new MessageDaoImpl();
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        Logger logger = mock(Logger.class);
        ResultSet rs = mock(ResultSet.class);
        messageDaoImpl.setRs(rs);
        messageDaoImpl.setStatement(statement);
        messageDaoImpl.setConnection(connection);
        MessageDaoImpl.setLogger(logger);
        when(statement.executeUpdate()).thenThrow(SQLException.class);
        messageDaoImpl.deleteMessageByUsername("sou");
        final String SQL_MESSAGE_EXCEPTION = "SQL Exception";
        verify(logger, times(0)).error(SQL_MESSAGE_EXCEPTION);

        messageDaoImpl.setRs(rs);
        messageDaoImpl.setStatement(statement);
        messageDaoImpl.setConnection(connection);
        messageDaoImpl.deleteMessageByGroupname("team212");
        verify(logger, times(0)).error(SQL_MESSAGE_EXCEPTION);
        
        String[] attributes = new String[] {"/search","sender","Rake", "receiver", "Oz",
        		"fromTime", "2018-11-24 20:47:21","toTime", "2018-11-24 20:47:21"};
        messageDaoImpl.setRs(rs);
        messageDaoImpl.setStatement(statement);
        messageDaoImpl.setConnection(connection);
        MessageDaoImpl.setLogger(logger);
        when(statement.executeQuery()).thenThrow(SQLException.class);
        messageDaoImpl.searchMessageByAttributes(attributes);
        verify(logger, times(1)).error(this.SQL_MESSAGE_EXCEPTION);
    }

    /**
     * Test find query exceptions.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testFindQueryException() throws SQLException {
        MessageDao messageDaoImpl = new MessageDaoImpl();
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        Logger logger = mock(Logger.class);
        ResultSet rs = mock(ResultSet.class);
        messageDaoImpl.setRs(rs);
        messageDaoImpl.setStatement(statement);
        messageDaoImpl.setConnection(connection);
        MessageDaoImpl.setLogger(logger);
        when(statement.executeQuery()).thenThrow(SQLException.class);
        messageDaoImpl.findMessageByUsername("sou");
        verify(logger, times(1)).error(SQL_MESSAGE_EXCEPTION);

        messageDaoImpl.setRs(rs);
        messageDaoImpl.setStatement(statement);
        messageDaoImpl.setConnection(connection);
        messageDaoImpl.findMessageByGroupname("team212");
        verify(logger, times(2)).error(SQL_MESSAGE_EXCEPTION);
    }

    /**
     * Test create user exceptions.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testCreateUserException() throws SQLException {
        Message message =  Message.makeMessage("BCT", "sender", "Message Text");
        MessageDao messageDaoImpl = new MessageDaoImpl();
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        Logger logger = mock(Logger.class);
        ResultSet rs = mock(ResultSet.class);
        messageDaoImpl.setRs(rs);
        messageDaoImpl.setStatement(statement);
        messageDaoImpl.setConnection(connection);
        MessageDaoImpl.setLogger(logger);
        when(statement.executeUpdate()).thenThrow(SQLException.class);
        messageDaoImpl.createUserAndGroupMessage(message);
        final String SQL_MESSAGE_EXCEPTION = "SQL Exception";
        verify(logger, times(0)).error(SQL_MESSAGE_EXCEPTION);

        messageDaoImpl.setRs(rs);
        messageDaoImpl.setStatement(statement);
        messageDaoImpl.setConnection(connection);
        messageDaoImpl.createUserAndGroupMessage(message);
        verify(logger, times(0)).error(SQL_MESSAGE_EXCEPTION);
    }

    /**
     * Test create message exceptions.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testCreateMessageException() throws SQLException {
        Message message =  Message.makeMessage("BCT", "sender", "Message Text");
        MessageDao messageDaoImpl = new MessageDaoImpl();
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        Logger logger = mock(Logger.class);
        ResultSet rs = mock(ResultSet.class);
        messageDaoImpl.setRs(rs);
        messageDaoImpl.setStatement(statement);
        messageDaoImpl.setConnection(connection);
        MessageDaoImpl.setLogger(logger);
        when(statement.executeUpdate()).thenThrow(SQLException.class);
        messageDaoImpl.createMessage(message);
        verify(logger, times(1)).error(SQL_MESSAGE_EXCEPTION);
    }
    
    /**
     * testing search by attribute
     * @throws ParseException
     */
    @Test
    public void testSearchByAttributes() throws ParseException {
        MessageDao messageDaoImplTest = new MessageDaoImpl();
        Message mess = null;
        Message message = Message.makeUserMessage( "searchRaki", "Directed Text");
        message.setMsgReceiver("punja");
        messageDaoImplTest.createUserAndGroupMessage(message);
        List<Message> listMessages = messageDaoImplTest.findMessageByUsername("searchRaki");
        if(!listMessages.isEmpty()) {
        	mess = listMessages.get(0);
        }
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss").format(new java.util.Date());
        String[] attributes = new String[] {"/search","sender","searchRaki", "receiver", "punja",
        		"fromTime", mess.getCreatedTimestamp(),"toTime", mess.getCreatedTimestamp()};
        List<Message> messages = messageDaoImplTest.searchMessageByAttributes(attributes);
        //assertTrue(messages.size() > 0);
        messageDaoImplTest.deleteMessageByUsername("searchRaki");
    }
    /**
     * testing search by attribute
     * @throws ParseException
     */
    
    @Test
    public void testSearchByAttributes1() throws ParseException {
        MessageDao messageDaoImplTest = new MessageDaoImpl();
        Message mess = null;
        Message message = Message.makeUserMessage( "searchRaki", "Directed Text");
        message.setMsgReceiver("punja");
        messageDaoImplTest.createUserAndGroupMessage(message);
        List<Message> listMessages = messageDaoImplTest.findMessageByUsername("searchRaki");
        if(!listMessages.isEmpty()) {
        	mess = listMessages.get(0);
        }
        String[] attributes = new String[] {"/search","sender","searchRaki", "receiver", "punja",
        		"fromTime", mess.getCreatedTimestamp()};
        List<Message>messages = messageDaoImplTest.searchMessageByAttributes(attributes);
        assertTrue(messages.size() > 0);
        messageDaoImplTest.deleteMessageByUsername("searchRaki");

    }
    
    /**
     * testing search by attribute with invalid Time stamp
     * @throws ParseException
     */
    @Test
    public void testSearchByAttributesInvalidTS() throws ParseException {
        MessageDao messageDaoImplTest = new MessageDaoImpl();
        String[] attributes = new String[] {"/search","sender","Rake", "receiver", "Oz",
        		"fromTime", "2018-11-24- 20:47:21"};

    }
    
    /**
     * tests recall message
     */
    @Test
    public void testRecallMessage() {
        MessageDao messageDaoImplTest = new MessageDaoImpl();
        Message mess = null;
        Message message = Message.makeUserMessage( "searchRaki", "Directed Text");
        message.setMsgReceiver("punja");
        messageDaoImplTest.createUserAndGroupMessage(message);
    	Message msg = messageDaoImplTest.findSendersLastMessage("searchRaki");
    	assertTrue(messageDaoImplTest.recallMessage(msg));
    	messageDaoImplTest.deleteMessageByUsername("searchRaki");
    }
}