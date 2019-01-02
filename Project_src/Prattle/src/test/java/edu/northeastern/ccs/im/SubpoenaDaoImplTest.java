package edu.northeastern.ccs.im;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.message.MessageDao;
import edu.northeastern.ccs.im.message.MessageDaoImpl;
import edu.northeastern.ccs.im.message.SubpoenaDao;
import edu.northeastern.ccs.im.message.SubpoenaDaoImpl;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Subpoena Dao Impl Tests
 * @author Sourabh Punja on 11/30/2018
 */
class SubpoenaDaoImplTest {
    private static final String SQL_MESSAGE_EXCEPTION = "SQL Exception in SubpoenaDaoImpl";

    @Test
    void createMessageAndUpdateSubpoenaUser() {
        SubpoenaDao subpoenaDao = new SubpoenaDaoImpl();
        Message message =  Message.makeMessage("DMS", "sender", "Message Text");
        message.setSubpoenaUser("subpoena");
        message.setMsgReceiver("punj");
        message.setMessageSeen(false);
        int result = subpoenaDao.createMessage(message);
        assertEquals( 1, result);
        List<Message> unseen = subpoenaDao.findMessageUnSeen("subpoena");
        List<Message> unseenExpected = new ArrayList<>();
        unseenExpected.add(message);
        assertEquals(unseenExpected.get(0),unseen.get(0));
        result = subpoenaDao.updateMessagesToSeen("subpoena");
        assertEquals(1, result);
        message.setMessageId(338);
        assertEquals(false, ((SubpoenaDaoImpl) subpoenaDao).recallMessage(message));
    }

    /**
     * Test find query exceptions.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testFindQueryException() throws SQLException {
        SubpoenaDao subpoenaDao = new SubpoenaDaoImpl();
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        Logger logger = mock(Logger.class);
        ResultSet rs = mock(ResultSet.class);
        subpoenaDao.setRs(rs);
        subpoenaDao.setStatement(statement);
        subpoenaDao.setConnection(connection);
        SubpoenaDaoImpl.setLogger(logger);
        when(statement.executeQuery()).thenThrow(SQLException.class);
        subpoenaDao.findMessageUnSeen("sou");
        verify(logger, times(1)).error(SQL_MESSAGE_EXCEPTION);

        subpoenaDao.setRs(rs);
        subpoenaDao.setStatement(statement);
        subpoenaDao.setConnection(connection);
        subpoenaDao.findMessageUnSeen("team212");
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
        SubpoenaDao subpoenaDao = new SubpoenaDaoImpl();
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        Logger logger = mock(Logger.class);
        ResultSet rs = mock(ResultSet.class);
        subpoenaDao.setRs(rs);
        subpoenaDao.setStatement(statement);
        subpoenaDao.setConnection(connection);
        SubpoenaDaoImpl.setLogger(logger);
        when(statement.executeUpdate()).thenThrow(SQLException.class);
        subpoenaDao.createMessage(message);
        final String SQL_MESSAGE_EXCEPTION = "SQL Exception";
        verify(logger, times(0)).error(SQL_MESSAGE_EXCEPTION);

        subpoenaDao.setRs(rs);
        subpoenaDao.setStatement(statement);
        subpoenaDao.setConnection(connection);
        subpoenaDao.createMessage(message);
        verify(logger, times(0)).error(SQL_MESSAGE_EXCEPTION);
    }

    /**
     * Test create user exceptions.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testUpdateUserException() throws SQLException {
        SubpoenaDao subpoenaDao = new SubpoenaDaoImpl();
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        Logger logger = mock(Logger.class);
        ResultSet rs = mock(ResultSet.class);
        subpoenaDao.setRs(rs);
        subpoenaDao.setStatement(statement);
        subpoenaDao.setConnection(connection);
        SubpoenaDaoImpl.setLogger(logger);
        when(statement.executeUpdate()).thenThrow(SQLException.class);
        subpoenaDao.updateMessagesToSeen("subpoena");
        final String SQL_MESSAGE_EXCEPTION = "SQL Exception";
        verify(logger, times(0)).error(SQL_MESSAGE_EXCEPTION);

        subpoenaDao.setRs(rs);
        subpoenaDao.setStatement(statement);
        subpoenaDao.setConnection(connection);
        subpoenaDao.updateMessagesToSeen("subpoena");
        verify(logger, times(0)).error(SQL_MESSAGE_EXCEPTION);
    }

    /**
     * Test update messages to recall exceptions.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testUpdateRecallException() throws SQLException {
        SubpoenaDao subpoenaDao = new SubpoenaDaoImpl();
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        Logger logger = mock(Logger.class);
        ResultSet rs = mock(ResultSet.class);
        subpoenaDao.setRs(rs);
        subpoenaDao.setStatement(statement);
        subpoenaDao.setConnection(connection);
        SubpoenaDaoImpl.setLogger(logger);
        when(statement.executeUpdate()).thenThrow(SQLException.class);
        ((SubpoenaDaoImpl) subpoenaDao).updateMessagesToRecalled(123);
        final String SQL_MESSAGE_EXCEPTION = "SQL Exception";
        verify(logger, times(0)).error(SQL_MESSAGE_EXCEPTION);
    }

}