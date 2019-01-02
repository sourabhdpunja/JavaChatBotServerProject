
package edu.northeastern.ccs.im;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.junit.jupiter.api.*;

public class PrintNetNBMockitoTest {
	/**
	 * Test if print() gives expected result for false cases
	 * @throws IOException
	 */
    @Test
    void printNBFalseWithSocketChannel() throws IOException {
        SocketChannel ssc = mock(SocketChannel.class);
        SocketNB sb = mock(SocketNB.class);
        PrintNetNB printNetNB2 = new PrintNetNB(sb);
        PrintNetNB printNetNB = new PrintNetNB(ssc);
        when(ssc.write(any(ByteBuffer.class))).thenReturn(10);
        assertFalse(printNetNB.print(Message.makeAcknowledgeMessage("ljasdf")));
        Message message = Message.makeSimpleLoginMessage("HI");
        when(ssc.write(ByteBuffer.wrap(message.toString().getBytes()))).thenThrow(IOException.class);
        assertFalse(printNetNB.print(Message.makeSimpleLoginMessage("HI")), "Test PrintnetNB Throws IOException");
    }
}

