package edu.northeastern.ccs.im.message;

import edu.northeastern.ccs.im.Message;

import java.util.List;

/**
 * @author Sourabh Punja on 11/25/2018
 */
public interface SubpoenaDao extends BaseDao{

    /**
     * Persist message in RDS.
     *
     * @param message the message to be persisted
     * @return the int denoting if the message has been added.
     */
    int createMessage(Message message);

    /**
     * Find all messages by username and isSeen False.
     *
     * @param userName the user name
     * @return the list of messages for the user
     */
    List<Message> findMessageUnSeen(String userName);

    /**
     * Update messages by username to Seen.
     *
     * @param username the user name
     * @return the int number of records deleted
     */
    int updateMessagesToSeen(String username);
}
