package edu.northeastern.ccs.im.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;

import edu.northeastern.ccs.im.message.MessageDaoImpl;
import edu.northeastern.ccs.im.message.SubpoenaDao;
import edu.northeastern.ccs.im.message.SubpoenaDaoImpl;
import edu.northeastern.ccs.im.users.*;
import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackMessage;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.PrintNetNB;
import edu.northeastern.ccs.im.ScanNetNB;
import edu.northeastern.ccs.im.guidance.InappropriateFilter;
import edu.northeastern.ccs.im.message.MessageDao;

/**
 * Instances of this class handle all of the incoming communication from a
 * single IM client. Instances are created when the client signs-on with the
 * server. After instantiation, it is executed periodically on one of the
 * threads from the thread pool and will stop being run only when the client
 * signs off..
 * <p>
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 *
 * @version 1.3
 */
public class ClientRunnable implements Runnable {
	/**
	 * Number of milliseconds that special responses are delayed before being sent.
	 */
	private static final int SPECIAL_RESPONSE_DELAY_IN_MS = 5000;

	/**
	 * Name to display for server bouncing a message.
	 */
	private static final String BOUNCER = "BOUNCER";
	/**
	 * Number of milliseconds after which we terminate a client due to inactivity.
	 * This is currently equal to 5 hours.
	 */
	private static final long TERMINATE_AFTER_INACTIVE_BUT_LOGGEDIN_IN_MS = 18000000;

	/**
	 * Number of milliseconds after which we terminate a client due to inactivity.
	 * This is currently equal to 5 hours.
	 */
	private static final long TERMINATE_AFTER_INACTIVE_INITIAL_IN_MS = 600000;

	/**
	 * Time at which we should send a response to the (private) messages we were
	 * sent.
	 */
	private Date sendResponses;
	private static final int BUFFER_SIZE = 64 * 1024;

	/** Time at which the client should be terminated due to lack of activity. */
	private GregorianCalendar terminateInactivity;

	/** Queue of special Messages that we must send immediately. */
	private Queue<Message> immediateResponse;

	/** Queue of special Messages that we will need to send. */
	private Queue<Message> specialResponse;

	/** Socket over which the conversation with the single client occurs. */
	private final SocketChannel socket;

	/**
	 * Utility class which we will use to receive communication from this client.
	 */
	private ScanNetNB input;

	/** Utility class which we will use to send communication to this client. */
	private PrintNetNB output;

	/** Id for the user for whom we use this ClientRunnable to communicate. */
	private int userId;

	/** Name that the client used when connecting to the server. */
	private String name;

	/**
	 * Whether this client has been initialized, set its user name, and is ready to
	 * receive messages.
	 */
	private boolean initialized;

	/**
	 * Boolean representing if this user has a filter on.
	 */
	private boolean messagefilter;

	InappropriateFilter filter = new InappropriateFilter();

	/**
	 * The future that is used to schedule the client for execution in the thread
	 * pool.
	 */
	private ScheduledFuture<ClientRunnable> runnableMe;

	/** Collection of messages queued up to be sent to this client. */
	private Queue<Message> waitingList;
	
    /** The Constant SENDER. */
    private static final String SENDER = "sender";
    
    /** The Constant RECEIVER. */
    private static final String RECEIVER = "receiver";

	public Queue<Message> getWaitingList() {
		return waitingList;
	}

	private MessageDao messageDaoImpl = new MessageDaoImpl();

	/** Subpeona DAO  for user */
	private boolean subpeonaUser;

	private SubpoenaDao subpoenaDaoImpl = new SubpoenaDaoImpl();

	/** logging error and info messages */
	private static Logger logger = LogManager.getLogger(ClientRunnable.class);
	
	/** The client ip address. */
	String clientIpAddress;
	
	/** The slack api. */
	SlackApi slackApi = new SlackApi("https://hooks.slack.com/services/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");

	public ClientRunnable(SocketChannel client) throws IOException {
		this(client, new LinkedList<>(), new LinkedList<>(), new ConcurrentLinkedQueue<>(), false,
				new GregorianCalendar(), null);
	}

