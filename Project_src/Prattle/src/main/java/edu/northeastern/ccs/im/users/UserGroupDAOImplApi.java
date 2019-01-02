package edu.northeastern.ccs.im.users;

import java.io.IOException;
import java.util.ArrayList;

import java.util.List;

import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * The Class UserGroupDaoImpl.
 */
public class UserGroupDAOImplApi implements UserGroupDAOApi {

	/** logging error and info messages. */
	private static Logger logger = LogManager.getLogger(UserDAOImplApi.class);

	/** The Constant AWS_LDAP. */
	private static final String AWS_LDAP = "ec2-52-14-175-200.us-east-2.compute.amazonaws.com";

	/** The Constant GROUPSDN. */
	private static final String GROUPSDN = ",ou=Groups,o=Prattle";
	
	private static final String GROUPSDN_NOCOMMA = "ou=Groups,o=Prattle";

	/** The Constant COMMON_NAME. */
	private static final String COMMON_NAME = "commonName=";

	/** The Constant USERSDN. */
	private static final String USERSDN = ",ou=users,o=Prattle";

	/** The Constant OBJECTCLASS. */
	private static final String OBJECTCLASS = "(objectclass=*)";

	/** The Constant MEMBER. */
	private static final String MEMBER = "member";

	/** The connection. */
	LdapConnection connection = null;

	public void setConnection(LdapConnection connection) {
		this.connection = connection;
	}

	/**
	 * Fetch user group names.
	 *
	 * @return the list
	 * @throws IOException
	 */
	@Override
	public List<String> fetchUserGroupNames() throws IOException {

		List<String> groupNames = new ArrayList<>();
		if (connection == null) {
			connection = new LdapNetworkConnection(AWS_LDAP, 10389);
		}
		try {
			connection.bind();
			EntryCursor cursor = connection.search(GROUPSDN_NOCOMMA, OBJECTCLASS, SearchScope.ONELEVEL, "*");
			for (Entry entry : cursor) {
				groupNames.add(entry.get("commonName").getString());
			}
		} catch (LdapException e) {
			logger.error(e.getMessage());
		} finally {
			connection.close();
			connection = null;
		}
		return groupNames;

	}

	/**
	 * Gets the users in the given group with name.
	 *
	 * @param groupname
	 *            of the group we are interested in
	 * @return the list of user (usernames) in the group
	 * @throws IOException 
	 */
	@Override
	public List<String> fetchUsersInGroup(String groupname) throws IOException {
		List<String> users = new ArrayList<>();
		if (connection == null) {
			connection = new LdapNetworkConnection(AWS_LDAP, 10389);
		}
		try {
			connection.bind();
			StringBuilder query = new StringBuilder(COMMON_NAME);
			query.append(groupname);
			query.append(",ou=Groups, o=Prattle");
			EntryCursor cursor = connection.search(query.toString(), OBJECTCLASS, SearchScope.SUBTREE, "*");
			parseEntriesForMembers(cursor, users, connection);
		} catch (LdapException e) {
			logger.error(e.getMessage());
		} finally {
			connection.close();
			connection = null;
		}

		return users;
	}

	/**
	 * Parse the entries and pull out the users in the groups
	 * 
	 * @param cursor
	 *            the entries
	 * @param users
	 *            the list of users to add to
	 */
	private void parseEntriesForMembers(EntryCursor cursor, List<String> users, LdapConnection connection) {
		for (Entry e : cursor) {
			String groupMembers = e.get(MEMBER).toString();
			String[] members = groupMembers.split("\n");
			for (String member : members) {
				if (!member.isEmpty()) {
					member = member.substring(8);
					try {
						EntryCursor cursor2 = connection.search(member, OBJECTCLASS, SearchScope.SUBTREE, "cn");
						for (Entry user : cursor2) {
							String username = user.get("cn").getString();
							users.add(username);
						}
					} catch (LdapException e2) {
						logger.error(e2.getMessage());
					}
				}
			}
		}
	}

