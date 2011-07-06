package rina.ribdaemon.api;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.Opcode;

/**
 * Implements the create/delete/read/write/start/stop RIB functionality for certain objects (identified by objectNames)
 * @author eduardgrasa
 *
 */
public interface RIBHandler{
	
	/**
	 * Reads/writes/created/deletes/starts/stops one or more objects at the RIB, matching the information specified by objectId + objectClass or objectInstance.
	 * At least objectName or objectInstance have to be not null. This operation is invoked because the RIB Daemon has received a CDAP message from another 
	 * IPC process
	 * @param cdapMessage The CDAP message received
	 * @param cdapSessionDescriptor Describes the CDAP session to where the CDAP message belongs
	 * @throws RIBDaemonException on a number of circumstances
	 */
	public void processOperation(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException;
	
	/**
	 * Reads/writes/created/deletes/starts/stops one or more objects at the RIB, matching the information specified by objectId + objectClass or objectInstance.
	 * At least objectName or objectInstance have to be not null. This method is invoked by tasks that are internal to the IPC process.
	 * @param opcode the operation (can only be read/write/create/delete/start/stop)
	 * @param objectClass
	 * @param objectName
	 * @param objectInstance
	 * @param objectToWrite the object that will be written if required by the operation
	 * @return may return an object (or a collection of objects) depending on the operation, null otherwise
	 * @throws RIBDaemonException on a number of circumstances
	 */
	public Object processOperation(Opcode opcode, String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException;

}