	/**
	 * Create a new thread with which we will communicate with this single client.
	 *
	 * @param client SocketChannel over which we will communicate with this new
	 *               client
	 * @throws IOException Exception thrown if we have trouble completing this
	 *                     connection
	 */
	public ClientRunnable(SocketChannel client, Queue<Message> immediateResponse, Queue<Message> specialResponse,
						  Queue<Message> waitingList,  boolean initialized,
						  GregorianCalendar terminateInactivity, ScanNetNB input) throws IOException {
		// Set up the SocketChannel over which we will communicate.
		socket = client;
		socket.configureBlocking(false);
		// Create the class we will use to receive input
		try {
			if (input == null) {
				Selector selector = Selector.open();
				SelectionKey key = socket.register(selector, SelectionKey.OP_READ);
				ByteBuffer buffer = java.nio.ByteBuffer.allocate(BUFFER_SIZE);
				this.input = new ScanNetNB(socket, selector, new ConcurrentLinkedQueue<>(), key, buffer);
			} else {
				this.input = input;
			}

		} catch (IOException e) {
		// For the moment we are going to simply cover up that there was a problem.
			logger.error(e.toString());
			assert false;
		}

		// Create the class we will use to send output
		this.output = new PrintNetNB(socket);
		// Mark that we are not initialized
		this.initialized = initialized;
		// Create our queue of special messages
		this.specialResponse = specialResponse;
		// Create the queue of messages to be sent
		this.waitingList = waitingList;
		// Create our queue of message we must respond to immediately
		this.immediateResponse = immediateResponse;
		// Mark that the client is active now and start the timer until we
		// terminate for inactivity.
		this.terminateInactivity = terminateInactivity;
		terminateInactivity
				.setTimeInMillis(terminateInactivity.getTimeInMillis() + TERMINATE_AFTER_INACTIVE_INITIAL_IN_MS);
	}

	/**
	 * Determines if this is a special message which we handle differently. It will
	 * handle the messages and return true if msg is "special." Otherwise, it
	 * returns false.
	 *
	 * @param msg Message in which we are interested.
	 * @return True if msg is "special"; false otherwise.
	 */
	private boolean broadcastMessageIsSpecial(Message msg) {
		boolean result = false;
		String text = msg.getText();
		if (text != null) {
			ArrayList<Message> responses = ServerConstants.getBroadcastResponses(text);
			if (responses != null) {
				for (Message current : responses) {
					handleSpecial(current);
				}
				result = true;
			}
		}
		return result;
	}

	/**
	 * Broadcasts the message given
	 * @param msg Message to be broadcasted
	 */
	private void broadCastMessage(Message msg) {
		// Check for our "special messages"
		if ((msg.isBroadcastMessage()) && (!broadcastMessageIsSpecial(msg))) {
			// Check for our "special messages"
			if ((msg.getText() != null)
					&& (msg.getText().compareToIgnoreCase(ServerConstants.BOMB_TEXT) == 0)) {
				initialized = false;
				Prattle.broadcastMessage(Message.makeQuitMessage(name));
			} else {
				Prattle.broadcastMessage(msg);
			}
		}
	}

    /**
     * Check to see for an initialization attempt and process the message sent.
     *
     * @throws IOException
     */
    private void checkForInitialization() throws IOException {
        // Check if there are any input messages to read
        if (input.hasNextMessage()) {
            // If a message exists, try to use it to initialize the connection
            Message msg = input.nextMessage();
            UserDAOImplApi userDAO = new UserDAOImplApi();
            if (setUserName(msg.getName())) {

                User user = new User(msg.getName());  //dummy user for validation
                String password = msg.getPassword();
                validatepassword(user, password);
                if (userDAO.fetchUserNames().contains(msg.getName())) {  // existing user
                    if (!userDAO.validateUser(user)) {
                        //############ re enter password - wrong password entered
						failedLogin(msg);
						// Should we terminate the connection here?
                    } else {
						successfulMessageEnqueue(msg);
						enqueueMessageToUsers(msg);
						//  Get the subpoena state for this user.
						determineSubpoenaState();
						// Check filter state for this user.
						determineFilterState();
						// Fetch messages for the user.
						fetchMessages();

					}

                } else {
					userDAO.createUser(user);  //create new user
					newUserCreation(msg);
                }
				filter.buildTree("src/main/resources/vulgar.txt");
                Prattle.setUserOnline(user, this);
                messageDaoImpl.createMessage(msg);
                // Update the time until we terminate this client due to inactivity.
                terminateInactivity.setTimeInMillis(
                        new GregorianCalendar().getTimeInMillis() + TERMINATE_AFTER_INACTIVE_INITIAL_IN_MS);
                // Set that the client is initialized.
               initialized = true;
            } else {
                initialized = false;
            }
        }
    }

	/**
	 * Helper action to be done when login is failed.
	 * @param msg message from the login
	 */
    private void failedLogin(Message msg) {
		Message invalidPassword = Message.makeUserMessage(msg.getName(), msg.getName()+ " Unsuccessful Login");
		enqueueMessage(invalidPassword);
		slackApi.call(new SlackMessage("Failed Login", "user entered an invalid password"));
	}

