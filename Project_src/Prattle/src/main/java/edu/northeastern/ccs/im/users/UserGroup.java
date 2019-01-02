package edu.northeastern.ccs.im.users;

import java.util.List;

/**
 * The Class UserGroup.
 */
public class UserGroup {
	
	/** The users. */
	private List<User> users;
	
	/** The group name. */
	private String groupName;
	
	/** The group id. */
	private int groupId;

	/**
	 * Instantiates a new user group.
	 *
	 * @param users the users
	 * @param groupName the group name
	 * @param groupId the group id
	 */
	public UserGroup(List<User> users, String groupName, int groupId) {
		super();
		this.users = users;
		this.groupName = groupName;
		this.groupId = groupId;
	}

	/**
	 * Gets the users.
	 *
	 * @return the users
	 */
	public List<User> getUsers() {
		return users;
	}

	/**
	 * Sets the users.
	 *
	 * @param users the new users
	 */
	public void setUsers(List<User> users) {
		this.users = users;
	}

	/**
	 * Gets the group name.
	 *
	 * @return the group name
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * Sets the group name.
	 *
	 * @param groupName the new group name
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/**
	 * Gets the group id.
	 *
	 * @return the group id
	 */
	public int getGroupId() {
		return groupId;
	}

	/**
	 * Sets the group id.
	 *
	 * @param groupId the new group id
	 */
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

}
