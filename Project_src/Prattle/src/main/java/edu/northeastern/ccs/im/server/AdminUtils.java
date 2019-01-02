package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.users.UserDAOImplApi;

import java.io.IOException;

/**
 * This is the class that contains the utilities for an admin.
 * @author oz
 */
public class AdminUtils {

    private static AdminUtils adminUtils = null;
    private static UserDAOImplApi userDAOImplApi = new UserDAOImplApi();
    /**
     * Adminutils setup with a singleton pattern.
     * @return the adminutils that is active.
     */
    public static AdminUtils getInstance() {
        if(adminUtils == null) {
            adminUtils = new AdminUtils();
        }
        return adminUtils;
    }

    /**
     * Sets the subpoena for group.
     * @param groupName the group to watch
     * @param listener the user account for the agency
     */
    public void setSubpoenaForGroup(String groupName, String listener) {
        Prattle.setSubpoenaActive(groupName, listener);
    }

    /**
     * Removes the subpoena for group.
     * @param groupName the group to watch
     * @param listener the user account for the agency
     */
    public void removeSubpoenaForGroup(String groupName, String listener) {
        Prattle.setSubpoenaInactive(groupName, listener);
    }

    /**
     * Sets up a subpoena between two users.
     * @param user1 is the username for user1
     * @param user2 is the username for user2
     * @param listener the username of the agency watching the users
     */
    public void setSubpoenaForUsers(String user1, String user2, String listener) throws IOException{
        if (validateUser(user1) && validateUser(user2)) {
            String target1 = user1 + "->" + user2;
            String target2 = user2 + "->" + user1;
            Prattle.setSubpoenaActive(target1, listener);
            Prattle.setSubpoenaActive(target2, listener);
        }
    }

    /**
     * Removes a subpoena between two users.
     * @param user1 is the username for user1
     * @param user2 is the username for user2
     * @param listener the username of the agency watching the users
     */
    public void removeSubpoenaForUsers(String user1, String user2, String listener) throws IOException {
        if (validateUser(user1) && validateUser(user2)) {
            String target1 = user1 + "->" + user2;
            String target2 = user2 + "->" + user1;
            Prattle.setSubpoenaInactive(target1, listener);
            Prattle.setSubpoenaInactive(target2, listener);
        }
    }

    /**
     * This function validates that a user to setup a subpoena for
     * @param user the username to check validity for
     * @return true is the user exists
     */
    private boolean validateUser(String user) throws IOException{
        return userDAOImplApi.userNamesContains(user);
    }
}