	/**
	 * Helper action to be done when login is successful.
	 * @param msg message from the login
	 */
	private void successfulMessageEnqueue(Message msg) throws IOException {
		Message successfulPassword = Message.makeUserMessage(msg.getName(), msg.getName()+ " relogin Successful");
		clientIpAddress = socket.socket().getInetAddress().getHostAddress();
		handleUserIpAddress(msg);
		enqueueMessage(successfulPassword);
	}

	/**
	 * Helper action to be done when login is for the first time.
	 * @param msg message from the login
	 */
	private void newUserCreation(Message msg) {
		//######### new user created
		Message successfulPassword = Message.makeUserMessage(msg.getName(), "First Time Successful Login");
		enqueueMessage(successfulPassword);
	}

	/**
	 * Helper action to be done to enqueue message to users clientrunnable.
	 * @param msg message from the login
	 */
	private void enqueueMessageToUsers(Message msg) {
		List<Message> messageList = getMessagesForUser(msg.getName());
		for (Message message : messageList) {
			if(!message.isRecalled()) {
				enqueueMessage(message);
			}
		}
	}

	/**
	 * Set the filter state of this user upon login.
	 */
	private void determineFilterState() {
		UserDAOImplApi api = new UserDAOImplApi();
		messagefilter = api.checkFilter(name);
	}

	/**
	 * Fetch messages for a user upon logging in.
	 */
	private void fetchMessages() {
		List<Message> messageList;
		if(subpeonaUser) {
			messageList = getMessagesForSubpoenaUser(name);
		} else {
			messageList = getMessagesForUser(name);
		}

		for (Message message : messageList) {
			enqueueMessage(message);
		}
	}

	/**
	 * Checks the subpeona state for this user. To avoid multiple calls to this server object,
	 * The api return is set as an object variable.
	 * @throws IOException if the connection with the server fails.
	 */
	private void determineSubpoenaState() {
    	UserDAOImplApi api = new UserDAOImplApi();
    	subpeonaUser = api.checkTitle(name, "wiretap");
	}
	/**
	 * Handles user Ip address
	 * @param msg
	 * @throws IOException
	 */
	private void handleUserIpAddress(Message msg) throws IOException {
		UserDAOAPi userDao = new UserDAOImplApi();
		User user = userDao.fetchUserByUserName(msg.getName());
		if(user.getIpAddress() !=null) {
			userDao.replaceAttribute("postalAddress", clientIpAddress, msg.getName());
		}else {
			userDao.addAttribute("postalAddress", clientIpAddress , msg.getName());
		}
	}

	/**
	 * gets messages for the user
	 * @param name name of user
	 * @return list of messages
	 */
	private List<Message> getMessagesForUser(final String name) {
    	List<Message> messageList;
    	MessageDao messageDao = new MessageDaoImpl();
    	messageList = messageDao.findMessageByUsernameAndUnSeen(name);
    	messageDao.updateMessagesToSeen(name);
    	return messageList;
	}

	/**
	 * gets messages for the subpoena user
	 * @param name of user
	 * @return list of messages
	 */
	private List<Message> getMessagesForSubpoenaUser(final String name) {
		List<Message> messageList;
		SubpoenaDao subpoenaDao = new SubpoenaDaoImpl();
		messageList = subpoenaDao.findMessageUnSeen(name);
		subpoenaDao.updateMessagesToSeen(name);
		return messageList;
	}

	/**
     * Validate the user password
     * @param user to validate
     * @param password of user to validate
     * @return the user with correct password
     */
    private void validatepassword(User user, String password){
        if (nullPassword(password)) {
            // ############ enter valid password
            user.setUserPassword("invalid password type");
        } else {
            user.setUserPassword(password);
        }
    }

    /**
     * check if password is null
     * @param password
     * @return true if null
     */
	public boolean nullPassword(String password) {
		return (password == null || password.trim().isEmpty());

	}



	/**
	 * Process one of the special responses
	 *
	 * @param msg Message to add to the list of special responses.
	 */
	private void handleSpecial(Message msg) {
		if (specialResponse.isEmpty()) {
			sendResponses = new Date();
			sendResponses.setTime(sendResponses.getTime() + SPECIAL_RESPONSE_DELAY_IN_MS);
		}
		specialResponse.add(msg);
	}

	/**
	 * Check if the message is properly formed. At the moment, this means checking
	 * that the identifier is set properly.
	 *
	 * @param msg Message to be checked
	 * @return True if message is correct; false otherwise
	 */
	private boolean messageChecks(Message msg) {
		// Check that the message name matches.
		return (msg.getName() != null) && (msg.getName().compareToIgnoreCase(getName()) == 0);
	}

