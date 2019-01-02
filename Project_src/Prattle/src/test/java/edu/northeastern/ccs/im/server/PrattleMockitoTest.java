package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.ScanNetNB;
import edu.northeastern.ccs.im.users.User;
import edu.northeastern.ccs.im.users.UserDAOImplApi;
import edu.northeastern.ccs.im.users.UserGroupDAOImplApi;
import junit.framework.Assert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Sourabh Punja on 11/4/2018
 */
public class PrattleMockitoTest {

	/**
	 * Testing Prattle private methods getNextKeyIterator() and handleClientRequest()
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
    @Test
    void testPrattlePrivateMethods() throws InvocationTargetException, IllegalAccessException, IOException {
        Method[] methods = Prattle.class.getDeclaredMethods();
        for (Method method : methods) {
            method.setAccessible(true);
        }
        for (Method method : methods) {
            if (method.getName().equals("getNextKeyIterartor")) {
                Iterator<SelectionKey> it = mock(Iterator.class);
                ServerSocketChannel serverSocket = mock(ServerSocketChannel.class);
                SelectionKey key = mock(SelectionKey.class);
                when(key.isAcceptable()).thenReturn(true);
                when(key.channel()).thenReturn(serverSocket);
                when(it.next()).thenReturn(key);
                method.invoke(null, it, serverSocket);
            }

            if (method.getName().equals("handleClientRequest")) {
                ServerSocketChannel serverSocket = mock(ServerSocketChannel.class);
                ServerSocketChannel serverSocket2 = mock(ServerSocketChannel.class);
                ScheduledExecutorService threadPool = mock(ScheduledExecutorService.class);
                SocketChannel socketChannel = mock(SocketChannel.class);
                when(serverSocket.accept()).thenThrow(AssertionError.class);
                method.invoke(null, serverSocket, threadPool);
                when(serverSocket2.accept()).thenReturn(socketChannel);
                method.invoke(null, serverSocket2, threadPool);
            }
        }
    }

    /**
     * test direct messages
     * @throws IOException
     */
    @Test
    void testDirectedMessages() throws IOException {

        // Mocking the first Client runnable for user "rak"
        MockitoClient rak = new MockitoClient("rak", "12345");

        // Mocking the first Client runnable for user "oz"
        MockitoClient oz = new MockitoClient("oz", "password");
        
        // Mocking the first Client runnable for user "punj"
        MockitoClient punj = new MockitoClient("punj", "password");
        
        // Mocking the first Client runnable for user "ram"
        MockitoClient ram = new MockitoClient("ram", "password");

        // Sending directMessage from rakto oz
        Message directMessage = Message.makeDirectMessage("rak", "@oz Hi");
        rak.composeMessage(directMessage);
        rak.run();

        oz.getWaitingList().poll();
        Assert.assertEquals("Check if the useer is getting the message",directMessage,oz.getWaitingList().peek());
        
        //Testing search by attribute
        Message msg1 = Message.makeSearchByAttributes("punj", "/search sender Rake");
        punj.composeMessage(msg1);  
        punj.run();
        
        Message msg2 = Message.makeSearchByAttributes("punj", "/search receiver punja fromTime 2018-11-30,23:21:30 toTime 2018-11-30,23:21:30");
        punj.composeMessage(msg2);  
        punj.run();

        //Testing Recall message
        Message msgRecall = Message.makeRecallMessage("rak", "/recall receiver punja text @punja hellowww");
        rak.composeMessage(msgRecall);  
        rak.run();
    }

    /**
     * test group create and delete
     * @throws IOException
     */
    @Test
    void testGroupCreateAndDelete() throws IOException {
        MockitoClient rak = new MockitoClient("rak", "12345");

        UserGroupDAOImplApi api = new UserGroupDAOImplApi();

        String group = "DUMMYGROUP";

        Message groupCreate = Message.makeGroupModifyMessage("rak", "/group create "+ group);
        rak.composeMessage(groupCreate);

        rak.setFutureCancel();

        rak.run();

        Assert.assertTrue(api.fetchUserGroupNames().contains(group));

        Message groupDelete = Message.makeGroupModifyMessage("rak", "/group delete "+ group);
        rak.composeMessage(groupDelete);

        rak.run();
        UserGroupDAOImplApi api2 = new UserGroupDAOImplApi();
        Assert.assertFalse(api2.fetchUserGroupNames().contains(group));
    }

