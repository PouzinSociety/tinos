package rina.enrollment.impl;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;

public class PendingEnrollmentRequest {
	
	/** The CDAP message that triggered the enrollment **/
	private CDAPMessage cdapMessage = null;
	
	/** The CDAP Session Descriptor that identifies the CDAP session 
	 * that has to be used to reply the request
	 */
	private CDAPSessionDescriptor cdapSessionDescriptor = null;
	
	/**
	 * The portId allocated to the new enrollment request
	 */
	private int portId = 0;
	
	public PendingEnrollmentRequest(){
	}
	
	public PendingEnrollmentRequest(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor, int portId){
		this.cdapMessage = cdapMessage;
		this.cdapSessionDescriptor = cdapSessionDescriptor;
		this.portId = portId;
	}

	public CDAPMessage getCdapMessage() {
		return cdapMessage;
	}

	public void setCdapMessage(CDAPMessage cdapMessage) {
		this.cdapMessage = cdapMessage;
	}

	public CDAPSessionDescriptor getCdapSessionDescriptor() {
		return cdapSessionDescriptor;
	}

	public void setCdapSessionDescriptor(CDAPSessionDescriptor cdapSessionDescriptor) {
		this.cdapSessionDescriptor = cdapSessionDescriptor;
	}

	public int getPortId() {
		return portId;
	}

	public void setPordId(int portId) {
		this.portId = portId;
	}
}
