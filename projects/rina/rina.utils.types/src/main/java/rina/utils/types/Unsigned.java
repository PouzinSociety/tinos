package rina.utils.types;

import rina.utils.types.UnsignedException.ErrorCodes;

/**
 * Represents an unsigned value of 1, 2, 3, 4 bytes. Provided because Java doesn't support 
 * unsigned types, therefore some tricks and validations have to be done behind the scenes to operate with 
 * unsigned types.
 * @author eduardgrasa
 *
 */
public class Unsigned {
	/**
	 * The number of bytes of this unsigned data type
	 */
	private int numberOfBytes = 0;
	
	/**
	 * The actual value of the type
	 */
	private long value = 0x00;
	
	private long maxValue = 0x00;
	
	/**
	 * Only values between 1 and 4 bytes are supported
	 * @param numberOfBytes
	 * @throws UnsignedException
	 */
	public Unsigned(int numberOfBytes) throws UnsignedException{
		if (numberOfBytes < 1 || numberOfBytes > 4){
			throw new UnsignedException(ErrorCodes.UnsupportedNumberOfBytes);
		}
		
		this.numberOfBytes = numberOfBytes;
		
		switch (numberOfBytes){
		case 1: 
			maxValue = 0xff;
			break;
		case 2:
			maxValue = 0xffff;
			break;
		case 3:
			maxValue = 0xffffff;
			break;
		case 4:
			maxValue = 0xffffffff;
			break;
		}
	}

	public long getValue() {
		return value;
	}

	/**
	 * Set the value of this unsigned int. If the value is not within 
	 * 0x00 and maxValue, this operation will throw an exception
	 * @param value
	 * @throws UnsignedException
	 */
	public void setValue(long value) throws UnsignedException{
		if (value > this.maxValue || value < 0x00){
			throw new UnsignedException(ErrorCodes.ValueOutOfBounds);
		}
		this.value = value;
	}

	public int getNumberOfBytes() {
		return numberOfBytes;
	}
	
	/**
	 * Get the byte representation of this unsigned int, in 
	 * numberOfBytes bytes. Network order (big endian) is used (i.e. 
	 * the most representatives bytes at the beginning).
	 * @return
	 */
	public byte[] getBytes(){
		byte[] buffer = new byte[numberOfBytes];
		
		switch(numberOfBytes){
		case 1:
			buffer[0] = (byte) (value & 0x000000FFL);
			break;
		case 2:
			buffer[1] = (byte) (value & 0x000000FFL);
			buffer[0] = (byte) ((value & 0x0000FF00L) >> 8);
			break;
		case 3:
			buffer[2] = (byte) (value & 0x000000FFL);
			buffer[1] = (byte) ((value & 0x0000FF00L) >> 8);
			buffer[0] = (byte) ((value & 0x00FF0000L) >> 16);
			break;
		case 4:
			buffer[3] = (byte) (value & 0x000000FFL);
			buffer[2] = (byte) ((value & 0x0000FF00L) >> 8);
			buffer[1] = (byte) ((value & 0x00FF0000L) >> 16);
			buffer[0] = (byte) ((value & 0xFF000000L) >> 24);
			break;
		}
		
		return buffer;
	}
	
	/**
	 * Read the value as an unsigned integer of numberOfBytes bytes, 
	 * from a byte array. Network order (big endian) is used (i.e. 
	 * the most representatives bytes at the beginning).
	 * @param buffer
	 * @throws UnsignedException
	 */
	public void setValue(byte[] buffer) throws UnsignedException{
		int firstByte, secondByte, thirdByte, fourthByte = 0;
		
		if (buffer == null){
			throw new UnsignedException(ErrorCodes.NullValueProvided);
		}
		
		if (buffer.length != numberOfBytes){
			throw new UnsignedException(ErrorCodes.UnsupportedNumberOfBytes);
		}
		
		switch(numberOfBytes){
		case 1:
			firstByte = (0x000000FF & ((int)buffer[0]));
			value  = ((long) firstByte) & 0xFF;
			break;
		case 2:
			firstByte = (0x000000FF & ((int)buffer[0]));
	        secondByte = (0x000000FF & ((int)buffer[1]));
	        value  = ((long) (firstByte << 8 | secondByte)) & 0xFFFF;
	        break;
		case 3:
			firstByte = (0x000000FF & ((int)buffer[0]));
	        secondByte = (0x000000FF & ((int)buffer[1]));
	        thirdByte = (0x000000FF & ((int)buffer[2]));
	        value  = ((long) (firstByte << 16 | secondByte << 8 | thirdByte)) & 0xFFFFFF;
	        break;
		case 4:
			firstByte = (0x000000FF & ((int)buffer[0]));
	        secondByte = (0x000000FF & ((int)buffer[1]));
	        thirdByte = (0x000000FF & ((int)buffer[2]));
	        fourthByte = (0x000000FF & ((int)buffer[3]));
	        value  = ((long) (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte)) & 0xFFFFFFFF;
	        break;
		}
	}
	
	/**
	 * Adds two unsigned, checking that they have the same number of bytes and that 
	 * is not greater than maxValue
	 * @param toAdd
	 */
	public void add(Unsigned toAdd) throws UnsignedException{
		if (this.numberOfBytes != toAdd.getNumberOfBytes()){
			throw new UnsignedException(ErrorCodes.UnsupportedNumberOfBytes);
		}
		
		long aux = this.value + toAdd.getValue();
		if (aux < this.maxValue){
			this.value = aux;
		}else{
			this.value = aux - this.maxValue;
		}
	}
	
	/**
	 * Increment the value of this unsigned int by one
	 */
	public void increment(){
		Unsigned one = new Unsigned(this.numberOfBytes);
		one.setValue(1);
		this.add(one);
	}
	
	public boolean equals(Object candidate){
		if (candidate == null){
			return false;
		}
		
		if (!(candidate instanceof Unsigned)){
			return false;
		}
		
		Unsigned unsigned =  (Unsigned) candidate;
		
		if (unsigned.getNumberOfBytes() == this.getNumberOfBytes() && 
				unsigned.getValue() == this.getValue()){
			return true;
		}
		
		return false;
	}
	
	public String toString(){
		return ""+value;
	}
}