	/**
	 * Immediately send this message to the client. This returns if we were
	 * successful or not in our attempt to send the message.
	 *
	 * @param message Message to be sent immediately.
	 * @return True if we sent the message successfully; false otherwise.
	 */
	private boolean sendMessage(Message message) {
		logger.info("\t" + message);
		return output.print(message);
	}

	/**
	 * Try allowing this user to set his/her user name to the given username.
	 *
	 * @param userName The new value to which we will try to set userName.
	 * @return True if the username is deemed acceptable; false otherwise
	 */
	public boolean setUserName(String userName) {
		// Now make sure this name is legal.
		if (!nullPassword(userName)) {
			// Optimistically set this users ID number.
			setName(userName);
			userId = hashCode();
			return true;
		}
		// Clear this name; we cannot use it. *sigh*

		//##################### username is null or empty; disconnect
		userId = -1;
		return false;
	}



	/**
	 * Add the given message to this client to the queue of message to be sent to
	 * the client.
	 *
	 * @param message Complete message to be sent.
	 */
	public void enqueueMessage(Message message) {
		if (checkFilter() && filter.inappropriate(message.getText())) {
			message.flagMsg();
		}
		waitingList.add(message);
	}

	/**
	 * Get the name of the user for which this ClientRunnable was created.
	 *
	 * @return Returns the name of this client.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of the user for which this ClientRunnable was created.
	 *
	 * @param name The name for which this ClientRunnable.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the name of the user for which this ClientRunnable was created.
	 *
	 * @return Returns the current value of userName.
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * Return if this thread has completed the initialization process with its
	 * client and is read to receive messages.
	 *
	 * @return True if this thread's client should be considered; false otherwise.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Perform the periodic actions needed to work with this client.
	 *
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		boolean terminate = false;
		// The client must be initialized before we can do anything else
		if (!initialized) {
		    try {
                checkForInitialization();
            } catch (IOException e) {
		        logger.error(e.getMessage());
            }
		} else {
			try {
				// Client has already been initialized, so we should first check
				// if there are any input
				// messages.
				if (input.hasNextMessage()) {
					terminate = processMessage();
				}
				checkForClient();
				// Check to make sure we have a client to send to.
				boolean processSpecial = !specialResponse.isEmpty()
						&& ((!initialized) || (!waitingList.isEmpty()) || sendResponses.before(new Date()));
				boolean keepAlive = !processSpecial;
				keepAlive = specialMessageResponses(processSpecial,keepAlive);
				keepAlive = sendMessagesToQueue(processSpecial, keepAlive);
				terminate |= !keepAlive;
			} catch (IOException e) {
				logger.error(e.getMessage());
			} finally {
				// When it is appropriate, terminate the current client.
				if (terminate) {
					terminateClient();
				}
			}
		}
		// Finally, check if this client have been inactive for too long and,
		// when they have, terminate
		// the client.
		if (!terminate && terminateInactivity.before(new GregorianCalendar())) {
			logger.error("Timing out or forcing off a user " + name);
			terminateClient();
		}
	}

	/**
	 * Process the message in the input
	 * @throws IOException
	 */
	private boolean processMessage() throws IOException {
		// Get the next message
		Message msg = input.nextMessage();
		// Update the time until we terminate the client for
		// inactivity.
		msg.setSenderIpAddress(clientIpAddress);
		if (msg.getText()!= null && msg.getText().contains("@") && !msg.getMsgHandle().equals("RCL")){
            setReceiversLatestIpAddress(msg);
        }
		terminateInactivity.setTimeInMillis(
				new GregorianCalendar().getTimeInMillis() + TERMINATE_AFTER_INACTIVE_BUT_LOGGEDIN_IN_MS);
		if (subpeonaUser) {
			Message invalidPassword = Message.makeUserMessage(msg.getName(), msg.getName()+ " user is read only. Can only add the tap");
			enqueueMessage(invalidPassword);
			return false;
		}
		userAndGroupMsg(msg);
		if (msg.isGroupModifyMessage()){
			parseModifyMessage(msg);
		}
		if (msg.isAdminMessage()) {
			if (isAdmin(name)) {
				parseAdminMessage(msg);
			} else {
				Message notAdmin = Message.makeUserMessage(BOUNCER,
						msg.getName() + " is not an administrator. Insufficient permissions to generate an admin message!");
				enqueueMessage(notAdmin);
			}

		}

		if(msg.isSearchByAttributes()) {
			List<Message> messages = parseSearchByAttribute(msg);
			for(Message message: messages) {
				Message invalidUser = Message.makeUserMessage(message.getName(), message.getMsgReceiver() + " here is the message  "+ message.getText());
				enqueueMessage(invalidUser);
			}
		}
		
		if(msg.isRecallMessage()) {
			parseRecallMessage(msg);
		}
		// If the message is a broadcast message, send it out
		if (msg.isDisplayMessage()) {
			// Check if the message is legal formatted
			broadcastMessageCheck(msg);
			messageDaoImpl.createMessage(msg);
		} else if (msg.terminate()) {
			// Reply with a quit message.
			enqueueMessage(Message.makeQuitMessage(name));
			messageDaoImpl.createMessage(msg);
			// Stop sending the poor client message.
			return true;
		}
		return false;
		// Otherwise, ignore it (for now).
	}

