package rina.enrollment.api;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.ipcprocess.api.IPCProcessComponent;

/**
 * The enrollment task manages the members of the DIF. It implements the state machines that are used 
 * to join a DIF or to collaboarate with a remote IPC Process to allow him to join the DIF.
 * @author eduardgrasa
 *
 */
public interface EnrollmentTask extends IPCProcessComponent{
	
	/**
	 * A remote IPC process Connect request has been received
	 * @param cdapMessage
	 * @param cdapSessionDescriptor
	 */
	public void connect(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor);
	
	/**
	 * A remote IPC process Connect response has been received
	 * @param cdapMessage
	 * @param cdapSessionDescriptor
	 */
	public void connectResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor);
	
	/**
	 * A remote IPC process Release request has been received
	 * @param cdapMessage
	 * @param cdapSessionDescriptor
	 */
	public void release(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor);
	
	/**
	 * A remote IPC process Release response has been received
	 * @param cdapMessage
	 * @param cdapSessionDescriptor
	 */
	public void releaseResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor);
	
	/**
	 * Called by the DIFMemberSetRIBObject when a CREATE request for a new member is received
	 * @param cdapMessage
	 * @param cdapSessionDescriptor
	 */
	public void initiateEnrollment(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor);
}
