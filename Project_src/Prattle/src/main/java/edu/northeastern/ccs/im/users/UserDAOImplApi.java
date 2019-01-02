package edu.northeastern.ccs.im.users;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.directory.api.ldap.model.constants.LdapSecurityConstants;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;

import org.apache.directory.api.ldap.model.password.PasswordUtil;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class UserDAOImplApi.
 */
public class UserDAOImplApi implements UserDAOAPi {

	/**  logging error and info messages. */
	private static Logger logger = LogManager.getLogger(UserDAOImplApi.class);
	
	/** The Constant AWS_LDAP. */
	private static final String AWS_LDAP = "ec2-52-14-175-200.us-east-2.compute.amazonaws.com";
	
	/** The Constant USERSDN. */
	private static final String USERSDN = ",ou=users,o=Prattle";
	
	/** The connection. */
	LdapConnection connection = null;
	
	private static final String USERSDN_NOCOMMA = "ou=users,o=Prattle";

	private static final String OBJECTCLASS = "(objectclass=*)";

    public void setConnection(LdapConnection connection) {
		this.connection = connection;
	}

	/**
     * Fetch user names.
     *
     * @return the list
	 * @throws IOException 
     */
	@Override
	public List<String> fetchUserNames() throws IOException{
		List<String> userNames = new ArrayList<>();
		if (connection == null) {
			connection = new LdapNetworkConnection(AWS_LDAP, 10389);			
		}
		try {
			connection.bind();
			EntryCursor cursor = connection.search(USERSDN_NOCOMMA, OBJECTCLASS, SearchScope.ONELEVEL, "*");
			for (Entry entry : cursor) {
				userNames.add(entry.get("cn").getString());
			}
		} catch (LdapException e) {
			logger.error(e.getMessage());
		}finally {
				connection.close();
				connection = null;
		}
		return userNames;
	}

    /**
     * Creates the user.
     *
     * @param user the user
     * @return the boolean
     * @throws IOException 
     */
	@Override
	public Boolean createUser(User user) throws IOException {
		if (connection == null) {
			connection = new LdapNetworkConnection(AWS_LDAP, 10389);			
		}
		String userName = user.getUserName();
		Boolean result = false;
		try {
			connection.bind();
			 byte[] password = PasswordUtil.createStoragePassword(user.getUserPassword(), LdapSecurityConstants.HASH_METHOD_CRYPT);
		    if(connection.exists( USERSDN_NOCOMMA )) {
			    connection.add(new DefaultEntry(
			        "cn="+userName+USERSDN,
			        "objectClass : inetOrgPerson",
			        "objectClass : top",
			        "objectClass : person",
			        "cn: "+userName,
			        "sn: "+user.getLastName(),
			        "userPassword",password));
			    
			    result = (connection.exists( "cn="+userName+USERSDN ));
		    }
			
		} catch (LdapException e) {
			logger.error(e.getMessage());
		}finally {
				connection.close();
				connection = null;
		}
		return result;
	}
	
    /**
     * Delete user.
     *
     * @param userName the user name
     * @return the boolean
     * @throws IOException 
     */
	public Boolean deleteUser(String userName) throws IOException{
		if (connection == null) {
			connection = new LdapNetworkConnection(AWS_LDAP, 10389);			
		}
		Boolean result = false;
		try {
	    	connection.bind();
			if( connection.exists( "cn="+userName+USERSDN ) ) {
			        connection.delete( "cn="+userName+USERSDN );
			        result = (! connection.exists( "cn="+userName+USERSDN ) );
			}
		}catch (LdapException e) {
			logger.error(e.getMessage());
		}finally {
				connection.close();
				connection = null;
		}
		return result;
	}
	
    /**
     * Adds the attribute.
     *
     * @param attributeName the attribute name
     * @param attributeValue the attribute value
     * @param userName the user name
     * @return the boolean
     * @throws IOException 
     */
	public Boolean addAttribute(String attributeName, Object attributeValue, String userName) throws IOException {
		if (connection == null) {
			connection = new LdapNetworkConnection(AWS_LDAP, 10389);			
		}
		Boolean result = false;
		try {
			connection.bind();
			Modification addedGivenName = new DefaultModification( ModificationOperation.ADD_ATTRIBUTE, attributeName,
				    attributeValue.toString() );

				connection.modify( "cn="+userName+USERSDN, addedGivenName );
				result = true;
		}catch (LdapException e) {
			logger.error(e.getMessage());
		}finally {
				connection.close();
				connection = null;
		}
		return result;
	}
	
    /**
     * Removes the attribute.
     *
     * @param attributeName the attribute name
     * @param userName the user name
     * @return the boolean
     * @throws IOException 
     */
	public Boolean removeAttribute(String attributeName, String userName) throws IOException {
		if (connection == null) {
			connection = new LdapNetworkConnection(AWS_LDAP, 10389);			
		}
		Boolean result = false;
		try {
			connection.bind();
			Modification removeGivenName = new DefaultModification( ModificationOperation.REMOVE_ATTRIBUTE, attributeName);

				connection.modify( "cn="+userName+USERSDN, removeGivenName );
				result = true;
		}catch (LdapException e) {
			logger.error(e.getMessage());
		}finally {
				connection.close();
				connection = null;
		}
		return result;
	}
	
