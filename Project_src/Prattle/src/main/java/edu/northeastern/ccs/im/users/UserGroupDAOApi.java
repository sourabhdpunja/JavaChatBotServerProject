package edu.northeastern.ccs.im.users;

import java.io.IOException;
import java.util.List;

/**
 * The Interface UserGroupDAO.
 */
public interface UserGroupDAOApi {
	
    /**
     * Fetch user group names.
     *
     * @return the list
     */
    public List<String> fetchUserGroupNames() throws IOException;
    
    /**
     * Creates the user group.
     *
     * @param userGroup the user group
     * @return the boolean
     */
    public Boolean createUserGroup(UserGroup userGroup) throws IOException;
    
    /**
     * Delete user group.
     *
     * @param groupName the group name
     * @return the boolean
     */
    public Boolean deleteUserGroup(String groupName) throws IOException;
    
    /**
     * Adds the user to group.
     *
     * @param userName the user name
     * @param groupName the group name
     * @return the boolean
     */
    public Boolean addUserToGroup(String userName, String groupName) throws IOException;
    
    /**
     * Removes the user from group.
     *
     * @param userName the user name
     * @param groupName the group name
     * @return the boolean
     */
    public Boolean removeUserFromGroup(String userName, String groupName) throws IOException;

    /**
     * Gets the users in the given group with name.
     * @param groupname of the group we are interested in
     * @return the list of user (usernames) in the group
     */
    List<String> fetchUsersInGroup(String groupname) throws IOException;
}
