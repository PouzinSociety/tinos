package rina.flowallocator.impl.ribobjects;

import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.flowallocator.api.DirectoryForwardingTable;
import rina.flowallocator.api.DirectoryForwardingTableEntry;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.NotificationPolicy;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.SimpleSetMemberRIBObject;

public class DirectoryForwardingTableEntryRIBObject extends SimpleSetMemberRIBObject{
	
	private static final Log log = LogFactory.getLog(DirectoryForwardingTableEntryRIBObject.class);
	
	DirectoryForwardingTableEntry directoryForwardingTableEntry = null;
	
	public DirectoryForwardingTableEntryRIBObject(IPCProcess ipcProcess, String objectName, 
			DirectoryForwardingTableEntry directoryForwardingTableEntry){
		super(ipcProcess, objectName, DirectoryForwardingTable.DIRECTORY_FORWARDING_TABLE_ENTRY_RIB_OBJECT_CLASS, 
				Calendar.getInstance().getTimeInMillis());
		this.directoryForwardingTableEntry = directoryForwardingTableEntry;
	}
	
	@Override
	public void create(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException{
		DirectoryForwardingTableEntry entry = null;
		
		//Decode the object
		try{
			entry = (DirectoryForwardingTableEntry) this.getEncoder().decode(cdapMessage.getObjValue().getByteval(), 
					DirectoryForwardingTableEntry.class);
		}catch(Exception ex){
			throw new RIBDaemonException(RIBDaemonException.PROBLEMS_DECODING_OBJECT, ex.getMessage());
		}
		
		if (!entry.getKey().equals(directoryForwardingTableEntry.getKey())){
			log.error("This entry cannot be updated by the received value.\n Received value: "
					+entry.toString() + "\n Current value: " + directoryForwardingTableEntry.toString());
			return;
		}
		
		//See if the entry value actually changes, if so call the ribdaemon causing it to notify
		if (!directoryForwardingTableEntry.equals(entry)){
			try{
				NotificationPolicy notificationObject = new NotificationPolicy(new int[]{cdapSessionDescriptor.getPortId()});
				this.getRIBDaemon().create(cdapMessage.getObjClass(), cdapMessage.getObjName(), 
						cdapMessage.getObjInst(), entry, notificationObject);
			}catch(RIBDaemonException ex){
				log.error(ex.getMessage());
				ex.printStackTrace();
			}
		}
	}
	
	@Override
	public void delete(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException{
		try{
			NotificationPolicy notificationObject = new NotificationPolicy(new int[]{cdapSessionDescriptor.getPortId()});
			this.getRIBDaemon().delete(cdapMessage.getObjClass(), cdapMessage.getObjName(), 
					cdapMessage.getObjInst(), null, notificationObject);
		}catch(RIBDaemonException ex){
			log.error(ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	@Override
	public Object getObjectValue() {
		return directoryForwardingTableEntry;
	}
}
