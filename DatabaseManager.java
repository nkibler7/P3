import java.io.RandomAccessFile;
import java.util.LinkedList;

/**
 * Database manager for keeping track of sequence memory
 * and free blocks.  Allows for several interface methods
 * using the Handle class to determine which bytes are
 * sequences.
 */
public class DatabaseManager {

	/**
	 * File pointer to our byte array on disk.  Used to
	 * store and access sequences based on give Handles,
	 * with given offsets and lengths.
	 */
	RandomAccessFile file;
	
	/**
	 * Linked List for keeping track of all free memory
	 * blocks.  Each block is represented by a Handle,
	 * with a given offset and length.
	 */
	LinkedList<Handle> free;
	
	/**
	 * Basic constructor for the DatabaseManager class.
	 * Will initialize all member fields appropriately.
	 */
	public DatabaseManager() {
		// TODO implement constructor
	}
	
	/**
	 * Method to insert a given sequence into the first
	 * free memory block.  If there are no free memory
	 * blocks of sufficient size, will create a new one
	 * and add it to the end of the file.
	 * 
	 * @param sequence - the sequence to insert
	 * @param length - the length of the given sequence
	 * @return - the Handle for the given sequence
	 */
	public Handle insert(String sequence, int length) {
		// TODO implement insert
		
		return new Handle(0, 0);
	}
	
	/**
	 * Method to remove a sequence from the database.
	 * Creates a new free memory block in the place of
	 * the removed sequence.
	 * 
	 * @param handle - the given Handle for the sequence
	 */
	public void remove(Handle handle) {
		// TODO implement remove
	}
	
	/**
	 * Method to retrieve a DNA sequence using a given
	 * handle.  Will give the bytes in memory regardless
	 * of whether or not they have meaning (i.e. has no
	 * error checking).
	 * 
	 * @param handle - the given Handle for the sequence
	 * @return - the sequence in the memory location
	 */
	public String getEntry(Handle handle) {
		// TODO implement getEntry
		
		return "NYI: getEntry(Handle handle)";
	}
	
	/**
	 * Method to produce a string representation of all
	 * free memory blocks.
	 * 
	 * @return - all elements of the linked list free
	 */
	public String toString() {
		// TODO implement toString
		
		return "NYI: toString()";
	}
}
