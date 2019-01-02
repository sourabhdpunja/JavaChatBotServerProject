package edu.northeastern.ccs.im.users;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.junit.jupiter.api.Test;

/**
 * UserDaoImplTest test class
 * 
 * @author rakesh
 */
class UserDAOImplTest {

	/**
	 * This test case is used to start a connection with the LDAP
	 * Server on AWS and test the CRUD operations on User.
	 * 
	 */
	@Test
	void testUserCRUDOperations() {
	  	User user = new User("rock", 24, "ne", "857933927");
    	UserDAOImpl userDaoImpl = new UserDAOImpl();
    	userDaoImpl.run(user);
    	try {
    		userDaoImpl.createUser(user);
        	userDaoImpl.createAttribute("telephoneNumber", user.getTelephoneNumber());
        	userDaoImpl.viewAttribute("cn");
        	userDaoImpl.updateAttribute("cn", "kimi_Rocks");
        	userDaoImpl.viewAttribute("cn");
        	userDaoImpl.removeUser();
        	user.setLastName("rak");
        	user.setTelephoneNumber("125502972");
        	user.setUserId(1);
        	user.setUserName("raki");
        	user.setIpAddress("10:0:0:0");
        	assertEquals(user.getIpAddress(), "10:0:0:0");
        	assertEquals("rak", user.getLastName());
        	assertEquals("125502972", user.getTelephoneNumber());
        	assertEquals(1, user.getUserId());
        	assertEquals("raki", user.getUserName());
    	}catch(NamingException ex) {
    	}
	}
	
	/**
	 * This test case is used to start a connection with the LDAP
	 * Server on AWS and test the CRUD operations on UserGroup.
	 * 
	 */
	@Test
	void testUserGroupCRUDOperations() {
    	//userGroup
		User user = new User("Kimi", 3, "Raikonnen", "8572609927");
    	List<User> userlist = new ArrayList<>();
    	userlist.add(user);
    	UserGroup userGroup = new UserGroup(userlist, "whatsapp", 4);
        //new LdapTest().run(user);
    	UserGroupDAOImpl userGroupDaoImpl = new UserGroupDAOImpl();
    	userGroupDaoImpl.run(userGroup);
    	try {
    		userGroupDaoImpl.createUserGroup(userGroup);
    		userGroupDaoImpl.createAttribute("businessCategory", "retailer");
        	userGroupDaoImpl.viewAttribute("businessCategory");
        	userGroupDaoImpl.updateAttribute("businessCategory", "kimi_Rocks");
        	userGroupDaoImpl.viewAttribute("businessCategory");
        	userGroupDaoImpl.removeUserGroup();
        	DirContext dir = userGroupDaoImpl.getLocalContext();
        	userGroupDaoImpl.setDnName("hey");
        	userGroupDaoImpl.setLocalContext(dir);
        	assertEquals("hey", userGroupDaoImpl.getDnName());
        	
        	userGroup.setGroupId(99);
        	userGroup.setGroupName("heyie");
        	userGroup.setUsers(userlist);
        	
        	assertEquals(99, userGroup.getGroupId());
        	assertEquals("heyie", userGroup.getGroupName());
        	assertEquals(1, userGroup.getUsers().size());
    	}catch(NamingException ex) {
    	}
	}

}
