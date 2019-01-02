package edu.northeastern.ccs.im.server;

import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.PrintNetNB;
import edu.northeastern.ccs.im.ScanNetNB;
import edu.northeastern.ccs.im.SocketNB;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

/**
 * @author oz
 */
public class TestDirectMessage {

  private static Thread thread;
  private static SocketNB socketNB;
  private static SocketNB socketNB2;
  private static ScanNetNB scanNetNB;
  private static PrintNetNB printNetNB;
  private static ScanNetNB scanNetNB2;
  private static PrintNetNB printNetNB2;

  private int BUFFER_SIZE = 64 * 1024;

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

    socketNB2 = new SocketNB("127.0.0.1", 4098);
    printNetNB2 = new PrintNetNB(socketNB2);


    assertTrue(printNetNB.print(Message.makeSimpleLoginMessage("BAZ")));
    assertTrue(printNetNB2.print(Message.makeSimpleLoginMessage("AAZ")));

    printNetNB2.print(Message.makeDirectMessage("AAZ", "@BAZ Hello I am directing a message"));

  }


  /**
   * Calls the main method of Prattle
   *
   * @author shweta
   */
  static class PrattleRunnable implements Runnable {

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