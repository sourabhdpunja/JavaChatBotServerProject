package edu.northeastern.ccs.im.users;

import javax.naming.NamingException;

/**
 * The Interface UserGroupDAO.
 */
public interface UserGroupDAO {
	
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
     * @param userGroupName the user group name
     * @throws NamingException the naming exception
     */
    public void viewAttribute(String userGroupName) throws NamingException;
    
    /**
     * Update attribute.
     *
     * @param attrName the attr name
     * @param attrValue the attr value
     * @throws NamingException the naming exception
     */
    public void updateAttribute(String attrName, Object attrValue) throws NamingException;
    
    /**
     * Removes the user group.
     *
     * @throws NamingException the naming exception
     */
    public void removeUserGroup() throws NamingException;
    
    /**
     * Run.
     *
     * @param userGroup the user group
     */
    public void run(UserGroup userGroup);
    
    /**
     * Creates the user group.
     *
     * @param userGroup the user group
     * @throws NamingException the naming exception
     */
    public void createUserGroup(UserGroup userGroup) throws NamingException;
}
