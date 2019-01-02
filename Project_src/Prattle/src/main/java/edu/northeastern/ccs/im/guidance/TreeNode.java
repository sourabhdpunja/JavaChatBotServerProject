package edu.northeastern.ccs.im.guidance;

import java.util.HashMap;

/**
 * Tree node for tree of bad words
 * @author shweta
 *
 */
public class TreeNode {
	private HashMap<Character, TreeNode> node;

	private boolean end;

	/**
	 * constructor for creating a treenode
	 */

	public TreeNode() {
		end = false;
		node = new HashMap<Character, TreeNode>();
	}

	/**
	 * constructor for adding a child
	 * @param letter the child character
	 */
	public TreeNode(Character letter) {
		this();
	}


	/**
	 * check if reached the end of the word
	 * @return true if end, else false
	 */
	public boolean isEnd() {
		return end;
	}

	/**
	 * set the end 
	 * @param isEnd end of bad word
	 */
	public void setEnd(boolean isEnd) {
		this.end = isEnd;
	}

	/**
	 * add child to tree
	 * @param ch child's letter
	 */
	public void addChild(Character ch) {
		TreeNode childNode = new TreeNode(ch);
		node.put(ch, childNode);
	}

	/**
	 * Find node of the child
	 * @param ch the character to be found
	 * @return node of child
	 */
	public TreeNode findChild(Character ch) {

		return node.get(ch);
	}

	/**
	 * check if child exists
	 * @param ch the character to be checked
	 * @return true if child exists else false
	 */
	public boolean hasChild(Character ch) {
		return node.containsKey(ch);
	}
}