    /**
     * test add and remove from user group
     * @throws IOException
     */
    @Test
    void testAddAndRemoveUserFromGroup() throws IOException {
        // Mocking the first Client runnable for user "rak"
        MockitoClient rak = new MockitoClient("rak", "12345");
        rak.run();

        UserGroupDAOImplApi api = new UserGroupDAOImplApi();

        String group = "DUMMYGROUP";
        String user = "oz";

        Message message = Message.makeGroupModifyMessage("rak", "/group create "+ group);
        rak.composeMessage(message);
        rak.run();

        rak.setFutureCancel();


        Assert.assertTrue(api.fetchUserGroupNames().contains(group));
        Assert.assertFalse(api.fetchUsersInGroup(group).contains(user));

        message = Message.makeGroupModifyMessage("rak", "/group adduser "+ group +" " + user);
        rak.composeMessage(message);
        rak.run();
        rak.run();

        Assert.assertTrue(api.fetchUsersInGroup(group).contains(user));


        message = Message.makeGroupModifyMessage("rak", "/group deleteuser "+ group +" " + user);
        rak.composeMessage(message);
        rak.run();
        rak.run();

        Assert.assertFalse(api.fetchUsersInGroup(group).contains(user));

    }

    /**
     * test initialization
     * @throws IOException
     */
    @Test
    void testInitialization() throws IOException{
        //Assert.assertFalse(Prattle.activeUser("rak"));
        // Mocking the first Client runnable for user "rak"
        MockitoClient rak = new MockitoClient("rak", "12345");
        rak.run();
        Assert.assertTrue(Prattle.activeUser("rak"));
    }

    /**
     * test init for new user
     * @throws IOException
     */
    @Test
    void testInitializationForNewUser() throws IOException{
        MockitoClient client = new MockitoClient("newuser40", "12345");
        client.run();

        UserDAOImplApi api = new UserDAOImplApi();

        Assert.assertTrue(api.fetchUserNames().contains("newuser40"));

        api.deleteUser(new User("newuser40").getUserName());

        Assert.assertFalse(api.fetchUserNames().contains("newuser40"));
    }
    /**
     * test messages for wire tap
     * @throws IOException
     */

    @Test
    void testMessagesForWireTapClient() throws IOException{
        MockitoClient admin = new MockitoClient("admin", "admin");
        admin.run();

        Message message = Message.makeAdminMessage("admin", "createSubpoenaUsers rak oz subpoena");
        admin.composeMessage(message);
        admin.run();

        MockitoClient oz = new MockitoClient("oz", "12345");
        Message directMessage = Message.makeDirectMessage("oz", "@rak hi rak");

        MockitoClient subpoena = new MockitoClient("subpoena", "12345");

        oz.composeMessage(directMessage);
        oz.run();

        message = Message.makeAdminMessage("admin", "removeSubpoenaUsers rak oz subpoena");
        admin.composeMessage(message);
        admin.run();


        //drop the login message
        subpoena.waitingList.poll();
        //check we saw the message between the users.
        String msg1 = subpoena.waitingList.peek().toString();
        String msg2 = directMessage.toString();
        Assert.assertEquals(msg1,msg2);


    }
    /**
     * test message for wiretap group
     * @throws IOException
     */
    @Test
    void testMessagesForWireTapGroup() throws IOException{
        MockitoClient admin = new MockitoClient("admin", "admin");
        admin.run();

        Message message = Message.makeAdminMessage("admin", "createSubpoenaGroup #testGroup subpoena");
        admin.composeMessage(message);
        admin.run();

        MockitoClient punja = new MockitoClient("punja", "12345");
        Message groupMessage = Message.makeGroupMessage("punja", "#testGroup hi group");

        MockitoClient subpoena = new MockitoClient("subpoena", "12345");

        punja.composeMessage(groupMessage);
        punja.run();

        message = Message.makeAdminMessage("admin", "removeSubpoenaGroup #testGroup subpoena");
        admin.composeMessage(message);
        admin.run();
        
        message = Message.makeAdminMessage("admin", "logger off");
        admin.composeMessage(message);
        admin.run();
        org.junit.Assert.assertEquals("OFF", LogManager.getLogger().getLevel().toString());
        
        message = Message.makeAdminMessage("admin", "logger on");
        admin.composeMessage(message);
        admin.run();
        org.junit.Assert.assertEquals("INFO", LogManager.getLogger().getLevel().toString());


        //drop the login message
        subpoena.waitingList.poll();
        //check we saw the message between the users.
        String msg1 = subpoena.waitingList.peek().toString();
        String msg2 = groupMessage.toString();
        Assert.assertEquals(msg1,msg2);


    }

