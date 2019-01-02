package edu.northeastern.ccs.im.message;

import edu.northeastern.ccs.im.Message;
import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Implementation of CRUD operations of Message DAO layer.
 *
 * @author Sourabh Punja on 11/11/2018
 */
public class MessageDaoImpl extends BaseDaoImpl implements MessageDao{
    private static final String CREATE_MESSAGE= "INSERT INTO messages (text,sender,handle,sender_ip_address,receiver_ip_address) VALUES (?,?,?,?,?)";
    private static final String CREATE_USER_MESSAGE= "INSERT INTO messages (text,sender,receiver,groupname,handle,isSeen,sender_ip_address,receiver_ip_address) "
    		+ "VALUES (?,?,?,?,?,?,?,?)";
    private static final String FIND_MESSAGES_BY_USERNAME = "SELECT * FROM messages where messages.sender=?";
    private static final String FIND_MESSAGES_BY_GROUPNAME = "SELECT * FROM messages where messages.groupname=?";
    private static final String FIND_UNSEEN_MESSAGES_BY_USERNAME = "SELECT * FROM messages where messages.receiver=? AND messages.isSeen=False ORDER BY created_at_timestamp";
    private static final String UPDATE_MESSAGE_TO_SEEN = "UPDATE messages SET messages.isSeen=True where messages.receiver=? AND messages.isSeen=False";
    private static final String DELETE_MESSAGES_FOR_USERNAME ="DELETE from messages where messages.sender=?";
    private static final String DELETE_MESSAGES_FOR_GROUPNAME ="DELETE from messages where messages.groupname=?";

    private static final String SQL_MESSAGE_EXCEPTION = "SQL Exception in MessageDaoImpl";
    private static final String FIND_MESSAGES_BY_ATTR_COMMON_QUERY = "SELECT * FROM messages where ";
    private static final String FIND_SENDERS_LAST_MESSAGE = "select * from messages where sender = ? order by created_at_timestamp DESC limit 1";
    private static final String UPDATE_MESSAGE_AS_RECALLED = "update messages set isRecalled = true where id = ? ";
    private static final String HANDLE = "handle";
    private static final String SENDER = "sender";
    private static final String RECEIVER = "receiver";
    private static final String GROUPNAME = "groupname";
    private static final String FIND_MESSAGES_BY_TEXT= "SELECT * FROM messages where sender = ? and receiver = ? and text = ?";
    private static final String SENDER_IP_ADDRESS = "sender_ip_address";
    private static final String RECEIVER_IP_ADDRESS = "receiver_ip_address";

    private static Logger logger = LogManager.getLogger(MessageDaoImpl.class);
    /** The slack api. */
    SlackApi slackApi = new SlackApi("https://hooks.slack.com/services/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");

    /**
     * Sets logger.
     *
     * @param logger the logger
     */
    public static void setLogger(final Logger logger) {
        MessageDaoImpl.logger = logger;
    }

    /**
     * Persist message in RDS.
     *
     * @param message the message to be persisted
     * @return the int denoting if the message has been added.
     */
    public int createMessage(Message message) {
        int result = -1;
        int messageId = 0;
        try{
            if (connection == null) {
                connection = ConnectionFactory.getConnection();
            }
            if (statement == null) {
                statement = connection.prepareStatement(CREATE_MESSAGE, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, message.getText());
                statement.setString(2, message.getName());
                statement.setString(3, message.getMsgHandle());
                statement.setString(4, message.getSenderIpAddress());
                statement.setString(5, message.getReceiverIpAddress());
            }
            result = statement.executeUpdate();
            rs = statement.getGeneratedKeys();
            while(rs.next()) {
                messageId = rs.getInt(1);
            }
            message.setMessageId(messageId);
        } catch (SQLException ex) {
            slackApi.call(new SlackMessage("SQL Exception", "SQL Exception when creating messages in messasge DB"));
            logger.error(SQL_MESSAGE_EXCEPTION);
        } finally {
            finallyBlockHandle();
        }
        return result;
    }

    /**
     * Persist message in RDS.
     *
     * @param message the user message to be persisted.
     * @return the int denoting if the message has been added.
     */
    public int createUserAndGroupMessage(Message message) {
        int result = -1;
        result = createMessageUserAndGroups(message, CREATE_USER_MESSAGE);
        return result;
    }

    /**
     * Find all messages by username.
     *
     * @param userName the user name
     * @return the list of messages for the user
     */
    public List<Message> findMessageByUsername(String userName) {
        ArrayList<Message> messages = new ArrayList<>();
        getMessagesFromQuery(messages, userName, FIND_MESSAGES_BY_USERNAME);
        return messages;
    }

