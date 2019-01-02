package edu.northeastern.ccs.im;

/**
 * Each instance of this class represents a single transmission by our IM clients.
 * <p>
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International
 * License. To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/. It
 * is based on work originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 *
 * @version 1.3
 */
public class Message {


	public boolean isDirectMessage() {
        return msgType == MessageType.DIRECTMESSAGE;
    }

    public boolean isGroupMessage() {
        return msgType == MessageType.GROUPMESSAGE;
    }

	public boolean isGroupModifyMessage() { return msgType == MessageType.GROUPMODIFY;
	}

	public boolean isAdminMessage() { return msgType == MessageType.ADMIN;
	}
	
    /**
     * Checks if is search by attributes.
     *
     * @return true, if is search by attributes
     */
    public boolean isSearchByAttributes() {
        return msgType == MessageType.SEARCHBYATTRIBUTES;
    }
    
    /**
     * Checks if is recall message.
     *
     * @return true, if is recall message
     */
    public boolean isRecallMessage() {
        return msgType == MessageType.RECALL;
    }

	/**
     * List of the different possible message types.
     */
    protected enum MessageType {
		/**
		 * Message sent by the user attempting to login using a specified username.
		 */
		HELLO("HLO"),
		/**
		 * Message sent by the server acknowledging a successful log in.
		 */
		ACKNOWLEDGE("ACK"),
		/**
		 * Message sent by the server rejecting a login attempt.
		 */
		NO_ACKNOWLEDGE("NAK"),
		/**
		 * Message sent by the user to start the logging out process and sent by the
		 * server once the logout process completes.
		 */
		QUIT("BYE"),
		/**
		 * Message type of group message.
		 */
		DIRECTMESSAGE("DMS"),
		/**
		 * Message whose contents is broadcast to all connected users.
		 */
		BROADCAST("BCT"),
		/**
		 * Message type for group modification.
		 */
		GROUPMODIFY("GMY"),

        /**
         * Message type for the sender to recieve messages of acknowledgement with text.
         */
        USERMESSAGE("USR"),

		/**
		 * Message type for group messaging.
		 */
        GROUPMESSAGE("GMS"),

		ADMIN("ADM"),
    	
    	/** The searchbyattributes. */
	    SEARCHBYATTRIBUTES("SBA"),
    	
	    /** The recall. */
    	RECALL("RCL");

        /**
         * Store the short name of this message type.
         */
        private String tla;

        /**
         * Define the message type and specify its short name.
         *
         * @param abbrev Short name of this message type, as a String.
         */
        private MessageType(String abbrev) {
            tla = abbrev;
        }

        /**
         * Return a representation of this Message as a String.
         *
         * @return Three letter abbreviation for this type of message.
         */
        @Override
        public String toString() {
            return tla;
        }
    }

    /**
     * The string sent when a field is null.
     */
    private static final String NULL_OUTPUT = "--";

	public MessageType getMsgType() {
		return msgType;
	}
	
	/**
	 * gets msg handle
	 * @return messege type
	 */

	public String getMsgHandle() {
		return msgType.toString();
	}

	/** The handle of the message. */
	private MessageType msgType;

    /**
     * The first argument used in the message. This will be the sender's identifier.
     */
    private String msgSender;

	private String msgReceiver;

	/**
	 * get receiver message
	 * @return receiver msg
	 */
	public String getMsgReceiver() {
		return msgReceiver;
	}

	/**
	 * set receiver msg
	 * @param msgReceiver
	 */
	public void setMsgReceiver(final String msgReceiver) {
		this.msgReceiver = msgReceiver;
	}

	/**
	 * get msg group
	 * @return msg group
	 */
	public String getMsgGroup() {
		return msgGroup;
	}
	
	/**
	 * set msg group
	 * @param msgGroup
	 */

	public void setMsgGroup(final String msgGroup) {
		this.msgGroup = msgGroup;
	}

	private String msgGroup;

	/** The second argument used in the message. */
	private String msgText;

	/**
	 * set msg id
	 * @param messageId
	 */
	public void setMessageId(final int messageId) {
		this.messageId = messageId;
	}

	private int messageId;

	/**
	 * set msg seen
	 * @param messageSeen
	 */
    public void setMessageSeen(final boolean messageSeen) {
        isMessageSeen = messageSeen;
    }

    /**
     * check if msg is seen
     * @return
     */
	public boolean isMessageSeen() {
		return isMessageSeen;
	}
	
	private boolean isFlagged = false;

	/** The created timestamp. */
	private String createdTimestamp;

