package edu.northeastern.ccs.im.users;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class UserGroupDaoImpl.
 */
public class UserGroupDAOImpl implements UserGroupDAO{
	
	/** The local context. */
	private DirContext localContext;
	
	/** The dn name. */
	private String dnName;
	
	/** logging error and info messages */
	private static Logger logger = LogManager.getLogger(UserGroupDAOImpl.class);

    /**
     * Removes the user group.
     *
     * @throws NamingException the naming exception
     */
	public void removeUserGroup() throws NamingException {
		localContext.destroySubcontext(dnName);
		
	}

	   /**
     * Run.
     *
     * @param userGroup the user group
     */
	@Override
	public void run(UserGroup userGroup) {
        try {
            DirContext lcontext = getContext();
            String name = "commonName=" + userGroup.getGroupName()+",ou=Groups,o=Prattle";
            this.localContext = lcontext;
            this.dnName = name;
        } catch (NamingException e) {
            logger.error(e.getMessage());
        }
    }
	
    /**
     * Creates the user group.
     *
     * @param userGroup the user group
     * @throws NamingException the naming exception
     */
	public void createUserGroup(UserGroup userGroup) throws NamingException {
        Attributes attributes = new BasicAttributes();
        Attribute attribute = new BasicAttribute("objectClass");
        attribute.add("groupOfNames");
        attributes.put(attribute);
        Attribute cn = new BasicAttribute("member");
        cn.add("userId="+userGroup.getUsers().get(0).getUserId()+",ou=users,o=Prattle");
        attributes.put(cn);
 
        localContext.createSubcontext(dnName, attributes);
    }
	
    /**
     * View attribute.
     *
     * @param userGroupName the user group name
     * @throws NamingException the naming exception
     */
	@Override
	public void viewAttribute(String attributeName) throws NamingException{
        Attributes attrs = localContext.getAttributes(dnName);
        logger.info(attributeName + ":" + attrs.get(attributeName).get());
    }
	
    /**
     * Gets the context.
     *
     * @return the context
     * @throws NamingException the naming exception
     */
    private DirContext getContext() throws NamingException {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
        properties.put(Context.PROVIDER_URL, "ldap://ec2-52-14-175-200.us-east-2.compute.amazonaws.com:10389");
 
        return new InitialDirContext(properties);
    }
	
	/**
	 * Gets the dn name.
	 *
	 * @return the dn name
	 */
	public String getDnName() {
		return dnName;
	}

	/**
	 * Sets the dn name.
	 *
	 * @param dnName the new dn name
	 */
	public void setDnName(String dnName) {
		this.dnName = dnName;
	}
	
    /**
     * Update attribute.
     *
     * @param attrName the attr name
     * @param attrValue the attr value
     * @throws NamingException the naming exception
     */
	@Override
	public void updateAttribute(String attributeName, Object attributeValue) throws NamingException{
        Attribute attribute = new BasicAttribute(attributeName, attributeValue);
        ModificationItem[] item = new ModificationItem[1];
        item[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attribute);
        
        localContext.modifyAttributes(dnName, item);
    }

	/**
	 * Gets the local context.
	 *
	 * @return the local context
	 */
	public DirContext getLocalContext() {
		return localContext;
	}

	/**
	 * Sets the local context.
	 *
	 * @param localContext the new local context
	 */
	public void setLocalContext(DirContext localContext) {
		this.localContext = localContext;
	}
	
    /**
     * Creates the attribute.
     *
     * @param attrName the attr name
     * @param attrValue the attr value
     * @throws NamingException the naming exception
     */
	@Override
	public void createAttribute(String attributeName, Object attributeValue) throws NamingException{
        Attribute attribute = new BasicAttribute(attributeName, attributeValue);
        ModificationItem[] item = new ModificationItem[1];
        item[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, attribute);
        
        localContext.modifyAttributes(dnName, item);
    }

}
