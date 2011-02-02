package rina.efcp.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the PDU reassembly queue
 * @author eduardgrasa
 *
 */
public class ReassemblyQueue {
	
	List<PDU> queue = null;
	
	public ReassemblyQueue(){
		queue = new ArrayList<PDU>();
	}
	
	/**
	 * Adds a pdu to the queue, in sequence number order
	 * @param pdu
	 * @return false if the pdu was already in the queue, true otherwise
	 */
	public boolean add(PDU pdu){
		if (queue.contains(pdu)){
			return false;
		}
		
		int index=0;
		for(index=0; index<queue.size(); index++){
			if (queue.get(index).getSequenceNumber().getValue()>pdu.getSequenceNumber().getValue()){
				break;
			}
		}
		
		queue.add(index, pdu);
		return true;
	}
	
	/**
	 * Tells if a pdu is already on the queue
	 * @param pdu
	 * @return
	 */
	public boolean contains(PDU pdu){
		return queue.contains(pdu);
	}
	
	public List<PDU> getQueue(){
		return queue;
	}
	
}
