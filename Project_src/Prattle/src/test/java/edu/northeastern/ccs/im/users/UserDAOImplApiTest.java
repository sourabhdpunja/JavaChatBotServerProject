package edu.northeastern.ccs.im.users;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

/**
 * The Class UserDAOImplApiTest. 
 */
class UserDAOImplApiTest {
	
	/** The Constant USERSDN. */
	private static final String USERSDN = ",ou=users,o=Prattle";
	
	/**  logging error and info messages. */
	private static Logger logger = LogManager.getLogger(UserDAOImplApiTest.class);

	/**
	 * Test create user.
	 */
	@Test
	void testCreateUser() {
		UserDAOImplApi userdao = new UserDAOImplApi();
		User user = new User("rocky", 5, "Raiknnen", "12344");
		user.setUserPassword("password123");
		try {
			List<String> userNames = userdao.fetchUserNames();
			if(!userNames.contains(user.getUserName())) {
				assertTrue(userdao.createUser(user));
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	

	
	/**
	 * Testfetch user names.
	 */
	@Test
	void testUserCRUDOperations() {
		UserDAOImplApi userdao = new UserDAOImplApi();
		User user = new User("rakifetch", 1, "Raikonnen", "12344");
		try {
			List<String> userNames = userdao.fetchUserNames();
			if(!userNames.contains(user.getUserName())) {
				assertTrue(userdao.createUser(user));
			}
			List<String> userNames2=userdao.fetchUserNames();
			assertTrue(userNames2.size() > 0);
			//addAttribute
			try {
				assertTrue(userdao.addAttribute("givenName", "myGivenName", "rakifetch"));
				assertTrue(userdao.replaceAttribute("givenName", "replacedGivenName", "rakifetch"));
				assertTrue(userdao.removeAttribute("givenName", "rakifetch"));
				assertTrue(userdao.deleteUser("rakifetch"));

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	//userGroups
	
	/**
	 * Test create user group.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
	void testCreateUserGroup() throws IOException {
		UserGroupDAOApi userGroupdao = new UserGroupDAOImplApi();
		User user = new User("glen");
		List<User> users = new ArrayList<>();
		users.add(user);
		UserGroup userGroup = new UserGroup(users, "group2", 2);
		try {
			List<String> userGroupNames = userGroupdao.fetchUserGroupNames();
			if(!userGroupNames.contains(userGroup.getGroupName())) {
				assertTrue(userGroupdao.createUserGroup(userGroup));
				List<String> strings = userGroupdao.fetchUsersInGroup("group2");
				assertEquals(strings.get(0),"glen");
			}


		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	/**
	 * testUserGroupCRUD user group CRUD operations.
	 */
	@Test
	void testUserGroupCRUD() {
		UserGroupDAOApi userGroupdao = new UserGroupDAOImplApi();
		User user = new User("glenfetch");
		List<User> users = new ArrayList<>();
		users.add(user);
		UserGroup userGroup = new UserGroup(users, "group3", 2);
		try {
			List<String> userGroupNames = userGroupdao.fetchUserGroupNames();
			if(!userGroupNames.contains(userGroup.getGroupName())) {
				assertTrue(userGroupdao.createUserGroup(userGroup));
			}
			List<String> groupNames=userGroupdao.fetchUserGroupNames();
			assertTrue(groupNames.size() > 0);
			assertTrue(userGroupdao.addUserToGroup("rocky", "group3"));
			assertTrue(userGroupdao.removeUserFromGroup("rocky", "group3"));
			assertTrue(userGroupdao.deleteUserGroup("group3"));
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	/**
	 * Test exceptions.
	 *
	 * @throws LdapException the ldap exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
	void testExceptions() throws LdapException, IOException {
		UserDAOImplApi userdao = new UserDAOImplApi();
		User user = new User("rakifetch", 1, "Raikonnen", "12344");
		LdapConnection ldapconnection = mock(LdapConnection.class);
		userdao.setConnection(ldapconnection);
		doThrow(LdapException.class).when(ldapconnection).bind();
		userdao.fetchUserNames();
		
		ldapconnection = mock(LdapConnection.class);
		userdao.setConnection(ldapconnection);
		doThrow(LdapException.class).when(ldapconnection).bind();
		userdao.createUser(user);
		
		ldapconnection = mock(LdapConnection.class);
		userdao.setConnection(ldapconnection);
		doThrow(LdapException.class).when(ldapconnection).bind();
		userdao.deleteUser("rakifetch");
		
		ldapconnection = mock(LdapConnection.class);
		userdao.setConnection(ldapconnection);
		doThrow(LdapException.class).when(ldapconnection).bind();
		userdao.addAttribute("rakifetch", "anything", "anyting");
		
		ldapconnection = mock(LdapConnection.class);
		userdao.setConnection(ldapconnection);
		doThrow(LdapException.class).when(ldapconnection).bind();
		userdao.removeAttribute("anything", "anyting");
		
		ldapconnection = mock(LdapConnection.class);
		userdao.setConnection(ldapconnection);
		doThrow(LdapException.class).when(ldapconnection).bind();
		userdao.replaceAttribute("rakifetch", "anything", "anyting");
	}

	 /** Test validateUser.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
	void testFetchUsers() throws IOException {
		UserDAOImplApi userdao = new UserDAOImplApi();
		User user = new User();
		user.setUserName("ijg");
		user.setUserPassword("password123");
		assertFalse(userdao.validateUser(user));
	}
	
	/**
	 * tests excpetion for user group
	 * @throws LdapException
	 * @throws IOException
	 */
	@Test
	void testExceptionsForUserGroup() throws LdapException, IOException {
		UserGroupDAOImplApi userGroupDao = new UserGroupDAOImplApi();
		User user = new User("rakifetch", 1, "Raikonnen", "12344");
		List<User> users = new ArrayList<>();
		users.add(user);
		UserGroup userGroup = new UserGroup(users, "group5", 5);
		LdapConnection ldapconnection = mock(LdapConnection.class);
		userGroupDao.setConnection(ldapconnection);
		doThrow(LdapException.class).when(ldapconnection).bind();
		userGroupDao.fetchUserGroupNames();
		
		ldapconnection = mock(LdapConnection.class);
		userGroupDao.setConnection(ldapconnection);
		doThrow(LdapException.class).when(ldapconnection).bind();
		userGroupDao.createUserGroup(userGroup);
		
		ldapconnection = mock(LdapConnection.class);
		userGroupDao.setConnection(ldapconnection);
		doThrow(LdapException.class).when(ldapconnection).bind();
		userGroupDao.fetchUsersInGroup("group5");
		
		ldapconnection = mock(LdapConnection.class);
		userGroupDao.setConnection(ldapconnection);
		doThrow(LdapException.class).when(ldapconnection).bind();
		userGroupDao.addUserToGroup("rakeshkr", "group5");
		
		ldapconnection = mock(LdapConnection.class);
		userGroupDao.setConnection(ldapconnection);
		doThrow(LdapException.class).when(ldapconnection).bind();
		userGroupDao.removeUserFromGroup("rakeshkr", "group5");
		
		ldapconnection = mock(LdapConnection.class);
		userGroupDao.setConnection(ldapconnection);
		doThrow(LdapException.class).when(ldapconnection).bind();
		userGroupDao.deleteUserGroup("group5");
	
	}
	
	/**
	 * Test validate users.
	 *
	 * @throws IOException Signals that an I/O exception has occurred. 
	 */
	@Test
	void testValidateUsers() throws IOException {
		UserDAOImplApi userdao = new UserDAOImplApi();
		User user = new User();
		user.setUserName("fischer");
		user.setUserPassword("fisher345");
		userdao.createUser(user);
		assertTrue(userdao.validateUser(user));
	}
	
	/**
	 * test fetch user by user name
	 * @throws IOException
	 */
	@Test
	void testFetchUserByUserName() throws IOException {
		UserDAOImplApi userdao = new UserDAOImplApi();
		userdao.fetchUserByUserName("robert");
		assertEquals("robert", userdao.fetchUserByUserName("robert").getUserName());
	}
	
	/**
	 * tests title
	 */
	@Test
	void testTitle() {
		User user = new User();
		user.setTitle("enduser");
		assertEquals("enduser", user.getTitle());
	}

	/**
	 * tests wiretap user
	 */
	@Test
	void testWireTapUser() {
		UserDAOImplApi api = new UserDAOImplApi();
		String wiretapUser = "subpoena";
		assertTrue(api.checkTitle(wiretapUser, "wiretap"));
		assertFalse(api.checkTitle("rak", "wiretap"));
	}
	/**
	 * test for admin user
	 */

	@Test
	void testAdminUser() {
		UserDAOImplApi api = new UserDAOImplApi();
		String admin = "admin";
		assertTrue(api.checkTitle(admin, "admin"));
		assertFalse(api.checkTitle("rak", "admin"));
	}

	/**
	 * test for filter
	 */
	@Test
	void testFilter() {
		UserDAOImplApi api = new UserDAOImplApi();
		String childfilter = "childfilter";
		assertTrue(api.checkFilter(childfilter));
		assertFalse(api.checkFilter("rak"));
	}
}