    /**
     * Find all messages by username and isSeen False.
     *
     * @param userName the user name
     * @return the list of messages for the user
     */
    public List<Message> findMessageByUsernameAndUnSeen(String userName) {
        ArrayList<Message> messages = new ArrayList<>();
        getMessagesFromQuery(messages, userName, FIND_UNSEEN_MESSAGES_BY_USERNAME);
        return messages;
    }

    /**
     * Find all messages by groupname.
     *
     * @param groupName the group name
     * @return the list of messages for the group
     */
    public List<Message> findMessageByGroupname(String groupName) {
        ArrayList<Message> messages = new ArrayList<>();
        getMessagesFromQuery(messages, groupName, FIND_MESSAGES_BY_GROUPNAME);
        return messages;
    }

    /**
     * Get meesages for groups or users.
     * @param messages handle to add the messages obtained from the query.
     * @param name groupname/username
     * @param command sql command for fetching group or user message FIND_MESSAGES_BY_USERNAME/FIND_MESSAGES_BY_GROUPNAME
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
                int messageId= rs.getInt("id");
                String handle = rs.getString(HANDLE);
                String text = rs.getString("text");
                String sender = rs.getString(SENDER);
                String receiver = rs.getString(RECEIVER);
                String groupname = rs.getString(GROUPNAME);
                String createdTimestamp = rs.getString("created_at_timestamp");
                int isRecalled = rs.getInt("isRecalled");
                Message message = Message.makeMessage(handle, sender, text);
                message.setSenderIpAddress(rs.getString(SENDER_IP_ADDRESS));
                message.setReceiverIpAddress(rs.getString(RECEIVER_IP_ADDRESS));
                message.setMessageId(messageId);
                message.setMsgReceiver(receiver);
                message.setMsgGroup(groupname);
                message.setCreatedTimestamp(createdTimestamp);
                message.setRecalled(((isRecalled == 1) ? true : false));
                messages.add(message);
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
     * Update messages by username to Seen.
     *
     * @param username the user name
     * @return the int number of records deleted
     */
    public int updateMessagesToSeen(String username) {
        int result = -1;
        result = queryFromMessages(username, UPDATE_MESSAGE_TO_SEEN);
        return result;
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
                statement = connection.prepareStatement(UPDATE_MESSAGE_AS_RECALLED);
                statement.setInt(1, messageId);
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
     * Delete messages by username.
     *
     * @param userName the user name
     * @return the int number of records deleted
     */
    public int deleteMessageByUsername(String userName) {
        int result = -1;
        result = queryFromMessages(userName, DELETE_MESSAGES_FOR_USERNAME);
        return result;
    }

    /**
     * Delete messages by groupname.
     *
     * @param groupName the group name
     * @return the int number of records deleted
     */
    public int deleteMessageByGroupname(String groupName) {
        int result = -1;
        result = queryFromMessages(groupName, DELETE_MESSAGES_FOR_GROUPNAME);
        return result;
    }
    
    /**
     * Search message by attributes.
     *
     * @param attributes the attributes
     * @return the list
     */
    public List<Message> searchMessageByAttributes(String[] attributes) {
        ArrayList<Message> messages = new ArrayList<>();
        int counter = 0;
		StringBuilder query = new StringBuilder(FIND_MESSAGES_BY_ATTR_COMMON_QUERY);
		counter = searchQueryFromAttributes(attributes, counter, query);
		//SELECT * FROM messages where sender = 'sender' and receiver = 'rak'
        try{
            if (connection == null) {
                connection = ConnectionFactory.getConnection();
            }
            prepareStatementFromSearchAttributes(attributes, counter, query);
            rs = statement.executeQuery();
            while(rs.next()) {
                int msgId= rs.getInt("id");
                String rec = rs.getString(RECEIVER);
                String msgHandle = rs.getString(HANDLE);
                String msgText = rs.getString("text");
                String msgSender = rs.getString(SENDER);
                String gname = rs.getString(GROUPNAME);
                Message msg = Message.makeMessage(msgHandle, msgSender, msgText);
                msg.setSenderIpAddress(rs.getString(SENDER_IP_ADDRESS));
                msg.setReceiverIpAddress(rs.getString(RECEIVER_IP_ADDRESS));
                msg.setMessageId(msgId);
                msg.setMsgReceiver(rec);
                msg.setMsgGroup(gname);
                messages.add(msg);
            }
        } catch (SQLException ex) {
            logger.error(SQL_MESSAGE_EXCEPTION);
        } finally {
             finallyBlockHandle();
        }
        return messages;
    }

	/**
	 * Prepare statement from search attributes.
	 *
	 * @param attributes the attributes
	 * @param counter the counter
	 * @param query the query
	 * @return the string builder
	 * @throws SQLException the SQL exception
	 */
	private StringBuilder prepareStatementFromSearchAttributes(String[] attributes, int counter, StringBuilder query)
			throws SQLException {
		Timestamp ts = null;
		if (statement == null) {
		    statement = connection.prepareStatement(query.toString());
		    for(int i=1; i<= (counter/2); i++) {
		    	int tsCounter = (i*2)-1;
		    	if(attributes[tsCounter].equalsIgnoreCase("fromTime") || attributes[tsCounter].equalsIgnoreCase("toTime")) {
					ts = getDateFromString(attributes, i, ts);
		    	}else {
		    			statement.setString(i, attributes[i*2]);
		    	}
		    }
		}
		return query;
	}

    /**
     * Helper method to set timestamp from the date
     * @param attributes the attributes for search
     * @param i counter value
     * @param ts Timestamp
     * @return Timestamp from the attributes
     * @throws SQLException
     */
	private Timestamp getDateFromString(String[] attributes, int i, Timestamp ts) throws SQLException {
        Date dates = null;
        try {
            dates = getTimeStampFromString(attributes[i*2]);
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }
        if(dates != null) {
            ts = new java.sql.Timestamp(dates.getTime());
        }
        statement.setTimestamp(i, ts);
        return ts;
    }

	/**
	 * Search query from attributes.
	 *
	 * @param attributes the attributes
	 * @param counter the counter
	 * @param query the query
	 * @return the int
	 */
	private int searchQueryFromAttributes(String[] attributes, int counter, StringBuilder query) {
		for(int i=1; i< attributes.length; i = i+2) {
			if(attributes[i].equalsIgnoreCase("fromTime")) {
				query.append(" created_at_timestamp >= ?");
				counter = counter + 2;
			}else if(attributes[i].equalsIgnoreCase("toTime")) {
				query.append(" created_at_timestamp <= ?");
				counter = counter + 2;
			}else {
				query.append(attributes[i]+" = ?");
				counter = counter + 2;
			}			
			if(i < attributes.length -2) {
				query.append(" and ");
			}
		}
		return counter;
	}
    
    /**
     * Gets the time stamp from string.
     *
     * @param string the string
     * @return the time stamp from string
     * @throws ParseException the parse exception
     */
    public Date getTimeStampFromString(String string) throws ParseException {
    	String modified = string.replace(",", " ");
    	 SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    	 format.setTimeZone(TimeZone.getTimeZone("UTC"));
    	return format.parse(modified);
    }
    
    /**
     * Find senders last message.
     *
     * @param senderName the sender name
     * @return the message
     */
    public Message findSendersLastMessage(String senderName) {
    	Message message = null;
        try{
            if (connection == null) {
                connection = ConnectionFactory.getConnection();
            }
            if (statement == null) {
                statement = connection.prepareStatement(FIND_SENDERS_LAST_MESSAGE);
                statement.setString(1, senderName);
            }
            rs = statement.executeQuery();
            while(rs.next()) {
                int mId= rs.getInt("id");
                String handleName = rs.getString(HANDLE);
                String txt = rs.getString("text");
                String sendersName = rs.getString(SENDER);
                String receiverName = rs.getString(RECEIVER);
                String grpname = rs.getString(GROUPNAME);
                int isSeen = rs.getInt("isSeen");
                message = Message.makeMessage(handleName, sendersName, txt);
                message.setSenderIpAddress(rs.getString(SENDER_IP_ADDRESS));
                message.setReceiverIpAddress(rs.getString(RECEIVER_IP_ADDRESS));
                message.setMessageId(mId);
                message.setMsgReceiver(receiverName);
                message.setMsgGroup(grpname);
                message.setMessageSeen(((isSeen == 1) ? true : false));
            }
        } catch (SQLException exception) {
        	logger.error(SQL_MESSAGE_EXCEPTION);
        } finally {
            finallyBlockHandle();
        }
        return message;
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
                statement.setString(1, sender);
                statement.setString(2, tokens[2]);
                statement.setString(3, tokens[4]);
            }
            rs = statement.executeQuery();
            while(rs.next()) {
                int mId= rs.getInt("id");
                String handleName = rs.getString(HANDLE);
                String txt = rs.getString("text");
                String sendersName = rs.getString(SENDER);
                String receiverName = rs.getString(RECEIVER);
                String grpname = rs.getString(GROUPNAME);
                int isSeen = rs.getInt("isSeen");
                Message message = Message.makeMessage(handleName, sendersName, txt);
                message.setMessageId(mId);
                message.setMsgReceiver(receiverName);
                message.setMsgGroup(grpname);
                message.setMessageSeen(((isSeen == 1) ? true : false));
                messages.add(message);
            }
        } catch (SQLException exception) {
            logger.error(SQL_MESSAGE_EXCEPTION);
        } finally {
            finallyBlockHandle();
        }
        return messages;


    }


}
