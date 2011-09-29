package rina.ribdaemon.api;

import java.util.List;

import rina.cdap.api.CDAPMessageHandler;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.ipcprocess.api.IPCProcessComponent;

/**
 * Specifies the interface of the RIB Daemon
 * @author eduardgrasa
 */
public interface RIBDaemon extends IPCProcessComponent{
	
	/**
	 * Invoked by the RMT when it detects a CDAP message. The RIB Daemon has to process the CDAP message and, 
	 * if valid, it will either pass it to interested subscribers and/or write to storage and/or modify other 
	 * tasks data structures. It may be the case that the CDAP message is not addressed to an application 
	 * entity within this IPC process, then the RMT may decide to rely the message to the right destination 
	 * (after consulting an adequate forwarding table).
	 * @param cdapMessage
	 * @param the portId of the flow from where the CDAP message was obtained
	 */
	public void cdapMessageDelivered(byte[] cdapMessage, int portId);
	
	/**
	 * Invoked by the RMT when it detects that a certain flow has been deallocated, and therefore any CDAP sessions 
	 * over it should be terminated.
	 * @param portId identifies the flow that has been deallocated
	 */
	public void flowDeallocated(int portId);
	
	/**
	 * Add a RIB object to the RIB
	 * @param ribHandler
	 * @param objectName
	 * @throws RIBDaemonException
	 */
	public void addRIBObject(RIBObject ribObject) throws RIBDaemonException;
	
	/**
	 * Remove a ribObject from the RIB
	 * @param objectName
	 * @throws RIBDaemonException
	 */
	public void removeRIBObject(RIBObject ribObject, String objectName) throws RIBDaemonException;
	
	/**
	 * Send an information update, consisting on a set of CDAP messages, using the updateStrategy update strategy
	 * (on demand, scheduled)
	 * @param cdapMessages
	 * @param updateStrategy
	 */
	public void sendMessages(CDAPMessage[] cdapMessages, UpdateStrategy updateStrategy);
	
	/**
	 * Causes a CDAP message to be sent
	 * @param cdapMessage the message to be sent
	 * @param sessionId the CDAP session id
	 * @param cdapMessageHandler the class to be called when the response message is received (if required)
	 * @throws RIBDaemonException
	 */
	public void sendMessage(CDAPMessage cdapMessage, int sessionId, CDAPMessageHandler cdapMessageHandler) throws RIBDaemonException;
	
	/**
	 * Reads/writes/created/deletes/starts/stops one or more objects at the RIB, matching the information specified by objectId + objectClass or objectInstance.
	 * At least objectName or objectInstance have to be not null. This operation is invoked because the RIB Daemon has received a CDAP message from another 
	 * IPC process
	 * @param cdapMessage The CDAP message received
	 * @param cdapSessionDescriptor Describes the CDAP session to where the CDAP message belongs
	 * @throws RIBDaemonException on a number of circumstances
	 */
	public void processOperation(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException;
	
	public void create(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException;
	
	public void delete(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException;
	
	public RIBObject read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException;
	
	public void write(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException;
	
	public void start(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException;
	
	public void stop(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException;	
	
	public List<RIBObject> getRIBObjects();
}