	/**
	 * Switch logging on or off.
	 *
	 * @param operation on/off string
	 */
	private void switchLoggingOnOrOff(String operation) {
		if(operation.equalsIgnoreCase("on")) {
			Configurator.setRootLevel(Level.INFO);
		}else if(operation.equalsIgnoreCase("off")) {
			Configurator.setRootLevel(Level.OFF);
		}

	}
	
	/**
	 * check if is admin
	 * @param name
	 * @return
	 */
	private boolean isAdmin(String name) {
		UserDAOImplApi api = new UserDAOImplApi();
		return api.checkTitle(name, "admin");
	}

	/**
	 * set receivers latest ip address
	 * @param msg message given
	 * @throws AssertionError
	 * @throws IOException
	 */
	private void setReceiversLatestIpAddress(Message msg) throws AssertionError, IOException {
		UserDAOAPi userDaoImpl = new UserDAOImplApi();
		String body = msg.getText();
		String target = parseMessageTargetUser(body);
		User msgReceiver = userDaoImpl.fetchUserByUserName(target);
		msg.setReceiverIpAddress(msgReceiver.getIpAddress());
	}

	/**
	 * Helper action to be done for users and group messages checks.
	 * @param msg message from the login
	 */
	private void userAndGroupMsg(Message msg) throws IOException {
		if (msg.isDirectMessage()) {
			sendDirectMessage(msg);
		}
		if (msg.isGroupMessage()) {
			messageGroup(msg);
		}
	}
	
	/**
	 * Parses the recall message.
	 *
	 */
	private void parseRecallMessage(Message message) {
		MessageDaoImpl msgDaoImpl = new MessageDaoImpl();

		String sender = this.name;
		String text = message.getText();
		String[] tokens = text.split(" ",5);

		List<Message> messages = msgDaoImpl.findMessagesByReceiverAndText(tokens, sender);
		for(Message msg:messages) {
			if(!msg.isMessageSeen() && validateTokensForRecall(tokens)) {
				msgDaoImpl.recallMessage(msg);
			}
		}

		SubpoenaDaoImpl subpoenaDao = new SubpoenaDaoImpl();
		List<Message> subMessages = subpoenaDao.findMessagesByReceiverAndText(tokens, sender);
		for(Message msg:subMessages) {
			if(!msg.isMessageSeen() && validateTokensForRecall(tokens)) {
				msgDaoImpl.recallMessage(msg);
			}
		}
	}
	
	/**
	 * Parses the search by attribute.
	 *
	 * @param msg the msg
	 * @return the list
	 */
	public List<Message> parseSearchByAttribute(Message msg) {
		String text = msg.getText();
		text = checkMessageForSenderReceiver(text);
		String[] tokens = text.split(" ");
		List<Message> messages = new ArrayList<>();
		MessageDao messageDao = new MessageDaoImpl();
		if(validateTokens(tokens)){
			messages = messageDao.searchMessageByAttributes(tokens);
		}else {
			logger.info("please enter a valid attribute to search");
		}
		return messages;
	}

	/**
	 * Check message for sender receiver.
	 *
	 * @param text the text
	 * @return the string
	 */
	private String checkMessageForSenderReceiver(String text) {
		if(text.toLowerCase().indexOf(SENDER.toLowerCase()) != -1 && text.toLowerCase().indexOf(RECEIVER.toLowerCase()) != -1) {
			logger.info("both sender and receiver mentioned in search message");
		}else if(text.toLowerCase().indexOf(SENDER.toLowerCase()) != -1) {
			text = text+" receiver "+this.name;
		}else if(text.toLowerCase().indexOf(RECEIVER.toLowerCase()) != -1) {
			text = text+" sender "+this.name;
		}else if((text.toLowerCase().indexOf("fromTime".toLowerCase()) != -1) || (text.toLowerCase().indexOf("toTime".toLowerCase()) != -1)){
			text = text+" sender "+this.name;
		}
		return text;
	}
	