	// Boolean to check if the message is online and has been sent to the user.
    // If not sent to user then set to false
	private boolean isMessageSeen;
	
	/** The ip address. */
	private String senderIpAddress;

	/** The receiver ip address. */
	private String receiverIpAddress;

	/**
	 * get subpoena user
	 * @return subponea user
	 */
	public String getSubpoenaUser() {
		return subpoenaUser;
	}

	/**
	 * set subpoenauser
	 * @param subpoenaUser
	 */
	public void setSubpoenaUser(final String subpoenaUser) {
		this.subpoenaUser = subpoenaUser;
	}
	
	private boolean isRecalled;
	private String subpoenaUser;

	/**
	 * Create a new message that contains actual IM text. The type of distribution
	 * is defined by the handle and we must also set the name of the message sender,
	 * message recipient, and the text to send.
	 * 
	 * @param handle  Handle for the type of message being created.
	 * @param srcName Name of the individual sending this message
	 * @param text    Text of the instant message
	 */
	private Message(MessageType handle, String srcName, String text) {
		msgType = handle;
		// Save the properly formatted identifier for the user sending the
		// message.
		msgSender = srcName;
		// Save the text of the message.
		msgText = text;
	}

	/**
	 * Generate a direct message from a user. The target user, should be in the message text.
	 * @param myName of the sender
	 * @param text the text of the message. Should lead with @TargetUser Message text
	 * @return The message that is created.
	 */
	public static Message makeDirectMessage(String myName, String text) {
		return new Message(MessageType.DIRECTMESSAGE, myName, text);
	}

	/**
	 * Generate a group message from a user. The target group should be in the message text.
	 * @param myName of the sender
	 * @param text the text of the message. Should lead with #GroupName Message Text
	 * @return the generated message
	 */
	public static Message makeGroupMessage(String myName, String text) {
		return new Message(MessageType.GROUPMESSAGE, myName, text);
	}

	/**
	 * Create simple command type message that does not include any data.
	 * 
	 * @param handle Handle for the type of message being created.
	 */
	private Message(MessageType handle) {
		this(handle, null, null);
	}
	
	/**
	 * Make search by attributes.
	 *
	 * @param myName the my name
	 * @param text the text
	 * @return the message
	 */
	public static Message makeSearchByAttributes(String myName, String text) {
		return new Message(MessageType.SEARCHBYATTRIBUTES, myName, text);
	}
	
	/**
	 * Make recall message.
	 *
	 * @param myName the my name
	 * @param text the text
	 * @return the message
	 */
	public static Message makeRecallMessage(String myName, String text) {
		return new Message(MessageType.RECALL, myName, text);
	}

	/**
	 * Create a new message that contains a command sent the server that requires a
	 * single argument. This message contains the given handle and the single
	 * argument.
	 * 
	 * @param handle  Handle for the type of message being created.
	 * @param srcName Argument for the message; at present this is the name used to
	 *                log-in to the IM server.
	 */
	private Message(MessageType handle, String srcName) {
		this(handle, srcName, null);
	}

	/**
	 * Create a new message to continue the logout process.
	 * 
	 * @return Instance of Message that specifies the process is logging out.
	 */
	public static Message makeQuitMessage(String myName) {
		return new Message(MessageType.QUIT, myName, null);
	}

	/**
	 * Create a new message broadcasting an announcement to the world.
	 * 
	 * @param myName Name of the sender of this very important missive.
	 * @param text   Text of the message that will be sent to all users
	 * @return Instance of Message that transmits text to all logged in users.
	 */
	public static Message makeBroadcastMessage(String myName, String text) {
		return new Message(MessageType.BROADCAST, myName, text);
	}

	/**
	 * Create a new message stating the name with which the user would like to
	 * login.
	 * 
	 * @param text Name the user wishes to use as their screen name.
	 * @return Instance of Message that can be sent to the server to try and login.
	 */
	protected static Message makeHelloMessage(String text) {
		return new Message(MessageType.HELLO, null, text);
	}

