package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.users.User;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.northeastern.ccs.im.Message;

/**
 * A network server that communicates with IM clients that connect to it. This
 * version of the server spawns a new thread to handle each client that connects
 * to it. At this point, messages are broadcast to all of the other clients. It
 * does not send a response when the user has gone off-line.
 * <p>
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 *
 * @version 1.3
 */
public abstract class Prattle {

    /**
     * Amount of time we should wait for a signal to arrive.
     */
    private static final int DELAY_IN_MS = 50;

    /**
     * Number of threads available in our thread pool.
     */
    private static final int THREAD_POOL_SIZE = 20;

    /**
     * Delay between times the thread pool runs the client check.
     */
    private static final int CLIENT_CHECK_DELAY = 200;

    /**
     * Collection of threads that are currently being used.
     */
    protected static ConcurrentLinkedQueue<ClientRunnable> active;

    /**
     * Map of user (by their username) to their clientrunnable threads
     */
    protected static final HashMap<String, List<ClientRunnable>> userMap;

    /**
     * logging error and info messages
     */
    private static Logger logger = LogManager.getLogger(Prattle.class);

    /**
     * Map of to and from users to  clientrunnable threads
     */
    protected static final HashMap<String, List<String>> subpoena;

    /** All of the static initialization occurs in this "method"*/
    static {
        // Create the new queue of active threads.
        active = new ConcurrentLinkedQueue<>();
        userMap = new HashMap<>();
        subpoena = new HashMap<>();
    }

    /**
     * Broadcast a given message to all the other IM clients currently on the
     * system. This message _will_ be sent to the client who originally sent it.
     *
     * @param message Message that the client sent.
     */
    public static void broadcastMessage(Message message) {
        // Loop through all of our active threasds
        for (ClientRunnable tt : active) {
            // Do not send the message to any clients that are not ready to receive it.
            if (tt.isInitialized()) {
                tt.enqueueMessage(message);
            }
        }
    }

    /**
     * Start up the threaded talk server. This class accepts incoming connections on
     * a specific port specified on the command-line. Whenever it receives a new
     * connection, it will spawn a thread to perform all of the I/O with that
     * client. This class relies on the server not receiving too many requests -- it
     * does not include any code to limit the number of extant threads.
     *
     * @param args String arguments to the server from the command line. At present
     *             the only legal (and required) argument is the port on which this
     *             server should list.
     * @throws IOException Exception thrown if the server cannot connect to the port to
     *                     which it is supposed to listen.
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocket = null;
        // Connect to the socket on the appropriate port to which this server connects.
        try {
            serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(false);
            serverSocket.socket().bind(new InetSocketAddress(ServerConstants.PORT));
            // Create the Selector with which our channel is registered.
            Selector selector = SelectorProvider.provider().openSelector();
            // Register to receive any incoming connection messages.
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            // Create our pool of threads on which we will execute.
            ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
            // Listen on this port until ...
            boolean done = false;
            while (conditionExpression(done)) {
                // Check if we have a valid incoming request, but limit the time we may wait.
                while (selector.select(DELAY_IN_MS) != 0) {
                    // Get the list of keys that have arrived since our last check
                    Set<SelectionKey> acceptKeys = selector.selectedKeys();
                    // Now iterate through all of the keys
                    Iterator<SelectionKey> it = acceptKeys.iterator();
                    while (it.hasNext()) {
                        getNextKeyIterartor(it, serverSocket);
                        handleClientRequest(serverSocket, threadPool);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }

    /**
     * Method to return reverse conditionExpression
     *
     * @param done a boolean
     * @return false, if done is true, vice-versa
     */
    private static boolean conditionExpression(boolean done) {
        return !done;
    }

    /**
     * Get the next key; it had better be from a new incoming connection
     *
     * @param it           a SelectionKey Iterator
     * @param serverSocket the channel to be used from a ServerSocketChannel
     */

    private static void getNextKeyIterartor(Iterator<SelectionKey> it, ServerSocketChannel serverSocket) {

        SelectionKey key = it.next();
        it.remove();
        // Assert certain things I really hope is true
        assert key.isAcceptable();
        assert key.channel() == serverSocket;
    }

