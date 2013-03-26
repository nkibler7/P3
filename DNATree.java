import java.text.DecimalFormat;

/**
 * DNA Tree class for Project 3.  This class represents the
 * main body of the DNA Tree, which stores DNA sequences id's
 * in a tree structure.  Contains a root and flyweight node,
 * as well as several methods to interface with the DNA Tree.
 * 
 * @author Chris Schweinhart (schwein)
 * @author Nate Kibler (nkibler7)
 */
public class DNATree {

	/**
	 * Node to keep track of the root of the tree.  Will be
	 * a pointer to fw when the tree is empty, a leaf node
	 * when there is only one sequence in the tree, and an
	 * internal node when there are multiples sequences.
	 */
	private DNATreeNode root;
	
	/**
	 * Flyweight node to represent all empty nodes in the tree.
	 * Instead of having multiple instances of empty leaf nodes,
	 * we will simply use one to save on space.  It also makes
	 * error checking easier, as we need not access a leaf node's
	 * sequence to know that it has one (i.e. is not empty).
	 */
	private FlyweightNode fw;

	/**
	 * Basic constructor for the DNATree class.  Creates a new
	 * flyweight for use of the tree, and initially points the
	 * root to fw (since the tree is empty).
	 */
	public DNATree() {
		fw = new FlyweightNode();
		root = fw;
	}

	// TODO fix insert method
	/**
	 * Inserts a sequence into the tree.  The method will try
	 * to find the closest node for the sequence to live at
	 * without ambiguity, moving around the other nodes as needed.
	 * Adds the given handle to the resulting node.  Will return
	 * the level of the tree, or -1 if unsuccessful.
	 * 
	 * @param sequence - the new DNA sequence id to insert
	 * @param handle - the memory handle for the sequence id
	 * @return the level of the new node, or -1 if unsuccessful
	 */
	public int insert(String sequence, Handle handle) 
	{
		// Check if the root is flyweight (empty tree)
		if (root instanceof FlyweightNode) {
			root = new LeafNode(sequence, 0, handle);
			return 0;
		}
		
		// Check if there is only one node in the tree
		if (root instanceof LeafNode) {
			return handleLeafSituation(sequence);
		}
		
		return insert(sequence, (InternalNode)root, handle);
	}

	/**
	 * Builds a tree structure from a starting LeafNode root
	 * and a String sequence value that will be added to this tree.
	 * Handles the case when the root is a LeafNode.
	 * 
	 * @param root - the LeafNode to build from
	 * @param sequence - the String value sequence to add
	 * @return the level at which the sequence was added
	 */
	private int handleLeafSituation(String sequence) {
		
		String sequence2 = ((LeafNode) root).getSequence();
		root = new InternalNode(fw, 0);
		InternalNode focusNode = (InternalNode) root;

		int i;
		for (i = 0; (i < sequence.length() || i < sequence2.length()); i++) {
			if (i == sequence.length()) {
				focusNode.addNode(new LeafNode(sequence2, i + 1), sequence2.charAt(i));
				focusNode.addNode(new LeafNode(sequence, i + 1), 'E');
				break;
			}
			if (i == sequence2.length()) {
				focusNode.addNode(new LeafNode(sequence, i + 1), sequence.charAt(i));
				focusNode.addNode(new LeafNode(sequence2, i + 1), 'E');
				break;
			}
			if (sequence.charAt(i) != sequence2.charAt(i)) {
				focusNode.addNode(new LeafNode(sequence, i + 1), sequence.charAt(i));
				focusNode.addNode(new LeafNode(sequence2, i + 1), sequence2.charAt(i));
				break;
			}
			InternalNode newFocus = new InternalNode(fw, i + 1);
			focusNode.addNode(newFocus, sequence.charAt(i));
			focusNode = newFocus;
		}
		return i + 1;
	}

	/**
	 * Recursive helper method to insert a sequence id into the
	 * tree.  Uses the current node to insert if the child is
	 * free or a leaf node.  Otherwise, recursively calls for
	 * internal nodes.
	 * 
	 * @param sequence - the new DNA sequence id to insert
	 * @param node - the current node in question
	 * @param handle - the memory handle for the sequence id
	 * @return the level of the new node, or -1 if unsuccessful
	 */
	private int insert(String sequence, InternalNode node, Handle handle)
	{
		DNATreeNode child = node.getNode(sequence.charAt(node.getLevel()));

		if (child instanceof FlyweightNode) {
			node.addNode(new LeafNode(sequence, node.getLevel() + 1, handle), sequence.charAt(node.getLevel()));
			return node.getLevel() + 1;
		}

		if (child instanceof LeafNode) {
			return replaceNodeWithTree(node, sequence);
		}

		return insert(sequence, (InternalNode)child);
	}

