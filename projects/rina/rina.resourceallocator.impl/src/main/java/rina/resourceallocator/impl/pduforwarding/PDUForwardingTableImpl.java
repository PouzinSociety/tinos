package rina.resourceallocator.impl.pduforwarding;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.efcp.api.PDU;
import rina.ipcprocess.api.IPCProcess;
import rina.resourceallocator.api.PDUForwardingTable;
import rina.resourceallocator.impl.pduforwarding.lookuptable.LookupTable;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;
import rina.ribdaemon.api.SimpleRIBObject;

public class PDUForwardingTableImpl implements PDUForwardingTable{

	private static final Log log = LogFactory.getLog(PDUForwardingTableImpl.class);
	
	private LookupTable lookupTable = null;
	
	public PDUForwardingTableImpl(){
		this.lookupTable = new LookupTable();
	}
	
	public void setIPCProcess(IPCProcess ipcProcess){
		RIBDaemon ribDaemon = (RIBDaemon) ipcProcess.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		RIBObject pduForwardingTableRIBObject = new SimpleRIBObject(ipcProcess, RIBObjectNames.PDU_FORWARDING_TABLE_RIB_OBJECT_CLASS, 
				RIBObjectNames.PDU_FORWARDING_TABLE_RIB_OBJECT_NAME, lookupTable);
		try {
			ribDaemon.addRIBObject(pduForwardingTableRIBObject);
		} catch (RIBDaemonException ex) {
			log.error(ex);
		}
	}
	
	/**
	 * Returns the N-1 portId through which the N PDU has to be sent
	 * @param pdu
	 * @return
	 */
	public int getNMinusOnePortId(PDU pdu) {
		return lookupTable.getPortId(pdu.getDestinationAddress(), 
				pdu.getConnectionId().getQosId(),
				pdu.getConnectionId().getDestinationCEPId());
	}
	
	/**
	 * Add an entry to the forwarding table
	 * @param destinationAddress
	 * @param qosId
	 * @param destinationCEPId
	 * @param portId the portId associated to the destination_address-qosId-destination_CEP_id 
	 */
	public void addEntry(long destinationAddress, int qosId, long destinationCEPId, int portId){
		lookupTable.addEntry(destinationAddress, qosId, destinationCEPId, portId);
	}
	
	/**
	 * Remove an entry from the forwarding table
	 * @param destinationAddress
	 * @param qosId
	 * @param destinationCEPId
	 */
	public void removeEntry(long destinationAddress, int qosId, long destinationCEPId){
		lookupTable.removeEntry(destinationAddress, qosId, destinationCEPId);
	}

}
