package rina.flowallocator.impl.ribobjects;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.flowallocator.api.DirectoryForwardingTable;
import rina.flowallocator.api.DirectoryForwardingTableEntry;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcprocess.api.IPCProcess.OperationalStatus;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.NotificationPolicy;
import rina.ribdaemon.api.ObjectInstanceGenerator;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;

public class DirectoryForwardingTableEntrySetRIBObject extends BaseRIBObject{
	
	private static final Log log = LogFactory.getLog(DirectoryForwardingTableEntrySetRIBObject.class);
	
	public DirectoryForwardingTableEntrySetRIBObject(IPCProcess ipcProcess){
		super(ipcProcess, DirectoryForwardingTable.DIRECTORY_FORWARDING_TABLE_ENTRY_SET_RIB_OBJECT_CLASS, 
				ObjectInstanceGenerator.getObjectInstance(), 
				DirectoryForwardingTable.DIRECTORY_FORWARDING_ENTRY_SET_RIB_OBJECT_NAME);
	}
	
	@Override
	/**
	 * A routing update with new and/or updated entries has been received -or during enrollment-. See what parts of the update we didn't now, and 
	 * tell the RIB Daemon about them (will create/update the objects and notify my neighbors except for the one that has 
	 * sent me the update)
	 * @param CDAPMessage the message containing the update
	 * @param CDAPSessionDescriptor the descriptor of the CDAP Session over which I've received the update
	 */
	public void create(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException{
		DirectoryForwardingTableEntry[] directoryForwardingTableEntries = null;
		List<DirectoryForwardingTableEntry> entriesToCreateOrUpdate = new ArrayList<DirectoryForwardingTableEntry>();
		DirectoryForwardingTableEntry currentEntry = null;
		
		//Decode the object
		try{
			directoryForwardingTableEntries = (DirectoryForwardingTableEntry[]) 
				this.getEncoder().decode(cdapMessage.getObjValue().getByteval(), DirectoryForwardingTableEntry[].class);
		}catch(Exception ex){
			throw new RIBDaemonException(RIBDaemonException.PROBLEMS_DECODING_OBJECT, ex.getMessage());
		}
		
		//Find out what objects have to be updated or created
		for(int i=0; i<directoryForwardingTableEntries.length; i++){
			currentEntry = this.getEntry(directoryForwardingTableEntries[i].getKey());
			
			if (currentEntry == null || !currentEntry.equals(directoryForwardingTableEntries[i])){
				entriesToCreateOrUpdate.add(directoryForwardingTableEntries[i]);
			}
		}
		
		//Tell the RIB Daemon to create or update the objects, and notify everyone except the neighbor that 
		//has notified me (except if we're in the enrollment phase, then we don't have to notify)
		try{
			NotificationPolicy notificationObject = null;
			
			//Only notify if we're not in the enrollment phase
			if (this.getIPCProcess().getOperationalStatus() == OperationalStatus.STARTED){
				notificationObject = new NotificationPolicy(new int[]{cdapSessionDescriptor.getPortId()});
			}
			
			this.getRIBDaemon().create(cdapMessage.getObjClass(), cdapMessage.getObjName(),
					entriesToCreateOrUpdate.toArray(new DirectoryForwardingTableEntry[]{}), notificationObject);
		}catch(RIBDaemonException ex){
			log.error(ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	@Override
	/**
	 * One or more local applications have registered to this DIF or a routing update has been received
	 */
	public void create(String objectClass, long objectInstance, String objectName,  Object object) throws RIBDaemonException{
		if (object instanceof DirectoryForwardingTableEntry[]){
			DirectoryForwardingTableEntry[] entries = (DirectoryForwardingTableEntry[]) object;
			for(int i=0; i<entries.length; i++){
				createOrUpdateChildObject(entries[i]);
			}
		}else{
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Object class ("+object.getClass().getName()+") does not match object name "+objectName);
		}
	}
	
	/**
	 * Creates a new child object representing the entry, or updates its value
	 * @param entry
	 */
	private void createOrUpdateChildObject(DirectoryForwardingTableEntry entry){
		DirectoryForwardingTableEntry existingEntry = this.getEntry(entry.getKey());
		if (existingEntry == null){
			//Create the new object
			RIBObject ribObject = new DirectoryForwardingTableEntryRIBObject(this.getIPCProcess(), 
					this.getObjectName()+RIBObjectNames.SEPARATOR + entry.getKey(), entry);

			//Add it as a child
			this.getChildren().add(ribObject);

			//Add it to the RIB
			try{
				this.getRIBDaemon().addRIBObject(ribObject);
			}catch(RIBDaemonException ex){
				log.error(ex);
			}
		}else{
			//Update the object value
			existingEntry.setAddress(entry.getAddress());
		}
	}
	
	@Override
	/**
	 * A routing update has been received
	 */
	public void delete(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException{
		DirectoryForwardingTableEntry[] directoryForwardingTableEntries = null;
		DirectoryForwardingTableEntry currentEntry = null;
		List<DirectoryForwardingTableEntry> entriesToDelete = null;
		
		if (cdapMessage.getObjValue().getByteval() != null){
			//Decode the object
			try{
				directoryForwardingTableEntries = (DirectoryForwardingTableEntry[]) 
				this.getEncoder().decode(cdapMessage.getObjValue().getByteval(), DirectoryForwardingTableEntry[].class);
			}catch(Exception ex){
				throw new RIBDaemonException(RIBDaemonException.PROBLEMS_DECODING_OBJECT, ex.getMessage());
			}
			
			//Find out what objects have to be deleted
			entriesToDelete = new ArrayList<DirectoryForwardingTableEntry>();
			for(int i=0; i<directoryForwardingTableEntries.length; i++){
				currentEntry = this.getEntry(directoryForwardingTableEntries[i].getKey());
				
				if (currentEntry != null ){
					entriesToDelete.add(directoryForwardingTableEntries[i]);
				}
			}
		}
		
		//Tell the RIB Daemon to delete the objects, and notify everyone except the neighbor that 
		//has notified me
		try{
			Object objectValue = null;
			if (entriesToDelete != null){
				objectValue = entriesToDelete.toArray(new DirectoryForwardingTableEntry[]{});
			}
			NotificationPolicy notificationObject = new NotificationPolicy(new int[]{cdapSessionDescriptor.getPortId()});
			this.getRIBDaemon().delete(cdapMessage.getObjClass(), cdapMessage.getObjInst(), cdapMessage.getObjName(),
					objectValue, notificationObject);
		}catch(RIBDaemonException ex){
			log.error(ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	@Override
	/**
	 * One or more local applications have unregistered from this DIF or a routing update has been received
	 */
	public void delete(Object objectValue) throws RIBDaemonException{
		RIBObject ribObject = null;
		
		if (objectValue == null){
			//All the set has to be deleted
			while(getChildren().size() > 0){
				ribObject = getChildren().remove(0);
				getRIBDaemon().removeRIBObject(ribObject);
			}
		}else if (objectValue instanceof DirectoryForwardingTableEntry[]){
			DirectoryForwardingTableEntry[] entries = (DirectoryForwardingTableEntry[]) objectValue;
			for(int i=0; i<entries.length; i++){
				ribObject = getObject(entries[i].getKey());
				if (ribObject != null){
					getChildren().remove(ribObject);
					getRIBDaemon().removeRIBObject(ribObject);
				}
			}
		}else{
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Object class ("+objectValue.getClass().getName()+") does not match object name "+this.getObjectName());
		}
	}
	
	/**
	 * Returns the entry identified by the candidateKey attribute
	 * @param candidateKey
	 * @return
	 */
	private DirectoryForwardingTableEntry getEntry(String candidateKey){
		DirectoryForwardingTableEntry currentEntry = null;
		
		for(int i=0; i<this.getChildren().size(); i++){
			currentEntry = (DirectoryForwardingTableEntry) this.getChildren().get(i).getObjectValue();
			if (currentEntry.getApNamingInfo().getEncodedString().equals(candidateKey)){
				return currentEntry;
			}
		}
		
		return null;
	}
	
	/**
	 * Return the child RIB Object whose object value is identified by candidate key
	 * @param candidateKey
	 * @return
	 */
	private RIBObject getObject(String candidateKey){
		RIBObject currentObject = null;
		DirectoryForwardingTableEntry currentEntry = null;
		
		for(int i=0; i<this.getChildren().size(); i++){
			currentObject = this.getChildren().get(i);
			currentEntry = (DirectoryForwardingTableEntry) currentObject.getObjectValue();
			if (currentEntry.getApNamingInfo().getEncodedString().equals(candidateKey)){
				return currentObject;
			}
		}
		
		return null;
	}
	
	
	@Override
	public Object getObjectValue() {
		DirectoryForwardingTableEntry[] entries = new DirectoryForwardingTableEntry[this.getChildren().size()];
		for(int i=0; i<entries.length; i++){
			entries[i] = (DirectoryForwardingTableEntry) getChildren().get(i).getObjectValue();
		}
		
		return entries;
	}

}
