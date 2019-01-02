package edu.northeastern.ccs.im;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Sourabh Punja on 11/3/2018
 */
public class ScanNetNBMockitoTest {
    /**
     * Testing the hasNextMessage() method false condition, when next does not exist
     * @throws IOException
     */
	
	@Test
    
    void testScanNetNBFalseWithSocketChannel() throws IOException {
        SocketChannel ssc = mock(SocketChannel.class);
        SelectionKey key = mock(SelectionKey.class);
        Selector selector = mock(Selector.class);
        SocketNB socketNB = mock(SocketNB.class);
        ByteBuffer buffer = mock(ByteBuffer.class);
        Queue<Message> messages = new ConcurrentLinkedQueue<>();
        ScanNetNB scanNetNB =  new ScanNetNB(ssc, selector, messages, key, buffer);
        ScanNetNB scanNetNB2 = new ScanNetNB(socketNB, selector, messages, key, buffer);
        Queue<Message> messages2 = new ConcurrentLinkedQueue<>();
        messages2.add(Message.makeBroadcastMessage("Sourabh", "HI All"));
        doNothing().when(selector).close();
        ScanNetNB scanNetNB3 =  new ScanNetNB(ssc, selector, messages2, key, buffer);
        scanNetNB3.close();
        doThrow(IOException.class).when(selector).close();
        assertThrows(AssertionError.class, () -> scanNetNB3.close(), "Check assertion error");
        assertEquals(true, scanNetNB3.hasNextMessage());
        assertEquals(Message.makeBroadcastMessage("Sourabh", "HI All").getText(), scanNetNB3.nextMessage().getText());
        assertEquals(false, scanNetNB3.hasNextMessage());
        assertThrows(NextDoesNotExistException.class, () -> scanNetNB3.nextMessage(), "Messages Empty");
    }

	/**
	 * Tests the functioning of hasNextMessage() when messages are present in the queue
	 * @throws IOException
	 */
    @Test
    void scanNetNBHasNextMessage() throws IOException {
        SocketChannel ssc = mock(SocketChannel.class);
        SelectionKey key = spy(SelectionKey.class);
        Selector selector = mock(Selector.class);
        SocketNB socketNB = mock(SocketNB.class);
        ByteBuffer buffer = mock(ByteBuffer.class);
        when(selector.selectNow()).thenReturn(1);
        when(key.isReadable()).thenReturn(true);
        Queue<Message> messages = new ConcurrentLinkedQueue<>();
        ScanNetNB scanNetNB =  new ScanNetNB(ssc, selector, messages, key, buffer);
        assertFalse(scanNetNB.hasNextMessage());
        when(buffer.limit()).thenReturn(10);
        when(buffer.position()).thenReturn(11);
//        ScanNetNB scanNetNB2 =  new ScanNetNB(ssc, selector, messages, key, ByteBuffer.wrap("Hellojhgfhjg".getBytes()));
        ScanNetNB scanNetNB2 =  new ScanNetNB(ssc, selector, messages, key, buffer);
        scanNetNB2.hasNextMessage();
    }
    
    /**
     * Testing readArgument method, by passing in a CharBuffer
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */

    @Test
    void testReadArgument() throws InvocationTargetException, IllegalAccessException {
        SocketChannel ssc = mock(SocketChannel.class);
        SelectionKey key = spy(SelectionKey.class);
        Selector selector = mock(Selector.class);
        ByteBuffer buffer = mock(ByteBuffer.class);
        Queue<Message> messages = new ConcurrentLinkedQueue<>();
        ScanNetNB scanNetNB2 =  new ScanNetNB(ssc, selector, messages, key, buffer);
        Class<? extends ScanNetNB> scanNetClass = scanNetNB2.getClass();
        Method[] methods = scanNetClass.getDeclaredMethods();
        for (Method method : methods) {
            method.setAccessible(true);
        }
        String result = null;
        CharBuffer charBuffer = CharBuffer.wrap("7 SOURABH 2 hi".toCharArray());
        for (Method method : methods) {
            if (method.getName().equals("readArgument")) {
                result = (String) method.invoke(scanNetNB2, charBuffer);
            }
        }
        assertEquals("SOURABH", result);
    }

    /**
     * Tests read argument with zero length, should return null result
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @Test
    void testReadArgumentWithZerolen() throws InvocationTargetException, IllegalAccessException {
        SocketChannel ssc = mock(SocketChannel.class);
        SelectionKey key = spy(SelectionKey.class);
        Selector selector = mock(Selector.class);
        ByteBuffer buffer = mock(ByteBuffer.class);
        Queue<Message> messages = new ConcurrentLinkedQueue<>();
        ScanNetNB scanNetNB2 =  new ScanNetNB(ssc, selector, messages, key, buffer);
        Class<? extends ScanNetNB> scanNetClass = scanNetNB2.getClass();
        Method[] methods = scanNetClass.getDeclaredMethods();
        for (Method method : methods) {
            method.setAccessible(true);
        }
        String result = "hi";
        CharBuffer charBuffer = CharBuffer.wrap("00 ".toCharArray());
        for (Method method : methods) {
            if (method.getName().equals("readArgument")) {
                result = (String) method.invoke(scanNetNB2, charBuffer);
            }
        }
        assertEquals(null, result);
    }

    /**
     * Tests change in result of minimumMethodRange() on increasing input buffer
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @Test
    void testMinimumMethodRange() throws InvocationTargetException, IllegalAccessException {
        CharBuffer charBuffer = CharBuffer.wrap("NAK 6 legger 3 bey".toCharArray());
        int start = 0;
        SocketChannel ssc = mock(SocketChannel.class);
        SelectionKey key = spy(SelectionKey.class);
        Selector selector = mock(Selector.class);
        ByteBuffer buffer = mock(ByteBuffer.class);
        Queue<Message> messages = new ConcurrentLinkedQueue<>();
        ScanNetNB scanNetNB2 =  new ScanNetNB(ssc, selector, messages, key, buffer);
        Class<? extends ScanNetNB> scanNetClass = scanNetNB2.getClass();
        Method[] methods = scanNetClass.getDeclaredMethods();
        for (Method method : methods) {
            method.setAccessible(true);
        }
        int result = 0;
        for (Method method : methods) {
            if (method.getName().equals("minimumMethodRange")) {
                result = (Integer) method.invoke(scanNetNB2, charBuffer, start);
            }
        }
        assertEquals(18, result);
        result = 0;
        charBuffer = CharBuffer.wrap("ANAK 6 legger 3 bey".toCharArray());
        for (Method method : methods) {
            if (method.getName().equals("minimumMethodRange")) {
                result = (Integer) method.invoke(scanNetNB2, charBuffer, 1);
            }
        }
        assertEquals(19, result);
    }

    /**
     * Check if hasNextMessage() throws exception when messages are empty
     * @throws IOException
     */

    @Test
    void scanNetNBthrowIOException() throws IOException {
        SocketChannel ssc = mock(SocketChannel.class);
        SelectionKey key = spy(SelectionKey.class);
        Selector selector = mock(Selector.class);
        SocketNB socketNB = mock(SocketNB.class);
        ByteBuffer buffer = mock(ByteBuffer.class);
        when(selector.selectNow()).thenReturn(1);
        when(key.isReadable()).thenReturn(true);
        when(ssc.read(buffer)).thenThrow(IOException.class);
        Queue<Message> messages = new ConcurrentLinkedQueue<>();
        ScanNetNB scanNetNB =  new ScanNetNB(ssc, selector, messages, key, buffer);
        assertThrows(AssertionError.class, () -> scanNetNB.hasNextMessage(), "Messages Empty");
    }
}