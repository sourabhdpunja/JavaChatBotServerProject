package edu.northeastern.ccs.im.server;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import edu.northeastern.ccs.im.Message;

public class ServerConstantsTest {
	protected static final String HELLO_COMMAND = "Hello";

	/** Command to ask about how things are going. */
	protected static final String QUERY_COMMAND = "How are you?";

	/** Command that showing the professor is hip (or is that hep?). */
	protected static final String COOL_COMMAND = "WTF";

	/** Command for impatient users */
	protected static final String IMPATIENT_COMMAND = "What time is it Mr. Fox?";

	/** Message to find the date. */
	protected static final String DATE_COMMAND = "What is the date?";

	/** Message to find the time. */
	protected static final String TIME_COMMAND = "What time is it?";
	
	/**
	 * Tests the server constants by broadcasting different types of messages
	 */
	@Test
	public void test() {
		Message message = Message.makeAcknowledgeMessage("source");
		ArrayList<Message> messages = ServerConstants.getBroadcastResponses("source");
		messages = ServerConstants.getBroadcastResponses(HELLO_COMMAND);
		assertFalse(messages.isEmpty());
		messages = ServerConstants.getBroadcastResponses(DATE_COMMAND);
		assertFalse(messages.isEmpty());
		messages = ServerConstants.getBroadcastResponses(TIME_COMMAND);
		assertFalse(messages.isEmpty());
		messages = ServerConstants.getBroadcastResponses(IMPATIENT_COMMAND);
		assertFalse(messages.isEmpty());
		messages = ServerConstants.getBroadcastResponses(COOL_COMMAND);
		assertFalse(messages.isEmpty());
		messages = ServerConstants.getBroadcastResponses(QUERY_COMMAND);
		assertFalse(messages.isEmpty());
	}

}