	/**
	 * Validate tokens. validates if the search
	 * attributes provided by the user are valid
	 *
	 * @param tokens the tokens
	 * @return true, if successful
	 */
	private boolean validateTokens(String[] tokens) {
		boolean valid = false;
		for(int i=1; i< tokens.length; i=i+2) {
			if(tokens[i].equalsIgnoreCase(SENDER) || tokens[i].equalsIgnoreCase(RECEIVER)
					|| tokens[i].equalsIgnoreCase("fromTime") || tokens[i].equalsIgnoreCase("ToTime")) {
				valid = true;
			}
		}
		return valid;
	}
	
	/**
	 * Validate tokens for recall.
	 *
	 * @param tokens the tokens
	 * @return true, if successful
	 */
	private boolean validateTokensForRecall(String[] tokens) {
		boolean valid = false;
		for(int i=1; i< tokens.length; i=i+2) {
			if(tokens[i].equalsIgnoreCase(RECEIVER) || tokens[i].equalsIgnoreCase("text")) {
				valid = true;
			}
		}
		return valid;
	}

	/**
	 * Parses a modify message for proper command
	 * @param msg to parse
	 */
	private void parseModifyMessage(Message msg) {
		String text = msg.getText();
		String[] tokens = text.split(" ");
		try {
			String command = tokens[1];
			String targetGroup = tokens[2];
			String userName= "";
			if(command.equalsIgnoreCase("adduser") || command.equalsIgnoreCase("deleteuser")) {
				userName = tokens[3];
			}
			UserGroupDAOImplApi api = new UserGroupDAOImplApi();
			switch (command) {
				case "create":
					User user = new User(this.name);
					List<User> users = new ArrayList<>();
					users.add(user);
					UserGroup group = new UserGroup(users, targetGroup, hashCode());
					if(api.createUserGroup(group)) {
						logger.info("group "+targetGroup+" created successfully");
					}
					break;
				case "delete":
					api.deleteUserGroup(targetGroup);
					break;
				case "adduser":
					if(api.addUserToGroup(userName, targetGroup)) {
						logger.info("user: "+userName+" added to the group: "+targetGroup);
					}else {
						logger.info("user "+userName+ " already in the group or not in the system");
					}
					break;
				case "deleteuser":
					if(api.removeUserFromGroup(userName, targetGroup)) {
						logger.info("user: "+userName+" removed from the group: "+targetGroup);
					}else {
						logger.info(userName+" not in the group");
					}
					break;
				default:
					logger.error("Invalid command for Group Modification.");

			}
		} catch (IOException e) {
			logger.error("LDAP Connection error");
		}
		catch (IndexOutOfBoundsException e2) {
			logger.error("Invalid command for Group Modification given.");
		}
	}

	/**
	 * Parse a subpoena message type and set that target active in Prattle application.
	 * @param msg the message that contains the modification information.
	 */
	private void parseAdminMessage(Message msg) throws IOException {
		String text = msg.getText();
		String[] tokens = text.split(" ");
		try {
			String success = "SUCESS";
			AdminUtils utils = AdminUtils.getInstance();
			String command = tokens[0];
			Message ack;
			switch (command) {
				case "createSubpoenaUsers":
					utils.setSubpoenaForUsers(tokens[1],tokens[2],tokens[3]);
					ack = Message.makeUserMessage(success, "Wiretap between Users added");
					enqueueMessage(ack);
					break;
				case "createSubpoenaGroup":
					utils.setSubpoenaForGroup(tokens[1], tokens[2]);
					ack = Message.makeUserMessage(success, "Wiretap for Group added");
					enqueueMessage(ack);
					break;
				case "removeSubpoenaUsers":
					utils.removeSubpoenaForUsers(tokens[1],tokens[2],tokens[3]);
					ack = Message.makeUserMessage(success, "Wiretap between Users removed");
					enqueueMessage(ack);
					break;
				case "removeSubpoenaGroup":
					utils.removeSubpoenaForGroup(tokens[1], tokens[2]);
					ack = Message.makeUserMessage(success, "Wiretap for Group removed.");
					enqueueMessage(ack);
					break;
				case "logger":
					switchLoggingOnOrOff(tokens[1]);
					ack = Message.makeUserMessage(success, "logging level set to "+tokens[1]);
					enqueueMessage(ack);
					break;
				default:
					Message error = Message.makeUserMessage(BOUNCER, "Invalid Admin Command");
					enqueueMessage(error);
					break;
			}

		} catch (IndexOutOfBoundsException e2) {
			logger.error("Invalid command for Admin Modification given.");
			Message error = Message.makeUserMessage(BOUNCER, "Invalid Admin Command");
			enqueueMessage(error);
		}
	}