	/**
	 * Given a handle, name and text, return the appropriate message instance or an
	 * instance from a subclass of message.
	 * 
	 * @param handle  Handle of the message to be generated.
	 * @param srcName Name of the originator of the message (may be null)
	 * @param text    Text sent in this message (may be null)
	 * @return Instance of Message (or its subclasses) representing the handle,
	 *         name, & text.
	 */
	public static Message makeMessage(String handle, String srcName, String text) {
		Message result = null;
		if (handle.compareTo(MessageType.QUIT.toString()) == 0) {
			result = makeQuitMessage(srcName);
		} else if (handle.compareTo(MessageType.HELLO.toString()) == 0) {
			result = makeLoginMessage(srcName, text);
		} else if (handle.compareTo(MessageType.BROADCAST.toString()) == 0) {
			result = makeBroadcastMessage(srcName, text);
		} else if (handle.compareTo(MessageType.ACKNOWLEDGE.toString()) == 0) {
			result = makeAcknowledgeMessage(srcName);
		} else if (handle.compareTo(MessageType.NO_ACKNOWLEDGE.toString()) == 0) {
			result = makeNoAcknowledgeMessage();
		} else if (handle.compareTo(MessageType.DIRECTMESSAGE.toString()) == 0) {
			result = makeDirectMessage(srcName, text);
		} else if (handle.compareTo(MessageType.GROUPMESSAGE.toString()) == 0) {
			result = makeGroupMessage(srcName, text);
		} else if (handle.compareTo(MessageType.GROUPMODIFY.toString()) == 0) {
			result = makeGroupModifyMessage(srcName, text);
		} else if (handle.compareTo(MessageType.SEARCHBYATTRIBUTES.toString()) == 0) {
			result = makeSearchByAttributes(srcName, text);
		}else if (handle.compareTo(MessageType.USERMESSAGE.toString()) == 0) {
		    result = makeUserMessage(srcName, text);
        }else if (handle.compareTo(MessageType.ADMIN.toString()) == 0) {
			result = makeAdminMessage(srcName, text);
        } else if (handle.compareTo(MessageType.RECALL.toString()) == 0) {
		    result = makeRecallMessage(srcName, text);
        }
		return result;
	}

	/*
	 * sets flag to true
	 */
	public void flagMsg() {
		isFlagged = true;
	}
	
	/**
	 * Make a groupmodift message
	 * @param srcName is the sender
	 * @param text contents of the message
	 * @return the message created
	 */
	public static Message makeGroupModifyMessage(String srcName, String text) {
		return new Message(MessageType.GROUPMODIFY, srcName, text);
	}

	/**
	 * Create a new message to reject the bad login attempt.
	 * 
	 * @return Instance of Message that rejects the bad login attempt.
	 */
	public static Message makeNoAcknowledgeMessage() {
		return new Message(MessageType.NO_ACKNOWLEDGE);
	}

	/**
	 * Create a new message to acknowledge that the user successfully logged as the
	 * name <code>srcName</code>.
	 * 
	 * @param srcName Name the user was able to use to log in.
	 * @return Instance of Message that acknowledges the successful login.
	 */
	public static Message makeAcknowledgeMessage(String srcName) {
		return new Message(MessageType.ACKNOWLEDGE, srcName);
	}

	public static Message makeAdminMessage(String myName, String text) {
		return new Message(MessageType.ADMIN, myName, text);
	}

	/**
	 * Create a new message for the early stages when the user logs in without all
	 * the special stuff.
	 * 
	 * @param myName Name of the user who has just logged in.
	 * @return Instance of Message specifying a new friend has just logged in.
	 */
	public static Message makeSimpleLoginMessage(String myName) {
		return new Message(MessageType.HELLO, myName);
	}
	public static Message makeSimpleLoginMessage(String myName, String text) {
		return new Message(MessageType.HELLO, myName, text);
	}


	/**
	 * Create a new message for the early stages when the user logs in 
	 * 
	 * @param myName Name of the user who has just logged in.
	 * @return Instance of Message specifying a new friend has just logged in.
	 */
	public static Message makeLoginMessage(String myName, String text) {
		return new Message(MessageType.HELLO, myName, text);
	}

	/**
	 * Return the name of the sender of this message.
	 * 
	 * @return String specifying the name of the message originator.
	 */
	public String getName() {
		return msgSender;
	}
	
	/**
	 * Get password from the HELLO message, else set password as the username by default
	 * @return the Password entered by the user
	 */
	public String getPassword() {
		if (getMsgType() == MessageType.HELLO) {
			String text = getText();
			if (text!= null) {
				return text;
			}
			else {
				return this.getName();   //set username as password by default
			}
		}
		else {
			return null; // for any message type other than HELLO
		}		
		
	}

	/**
	 * Return the text of this message.
	 * 
	 * @return String equal to the text sent by this message.
	 */
	public String getText() {
		return msgText;
	}

	/**
	 * Determine if this message is an acknowledgement message.
	 * 
	 * @return True if the message is an acknowledgement message; false otherwise.
	 */
	public boolean isAcknowledge() {
		return (msgType == MessageType.ACKNOWLEDGE);
	}

