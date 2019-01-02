package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.ScanNetNB;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.SocketChannel;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Sourabh Punja on 11/4/2018
 */
public class ClientRunnableTest {

	/**
	 * Test ClientRunnable methods: getters and setters
	 * @throws IOException
	 */
    @Test
    void testClientRunnable() throws IOException {
        SocketChannel ssc = mock(SocketChannel.class);
        Queue<Message> immediateResponse = new LinkedList<>();
        Queue<Message> specialResponse = new LinkedList<>();
        Queue<Message> waitingList = new ConcurrentLinkedQueue<>();
        boolean initialized = false;
        GregorianCalendar terminateInactivity = new GregorianCalendar();
        ScanNetNB scanNetNB = mock(ScanNetNB.class);
        ClientRunnable clientRunnable = new ClientRunnable(ssc, immediateResponse, specialResponse, waitingList, initialized, terminateInactivity, scanNetNB);
        assertFalse(clientRunnable.setUserName(null));
        assertTrue(clientRunnable.setUserName("Sourabh"));
        clientRunnable.enqueueMessage(Message.makeQuitMessage("quit"));
        assertEquals("Sourabh", clientRunnable.getName());
        clientRunnable.setName("Deep");
        assertEquals("Deep", clientRunnable.getName());
        assertFalse(clientRunnable.isInitialized());
        clientRunnable.getUserId();
        ScheduledFuture<ClientRunnable> future = mock(ScheduledFuture.class);
        when(future.cancel(false)).thenReturn(false);
        clientRunnable.setFuture(future);
        doNothing().when(scanNetNB).close();
        doNothing().when(ssc).close();
        clientRunnable.terminateClient();
        doThrow(IOException.class).when(ssc).close();
        doNothing().when(scanNetNB).close();
        clientRunnable.terminateClient();
    }

