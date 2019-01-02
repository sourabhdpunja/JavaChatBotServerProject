package edu.northeastern.ccs.im.users;

import javax.naming.NamingException;

/**
 * The Interface UserDAO.
 */
public interface UserDAO {
	
    /**
     * Creates the attribute.
     *
     * @param attrName the attr name
     * @param attrValue the attr value
     * @throws NamingException the naming exception
     */
    public void createAttribute(String attrName, Object attrValue) throws NamingException;
    
    /**
     * View attribute.
     *
     * @param userName the user name
     * @throws NamingException the naming exception
     */
    public void viewAttribute(String userName) throws NamingException;
    
    /**
     * Update attribute.
     *
     * @param attrName the attr name
     * @param attrValue the attr value
     * @throws NamingException the naming exception
     */
    public void updateAttribute(String attrName, Object attrValue) throws NamingException;
    
    /**
     * Removes the user.
     *
     * @throws NamingException the naming exception
     */
    public void removeUser() throws NamingException;
    
    /**
     * Run.
     *
     * @param user the user
     */
    public void run(User user);
    
    /**
     * Creates the user.
     *
     * @param user the user
     * @throws NamingException the naming exception
     */
    public void createUser(User user) throws NamingException;

}