	/**
	 * Helper function to direct a message to a user.
	 * @param msg to send to the user.
	 * @throws IOException if the DAO fails the connection.
	 */
	private void sendDirectMessage(Message msg) throws IOException {
		String body = msg.getText();
		String target = parseMessageTargetUser(body);
		msg.setMsgReceiver(target);

		if (!isUserInLDAP(target)) {
			msg.setMessageSeen(false);
			Message invalidUser = Message.makeUserMessage(msg.getName(), msg.getMsgReceiver()+ " is not in the System");
			enqueueMessage(invalidUser);
			messageDaoImpl.createUserAndGroupMessage(msg);
		} else {
			if (Prattle.activeUser(target)) {
				sendMessageToActiveUser(msg, target);
			} else {
				msg.setMessageSeen(false);
				Message userOffline = Message.makeUserMessage(msg.getName(), msg.getMsgReceiver()+ " is offline");
				enqueueMessage(userOffline);
				messageDaoImpl.createUserAndGroupMessage(msg);
			}
		}
		String subpoenaTarget = msg.getName()+ "->" + target;
		if (Prattle.activeSubpoenaUser(subpoenaTarget)) {
			sendWireTapMessage(subpoenaTarget, msg);
		}

	}

	/**
	 * Send a wiretap message to a wiretap user.
	 * @param target the target tap being listened to 'sender->receiver'
	 * @param msg to send to a wiretap user
	 */
	private void sendWireTapMessage(String target, Message msg){
		msg.setSubpoenaUser(this.name);
		List<String> wiretaps = Prattle.getActiveTaps(target);
		for(String username : wiretaps) {
			if (Prattle.activeUser(username)) {
				sendMessageToActiveSubpoenaUser(msg, username);
			} else {
				msg.setMessageSeen(false);
				subpoenaDaoImpl.createMessage(msg);
			}
		}
	}

	/**
	 * Checks the target username in LDAP Service to determine if they are a valid user
	 * @param target is the username to check if it is in database
	 * @return true if this is a valid user
	 * @throws IOException if there is an exception during intialization of connection to the database.
	 */
	private boolean isUserInLDAP(String target) throws IOException {
		UserDAOImplApi userDAO = new UserDAOImplApi();
		return userDAO.fetchUserNames().contains(target);
	}

	/**
	 * Helper for messaging a group.
	 * @param msg to send to group
	 * @throws IOException
	 */
	private void messageGroup(Message msg) throws IOException {
		String body = msg.getText();
		String target = parseMessageTargetGroup(body);
		msg.setMsgGroup(target);

		UserGroupDAOImplApi api = new UserGroupDAOImplApi();
		List<String> groupMembers =  api.fetchUsersInGroup(target);

		// add persistance for group message here.
		for(String user : groupMembers) {
			msg.setMsgReceiver(user);
			UserDAOAPi userDao = new UserDAOImplApi();
			User msgReceiver = userDao.fetchUserByUserName(user);
			msg.setReceiverIpAddress(msgReceiver.getIpAddress());
			if (Prattle.activeUser(user)) {
				sendMessageToActiveUser(msg, user);
			} else {
				msg.setMessageSeen(false);
				Message userOffline = Message.makeUserMessage(msg.getName(), msg.getMsgReceiver() + " of group " + msg.getMsgGroup() + " is offline");
				enqueueMessage(userOffline);
				messageDaoImpl.createUserAndGroupMessage(msg);
				sendMessageToUser(msg, user);
			}
		}
		String subpoenaTarget = "#" + target;
		if (Prattle.activeSubpoenaUser(subpoenaTarget)) {
			sendWireTapMessage(subpoenaTarget, msg);
		}

	}

	/**
	 * sends message to an active user
	 * @param msg message to be sent
	 * @param user receiver
	 */
	private void sendMessageToActiveUser(Message msg, String user) {
		msg.setMessageSeen(true);
		messageDaoImpl.createUserAndGroupMessage(msg);
		sendMessageToUser(msg, user);
	}

	/**
	 * sends message to an active subpoena user
	 * @param msg message to be sent
	 * @param user receiver
	 */
	private void sendMessageToActiveSubpoenaUser(Message msg, String user) {
		msg.setMessageSeen(true);
		subpoenaDaoImpl.createMessage(msg);
		sendMessageToUser(msg, user);
	}

