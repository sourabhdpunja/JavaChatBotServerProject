package edu.northeastern.ccs.im;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.northeastern.ccs.im.users.User;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author oz These are the tests for the Messages in the Prattle server.
 */

public class MessageTest {


    /**
     * Generate all the different message types for use in further tests.
     */
    // No acknowledge message
    private Message noAcknowledgeMessage;
    // Hello Message
    private Message m;
    // Acknowledge Message
    private Message acknowledgeMessage;
    // Simple Login Message
    private Message simpleLoginMessage;
    // BroadCast Message
    private Message broadcastMessage;
    // Quit Message
    private Message quitMessage;
    //User Message
    private Message userMessage;

    @BeforeEach
    public void setup() {
        noAcknowledgeMessage = Message.makeNoAcknowledgeMessage();
        m = Message.makeHelloMessage("HI");
        acknowledgeMessage = Message.makeAcknowledgeMessage("hi");
        simpleLoginMessage = Message.makeSimpleLoginMessage("LOGIN");
        broadcastMessage = Message.makeBroadcastMessage("Broadcast Test", "TEST");
        quitMessage = Message.makeQuitMessage("Quitting");
        userMessage = Message.makeUserMessage("rock", "how are you");
        
    }

    /**
     * Test that the contents and name of each message type was completed correctly.
     */
    @Test
    public void initializeTest() {

        assertNull(noAcknowledgeMessage.getName());
        assertNull(noAcknowledgeMessage.getText());

        assertEquals("HI", m.getText());
        assertNull(m.getName());

        assertEquals("hi", acknowledgeMessage.getName());
        assertNull(acknowledgeMessage.getText());

        assertEquals("LOGIN", simpleLoginMessage.getName());
        assertNull(simpleLoginMessage.getText());

        assertEquals("TEST", broadcastMessage.getText());
        assertEquals("Broadcast Test", broadcastMessage.getName());

        assertEquals("Quitting", quitMessage.getName());
        assertNull(quitMessage.getText());
    }


    /**
     * Test where each MessageType is set correctly upon creation.
     */
    @Test
    public void testMessageType() {
        setup();
        //noAcknowledge Message
        assertFalse(noAcknowledgeMessage.isAcknowledge());
        assertFalse(noAcknowledgeMessage.isBroadcastMessage());
        assertFalse(noAcknowledgeMessage.isDisplayMessage());
        assertFalse(noAcknowledgeMessage.isInitialization());
        assertFalse(noAcknowledgeMessage.terminate());

        // Hello Message should only be considered an initialization Message
        assertFalse(m.isAcknowledge());
        assertFalse(m.isBroadcastMessage());
        assertFalse(m.isDisplayMessage());
        assertTrue(m.isInitialization());
        assertFalse(m.terminate());

        // Acknowledge Message should only be considered an acknowledge message
        assertTrue(acknowledgeMessage.isAcknowledge());
        assertFalse(acknowledgeMessage.isBroadcastMessage());
        assertFalse(acknowledgeMessage.isDisplayMessage());
        assertFalse(acknowledgeMessage.isInitialization());
        assertFalse(acknowledgeMessage.terminate());

        // Login Message is initialization type
        assertFalse(simpleLoginMessage.isAcknowledge());
        assertFalse(simpleLoginMessage.isBroadcastMessage());
        assertFalse(simpleLoginMessage.isDisplayMessage());
        assertFalse(simpleLoginMessage.terminate());
        assertTrue(simpleLoginMessage.isInitialization());

        // Broadcast Message is display and broadcast message
        assertFalse(broadcastMessage.isAcknowledge());
        assertTrue(broadcastMessage.isBroadcastMessage());
        assertTrue(broadcastMessage.isDisplayMessage());
        assertFalse(broadcastMessage.isInitialization());
        assertFalse(broadcastMessage.terminate());

        // Quit message is none of these types
        assertFalse(quitMessage.isAcknowledge());
        assertFalse(quitMessage.isBroadcastMessage());
        assertFalse(quitMessage.isDisplayMessage());
        assertFalse(quitMessage.isInitialization());
        assertTrue(quitMessage.terminate());
        
        //User message
        assertTrue(userMessage.isUserMessage());

    }

