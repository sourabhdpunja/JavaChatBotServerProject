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
 * The Class UserDaoImpl.
 */
public class UserDAOImpl implements UserDAO {

	/** The local context. */
	private DirContext localContext;

	/** The dn name. */
	private String dnName;

	/** logging error and info messages */
	private static Logger logger = LogManager.getLogger(UserDAOImpl.class);

	/**
	 * Creates the attribute.
	 *
	 * @param attrName
	 *            the attr name
	 * @param attrValue
	 *            the attr value
	 * @throws NamingException
	 *             the naming exception
	 */
	@Override
	public void createAttribute(String attributeName, Object attributeValue) throws NamingException {
		Attribute attribute = new BasicAttribute(attributeName, attributeValue);
		ModificationItem[] item = new ModificationItem[1];
		item[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, attribute);

		localContext.modifyAttributes(dnName, item);
	}

	/**
	 * View attribute.
	 *
	 * @param userName
	 *            the user name
	 * @throws NamingException
	 *             the naming exception
	 */
	@Override
	public void viewAttribute(String attributeName) throws NamingException {
		Attributes attrs = localContext.getAttributes(dnName);
		logger.info(attributeName + ":" + attrs.get(attributeName).get());
	}

	/**
	 * Update attribute.
	 *
	 * @param attrName
	 *            the attr name
	 * @param attrValue
	 *            the attr value
	 * @throws NamingException
	 *             the naming exception
	 */
	@Override
	public void updateAttribute(String attributeName, Object attributeValue) throws NamingException {
		Attribute attribute = new BasicAttribute(attributeName, attributeValue);
		ModificationItem[] item = new ModificationItem[1];
		item[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attribute);

		localContext.modifyAttributes(dnName, item);
	}

	/**
	 * Removes the user.
	 *
	 * @throws NamingException
	 *             the naming exception
	 */
	@Override
	public void removeUser() throws NamingException {
		localContext.destroySubcontext(dnName);

	}

	/**
	 * Run.
	 *
	 * @param user
	 *            the user
	 */
	@Override
	public void run(User user) {
		try {
			DirContext lcontext = null;
			lcontext = getContext();

			String name = "userId=" + user.getUserId() + ",ou=users,o=Prattle";
			this.localContext = lcontext;
			this.dnName = name;
		} catch (NamingException e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Creates the user.
	 *
	 * @param user
	 *            the user
	 * @throws NamingException
	 *             the naming exception
	 */
	public void createUser(User user) throws NamingException {
		Attributes attributes = new BasicAttributes();

		Attribute attribute = new BasicAttribute("objectClass");
		attribute.add("inetOrgPerson");
		attributes.put(attribute);
		Attribute sn = new BasicAttribute("sn");
		sn.add(user.getUserName());
		attributes.put(sn);
		Attribute cn = new BasicAttribute("cn");
		cn.add(user.getLastName());
		attributes.put(cn);

		localContext.createSubcontext(dnName, attributes);
	}

	/**
	 * Gets the context.
	 *
	 * @return the context
	 * @throws NamingException
	 *             the naming exception
	 */
	private DirContext getContext() throws NamingException {
		Properties properties = new Properties();
		properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
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
	 * @param dnName
	 *            the new dn name
	 */
	public void setDnName(String dnName) {
		this.dnName = dnName;
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
	 * @param localContext
	 *            the new local context
	 */
	public void setLocalContext(DirContext localContext) {
		this.localContext = localContext;
	}

}