    /**
     * Replace attribute.
     *
     * @param attributeName the attribute name
     * @param attributeValue the attribute value
     * @param userName the user name
     * @return the boolean
     * @throws IOException 
     */
	public Boolean replaceAttribute(String attributeName, Object attributeValue, String userName) throws IOException {
		if (connection == null) {
			connection = new LdapNetworkConnection(AWS_LDAP, 10389);			
		}
		Boolean result = false;
		try {
			connection.bind();
			Modification replaceGivenName = new DefaultModification( ModificationOperation.REPLACE_ATTRIBUTE, attributeName,
				    attributeValue.toString() );

				connection.modify( "cn="+userName+USERSDN, replaceGivenName );
				result = true;
		}catch (LdapException e) {
			logger.error(e.getMessage());
		}finally {
				connection.close();
				connection = null;
		}
		return result;
	}
	
    /**
     * Validate user.
     *
     * @param user the user
     * @return true, if successful
     * @throws IOException Signals that an I/O exception has occurred.
     */
	@Override
	public boolean validateUser(User user) throws IOException{
		boolean validPassword = false;
		if (connection == null) {
			connection = new LdapNetworkConnection(AWS_LDAP, 10389);			
		}
		try {
			connection.bind();
			EntryCursor cursor = connection.search(USERSDN_NOCOMMA, OBJECTCLASS, SearchScope.ONELEVEL, "*");
			for (Entry entry : cursor) {
				if(entry.get("cn").getString().equalsIgnoreCase(user.getUserName())) {
					byte[] passBytes = user.getUserPassword().getBytes();
					byte[] bytes = entry.get("userpassword").getBytes();
					validPassword = PasswordUtil.compareCredentials(passBytes, bytes);
				}
			}
		} catch (LdapException e) {
			logger.error(e.getMessage());
		}finally {
				connection.close();
				connection = null;
		}
		return validPassword;
	}

	/**
	 * check title for user
	 * @param username
	 * @param title
	 * @return true if title is of user
	 */
	public boolean checkTitle(String username, String title) {
		return checkAttributeForUser(username, title, "title");
	}

	/**
	 * check if filter is on for a user
	 * @param username
	 * @return true if filter is on, else false
	 */
    public boolean checkFilter(String username) {
	    return checkAttributeForUser(username, "filter", "displayName");
    }

    /**
     * check if user has certain attribute
     * @param username
     * @param value
     * @param attrname
     * @return true if user contains attribute, else false
     */
	private boolean checkAttributeForUser(String username, String value, String attrname) {
        if (connection == null) {
            connection = new LdapNetworkConnection(AWS_LDAP, 10389);
        }
        try {
            connection.bind();
            String userDN = "cn=" + username + USERSDN;
            EntryCursor cursor = connection.search(userDN, OBJECTCLASS, SearchScope.SUBTREE, "*");

            for (Entry e : cursor) {
                Collection<Attribute> attributes = e.getAttributes();
                for (Attribute a : attributes) {
                    if(a.getId().equalsIgnoreCase(attrname)) {
                        return a.getString().equalsIgnoreCase(value);
                    }
                }
            }

            return false;

        } catch (LdapException e) {
            logger.error(e.getMessage());
        }
        return false;
    }
	
    /**
     * Fetch user by user name.
     *
     * @param userName the user name
     * @return the user
     * @throws IOException Signals that an I/O exception has occurred.
     */
	@Override
	public User fetchUserByUserName(String userName) throws IOException{
		User user = new User();
		if (connection == null) {
			connection = new LdapNetworkConnection(AWS_LDAP, 10389);			
		}
		try {
			connection.bind();
			EntryCursor cursor = connection.search("cn="+userName+USERSDN, OBJECTCLASS, SearchScope.SUBTREE, "*");
			for (Entry entry : cursor) {
				user.setUserName(entry.get("cn").getString());
				user.setIpAddress(entry.get("postalAddress").getString());
				user.setTitle(entry.get("title").getString());
			}
		} catch (LdapException e) {
			logger.error(e.getMessage());
		}finally {
				connection.close();
				connection = null;
		}
		return user;
	}

	/**
	 * check if username is in LDAP
	 * @param username
	 * @return true if present, else false
	 * @throws IOException
	 */
	public boolean userNamesContains(String username) throws IOException{
		boolean userExists = false;
		if (connection == null) {
			connection = new LdapNetworkConnection(AWS_LDAP, 10389);
		}
		try {

			connection.bind();
			String userDN = "cn=" + username + USERSDN;
			EntryCursor cursor = connection.search(userDN, OBJECTCLASS, SearchScope.SUBTREE, "*");
			for (Entry entry : cursor) {
				userExists = entry.get("cn").getString().equals(username);
			}

		}catch (LdapException e) {
			logger.error(e.getMessage());
		}finally {
			connection.close();
			connection = null;
		}
		return userExists;
	}


}

