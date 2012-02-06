package rina.applibrary.api;

import java.net.Socket;

import rina.cdap.api.message.CDAPMessage;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.QualityOfServiceSpecification;

/**
 *
 * Classes implementing FlowImpl perform the actual job of 
 * implementing the operations provided by the Flow class
 * @author eduardgrasa
 *
 */
public interface FlowImpl {
	
	public enum State {ALLOCATED, DEALLOCATED};
	
	/**
	 * Will try to allocate a flow to the destination application process with the level of 
	 * service specified by qosSpec. SDUs received from the flow will be passed to the 
	 * sduListener class.
	 * @throws FlowException if something goes wrong. The explanation of what went wrong and why 
	 * will be in the "message" and "errorCode" fields of the class.
	 */
	public void allocate() throws IPCException;
	
	/**
	 * Sends an SDU to the flow
	 * @param sdu
	 */
	public void write(byte[] sdu) throws IPCException;
	
	/**
	 * Causes the flow to be terminated. All the resources associated to it will be deallocated 
	 * @throws IPCException
	 */
	public void deallocate() throws IPCException;
	
	/**
	 * Set the socket that the FlowImpl object will use. This operation 
	 * will cause the FlowImpl to start a new SocketReader thread
	 * @param socket
	 * @throws IPCException if the FlowImpl object socket was already created
	 */
	public void setSocket(Socket socket) throws IPCException;
	
	/**
	 * Invoked by the thread reading the socket when a deallocate request is 
	 * received
	 * @param cdapMessage
	 */
	public void deallocateReceived(CDAPMessage cdapMessage);
	
	/**
	 * Invoked when the socketReader detects that the socket 
	 * has been closed
	 */
	public void socketClosed();
	
	/* Setters and getters */
	/**
	 * Get the name of the source application of this flow (i.e. the application that requested
	 * the establishment of this flow)
	 * @return source application process
	 */
	public ApplicationProcessNamingInfo getSourceApplication();

	/**
	 * Set the name of the source application of this flow (i.e. the application that requested
	 * the establishment of this flow)
	 * @param source application process
	 */
	public void setSourceApplication(ApplicationProcessNamingInfo sourceApplication);

	/**
	 * Get the name of the destination application of this flow (i.e. the application that accepted 
	 * the establishment of this flow)
	 * @return destination application process
	 */
	public ApplicationProcessNamingInfo getDestinationApplication();

	/**
	 * Set the name of the destination application of this flow (i.e. the application that accepted 
	 * the establishment of this flow)
	 * @return destination application process
	 */
	public void setDestinationApplication(ApplicationProcessNamingInfo destinationApplication);

	/**
	 * Get the quality of service requested for this flow. This is the service level 
	 * agreement negotiated between the application requesting the flow and the DIF
	 * @return QoS Specification
	 */
	public QualityOfServiceSpecification getQosSpec();

	/**
	 * Set the quality of service requested for this flow. This is the service level 
	 * agreement negotiated between the application requesting the flow and the DIF
	 * @param QoS Specification
	 */
	public void setQosSpec(QualityOfServiceSpecification qosSpec);

	/**
	 * Get the class to be called when a new SDU is received by this flow
	 * @return SDU Listener
	 */
	public SDUListener getSduListener();

	/**
	 * Set the class to be called when a new SDU is received by this flow
	 * @param SDU Listener
	 */
	public void setSduListener(SDUListener sduListener);
	
	/**
	 * Returns the flow state
	 * @return
	 */
	public State getState();
	
	/**
	 * Sets the flow state
	 * @param current state
	 * @param state
	 */
	public void setState(State state);
	
	/**
	 * Return the portId associated to this flow
	 * @return
	 */
	public int getPortId();
}