	/**
	 * Determine if this message is broadcasting text to everyone.
	 * 
	 * @return True if the message is a broadcast message; false otherwise.
	 */
	public boolean isBroadcastMessage() {
		return (msgType == MessageType.BROADCAST);
	}

	/**
	 * Determine if this message contains text which the recipient should display.
	 * 
	 * @return True if the message is an actual instant message; false if the
	 *         message contains data
	 */
	public boolean isDisplayMessage() {
		return (msgType == MessageType.BROADCAST);
	}

	/**
	 * Determine if this message is sent by a new client to log-in to the server.
	 * 
	 * @return True if the message is an initialization message; false otherwise
	 */
	public boolean isInitialization() {
		return (msgType == MessageType.HELLO);
	}

	/**
	 * Determine if this message is a message signing off from the IM server.
	 * 
	 * @return True if the message is sent when signing off; false otherwise
	 */
	public boolean terminate() {
		return (msgType == MessageType.QUIT);
	}

	public int getMessageId() {
		return messageId;
	}

	/**
	 * Representation of this message as a String. This begins with the message
	 * handle and then contains the length (as an integer) and the value of the next
	 * two arguments.
	 * 
	 * @return Representation of this message as a String.
	 */
	@Override
	public String toString() {
		String result = msgType.toString();
		if (msgSender != null) {
			result += " " + msgSender.length() + " " + msgSender;
		} else {
			result += " " + NULL_OUTPUT.length() + " " + NULL_OUTPUT;
		}
		if (msgText != null) {
			result += " " + msgText.length() + " " + msgText;
		} else {
			result += " " + NULL_OUTPUT.length() + " " + NULL_OUTPUT;
		}
		return result;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof Message)) return false;

		final Message message = (Message) o;

		if (getMsgType() != message.getMsgType()) return false;
		if (msgSender != null ? !msgSender.equals(message.msgSender) : message.msgSender != null) return false;
		if (getMsgReceiver() != null ? !getMsgReceiver().equals(message.getMsgReceiver()) : message.getMsgReceiver() != null)
			return false;
		if (getMsgGroup() != null ? !getMsgGroup().equals(message.getMsgGroup()) : message.getMsgGroup() != null)
			return false;
		return msgText != null ? msgText.equals(message.msgText) : message.msgText == null;
	}

	@Override
	public int hashCode() {
		int result = getMsgType().hashCode();
		result = 31 * result + (msgSender != null ? msgSender.hashCode() : 0);
		result = 31 * result + (getMsgReceiver() != null ? getMsgReceiver().hashCode() : 0);
		result = 31 * result + (getMsgGroup() != null ? getMsgGroup().hashCode() : 0);
		result = 31 * result + (msgText != null ? msgText.hashCode() : 0);
		return result;
	}
	
	   /**
   	 * Make user message.
   	 *
   	 * @param myName the my name
   	 * @param text the text
   	 * @return the message
   	 */
   	public static Message makeUserMessage(String myName, String text) {
	        return new Message(MessageType.USERMESSAGE, myName, text);
	    }
	   
	   /**
   	 * Checks if is user message.
   	 *
   	 * @return true, if is user message
   	 */
   	public boolean isUserMessage() {
	        return (msgType == MessageType.USERMESSAGE);
	   }
   	
	/**
	 * Checks if is recalled.
	 *
	 * @return true, if is recalled
	 */
	public boolean isRecalled() {
		return isRecalled;
	}

	/**
	 * Sets the recalled.
	 *
	 * @param isRecalled the new recalled
	 */
	public void setRecalled(boolean isRecalled) {
		this.isRecalled = isRecalled;
	}
	 /* Gets the created timestamp.
	 *
	 * @return the created timestamp
	 */
	public String getCreatedTimestamp() {
		return createdTimestamp;
	}

	/**
	 * Sets the created timestamp.
	 *
	 * @param createdTimestamp the new created timestamp
	 */
	public void setCreatedTimestamp(String createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}
	
	/**
	 * gets sender ip address
	 * @return sender ip address
	 */
	public String getSenderIpAddress() {
		return senderIpAddress;
	}

	/**
	 * sets sender ip address
	 * @param senderIpAddress
	 */
	public void setSenderIpAddress(String senderIpAddress) {
		this.senderIpAddress = senderIpAddress;
	}

	/**
	 * get receiver ip address
	 * @return receiver ip address
	 */
	public String getReceiverIpAddress() {
		return receiverIpAddress;
	}

	/**
	 * set receiver ip address 
	 * @param receiverIpAddress
	 */
	public void setReceiverIpAddress(String receiverIpAddress) {
		this.receiverIpAddress = receiverIpAddress;
	}
	
}
