package edu.northeastern.ccs.im.chatter;

import java.util.Scanner;

import edu.northeastern.ccs.im.IMConnection;
import edu.northeastern.ccs.im.KeyboardScanner;
import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.MessageScanner;

/**
 * Class which can be used as a command-line IM client.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 *
 * @version 1.3
 */
public class CommandLineMain {

	/**
	 * This main method will perform all of the necessary actions for this phase of
	 * the course project.
	 *
	 * @param args Command-line arguments which we ignore
	 */
	public static void main(String[] args) {
		IMConnection connect;
		@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);

		do {
			// Prompt the user to type in a username.
			System.out.println("What username would you like?");

			String username = in.nextLine();

			System.out.println("What password would you like?");

			String password = in.nextLine();

			// Create a Connection to the IM server.
			connect = new IMConnection(args[0], Integer.parseInt(args[1]), username, password);
		} while (!connect.connect());

		// Create the objects needed to read & write IM messages.
		KeyboardScanner scan = connect.getKeyboardScanner();
		MessageScanner mess = connect.getMessageScanner();

		// Repeat the following loop
		while (connect.connectionActive()) {
			// Check if the user has typed in a line of text to broadcast to the IM server.
			// If there is a line of text to be
			// broadcast:
			if (scan.hasNext()) {
				handleInput(connect, scan);
			}
			// Get any recent messages received from the IM server.
			if (mess.hasNext()) {
				Message message = mess.next();
				if (!message.getSender().equals(connect.getUserName())) {
					System.out.println(message.getSender() + ": " + message.getText() + "\n");
				} else if (message.getSender().equals(connect.getUserName()) && message.isUserMessage()) {
					System.out.println(message.getText() + "\n");
				}
			}
		}
		System.out.println("Program complete.");
		System.exit(0);
	}

	private static void handleInput(IMConnection connect, KeyboardScanner scan) {
		String line = scan.nextLine();
		char firstChar = line.charAt(0);
		switch (firstChar) {
			case '/':
				handleCommand(line, connect);
				break;
			case '#':
				connect.sendGroupMessage(line);
				break;
			case '@':
				connect.sendDirectMessage(line);
				break;
			default:
				connect.sendMessage(line);
		}
	}

	private static void handleCommand(String line, IMConnection connect) {
		String[] tokens = line.split(" ");
		String command = tokens[0];
		switch (command) {
			case "/quit":
				closeConnection(connect);
				break;
			case "/group":
				handleGroupModifyMessage(line, connect);
				break;
			case "/search":
				handleSearchByAttribute(line, connect);
				break;
			case "/subpoena":
				handleSubpoenaMessage(line,connect);
			case "/recall":
				handleRecallMessage(line, connect);
				break;
			default:
				System.out.println("Please enter a valid command.");
		}
	}

	private static void handleSubpoenaMessage(String line, IMConnection connect) {
		connect.sendSubpoenaMessage(line);
	}
	
	/**
	 * Handle search by attribute.
	 *
	 * @param line the line
	 * @param connect the connect
	 */
	private static void handleSearchByAttribute(String line, IMConnection connect) {
		connect.sendSearchByAttributeMessage(line);
	}
	
	private static void handleRecallMessage(String line, IMConnection connect) {
		connect.sendRecallMessage(line);
	}

	private static void handleGroupModifyMessage(String line, IMConnection connect) {
		String[] tokens = line.split(" ");
		try {
			String command = tokens[1];
			String groupname = tokens[2];
			switch (command) {
				case "create":
				case "delete":
					connect.sendGroupModifyMessage(line);
					break;
				case "adduser":
					connect.sendGroupModifyMessage(line);
					break;
				case "deleteuser":
					connect.sendGroupModifyMessage(line);
					break;
				default:
					System.out.println("Enter a valid group command.\nList includes: create");

			}
		} catch (IndexOutOfBoundsException e) {
			System.out.println("Enter a valid command for group modification.\n" +
					"/group create #groupName");
		}

	}

	private static void closeConnection(IMConnection connect) {
		connect.disconnect();
	}
}