    /**
     * Test the make message, that takes a handle as a string.
     */
    @Test
    public void testMakeMessage() {
        Message m1 = Message.makeMessage("BYE", "1", "1");
        Message m2 = Message.makeMessage("HLO", "1", "1");
        Message m3 = Message.makeMessage("BCT", "1", "1");
        Message m4 = Message.makeMessage("ACK", "1", "1");
        Message m5 = Message.makeMessage("NAK", "1", "1");
        Message m6 = Message.makeMessage("GMS", "punja", "#batmangroup hey!");
        Message m7 = Message.makeMessage("GMY", "punja", "/group adduser testGroup lex");
        Message m8 = Message.makeMessage("SBA", "punja", "/group adduser testGroup lex");
        Message m9 = Message.makeMessage("USR", "punja", "hey");
        Message m10 = Message.makeMessage("RCL", "punja", "/recall");

        assertEquals("BYE 1 1 2 --", m1.toString());
        assertEquals("HLO 1 1 1 1", m2.toString());
        assertEquals("BCT 1 1 1 1", m3.toString());
        assertEquals("ACK 1 1 2 --", m4.toString());
        assertEquals("NAK 2 -- 2 --", m5.toString());
        assertEquals("GMS 5 punja 17 #batmangroup hey!", m6.toString());
        assertEquals("GMY 5 punja 28 /group adduser testGroup lex", m7.toString());
        assertEquals("SBA 5 punja 28 /group adduser testGroup lex", m8.toString());
        assertEquals("USR 5 punja 3 hey", m9.toString());
        assertEquals("RCL 5 punja 7 /recall", m10.toString());
    }
    
    /**
     * Tests User password setting
     */
    @Test
    public void testUserPassword() {
    	Message login = Message.makeMessage("HLO", "shweta", "oak");
    	Message login2 = Message.makeMessage("HLO", "shweta", null);
    	Message login3 = Message.makeMessage("BCT", "shweta", "hi");
    	User user = new User(login.getName());
    	User user2 = new User(login2.getName());
    	User user3 = new User(login3.getName());
    	user.setUserPassword(login.getPassword());
    	user2.setUserPassword(login2.getPassword());
    	user3.setUserPassword(login3.getPassword());
    	assertEquals(user2.getUserPassword(), "shweta");
    	assertEquals(login.getText(), user.getUserPassword());
    	assertEquals(null, user3.getUserPassword());
    }

    /**
     * Tests that the toString() method is functioning correctly for all message types.
     */
    @Test
    public void testToString() {
        setup();
        assertEquals("NAK 2 -- 2 --", noAcknowledgeMessage.toString());
        assertEquals("ACK 2 hi 2 --", acknowledgeMessage.toString());
        assertEquals("HLO 2 -- 2 HI", m.toString());
        assertEquals("BCT 14 Broadcast Test 4 TEST", broadcastMessage.toString());
        assertEquals("BYE 8 Quitting 2 --", quitMessage.toString());
    }

    /**
     * testing user message
     */
    @Test
    public void testUserMessage() {
        Message userMessage = Message.makeMessage("USR","Sou", "Hi Sourabh");
        userMessage.setMessageSeen(true);
        assertTrue(userMessage.isMessageSeen());
        assertEquals("USR 3 Sou 10 Hi Sourabh", userMessage.toString());
    }

    /**
     * testing direct message
     */
    @Test
    public void testDirectMessage() {
        Message directMessage = Message.makeDirectMessage("oz", "@Rakesh hi rakesh");
        assertEquals("DMS 2 oz 17 @Rakesh hi rakesh", directMessage.toString());
    }

    /**
     * test group message
     */
    @Test
    public void testGroupMessage() {
        Message groupMessage = Message.makeGroupMessage("oz", "#MSD212 Hi group!!");
        assertEquals("GMS 2 oz 18 #MSD212 Hi group!!", groupMessage.toString());
    }
    /**
     * Test equals and hash code.
     */
    @Test
    public void testEqualsAndHashCode() {
        Message message1 = Message.makeMessage("BCT", "oz", "HI");
        Message message2 = Message.makeMessage("BCT", "oz", "HI");
        Message message3 = Message.makeGroupMessage( "po", "HI");
        assertTrue(message3.isGroupMessage());
        message1.getMessageId();
        assertTrue(message1.equals(message2) && message2.equals(message1));
        assertTrue(message1.hashCode() == message2.hashCode());
        assertFalse(message1.equals(message3) && message3.equals(message1));
        assertFalse(message1.hashCode() == message3.hashCode());
    }

}