	/**
	 * Creates the user group.
	 *
	 * @param userGroup
	 *            the user group
	 * @return the boolean
	 * @throws IOException 
	 */
	@Override
	public Boolean createUserGroup(UserGroup userGroup) throws IOException {

		String userGroupName = userGroup.getGroupName();
		List<String> groupNames = new ArrayList<>();
		Boolean result = false;
		if (connection == null) {
			connection = new LdapNetworkConnection(AWS_LDAP, 10389);
		}
		try {
			connection.bind();
			EntryCursor cursor = connection.search(GROUPSDN_NOCOMMA, OBJECTCLASS, SearchScope.ONELEVEL, "*");
			for (Entry entry : cursor) {
				groupNames.add(entry.get("commonName").getString());
			}
			if(!groupNames.contains(userGroup.getGroupName()) && connection.exists(GROUPSDN_NOCOMMA)) {
					connection.add(new DefaultEntry(COMMON_NAME + userGroupName + GROUPSDN, "objectClass : groupOfNames",
							"objectClass : top", "member : cn=" + userGroup.getUsers().get(0).getUserName() + USERSDN));

					result = (connection.exists(COMMON_NAME + userGroupName + GROUPSDN));
			}
		} catch (LdapException e) {
			logger.error(e.getMessage());
		} finally {
			connection.close();
			connection = null;
		}
		return result;

	}

	/**
	 * Delete user group.
	 *
	 * @param groupName
	 *            the group name
	 * @return the boolean
	 * @throws IOException 
	 */
	@Override
	public Boolean deleteUserGroup(String groupName) throws IOException {
		if (connection == null) {
			connection = new LdapNetworkConnection(AWS_LDAP, 10389);
		}
		Boolean result = false;
		try {
			connection.bind();
			if (connection.exists(COMMON_NAME + groupName + GROUPSDN)) {
				connection.delete("cn=" + groupName + GROUPSDN);
				result = (!connection.exists("cn=" + groupName + GROUPSDN));
			}
		} catch (LdapException e) {
			logger.error(e.getMessage());
		} finally {
			connection.close();
			connection = null;
		}
		return result;
	}

	/**
	 * Adds the user to group.
	 *
	 * @param userName
	 *            the user name
	 * @param groupName
	 *            the group name
	 * @return the boolean
	 * @throws IOException 
	 * @throws LdapException 
	 */
	public Boolean addUserToGroup(String userName, String groupName) throws IOException {
		if (connection == null) {
			connection = new LdapNetworkConnection(AWS_LDAP, 10389);
		}
		Boolean result = false;
		String userDN = "cn=" + userName + USERSDN;
		List<String> users = fetchUsersInGroup(groupName);
		connection = new LdapNetworkConnection(AWS_LDAP, 10389);
		try {
			if(!(users.contains(userName))){
				Modification addedUser = new DefaultModification(ModificationOperation.ADD_ATTRIBUTE, MEMBER, userDN);

				connection.modify(COMMON_NAME + groupName + GROUPSDN, addedUser);
				result = true;
			}
		} catch (LdapException e) {
			logger.error(e.getMessage());
		} finally {
			connection.close();
			connection = null;
		}
		return result;
	}

	/**
	 * Removes the user from group.
	 *
	 * @param userName
	 *            the user name
	 * @param groupName
	 *            the group name
	 * @return the boolean
	 * @throws IOException 
	 */
	public Boolean removeUserFromGroup(String userName, String groupName) throws IOException {
		if (connection == null) {
			connection = new LdapNetworkConnection(AWS_LDAP, 10389);
		}
		Boolean result = false;
		String userDN = "cn=" + userName + USERSDN;
		try {
			connection.bind();
			Modification removeGivenName = new DefaultModification(ModificationOperation.REMOVE_ATTRIBUTE, MEMBER,
					userDN);

			connection.modify(COMMON_NAME + groupName + GROUPSDN, removeGivenName);
			result = true;
		} catch (LdapException e) {
			logger.error(e.getMessage());
		} finally {
			connection.close();
			connection = null;
		}
		return result;
	}

}
