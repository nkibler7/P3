import java.io.FileNotFoundException;
import java.io.IOException;
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
		try {
			file = new RandomAccessFile("biofile.bin", "rw");
			// Make sure we are overwriting file.
			file.setLength(0);
		} catch (FileNotFoundException e) {
			System.err.println("Could not find/create file.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Could not overwrite file.");
			e.printStackTrace();
		}
		free = new LinkedList<Handle>();
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
		// Calculate number of bytes needed to store this sequence
		int bytesNeeded = (int) Math.ceil((double)(length / 4.0));
		for (Handle freeBlock: free) {
			int offset = freeBlock.getOffset();
			if (freeBlock.getLength() >= bytesNeeded) {
				try {
					file.seek(offset);
					file.write(buildByteArray(sequence, bytesNeeded));
				} catch (IOException e) {
					System.err.println("Problem writing to file. See stack trace for details.");
					e.printStackTrace();
					return null;
				}
				if (freeBlock.getLength() == bytesNeeded) {
					free.remove(freeBlock);
				}
				else {
					free.set(free.indexOf(freeBlock), new Handle(offset + bytesNeeded, freeBlock.getLength() - bytesNeeded));
				}
				return new Handle(offset, bytesNeeded);
			}
		}
		// No valid free space so append to end of file
		//TODO: Improve this. What about if we have a small free block at the end of the file? Extend only amount necessary.
		int oldLength = -1;
		try {
			oldLength = (int) file.length();
			file.setLength(oldLength + bytesNeeded);
			file.seek(oldLength);
			file.write(buildByteArray(sequence, bytesNeeded));
		} catch (IOException e) {
			System.err.println("Problem writing to file. See stack trace for details.");
			e.printStackTrace();
		}
		return new Handle(oldLength, bytesNeeded);
	}
	
	/**
	 * Builds a byte array of the given sequence represented in binary.
	 * @param sequence - the String sequence to build from
	 * @param bytesNeeded - the number of bytes needed to represent the sequence
	 * @return a byte array that should be written to the file
	 */
	private byte[] buildByteArray(String sequence, int bytesNeeded) {
		byte[] array = new byte[bytesNeeded];
		int currentByte = 0, count = 0;
		for (int i = 0; i < sequence.length(); i++) {
			int mod = i % 4;
			switch (mod) {
			case 0:
				currentByte = (getCharValue(sequence.charAt(i))) << 6;
				break;
			case 1:
				currentByte |= (getCharValue(sequence.charAt(i))) << 4;
				break;
			case 2:
				currentByte |= (getCharValue(sequence.charAt(i))) << 2;
				break;
			case 3:
				currentByte |= getCharValue(sequence.charAt(i));
				array[count] = (byte) currentByte;
				count++;
				break;
			}
		}
		// Makes sure we set the last byte, in case 4 does not divide sequence.length()
		if (count == bytesNeeded - 1) {
			array[count] = (byte) currentByte;
		}
		return array;
	}
	
	/**
	 * Returns the binary representation for the given character.
	 * @param c - the character to convert to binary
	 * @return the binary value of the given character
	 */
	private int getCharValue(char c) {
		c = Character.toUpperCase(c);
		if (c == 'A')
			return 0b00;
		if (c == 'C')
			return 0b01;
		if (c == 'G')
			return 0b10;
		if (c == 'T')
			return 0b11;
		System.err.println(c + " is not a valid character for this sequence.");
		return -1;
	}
	
	/**
	 * Method to remove a sequence from the database.
	 * Creates a new free memory block in the place of
	 * the removed sequence.
	 * 
	 * @param handle - the given Handle for the sequence
	 */
	public void remove(Handle handle) {
		for (Handle h: free) {
			if (handle.getOffset() < h.getOffset()) {
				free.add(free.indexOf(h), handle);
				return;
			}
		}
		free.add(handle);
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
		byte[] bytes = new byte[handle.getLength()];
		try {
			file.seek(handle.getOffset());
			file.read(bytes);
		} catch (IOException e) {
			System.err.println("Cannot read byte sequence for given handle.");
			e.printStackTrace();
		}
		String result = "";
		for (byte b: bytes) {
			result += getStrFromBin(b);
		}
		return result;
	}
	
	/**
	 * Returns the binary representation for the given character.
	 * @param c - the character to convert to binary
	 * @return the binary value of the given character
	 */
	private String getStrFromBin(byte b) {
		int[] charsInByte = {(b & 0xC0) >> 6, (b & 0x30) >> 4, (b & 0x0C) >> 2, (b & 0x03)};
		String res = "";
		for (int c: charsInByte) {
			if (c == 0)
				res += "A";
			else if (c == 1)
				res += "C";
			else if (c == 2)
				res += "G";
			else if (c == 3)
				res += "T";
		}
		return res;
	}
	
	/**
	 * Method to produce a string representation of all
	 * free memory blocks.
	 * 
	 * @return - all elements of the linked list free
	 */
	public String toString() {
		String ret = "";
		for (Handle h: free) {
			ret += h.toString();
		}
		return ret.isEmpty() ? "No free memory blocks.": ret;
	}
}
