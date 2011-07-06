package rina.ribdaemon.api;

import rina.cdap.api.CDAPMessageHandler;
import rina.cdap.api.message.CDAPMessage;
import rina.ipcprocess.api.IPCProcessComponent;

/**
 * Specifies the interface of the RIB Daemon
 * @author eduardgrasa
 */
public interface RIBDaemon extends IPCProcessComponent, RIBHandler{
	
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
	 * Add a ribHandler for a certain object name
	 * @param ribHandler
	 * @param objectName
	 * @throws RIBDaemonException
	 */
	public void addRIBHandler(RIBHandler ribHandler, String objectName) throws RIBDaemonException;
	
	/**
	 * Remove a ribHandler from a certain object name
	 * @param objectName
	 * @throws RIBDaemonException
	 */
	public void removeRIBHandler(RIBHandler ribHandler, String objectName) throws RIBDaemonException;
	
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
}