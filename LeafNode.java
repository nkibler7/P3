/**
 * This class represents a leaf node in the tree that contains a String
 * that represents the DNA sequence id.  Also stores a reference to a
 * handle that will be used for the memory manager.
 */
public class LeafNode extends DNATreeNode {
	
	/**
	 * String variable that holds the DNA sequence.
	 */
	private String sequence;
	
	/**
	 * Handle variable that holds the sequence handle.
	 */
	private Handle handle;
	
	/**
	 * Constructor that initializes the given sequence as the sequence
	 * of this node.
	 * 
	 * @param seq - the DNA sequence
	 * @param level - the level of the node
	 */
	public LeafNode(String seq, int level, Handle hand) {
		sequence = seq;
		setLevel(level);
		handle = hand;
	}
	
	/**
	 * Returns a String value of the sequence id of this leaf node.
	 * 
	 * @return - the DNA sequence id
	 */
	public String getSequence() {
		return sequence;
	}
	
	/**
	 * Returns a Handle value of the sequence in memory.
	 * 
	 * @return - the DNA sequence handle
	 */
	public Handle getHandle() {
		return handle;
	}
}