	/**
	 * Private helper method for the insert operation.
	 * Will build a tree at the specified node to replace
	 * the leaf node in question with a tree. Depending on
	 * how many characters the sequence and the leaf node share,
	 * the height of the tree may differ.
	 * 
	 * @param node - the internal node above the leaf node
	 * @param sequence - the string sequence for DNA
	 * @return the level of the new inserted node
	 */
	private int replaceNodeWithTree(InternalNode node, String sequence)
	{
		LeafNode save = (LeafNode)node.getNode(sequence.charAt(node.getLevel()));
		String pattern = save.getSequence();
		int count = save.getLevel();
		InternalNode newNode = new InternalNode(fw, count);
		node.addNode(newNode, sequence.charAt(node.getLevel()));
		node = newNode;
		
		if (count == sequence.length()) {
			save.setLevel(count + 1);
			node.addNode(save, pattern.charAt(count));
			node.addNode(new LeafNode(sequence, count + 1), 'E');
			return count + 1;
		}

		while (count < sequence.length() && count < pattern.length()) {
			if (pattern.charAt(count) == sequence.charAt(count)) {
				InternalNode temp = new InternalNode(fw, count);
				node.addNode(temp, sequence.charAt(count));
				node = temp;
				count++;
			} else {
				save.setLevel(count + 1);
				node.addNode(save, pattern.charAt(count));
				node.addNode(new LeafNode(sequence, count + 1), sequence.charAt(count));
				return count + 1;
			}
		}
		if (count == sequence.length()) {
			save.setLevel(count + 1);
			node.addNode(save, pattern.charAt(count));
			node.addNode(new LeafNode(sequence, count + 1), 'E');
		}
		else if (count == pattern.length()) {
			save.setLevel(count + 1);
			node.addNode(new LeafNode(sequence, count + 1), sequence.charAt(count));
			node.addNode(save, 'E');
		}
		return count;
	}

	/**
	 * Removes the given sequence from the tree, if it is
	 * found.  Returns the handle for the removed node, or
	 * null otherwise.  Method will rebuild the area around
	 * the removed Leaf Node if needed.
	 * 
	 * @param sequence - the DNA sequence id to be removed
	 * @return the Handle for the removed node, or null
	 * if not found
	 */
	public Handle remove(String sequence) {
		
		// Check if root is flyweight (empty tree)
		if (root instanceof FlyweightNode) {
			return null;
		}
		
		// Check if there is only one node in the tree
		if (root instanceof LeafNode) {
			if (((LeafNode)root).getSequence().equals(sequence)) {
				root = fw;
				return ((LeafNode)root).getHandle();
			}
			return null;
		}
		
		return findAndRemove(sequence, (InternalNode)root);
	}

	/**
	 * Recursive helper method to find and remove a
	 * given sequence from the tree.  First, determines
	 * the location of the nearest parent node, then
	 * removes the sequence if found.
	 * 
	 * @param sequence - the sequence to be removed
	 * @param node - the internal node in question
	 * @return - the Handle for the removed node, or null
	 * if not found
	 */
	private Handle findAndRemove(String sequence, InternalNode node) {
		
		char character = node.getLevel() >= sequence.length() ? 'E' : sequence.charAt(node.getLevel());
		DNATreeNode nextNode = node.getNode(character);
		
		// Determine if the node we're looking for is in the tree
		if (nextNode instanceof FlyweightNode) {
			return null;
		}
		
		// Check if the node we're looking for is a match
		if (nextNode instanceof LeafNode) {
			if (((LeafNode)nextNode).getSequence().equals(sequence)) {
				node.addNode(fw, character);
				return ((LeafNode)nextNode).getHandle();
			}
			return null;
		}
		
		// Recursive case to handle internal node
		Handle handle = findAndRemove(sequence, (InternalNode) nextNode);
		if (handle != null) {
			// Handle cases with internal node with only one child
			// This is when an internal node is no longer needed,
			// since it can just be replaced by its only non-empty child
			if (((InternalNode)nextNode).getNumFlyNodes() == 4) {
				char[] chars = {'A', 'C', 'G', 'T', 'E'};
				for (char currentChar: chars) {
					DNATreeNode child = ((InternalNode)nextNode).getNode(currentChar);
					if (child instanceof LeafNode) {
						node.addNode(child, character);
						return handle;
					}
				}
			}
			return handle;
		}
		return handle;
	}

