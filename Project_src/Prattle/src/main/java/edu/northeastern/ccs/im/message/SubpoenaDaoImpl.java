package edu.northeastern.ccs.im.message;

import edu.northeastern.ccs.im.Message;
import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sourabh Punja on 11/25/2018
 */
public class SubpoenaDaoImpl extends BaseDaoImpl implements SubpoenaDao{
    private static final String CREATE_SUBPOENA_USER_MESSAGE= "INSERT INTO subpoena (text,sender,receiver,groupname,handle,isSeen,subpoenauser,sender_ip_address,receiver_ip_address) VALUES (?,?,?,?,?,?,?,?,?)";
    private static final String FIND_UNSEEN_MESSAGES_BY_USERNAME = "SELECT * FROM subpoena where subpoena.isSeen=False AND subpoena.subpoenauser=? ORDER BY created_at_timestamp";
    private static final String UPDATE_MESSAGE_TO_SEEN = "UPDATE subpoena SET subpoena.isSeen=True where subpoena.subpoenauser=? AND subpoena.isSeen=False";
    private static final String FIND_MESSAGES_BY_TEXT= "SELECT * FROM messages where sender = ? and receiver = ? and text = ?";
    private static final String UPDATE_RECALL_MESSAGE = "update messages set isRecalled = true where id = ? ";

    private static final String HANDLE = "handle";
    private static final String SENDER = "sender";
    private static final String RECEIVER = "receiver";
    private static final String GROUPNAME = "groupname";

    private static final String SENDER_IP_ADDRESS = "sender_ip_address";
    private static final String RECEIVER_IP_ADDRESS = "receiver_ip_address";


    private static final String SQL_MESSAGE_EXCEPTION = "SQL Exception in SubpoenaDaoImpl";

    private static Logger logger = LogManager.getLogger(MessageDaoImpl.class);
    /** The slack api. */
    SlackApi slackApi = new SlackApi("https://hooks.slack.com/services/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");

    /**
     * Sets logger.
     *
     * @param logger the logger
     */
    public static void setLogger(final Logger logger) {
        SubpoenaDaoImpl.logger = logger;
    }

    /**
     * Persist message in RDS.
     *
     * @param message the user message to be persisted.
     * @return the int denoting if the message has been added.
     */
    public int createMessage(Message message) {
        int result = -1;
        result = makeGroupsOrUsers(message, CREATE_SUBPOENA_USER_MESSAGE);
        return result;
    }

    /**
     * Method to makeGroupsOrUsers
     * @param message message
     * @param command sql command for CREATE_SUBPOENA_USER_MESSAGE
     * @return Records added in DB.
     */
    protected int makeGroupsOrUsers(Message message, String command) {
        int msgId = 0;
        int numberOfRecordsCreated = -1;
        try{
            if (connection == null) {
                connection = ConnectionFactory.getConnection();
            }
            if (statement == null) {
                statement = connection.prepareStatement(command, Statement.RETURN_GENERATED_KEYS);
                statement.setString(5, message.getMsgHandle());
                statement.setBoolean(6, message.isMessageSeen());
                statement.setString(7, message.getSubpoenaUser());
                statement.setString(2, message.getName());
                statement.setString(3, message.getMsgReceiver());
                statement.setString(1, message.getText());
                statement.setString(4, message.getMsgGroup());

                statement.setString(8, message.getSenderIpAddress());
                statement.setString(9, message.getReceiverIpAddress());
            }
            numberOfRecordsCreated = statement.executeUpdate();
            rs = statement.getGeneratedKeys();
            while(rs.next()) {
                msgId = rs.getInt(1);
            }
            message.setMessageId(msgId);
        } catch (SQLException e) {
            slackApi.call(new SlackMessage("SQL Exception", "SQL Exception when creating messages in messasge DB"));
            logger.error(SQL_MESSAGE_EXCEPTION);
        } finally {
            finallyBlockHandle();
        }
        return numberOfRecordsCreated;
    }

    /**
     * Find all messages by username and isSeen False.
     *
     * @param userName the user name
     * @return the list of messages for the user
     */
    public List<Message> findMessageUnSeen(String userName) {
        ArrayList<Message> messages = new ArrayList<>();
        getMessagesFromQuery(messages, userName, FIND_UNSEEN_MESSAGES_BY_USERNAME);
        return messages;
    }

