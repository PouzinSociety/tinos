package rina.utils.types;

import rina.utils.types.UnsignedException.ErrorCodes;

/**
 * Represents an unsigned value of 1, 2, 3, 4, 5, 6 or 7 bytes. Provided because Java doesn't support 
 * unsigned types, therefore some tricks and validations have to be done behind the scenes to operate with 
 * unsigned types.
 * @author eduardgrasa
 *
 */
public class Unsigned {
	/**
	 * The number of bytes of this unsigned data type
	 */
	private byte numberOfBytes = 0;
	
	/**
	 * The actual value of the type
	 */
	private long value = 0L;
	
	public Unsigned(byte numberOfBytes) throws UnsignedException{
		if (numberOfBytes < 1 || numberOfBytes > 7){
			throw new UnsignedException(ErrorCodes.UnsupportedNumberOfBytes);
		}
		
		this.numberOfBytes = numberOfBytes;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public byte getNumberOfBytes() {
		return numberOfBytes;
	}
}
