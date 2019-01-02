package edu.northeastern.ccs.im.message;

import edu.northeastern.ccs.im.Message;

import java.util.List;

/**
 * The interface Message DAO layer.
 *
 * @author Sourabh Punja on 11/13/2018
 */
public interface MessageDao extends BaseDao{

    int createMessage(Message message);

    /**
     * Persist message in RDS.
     *
     * @param message the user message to be persisted.
     * @return the int denoting if the message has been added.
     */
    int createUserAndGroupMessage(Message message);

    /**
     * Find all messages by username and isSeen False.
     *
     * @param userName the user name
     * @return the list of messages for the user
     */
    List<Message> findMessageByUsernameAndUnSeen(String userName);

    /**
     * Find all messages by username.
     *
     * @param userName the user name
     * @return the list of messages for the user
     */
    List<Message> findMessageByUsername(String userName);


    /**
     * Find all messages by groupname.
     *
     * @param groupName the group name
     * @return the list of messages for the group
     */
    List<Message> findMessageByGroupname(String groupName);

    /**
     * Update messages by username to Seen.
     *
     * @param username the user name
     * @return the int number of records deleted
     */
    int updateMessagesToSeen(String username);
    /**
     * Delete messages by username.
     *
     * @param userName the user name
     * @return the int number of records deleted
     */
    int deleteMessageByUsername(String userName);

    /**
     * Delete messages by groupname.
     *
     * @param groupName the group name
     * @return the int number of records deleted
     */
    int deleteMessageByGroupname(String groupName);
    
    /**
     * Search message by attributes.
     *
     * @param attributes the attributes
     * @return the list
     */
    List<Message> searchMessageByAttributes(String[] attributes);
    
    /**
     * Find senders last message.
     *
     * @param senderName the sender name
     * @return the message
     */
    Message findSendersLastMessage(String senderName);
    
    /**
     * Recall message.
     *
     * @param message the message
     * @return true, if successful
     */
    boolean recallMessage(Message message);
    
    /**
     * Update messages to recalled.
     *
     * @param messageId the message id
     * @return true, if successful
     */
    boolean updateMessagesToRecalled(int messageId);
    
    /**
     * Find messages by receiver and text.
     *
     * @param tokens the tokens
     * @param sender the sender
     * @return the list
     */
    List<Message> findMessagesByReceiverAndText(String[] tokens, String sender);


}