    /**
     * Find users and group messages
     * @param messages handle to add the messages obtained from the query.
     * @param name groupname/username
     * @param command sql command for fetching group or user message FIND_UNSEEN_MESSAGES_BY_USERNAME
     * @return List of Messages obtained from the query.
     */
    private List<Message> getMessagesFromQuery(ArrayList<Message> messages, String name, String command) {
        try{
            if (connection == null) {
                connection = ConnectionFactory.getConnection();
            }
            if (statement == null) {
                statement = connection.prepareStatement(command);
                statement.setString(1, name);
            }
            rs = statement.executeQuery();
            while(rs.next()) {
                String subpoenauser = rs.getString("subpoenauser");
                int messageId= rs.getInt("id");
                String handle = rs.getString(HANDLE);
                String receiver = rs.getString(RECEIVER);
                String groupname = rs.getString(GROUPNAME);
                String text = rs.getString("text");
                String sender = rs.getString(SENDER);
                int isRecalled = rs.getInt("isRecalled");

                Message message = Message.makeMessage(handle, sender, text);
                message.setSenderIpAddress(rs.getString(SENDER_IP_ADDRESS));
                message.setReceiverIpAddress(rs.getString(RECEIVER_IP_ADDRESS));
                message.setMsgGroup(groupname);
                message.setSubpoenaUser(subpoenauser);
                message.setMessageId(messageId);
                message.setMsgReceiver(receiver);
                messages.add(message);
                message.setRecalled(((isRecalled == 1) ? true : false));
            }
        } catch (SQLException e) {
            slackApi.call(new SlackMessage("SQL Exception", "Not Able to get messages from messages table"));
            logger.error(SQL_MESSAGE_EXCEPTION);
        } finally {
            finallyBlockHandle();
        }
        return messages;
    }

    /**
     * Update messages.
     *
     * @param username user name
     * @return records updated
     */
    public int updateMessagesToSeen(String username) {
        int result = -1;
        result = queryFromMessages(username, UPDATE_MESSAGE_TO_SEEN);
        return result;
    }

    /**
     * Find messages by receiver and text.
     *
     * @param tokens the tokens
     * @param sender the sender
     * @return the list
     */
    public List<Message> findMessagesByReceiverAndText(String[] tokens, String sender){
        ArrayList<Message> messages = new ArrayList<>();
        try{
            if (connection == null) {
                connection = ConnectionFactory.getConnection();
            }
            if (statement == null) {
                statement = connection.prepareStatement(FIND_MESSAGES_BY_TEXT);
                statement.setString(2, tokens[2]);
                statement.setString(1, sender);
                statement.setString(3, tokens[4]);
            }
            rs = statement.executeQuery();
            while(rs.next()) {
                int mId= rs.getInt("id");
                String txt = rs.getString("text");
                String handleName = rs.getString(HANDLE);
                String sendersName = rs.getString(SENDER);
                String receiverName = rs.getString(RECEIVER);
                int isSeen = rs.getInt("isSeen");
                String grpname = rs.getString(GROUPNAME);

                Message msg = Message.makeMessage(handleName, sendersName, txt);
                msg.setMsgReceiver(receiverName);
                msg.setMessageId(mId);
                msg.setMsgGroup(grpname);
                msg.setMessageSeen(((isSeen == 1) ? true : false));
                messages.add(msg);
            }
        } catch (SQLException exception) {
            logger.error(SQL_MESSAGE_EXCEPTION);
        } finally {
            finallyBlockHandle();
        }
        return messages;
    }

    /**
     * Update messages to recalled.
     *
     * @param messageId the messageId
     * @return true, if successful
     */
    public boolean updateMessagesToRecalled(int messageId) {
        int result = -1;
        try{
            if (connection == null) {
                connection = ConnectionFactory.getConnection();
            }
            if (statement == null) {
                statement = connection.prepareStatement(UPDATE_RECALL_MESSAGE);
                int index = 1;
                statement.setInt(index, messageId);
            }
            result = statement.executeUpdate();
        } catch (SQLException e) {
            logger.error(SQL_MESSAGE_EXCEPTION);
        } finally {
            finallyBlockHandle();
        }
        return ((result ==1) ? true: false);
    }

    /**
     * Recall message.
     *
     * @param message the message
     * @return true, if successful
     */
    public boolean recallMessage(Message message) {
        return updateMessagesToRecalled(message.getMessageId());
    }

}