    /**
     * Create a new thread to handle the client for which we just received a
     * request.
     *
     * @param serverSocket a ServerSocketChannel connection
     * @param threadPool   to create a thread in the threadpool, schedule threads
     */
    private static void handleClientRequest(ServerSocketChannel serverSocket, ScheduledExecutorService threadPool) {

        try {
            // Accept the connection and create a new thread to handle this client.
            SocketChannel socket = serverSocket.accept();
            // Make sure we have a connection to work with.
            if (socket != null) {
                ClientRunnable tt = new ClientRunnable(socket);
                // Add the thread to the queue of active threads
                active.add(tt);
                // Have the client executed by our pool of threads.
                @SuppressWarnings("rawtypes")
                ScheduledFuture clientFuture = threadPool.scheduleAtFixedRate(tt, CLIENT_CHECK_DELAY,
                        CLIENT_CHECK_DELAY, TimeUnit.MILLISECONDS);
                tt.setFuture(clientFuture);
            }
        } catch (AssertionError ae) {
            logger.error("Caught Assertion: " + ae.toString());
        } catch (Exception e) {
            logger.error("Caught Exception: " + e.toString());
        }
    }

    /**
     * Given a user instance and client runnable. Add this association to the usermap.
     *
     * @param user the user for the ClientInstance
     * @param tt   the client for the user
     * @return trye if the user is added successfully and false otherwise.
     */
    public static boolean setUserOnline(User user, ClientRunnable tt) {
        boolean bool = false;
        String username = user.getUserName();
        return addtoMap(username, tt, userMap);
    }

    private static boolean addtoMap(String key, ClientRunnable tt, HashMap<String, List<ClientRunnable>> map) {
        boolean bool = false;
        if(map.containsKey(key)) {
            List<ClientRunnable> threads = map.get(key);
            bool = threads.add(tt);
        } else {
            List<ClientRunnable> threads = new ArrayList<>();
            threads.add(tt);
            map.put(key, threads);
            bool = true;
        }
        return bool;
    }

    /**
     * Remove the given IM client from the list of active threads.
     *
     * @param dead Thread which had been handling all the I/O for a client who has
     *             since quit.
     */
    public static void removeClient(ClientRunnable dead) {
        // Test and see if the thread was in our list of active clients so that we
        // can remove it.
        if (!active.remove(dead)) {
            logger.info("Could not find a thread that I tried to remove!\n");
        }
    }

    /**
     * Check if a given username is in the usermap and associate with any active clients
     *
     * @param target user name of the user to check
     * @return true if user is onlines
     */
    public static boolean activeUser(String target) {
        if (userMap.containsKey(target)) {
            List<ClientRunnable> threads = userMap.get(target);
            if (!threads.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a given username is in the usermap and associate with any active clients
     *
     * @param target user name of the user to check
     * @return true if user is onlines
     */
    public static boolean activeSubpoenaUser(String target) {
        return subpoena.containsKey(target);
    }

    /**
     * Enqueues a message to a specific user.
     *
     * @param targetUser to message
     * @param msg        the message to send to the user
     */
    public static void messageUser(String targetUser, Message msg) {
        List<ClientRunnable> threads = userMap.get(targetUser);
        for (ClientRunnable t : threads) {
            t.enqueueMessage(msg);
        }
    }

    /**
     * Get the listeners for this target.
     * @param target to find the listeners for.
     * @return the listener usernames for this target.
     */
    public static List<String> getActiveTaps(String target) {
        return subpoena.get(target);
    }

    /**
     * sets the subpoena active
     * @param target user target
     * @param listener the listener
     */
    public static void setSubpoenaActive(String target, String listener) {
        if(subpoena.containsKey(target)) {
            List<String> listeners = subpoena.get(target);
            listeners.add(listener);
        } else {
            List<String> listeners = new ArrayList<>();
            listeners.add(listener);
            subpoena.put(target, listeners);
        }
    }

    /**
     * set subpoena user inactive
     * @param target the user target
     * @param listener the listener
     */
    public static void setSubpoenaInactive(String target, String listener) {
        if(subpoena.containsKey(target)) {
            List<String> listeners = subpoena.get(target);
            listeners.remove(listener);
        }
    }
}
