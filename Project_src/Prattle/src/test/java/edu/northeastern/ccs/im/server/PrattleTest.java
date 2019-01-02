package edu.northeastern.ccs.im.server;

import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.northeastern.ccs.im.SocketNB;
import edu.northeastern.ccs.im.*;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;


/**
 * @author oz
 */

public class PrattleTest {

    private static Thread thread;
    private static SocketNB socketNB;
    private static ScanNetNB scanNetNB;
    private static PrintNetNB printNetNB;
    private static ScanNetNB scanNetNB2;
    private static PrintNetNB printNetNB2;

    /**
     * Check if the connnection created is able to send messsages from the user.
     */
    @Test
    public void setUpServer() throws Exception {
        thread = new Thread(new PrattleRunnable());
        thread.start();
        TimeUnit.SECONDS.sleep(1);
        socketNB = new SocketNB("127.0.0.1", 4098);
        printNetNB = new PrintNetNB(socketNB);
        assertTrue(printNetNB.print(Message.makeSimpleLoginMessage("BAZ")));
    }

    /**
     * Calls the main method of Prattle 
     * @author shweta
     *
     */
   private static class PrattleRunnable implements Runnable {
        public void run() {
            try {
                String[] args = new String[10];
                Prattle.main(args);
            } catch (Exception exc) {
                exc.printStackTrace();
                
            }
        }
    }
}