	// TODO Fix the search methods
	/**
	 * Method to search the tree for a particular pattern.  There
	 * are two types of patterns allowed: (1) prefix search, and
	 * (2) exact matching.  Prefix will return all sequences with a
	 * given prefix, and exact matching will print the exact sequence
	 * if found in the tree.
	 * 
	 * @param pattern - the search pattern to look for
	 * @return the number of nodes visited and search results
	 */
	public String search(String pattern) {

		
		// Check for special root case
		if (root instanceof FlyweightNode) {
			return "Search called on empty tree.";
		}

		String output = "\n";
		int[] visited = new int[1];
		visited[0] = 1;
		boolean exact;

		// Check for exact searching or prefix searching
		if(pattern.charAt(pattern.length() - 1) == '$') {
			exact = true;
			pattern = pattern.substring(0, pattern.length() - 1);
		} else {
			exact = false;
		}
		
		// Check for only one node
		if (root instanceof LeafNode) {
			String sequence = ((LeafNode)root).getSequence();
			if (!exact && sequence.substring(0, pattern.length()).equals(pattern)) {
				output += "Sequence: " + pattern;
			} else if (exact && sequence.equals(pattern)) {
				output += "Sequence: " + pattern;
			} else {
				output += "No sequence found";
			}
		} else {
			// Search for the closest parent node for our pattern
			int count = 0;
			InternalNode focus = (InternalNode)root;
			while (count < pattern.length()) {
				if (focus.getNode(pattern.charAt(count)) instanceof InternalNode) {
					focus = (InternalNode)focus.getNode(pattern.charAt(count));
				} else {
					break;
				}
				count ++;
				visited[0]++;
			}

			char position;
			
			if (count == pattern.length()) {
				position = 'E';
			} else {
				position = pattern.charAt(count);
			}
			
			DNATreeNode nextNode = focus.getNode(position);
			
			if (exact) {
				if (nextNode instanceof LeafNode && ((LeafNode)nextNode).getSequence().equals(pattern)) {
					output += "Sequence: " + ((LeafNode)nextNode).getSequence();
				} else {
					output += "No sequence found";
				}
				visited[0]++;
			} else {
				if (position != 'E' && nextNode instanceof LeafNode && ((LeafNode)nextNode).getSequence().substring(0, pattern.length()).equals(pattern)) {
					output += "Sequence: " + ((LeafNode)nextNode).getSequence();
					visited[0]++;
				} else if (position == 'E') {
					output += printAllLeafNodes((InternalNode)focus, visited);
					visited[0]--;
					output = output.substring(0, output.length() - 1);
				} else {
					output += "No sequence found";
					visited[0]++;
				}
			}
		}

		return "Number of nodes visited: " + visited[0] + output + "\n";
	}

	/**
	 * Helper method for printing all of the nodes in a tree.
	 * 
	 * @param node - the root of the tree
	 * @param visited - an int counter for the nodes visited
	 * @return an output string for all the sequences
	 */
	private String printAllLeafNodes(InternalNode node, int[] visited) {

		visited[0]++;
		String output = "";

		// Handle the A child
		if (node.getNode('A') instanceof LeafNode) {
			output += "Sequence: " + ((LeafNode)node.getNode('A')).getSequence() + "\n";
			visited[0]++;
		} else if (node.getNode('A') instanceof InternalNode) {
			output += printAllLeafNodes((InternalNode)node.getNode('A'), visited);
		} else {
			visited[0]++;
		}

		// Handle the C child
		if (node.getNode('C') instanceof LeafNode) {
			output += "Sequence: " + ((LeafNode)node.getNode('C')).getSequence() + "\n";
			visited[0]++;
		} else if (node.getNode('C') instanceof InternalNode) {
			output += printAllLeafNodes((InternalNode)node.getNode('C'), visited);
		} else {
			visited[0]++;
		}

		// Handle the G child
		if (node.getNode('G') instanceof LeafNode) {
			output += "Sequence: " + ((LeafNode)node.getNode('G')).getSequence() + "\n";
			visited[0]++;
		} else if (node.getNode('G') instanceof InternalNode) {
			output += printAllLeafNodes((InternalNode)node.getNode('G'), visited);
		} else {
			visited[0]++;
		}

		// Handle the T child
		if (node.getNode('T') instanceof LeafNode) {
			output += "Sequence: " + ((LeafNode)node.getNode('T')).getSequence() + "\n";
			visited[0]++;
		} else if (node.getNode('T') instanceof InternalNode) {
			output += printAllLeafNodes((InternalNode)node.getNode('T'), visited);
		} else {
			visited[0]++;
		}

		// Handle exact child
		if (node.getNode('E') instanceof LeafNode) {
			output += "Sequence: " + ((LeafNode)node.getNode('E')).getSequence() + "\n";
		}

		visited[0]++;

		return output;
	}
	
	/**
	 * Method to print out the sequence id's of the tree.
	 * Performs a simple preorder traversal for all the
	 * nodes to print out sequence id's.
	 * 
	 * @return - the string printing of the sequence id's
	 */
	public String toString() {
		return "Sequence IDs:\n" + print(root);
	}
	
	/**
	 * Helper method for the toString method.  Will recursively perform
	 * a preorder traversal, hitting all the nodes to print them.
	 * 
	 * @param node - the current node to print
	 * @return the print for this node and any children
	 */
	private String print(DNATreeNode node) {
		
		// Check if empty node
		if (node instanceof FlyweightNode) {
			return null;
		}
		
		// Check if leaf node
		if(node instanceof LeafNode) {
			return ((LeafNode)node).getSequence() + "\n";
		}

		String output = "";
		
		// Recursive calls to children
		output += print(((InternalNode)node).getNode('A'));
		output += print(((InternalNode)node).getNode('C'));
		output += print(((InternalNode)node).getNode('G'));
		output += print(((InternalNode)node).getNode('T'));
		output += print(((InternalNode)node).getNode('E'));
		
		return output;
	}
}