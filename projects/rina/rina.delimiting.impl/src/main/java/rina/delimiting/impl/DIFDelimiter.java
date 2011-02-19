package rina.delimiting.impl;

import java.util.ArrayList;
import java.util.List;

import rina.delimiting.api.Delimiter;

public class DIFDelimiter implements Delimiter {
	
	public byte[] getDilimitedSdus(List<byte[]> rawSdus) {
		List<byte[]> delimitedSdus = new ArrayList<byte[]>();
		int length =0;
		
		for(int i=0; i<rawSdus.size(); i++){
			delimitedSdus.add(getDelimitedSdu(rawSdus.get(i)));
			length = length + rawSdus.get(i).length;
		}
		
		byte[] delimitedSdusArray = new byte[length];
		int index =0;
		
		for(int i=0; i<delimitedSdus.size(); i++){
			for(int j=0; j<delimitedSdus.get(i).length; j++){
				delimitedSdusArray[index+j] = delimitedSdus.get(i)[j];
			}
			index = index + delimitedSdus.get(i).length;
		}
		
		return delimitedSdusArray;
	}

	public List<byte[]> getRawSdus(byte[] delimitedSdusArray) {
		// TODO Auto-generated method stub
		return null;
	}
	
	  
		
	private byte[] getDelimitedSdu(byte[] rawSdu){
		return null;
	}
	
	/**
	   * Read a raw Varint from the stream.  If larger than 32 bits, discard the
	   * upper bits.
	   */
	private int decodelength(byte[] delimitedSdu){
		int index = 0;
		byte temp = delimitedSdu[index];
		//continue processing while the most significative bit of the byte is 1
		while(temp > 0){
			
		}
	}
	
	  public int readRawVarint32() throws IOException {
	    byte tmp = readRawByte();
	    if (tmp >= 0) {
	      return tmp;
	    }
	    int result = tmp & 0x7f;
	    if ((tmp = readRawByte()) >= 0) {
	      result |= tmp << 7;
	    } else {
	      result |= (tmp & 0x7f) << 7;
	      if ((tmp = readRawByte()) >= 0) {
	        result |= tmp << 14;
	      } else {
	        result |= (tmp & 0x7f) << 14;
	        if ((tmp = readRawByte()) >= 0) {
	          result |= tmp << 21;
	        } else {
	          result |= (tmp & 0x7f) << 21;
	          result |= (tmp = readRawByte()) << 28;
	          if (tmp < 0) {
	            // Discard upper 32 bits.
	            for (int i = 0; i < 5; i++) {
	              if (readRawByte() >= 0) {
	                return result;
	              }
	            }
	            throw InvalidProtocolBufferException.malformedVarint();
	          }
	        }
	      }
	    }
	    return result;
	  }
}
