package edu.northeastern.ccs.im.message;

import edu.northeastern.ccs.im.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

/**
 * Instantiate the DB with RDS credentials
 *
 * @author Sourabh Punja on 11/11/2018
 */
public class BaseDaoImpl implements BaseDao{

    protected static Logger logger = LogManager.getLogger(BaseDaoImpl.class);
    protected Connection connection = null;
    protected PreparedStatement statement = null;
    protected ResultSet rs = null;
    private static final String SQL_MESSAGE_EXCEPTION = "SQL Exception";

    protected BaseDaoImpl() {
    }

    /**
     * sets the connection
     */
    @Override
    public void setConnection(final Connection connection) {
        this.connection = connection;
    }

    /**
     * sets statement
     */
    @Override
    public void setStatement(final PreparedStatement statement) {
        this.statement = statement;
    }

    /**
     * sets result set
     */
    @Override
    public void setRs(final ResultSet rs) {
        this.rs = rs;
    }

    /**
     * Helper method to delete messages in users and groups in RDS.
     * @param name groupname/username
     * @param command sql command for deleting messages from group or user DELETE_MESSAGES_FOR_USERNAME/DELETE_MESSAGES_FOR_GROUPNAME
     * @return number of records deleted in the RDS from the query.
     */
    protected int queryFromMessages(String name, String command) {
        int result = -1;
        try{
            if (connection == null) {
                connection = ConnectionFactory.getConnection();
            }
            if (statement == null) {
                statement = connection.prepareStatement(command);
                statement.setString(1, name);
            }
            result = statement.executeUpdate();
        } catch (SQLException e) {
            logger.error(SQL_MESSAGE_EXCEPTION);
        } finally {
            finallyBlockHandle();
        }
        return result;
    }

    /**
     * Helper method to persist messages in users and groups in RDS.
     * @param message message to be persisted
     * @param command sql command for persisting group or user message CREATE_GROUP_MESSAGE/CREATE_USER_MESSAGE
     * @return number of records added in the RDS from the query.
     */
    protected int createMessageUserAndGroups(Message message, String command) {
        int result = -1;
        int messageId = 0;
        try{
            if (connection == null) {
                connection = ConnectionFactory.getConnection();
            }
            if (statement == null) {
                statement = connection.prepareStatement(command, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, message.getText());
                statement.setString(2, message.getName());
                statement.setString(3, message.getMsgReceiver());
                statement.setString(4, message.getMsgGroup());
                statement.setString(5, message.getMsgHandle());
                statement.setBoolean(6, message.isMessageSeen());
                statement.setString(7, message.getSenderIpAddress());
                statement.setString(8, message.getReceiverIpAddress());
            }
            result = statement.executeUpdate();
            rs = statement.getGeneratedKeys();
            while(rs.next()) {
                messageId = rs.getInt(1);
            }
            message.setMessageId(messageId);
        } catch (SQLException e) {
            logger.error(SQL_MESSAGE_EXCEPTION);
        } finally {
            finallyBlockHandle();
        }
        return result;
    }

    /**
     * close Statement, Connection and ResultSet.
     */
    protected void finallyBlockHandle () {
        closeStatement();
        closeConnection();
        closeResultSet();
    }

    /**
     * Helper to close connection.
     */
    private void closeConnection() {
        if (this.connection != null) {
            try {
                this.connection.close();
                this.connection = null;
            } catch (SQLException e) {
                logger.error(SQL_MESSAGE_EXCEPTION);
            }
        }
    }

    /**
     * Helper to close statement.
     */
    private void closeStatement() {
        if (this.statement != null) {
            try {
                this.statement.close();
                this.statement = null;

            } catch (SQLException e) {
                logger.error(SQL_MESSAGE_EXCEPTION);
            }
        }
    }

    /**
     * Helper to close result set.
     */
    private void closeResultSet() {
        if (this.rs != null) {
            try {
                this.rs.close();
                this.rs = null;
            } catch (SQLException e) {
                logger.error(SQL_MESSAGE_EXCEPTION);
            }
        }
    }
}
