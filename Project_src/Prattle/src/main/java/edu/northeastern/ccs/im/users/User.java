package edu.northeastern.ccs.im.users;

/**
 * The Class User.
 */
public class User {

	/**
	 * Instantiates a new user.
	 *
	 * @param userName the user name
	 * @param userId the user id
	 * @param lastName the last name
	 * @param telephoneNumber the telephone number
	 */
	public User(String userName, int userId, String lastName, String telephoneNumber) {
		super();
		this.userName = userName;
		this.userId = userId;
		this.lastName = lastName;
		this.telephoneNumber = telephoneNumber;
		
	}
	/**
	 * constructor
	 */
	public User() {
		
	}
	
	/**
	 * constructor accepts username 
	 * @param userName
	 */
	public User(String userName){
		this.userName = userName;
	}
	/**
	 * Password of the user
	 */
	private String password;
	/** The user name. */
	private String userName;

	/** The user id. */
	private int userId;

	/** The last name. */
	private String lastName;

	/** The telephone number. */
	private String telephoneNumber;
	
	/** The ip address. */
	private String ipAddress;
	
	private String title;

	/**
	 * Gets the user name.
	 *
	 * @return the user name
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * gets user password
	 * @return
	 */
	public String getUserPassword() {
		return password;
	}

	/**
	 * Sets the user name.
	 *
	 * @param userName the new user name
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Gets the user id.
	 *
	 * @return the user id
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * Sets the user id.
	 *
	 * @param userId the new user id
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	/**
	 * Sets the user password.
	 *
	 * @param password, the password of the user
	 */
	public void setUserPassword(String password) {
		this.password = password;
	}

	/**
	 * Gets the last name.
	 *
	 * @return the last name
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Sets the last name.
	 *
	 * @param lastName the new last name
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * Gets the telephone number.
	 *
	 * @return the telephone number
	 */
	public String getTelephoneNumber() {
		return telephoneNumber;
	}

	/**
	 * Sets the telephone number.
	 *
	 * @param telephoneNumber the new telephone number
	 */
	public void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}
	
	/**
	 * Gets the ip address.
	 *
	 * @return the ip address
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * Sets the ip address.
	 *
	 * @param ipAddress the new ip address
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	/**
	 * returns the title
	 * @return title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * sets the title
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

}
