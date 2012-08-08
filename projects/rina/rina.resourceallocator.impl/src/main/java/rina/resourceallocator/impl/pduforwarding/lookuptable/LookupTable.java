package rina.resourceallocator.impl.pduforwarding.lookuptable;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple and not very efficient implementation of the lookup table.
 * @author eduardgrasa
 *
 */
public class LookupTable {
	
	private static final Log log = LogFactory.getLog(LookupTable.class);
	
	private Map<Long, AddressNode> addressNodes = null;
	
	public LookupTable(){
		this.addressNodes = new ConcurrentHashMap<Long, AddressNode>();
	}
	
	/**
	 * Return the portId associated to the address, qosId and cepId
	 * @param address must be a positive number
	 * @param qosId if it is negative it will not be used 
	 * @return the portId that is the longest match of the requested data, or null if the search 
	 * produced no results
	 */
	public int[] getPortIds(long address, int qosId){
		AddressNode addressNode = this.addressNodes.get(new Long(address));
		if (addressNode == null){
			return null;
		}
		
		if (qosId < 0 ){
			return addressNode.getPortIds();
		}
		
		QoSIdNode qosIdNode = addressNode.getQoSIdNode(qosId);
		if (qosIdNode == null){
			return addressNode.getPortIds();
		}
		
		return qosIdNode.getPortIds();
	}
	
	/**
	 * Add an entry to the forwarding table
	 * @param destinationAddress
	 * @param qosId
	 * @param portIds the portId associated to the destination_address-qosId-destination_CEP_id 
	 */
	public void addEntry(long destinationAddress, int qosId, int[] portIds){
		if (destinationAddress < 0){
			log.warn("Tried to add an entry with a negative address to the lookup table "+destinationAddress 
					+". Operation ignored.");
			return;
		}
		
		AddressNode addressNode = this.addressNodes.get(new Long(destinationAddress));
		if (addressNode == null){
			addressNode = new AddressNode();
			this.addressNodes.put(new Long(destinationAddress), addressNode);
		}
		
		if (qosId<0){
			addressNode.setPortIds(portIds);
			log.debug("Added entry to lookup table: PDUs with destination @ "+destinationAddress 
					+" should be forwarded to the N-1 flows with portIds "
					+ getPortIdsString(portIds));
			return;
		}
		
		QoSIdNode qosIdNode = new QoSIdNode(portIds);
		addressNode.getQoSIdNodes().put(new Integer(qosId), qosIdNode);
		
		log.debug("Added entry to lookup table: PDUs with destination @ "+destinationAddress 
				+" and QoS id "+qosId+" should be forwarded to the N-1 flows with portIds "
				+ getPortIdsString(portIds));
	}
	
	/**
	 * Remove an entry from the forwarding table
	 * @param destinationAddress
	 * @param qosId
	 */
	public void removeEntry(long destinationAddress, int qosId){
		if (destinationAddress < 0){
			log.warn("Tried to remove an entry with a negative address from the lookup table "+destinationAddress 
					+". Operation ignored.");
			return;
		}
		
		if (qosId <0){
			this.addressNodes.remove(new Long(destinationAddress));
			return;
		}
		
		AddressNode addressNode = this.addressNodes.get(new Long(destinationAddress));
		if (addressNode == null){
			return;
		}
		addressNode.removeQoSIdNode(qosId);
		if (addressNode.getQoSIdNodes().isEmpty() && addressNode.getPortIds() == null){
				this.addressNodes.remove(new Long(destinationAddress));
		}
	}
	
	public String toString(){
		if (this.addressNodes.isEmpty()){
			return "There are currently no entries in the forwarding table";
		}
		
		String result = "Current entries in the forwarding table: \n";
		Iterator<Entry<Long, AddressNode>> addressIterator = this.addressNodes.entrySet().iterator();
		Entry<Long, AddressNode> currentAddress = null;
		while(addressIterator.hasNext()){
			currentAddress = addressIterator.next();
			if (currentAddress.getValue().getPortIds() != null){
				result = result + "PDUs with destination @ "+currentAddress.getKey() 
				+" should be forwarded to the N-1 flow identified by portId "
				+getPortIdsString(currentAddress.getValue().getPortIds()) + "\n";
			}

			Iterator<Entry<Integer, QoSIdNode>> qosIterator = currentAddress.getValue().getQoSIdNodes().entrySet().iterator();
			Entry<Integer, QoSIdNode> currentQoS = null;
			while(qosIterator.hasNext()){
				currentQoS = qosIterator.next();
				result = result + "PDUs with destination @ "+currentAddress.getKey() 
				+" and QoS id " + currentQoS.getKey()+" should be forwarded to the N-1 flows identified by portIds "
				+getPortIdsString(currentQoS.getValue().getPortIds()) + "\n";
			}
		}
		
		return result;
	}
	
	private String getPortIdsString(int[] portIds){
		String result = "";
		for(int i=0; i<portIds.length; i++){
			result = result + " " + portIds[i];
		}
		return result;
	}

}