    /**
     * tests for private methods on passing messages as parameters
     * @throws IOException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @Test
    void testClientRunnablePrivateMethods() throws IOException, InvocationTargetException, IllegalAccessException {
        SocketChannel ssc = mock(SocketChannel.class, Mockito.RETURNS_DEEP_STUBS);
        Queue<Message> immediateResponse = new LinkedList<>();
        Queue<Message> specialResponse = new LinkedList<>();
        Queue<Message> waitingList = new ConcurrentLinkedQueue<>();
        boolean initialized = false;
        GregorianCalendar terminateInactivity = new GregorianCalendar();
        ScanNetNB scanNetNB = mock(ScanNetNB.class);
        ClientRunnable clientRunnable = new ClientRunnable(ssc, immediateResponse, specialResponse, waitingList, initialized, terminateInactivity, scanNetNB);
        Class<? extends ClientRunnable> clientRunnableClass = clientRunnable.getClass();
        Method[] methods = clientRunnableClass.getDeclaredMethods();
        for (Method method : methods) {
            method.setAccessible(true);
        }
        boolean result = false;
        for (Method method : methods) {
            if (method.getName().equals("broadcastMessageIsSpecial")) {
                Message message = Message.makeBroadcastMessage("rak", "Wassup");
                result = (Boolean) method.invoke(clientRunnable, message);
                assertEquals(false, result);
                Message message2 = Message.makeBroadcastMessage("rak", "What is the date?");
                result = (Boolean) method.invoke(clientRunnable, message2);
                assertEquals(true, result);
                Message message3 = Message.makeBroadcastMessage("rak", null);
                result = (Boolean) method.invoke(clientRunnable, message3);
                assertEquals(false, result);
            }

            if (method.getName().equals("broadCastMessage")) {
                Message message4 = Message.makeBroadcastMessage("rak", "Prattle says everyone log off");
                method.invoke(clientRunnable, message4);
                Message message5 = Message.makeBroadcastMessage("rak", "ssup");
                method.invoke(clientRunnable, message5);
            }

            if (method.getName().equals("checkForInitialization")) {
                when(scanNetNB.hasNextMessage()).thenReturn(true);
                Message message6 = Message.makeMessage("HLO", "sou", "pass");
                when(scanNetNB.nextMessage()).thenReturn(message6);
                method.invoke(clientRunnable);
                when(scanNetNB.hasNextMessage()).thenReturn(true);
                Message message7 = Message.makeMessage("HLO", "Sid", "pass");
                when(scanNetNB.nextMessage()).thenReturn(message7);
                when(ssc.socket().getInetAddress().getHostAddress()).thenReturn("123:0:0:0");
                method.invoke(clientRunnable);
                Message message8 = Message.makeMessage("HLO", "Rak", "pass");
                when(scanNetNB.hasNextMessage()).thenReturn(false);
                method.invoke(clientRunnable);
            }

            if (method.getName().equals("messageChecks")) {
                Message message9 = Message.makeBroadcastMessage("rak", "Prattle says everyone log off");
                clientRunnable.setUserName("rak");
                assertTrue((Boolean) method.invoke(clientRunnable, message9));
                Message message10 = Message.makeBroadcastMessage(null, "Prattle says everyone log off");
                assertFalse((Boolean) method.invoke(clientRunnable, message10));
                clientRunnable.setUserName("hdd");
                Message message11 = Message.makeBroadcastMessage("Rak", "Prattle says everyone log off");
                assertFalse((Boolean) method.invoke(clientRunnable, message11));
            }

            if (method.getName().equals("sendMessage")) {
                Message message9 = Message.makeBroadcastMessage("rak", "Prattle says everyone log off");
                assertFalse((Boolean) method.invoke(clientRunnable, message9));
            }

            if (method.getName().equals("checkForClient")) {
                Message message9 = Message.makeBroadcastMessage("rak", "Prattle says everyone log off");
                method.invoke(clientRunnable);
                immediateResponse.add(message9);
                method.invoke(clientRunnable);
            }

            if (method.getName().equals("specialMessageResponses")) {
                assertTrue((Boolean) method.invoke(clientRunnable, false ,true));
                Message message9 = Message.makeBroadcastMessage("rak", "Prattle says everyone log off");
                specialResponse.add(message9);
                assertTrue((Boolean) method.invoke(clientRunnable, true ,true));
            }

            if (method.getName().equals("sendMessagesToQueue")) {
                method.invoke(clientRunnable, false ,true);
                Message message9 = Message.makeBroadcastMessage("rak", "Prattle says everyone log off");
                waitingList.add(message9);
                assertFalse((Boolean) method.invoke(clientRunnable, false ,true));
                assertTrue((Boolean) method.invoke(clientRunnable, true ,true));
            }

            if (method.getName().equals("messageGroup")) {
                Message message10 = Message.makeGroupMessage("Sou", "#batmangroup I am batman");
                method.invoke(clientRunnable, message10);
            }

            if (method.getName().equals("sendDirectMessage")) {
                Message message10 = Message.makeDirectMessage("Sou", "@testname I am angiman");
                method.invoke(clientRunnable, message10);
                Message message11 = Message.makeDirectMessage("Sou", "@punjas I am angiman");
                method.invoke(clientRunnable, message11);
            }
        }
    }

/**
 * Tests for run() method of ClientRunnable
 * @throws IOException
 */
    @Test
    void testRun() throws IOException {
        SocketChannel ssc = mock(SocketChannel.class, Mockito.RETURNS_DEEP_STUBS);
        Queue<Message> immediateResponse = new LinkedList<>();
        Queue<Message> specialResponse = new LinkedList<>();
        Queue<Message> waitingList = new ConcurrentLinkedQueue<>();
        boolean initialized = false;
        GregorianCalendar terminateInactivity = mock(GregorianCalendar.class);
        ScanNetNB scanNetNB = mock(ScanNetNB.class);
        when(terminateInactivity.before(new GregorianCalendar())).thenReturn(true);
        ClientRunnable clientRunnable = new ClientRunnable(ssc, immediateResponse, specialResponse, waitingList, initialized, terminateInactivity, scanNetNB);       
        assertTrue(clientRunnable.nullPassword("   "));
        assertFalse(clientRunnable.nullPassword("shweta"));
        ScheduledFuture<ClientRunnable> future2 = mock(ScheduledFuture.class);
        when(future2.cancel(false)).thenReturn(false);
        clientRunnable.setFuture(future2);
        clientRunnable.run();
        ClientRunnable clientRunnable2 = new ClientRunnable(ssc, immediateResponse, specialResponse, waitingList, true, terminateInactivity, scanNetNB);
        when(scanNetNB.hasNextMessage()).thenReturn(true);
        Message message6 = Message.makeBroadcastMessage("rak", "Prattle says everyone log off");
        clientRunnable2.setUserName("rak");
        when(scanNetNB.nextMessage()).thenReturn(message6);
        clientRunnable2.run();
        ClientRunnable clientRunnable3 = new ClientRunnable(ssc, immediateResponse, specialResponse, waitingList, true, terminateInactivity, scanNetNB);
        Message message5 = Message.makeBroadcastMessage("gh", "ssup");
        clientRunnable3.setUserName("rak");
        when(scanNetNB.nextMessage()).thenReturn(message5);
        ScheduledFuture<ClientRunnable> future = mock(ScheduledFuture.class);
        when(future.cancel(false)).thenReturn(false);
        clientRunnable3.setFuture(future);
        clientRunnable3.run();
        ClientRunnable clientRunnable4 = new ClientRunnable(ssc, immediateResponse, specialResponse, waitingList, true, terminateInactivity, scanNetNB);
        Message message7 = Message.makeAcknowledgeMessage("bye");
        clientRunnable4.setUserName("rak");
        when(scanNetNB.nextMessage()).thenReturn(message7);
        clientRunnable4.run();
        Message message8 = Message.makeQuitMessage("good bye");
        when(scanNetNB.nextMessage()).thenReturn(message8);
        ScheduledFuture<ClientRunnable> future3 = mock(ScheduledFuture.class);
        when(future3.cancel(false)).thenReturn(false);
        clientRunnable4.setFuture(future3);
        clientRunnable4.run();
        ClientRunnable clientRunnable5 = new ClientRunnable(ssc, immediateResponse, specialResponse, waitingList, false, terminateInactivity, scanNetNB);
        Message message912 = Message.makeLoginMessage("shweta", "oak");
        when(scanNetNB.nextMessage()).thenReturn(message912);
        clientRunnable5.run();
        
        ClientRunnable clientRunnable7 = new ClientRunnable(ssc, immediateResponse, specialResponse, waitingList, false, terminateInactivity, scanNetNB);
        Message message914 = Message.makeLoginMessage("dsjhfb", "123");
        when(scanNetNB.nextMessage()).thenReturn(message914);
        when(ssc.socket().getInetAddress().getHostAddress()).thenReturn("123:0:0:0");
        clientRunnable7.run();
    }
    /**
     * test search by attribute for invalid attribute
     * @throws IOException
     */
    @Test
    void testparseSearchByAttributeInvalidAttribute() throws IOException {
        SocketChannel ssc = mock(SocketChannel.class);
        Queue<Message> immediateResponse = new LinkedList<>();
        Queue<Message> specialResponse = new LinkedList<>();
        Queue<Message> waitingList = new ConcurrentLinkedQueue<>();
        boolean initialized = false;
        GregorianCalendar terminateInactivity = mock(GregorianCalendar.class);
        ScanNetNB scanNetNB = mock(ScanNetNB.class);
        ClientRunnable clientRunnable = new ClientRunnable(ssc, immediateResponse, specialResponse, waitingList, false, terminateInactivity, scanNetNB);
        clientRunnable.setClientIpAddress("10:0:0:0");
        assertEquals(clientRunnable.getClientIpAddress(), "10:0:0:0");
        Message message = Message.makeSearchByAttributes("rak", "tim");
        List<Message> listMessages = clientRunnable.parseSearchByAttribute(message);
        assertTrue(listMessages.size() == 0);
    }
}