	/**
	 * Helper function to message a user.
	 * @param msg the message to send
	 * @param user to send the message to
	 */
	private void sendMessageToUser(Message msg, String user) {
		if (Prattle.activeUser(user)) {
			Prattle.messageUser(user, msg);
		} else {
			// Handle incorrect user for target, for now this is empty.
		}
	}

	/**
	 * Parse a body of a message for the target group for message
	 * @param body the text to parse
	 * @return the target group name
	 * @throws AssertionError if the first character is not a # to denote a target group.
	 */
	private String parseMessageTargetGroup(String body) throws AssertionError {
		String[] tokens = body.split(" ");
		String firstToken = tokens[0];
		assert firstToken.charAt(0) == '#';
		return firstToken.substring(1);
	}

	/**
	 * Parse string from a direct message for a target user.
	 * @param body is the message text to parse The body leads with the target user with '@' before
	 * the name. Example: "@TargetUser Hi, the remaining tokens are the message"
	 * @return the target user in the message. Example: "TargetUser"
	 * @throws AssertionError if the message's target is not formatted correctly with '@', an
	 * assertion error is thrown.
	 */
	private String parseMessageTargetUser(String body) throws AssertionError{
		String[] tokens = body.split(" ");
		String firstToken = tokens[0];
		assert firstToken.charAt(0) == '@';
		return firstToken.substring(1);
	}

	/**
	 * Check if message is broadcast, else enqueue the message
	 * @param msg the Message to be checked
	 */
	private void broadcastMessageCheck(Message msg) {
		// Check if the message is legal formatted
		if (messageChecks(msg)) {
			// Check for our "special messages"
			broadCastMessage(msg);
		} else {
			Message sendMsg;
			sendMsg = Message.makeBroadcastMessage(ServerConstants.BOUNCER_ID,
					"Last message was rejected because it specified an incorrect user name.");
			enqueueMessage(sendMsg);
		}
	}


	/**
	 * Check for response from Client
	 */
	private void checkForClient() {
		if (!immediateResponse.isEmpty()) {
			while (!immediateResponse.isEmpty()) {
				sendMessage(immediateResponse.remove());
			}
		}
	}

	/**
	 * end the responses to any special messages we were asked.
	 * @param processSpecial True if the process is a special process
	 * @param keepAlive True if process is alive
	 * @return keepAlive flag after receiving the response
	 */
	private boolean specialMessageResponses(boolean processSpecial, boolean keepAlive) {

		if (processSpecial) {
			// Send all of the messages and check that we get valid
			// responses.
			while (!specialResponse.isEmpty()) {
				keepAlive |= sendMessage(specialResponse.remove());
			}
		}
		return keepAlive;
	}

	/**
	 * Send out all of the message that have been added to the queue.
	 * @param processSpecial True if process is a special process
	 * @param keepAlive flag for if the process is to be kept alive or not
	 * @return Value of keepAlive after sending all messages
	 */
	private boolean sendMessagesToQueue(boolean processSpecial, boolean keepAlive) {
		if (!waitingList.isEmpty()) {
			if (!processSpecial) {
				keepAlive = false;
			}
			// Send out all of the message that have been added to the
			// queue.
			do {
				Message msg = waitingList.remove();
				boolean sentGood = sendMessage(msg);
				keepAlive |= sentGood;
			} while (!waitingList.isEmpty());
		}
		return keepAlive;
	}

	/**
	 * Store the object used by this client runnable to control when it is scheduled
	 * for execution in the thread pool.
	 *
	 * @param future Instance controlling when the runnable is executed from within
	 *               the thread pool.
	 */
	public void setFuture(ScheduledFuture<ClientRunnable> future) {
		runnableMe = future;
	}

	/**
	 * Terminate a client that we wish to remove. This termination could happen at
	 * the client's request or due to system need.
	 */
	public void terminateClient() {
		try {
			// Once the communication is done, close this connection.
			input.close();
			socket.close();
		} catch (IOException e) {
			// If we have an IOException, ignore the problem
			logger.error(e.getMessage());
		} finally {
			// Remove the client from our client listing.
			Prattle.removeClient(this);
			// And remove the client from our client pool.
			runnableMe.cancel(false);
		}
	}
	
	/**
	 * gets client ip address
	 * @return client ip address
	 */
	public String getClientIpAddress() {
		return clientIpAddress;
	}

	/**
	 * sets client ip address
	 * @param clientIpAddress
	 */
	public void setClientIpAddress(String clientIpAddress) {
		this.clientIpAddress = clientIpAddress;
	}

	/**
	 * Get the messagefilter state for this user.
	 * @return the message filter state.
	 */
	private boolean checkFilter() {
		return messagefilter;
	}
}
