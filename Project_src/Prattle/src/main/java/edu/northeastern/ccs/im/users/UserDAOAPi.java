package edu.northeastern.ccs.im.users;

import java.io.IOException;
import java.util.List;

/**
 * The Interface UserDAO.
 */
public interface UserDAOAPi {
    
    /**
     * Fetch user names.
     *
     * @return the list
     */
    public List<String> fetchUserNames() throws IOException;
    
    /**
     * Creates the user.
     *
     * @param user the user
     * @return the boolean
     */
    public Boolean createUser(User user) throws IOException;
    
    /**
     * Delete user.
     *
     * @param userName the user name
     * @return the boolean
     */
    public Boolean deleteUser(String userName) throws IOException;
    
    /**
     * Adds the attribute.
     *
     * @param attributeName the attribute name
     * @param attributeValue the attribute value
     * @param userName the user name
     * @return the boolean
     */
    public Boolean addAttribute(String attributeName, Object attributeValue, String userName) throws IOException;
    
    /**
     * Removes the attribute.
     *
     * @param attributeName the attribute name
     * @param userName the user name
     * @return the boolean
     */
    public Boolean removeAttribute(String attributeName, String userName) throws IOException;
    
    /**
     * Replace attribute.
     *
     * @param attributeName the attribute name
     * @param attributeValue the attribute value
     * @param userName the user name
     * @return the boolean
     */
    public Boolean replaceAttribute(String attributeName, Object attributeValue, String userName) throws IOException;
    

    /**
     * Validate user.
     *
     * @param user the user
     * @return true, if successful
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public boolean validateUser(User user) throws IOException;
    
    /**
     * Fetch user by user name.
     *
     * @param userName the user name
     * @return the user
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public User fetchUserByUserName(String userName) throws IOException;

}
