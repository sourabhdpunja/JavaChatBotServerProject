package edu.northeastern.ccs.im.guidance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Inappropriate word filter
 *
 * @author shweta
 */

public class InappropriateFilter {

    private TreeNode root;
    private int start;
    private int end;
    private boolean possiblyInappropriate;
    private boolean[] asterisk;


/**
 * constructor for inappropriate filter
 */
    public InappropriateFilter() {
        root = new TreeNode();
    }




    /**
     * Make tree for inappropriate words
     *
     * @param fileName path of corpus file
     * @throws IOException
     */
    public void buildTree(String fileName) throws IOException {
        String line;

        try (BufferedReader in = new BufferedReader(new FileReader(fileName))){
            while ((line = in.readLine()) != null) {

                addTreeNode(line, 0, root);
            }
        }
    }


    /**
     * add a node to the tree
     * @param msg the bad word
     * @param index
     *            : index of each letter in a bad word
     * @param node
     *            that iterates through the tree
     */
    private void addTreeNode(String msg, int index, TreeNode node) {
        if (index < msg.length()) {
            Character c = msg.charAt(index);
            if (!node.hasChild(c)) {
                node.addChild(c);
            }
            node = node.findChild(c);

            if (index == (msg.length() - 1)) {

                node.setEnd(true);
            } else {

                addTreeNode(msg, index + 1, node);
            }
        }
    }

    /**
     * Replace some of the letters in userInput as * according to asteriskMark
     *
     * @param msg
     * @return string filtered message
     */
    private String applyAsterisk(String msg) {
        StringBuilder filteredMsg = new StringBuilder(msg);
        for (int i = 0; i < asterisk.length; i++) {
            if (asterisk[i] == true) {
                filteredMsg.setCharAt(i, '*');
            }
        }
        return filteredMsg.toString();
    }

    /**
     * Identify the letters of that needs to be marked as "*"
     *
     * @param start of word
     * @param end of word
     */
    private void markAsterisk(int start, int end) {
        for (int i = start; i <= end; i++) {
            asterisk[i] = true;
        }
    }


    /**
     * censors the inappropriate words
     * @param msg bad word
     * @return string with bad words filtered
     */
    public String filterBadWords(String msg) {
        initsearch(msg);
        return applyAsterisk(msg);
    }

    /**
     * checks if message is inappropriate or not
     * @param msg the message to be checked
     * @return True if vulgar, false if not 
     */
    public boolean inappropriate(String msg) {
        initsearch(msg);
        StringBuilder filteredMsg = new StringBuilder(msg);
        for (int i = 0; i < asterisk.length; i++) {
            if (asterisk[i] == true) {
                return true;
            }
        }
        return false;

    }

    /**
     * initialize the tree and search
     * @param msg message to be checked
     */
    private void initsearch(String msg) {
        init(msg.length());

        for (int i = 0; i < msg.length(); i++) {
            searchTree(msg, i, root);
        }
    }

    /**
     * Initialize the asterisk marking
     * @param length of message
     */
    private void init(int length) {
        asterisk = new boolean[length];
        for (int i = 0; i < length; i++) {
            asterisk[i] = false;
        }
        start = -1;
        end = -1;
        possiblyInappropriate = false;
    }

    /**
     * search tree if word in message is a bad word
     * @param pUserInput input message
     * @param index index of char in word
     * @param node node to be searched
     */
    private void searchTree(String pUserInput, int index,
                            TreeNode node) {
        if (index < pUserInput.length()) {

            Character ch = pUserInput.charAt(index);
            if (node.hasChild(ch)) {

                if (!possiblyInappropriate ) {
                    possiblyInappropriate = true;
                    start = index;
                }

                if (node.findChild(ch).isEnd()) {
                    end = index;
                    markAsterisk(start, end);
                }
                node = node.findChild(ch);
                searchTree(pUserInput, index + 1, node);
            } else {

                possiblyInappropriate = false;
                start = -1;
                end = -1;
            }
        }
    }


}