    /**
     * test invalid admin command
     * @throws IOException
     */
    @Test
    void testInvalidAdminCommand() throws IOException{
        MockitoClient admin = new MockitoClient("admin", "admin");
        admin.run();


        admin.composeMessage(Message.makeAdminMessage("admin", "invalidcommand text blah blah"));
        admin.run();
        admin.waitingList.peek();

    }

    /**
     * Mockito client
     * @author shweta
     *
     */
    private class MockitoClient {

        SocketChannel ssc;
        Queue<Message> immediateResponse;
        Queue<Message> specialResponse;
        Queue<Message> waitingList;
        boolean initialized;
        GregorianCalendar terminateInactivity;
        ScanNetNB scanNetNB;
        ScheduledFuture<ClientRunnable> future;
        ClientRunnable clientRunnable;

/**
 * constructor for mockito client
 * @param username
 * @param password
 * @throws IOException
 */
        public MockitoClient(String username, String password) throws IOException {
            ssc = mock(SocketChannel.class, Mockito.RETURNS_DEEP_STUBS);
            immediateResponse = new LinkedList<>();
            specialResponse = new LinkedList<>();
            waitingList = new ConcurrentLinkedQueue<>();
            initialized = false;
            terminateInactivity = new GregorianCalendar();
            scanNetNB = mock(ScanNetNB.class);
            future = mock(ScheduledFuture.class);

            when(scanNetNB.hasNextMessage()).thenReturn(true);
            Message loginMessage = Message.makeSimpleLoginMessage(username, password);
            when(scanNetNB.nextMessage()).thenReturn(loginMessage);
            clientRunnable = new ClientRunnable(ssc, immediateResponse, specialResponse, waitingList, initialized, terminateInactivity, scanNetNB);
            clientRunnable.setFuture(future);
            when(ssc.socket().getInetAddress().getHostAddress()).thenReturn("123:0:0:0");
            when(future.cancel(false)).thenReturn(true);
            clientRunnable.run();
            Prattle.active.add(clientRunnable);
        }

        /**
         * compose a message
         * @param msg
         */
        public void composeMessage(Message msg) {
            when(scanNetNB.hasNextMessage()).thenReturn(true);
            when(scanNetNB.nextMessage()).thenReturn(msg);
        }

        /**
         * runs the client runnable
         */
        public void run() {
            clientRunnable.run();
        }

        /**
         * gets the enqueued messages
         * @return
         */
        public Queue<Message> getWaitingList() {
            return waitingList;
        }

        /**
         * set future cancel
         */
        public void setFutureCancel() {
            ScheduledFuture<ClientRunnable> future = mock(ScheduledFuture.class);
            clientRunnable.setFuture(future);
            when(future.cancel(false)).thenReturn(true);

            //clientRunnable.run();
        }
    }
}
