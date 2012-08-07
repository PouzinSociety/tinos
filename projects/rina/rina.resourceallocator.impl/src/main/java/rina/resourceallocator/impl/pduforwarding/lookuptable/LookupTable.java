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
	 * @param cepId if qosId is not negative and cepId is not negative, it will be used to search
	 * @return the portId that is the longest match of the requested data, or -1 if the search 
	 * produced no results
	 */
	public int getPortId(long address, int qosId, long cepId){
		AddressNode addressNode = this.addressNodes.get(new Long(address));
		if (addressNode == null){
			return -1;
		}
		
		if (qosId < 0 ){
			return addressNode.getPortId();
		}
		
		QoSIdNode qosIdNode = addressNode.getQoSIdNode(qosId);
		if (qosIdNode == null){
			return addressNode.getPortId();
		}
		
		if (cepId < 0 ){
			return qosIdNode.getPortId();
		}
		
		CEPIdNode cepIdNode = qosIdNode.getCEPIdNode(cepId);
		if (cepIdNode == null){
			return qosIdNode.getPortId();
		}
		
		return cepIdNode.getPortId();
	}
	
	/**
	 * Add an entry to the forwarding table
	 * @param destinationAddress
	 * @param qosId
	 * @param destinationCEPId
	 * @param portId the portId associated to the destination_address-qosId-destination_CEP_id 
	 */
	public void addEntry(long destinationAddress, int qosId, long destinationCEPId, int portId){
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
			addressNode.setPortId(portId);
			log.debug("Added entry to lookup table: PDUs with destination @ "+destinationAddress 
					+" should be forwarded to the N-1 flow with portId "+portId);
			return;
		}
		
		QoSIdNode qosIdNode = addressNode.getQoSIdNode(qosId);
		if (qosIdNode == null){
			qosIdNode = new QoSIdNode();
			addressNode.getQoSIdNodes().put(new Integer(qosId), qosIdNode);
		}
		
		if (destinationCEPId <0){
			qosIdNode.setPortId(portId);
			log.debug("Added entry to lookup table: PDUs with destination @ "+destinationAddress 
					+" and QoS id "+qosId+" should be forwarded to the N-1 flow with portId "+portId);
			return;
		}
		
		CEPIdNode cepIdNode = qosIdNode.getCEPIdNode(destinationCEPId);
		if (cepIdNode == null){
			cepIdNode = new CEPIdNode(portId);
			qosIdNode.getCEPIdNodes().put(new Long(destinationCEPId), cepIdNode);
			log.debug("Added entry to lookup table: PDUs with destination @ "+destinationAddress 
					+" and QoS id "+qosId+" and destination CEP-id "+destinationCEPId 
					+" should be forwarded to the N-1 flow with portId "+portId);
		}
	}
	
	/**
	 * Remove an entry from the forwarding table
	 * @param destinationAddress
	 * @param qosId
	 * @param destinationCEPId
	 */
	public void removeEntry(long destinationAddress, int qosId, long destinationCEPId){
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
		
		if (destinationCEPId < 0){
			addressNode.removeQoSIdNode(qosId);
			if (addressNode.getQoSIdNodes().isEmpty() && addressNode.getPortId() == -1){
				this.addressNodes.remove(new Long(destinationAddress));
			}
			return;
		}
		
		QoSIdNode qosIdNode = addressNode.getQoSIdNode(new Integer(qosId));
		qosIdNode.removeCEPIdNode(destinationCEPId);
		if (qosIdNode.getCEPIdNodes().isEmpty() && qosIdNode.getPortId() == -1){
			addressNode.removeQoSIdNode(qosId);
		}
		if (addressNode.getQoSIdNodes().isEmpty() && addressNode.getPortId() == -1){
			this.addressNodes.remove(new Long (destinationAddress));
		}
	}
	
	public String toString(){
		if (this.addressNodes.isEmpty()){
			return "There are currently no entries in the forwarding table";
		}
		
		String result = "Current entries in the forwarding table:";
		Iterator<Entry<Long, AddressNode>> addressIterator = this.addressNodes.entrySet().iterator();
		Entry<Long, AddressNode> currentAddress = null;
		while(addressIterator.hasNext()){
			currentAddress = addressIterator.next();
			if (currentAddress.getValue().getPortId() != -1){
				result = result + "PDUs with destination @ "+currentAddress.getKey() 
					+" should be forwarded to the N-1 flow identified by portId "
					+currentAddress.getValue().getPortId() + "\n";
			}
			
			Iterator<Entry<Integer, QoSIdNode>> qosIterator = currentAddress.getValue().getQoSIdNodes().entrySet().iterator();
			Entry<Integer, QoSIdNode> currentQoS = null;
			while(qosIterator.hasNext()){
				currentQoS = qosIterator.next();
				if (currentQoS.getValue().getPortId() != -1){
					result = result + "PDUs with destination @ "+currentAddress.getKey() 
					+" and QoS id " + currentQoS.getKey()+" should be forwarded to the N-1 flow identified by portId "
					+currentQoS.getValue().getPortId() + "\n";
				}
				
				Iterator<Entry<Long, CEPIdNode>> cepIdIterator = currentQoS.getValue().getCEPIdNodes().entrySet().iterator();
				Entry<Long, CEPIdNode> currentCEPId = null;
				while(cepIdIterator.hasNext()){
					currentCEPId = cepIdIterator.next();
					result = result + "PDUs with destination @ "+currentAddress.getKey() 
					+" and QoS id " + currentQoS.getKey()+" and destination CEP-id "
					+ currentCEPId.getKey()+" should be forwarded to the N-1 flow identified by portId "
					+currentCEPId.getValue().getPortId() + "\n";
				}
			}
		}
		
		return result;
	}

}
