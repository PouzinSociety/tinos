package rina.applibrary.impl;

import java.net.FauxSocketFactory;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applibrary.api.Flow;
import rina.applibrary.api.FlowImpl;
import rina.applibrary.api.FlowListener;
import rina.applibrary.api.SDUListener;
import rina.applibrary.api.SocketFactory;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.delimiting.api.Delimiter;
import rina.encoding.api.Encoder;
import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.QualityOfServiceSpecification;

/**
 * The default implementation for the FlowImpl interface.
 * @author eduardgrasa
 *
 */
public class DefaultFlowImpl implements FlowImpl{
	
	private static final Log log = LogFactory.getLog(DefaultFlowImpl.class);
	
	private static final int MAX_WAITTIME_IN_SECONDS = 15;
	
	/**
	 * The name of the source application of this flow (i.e. the application that requested
	 * the establishment of this flow)
	 */
	private ApplicationProcessNamingInfo sourceApplication = null;
	
	/**
	 * The name of the destination application of this flow (i.e. the application that accepted 
	 * the establishment of this flow)
	 */
	private ApplicationProcessNamingInfo destinationApplication = null;
	
	/**
	 * The quality of service requested for this flow. This is the service level 
	 * agreement negotiated between the application requesting the flow and the DIF
	 */
	private QualityOfServiceSpecification qosSpec = null;
	
	/**
	 * The class to whom the received SDUs will be delivered
	 */
	private SDUListener sduListener = null;
	
	/**
	 * The class that will be notified when the flow is allocated 
	 * or deallocated
	 */
	private FlowListener flowListener = null;
	
	/**
	 * A pointer to the Flow class associated to this Flow implementation
	 */
	private Flow flow = null;
	
	/**
	 * The portId associated to this flow
	 */
	private int portId = 0;
	
	/**
	 * The state of the flow
	 */
	private State state = State.DEALLOCATED;
	
	/**
	 * The socket that this FlowImpl will be reading or writing
	 */
	private Socket socket = null;
	
	/**
	 * The reader of the socket, executed in a separate thread. Will get incoming 
	 * M_WRITE (incoming data) and M_DELETE (deallocation) requests
	 */
	private FlowSocketReader flowSocketReader = null;
	
	/**
	 * Queue used to exchange information and synchronize with the flowSocketReader
	 */
	private BlockingQueue<CDAPMessage> flowQueue = null;
	
	/**
	 * Controls if this object was created by the fauxSockets implementation and 
	 * therefore needs to use the faux Sockets constructors
	 */
	private boolean fauxSockets = false;
	
	/**
	 * The class that creates the socket instances
	 */
	private SocketFactory socketFactory = null;
	
	/* RINA Infrastructure */
	private Delimiter delimiter = null;
	private CDAPSessionManager cdapSessionManager = null;
	private Encoder encoder = null;
	
	/* Variables to write SDUs. They are declared here and initialized once since it is more efficient
	 * and 'write' can potentially be called many times */
	private ObjectValue writeObjectValue = null;
	private CDAPMessage writeCDAPMessage = null;
	
	public DefaultFlowImpl(){
		this.flowQueue = new LinkedBlockingQueue<CDAPMessage>();
		this.socketFactory = new StandardSocketFactory();
	}
	
	public DefaultFlowImpl(boolean fauxSockets){
		this.flowQueue = new LinkedBlockingQueue<CDAPMessage>();
		this.fauxSockets = fauxSockets;
		if (fauxSockets){
			this.socketFactory = new FauxSocketFactory();
		}else{
			this.socketFactory = new StandardSocketFactory();
		}
	}

	/**
	 * Will try to allocate a flow to the destination application process with the level of 
	 * service specified by qosSpec. SDUs received from the flow will be passed to the 
	 * sduListener class.
	 * @throws IPCException if something goes wrong. The explanation of what went wrong and why 
	 * will be in the "message" and "errorCode" fields of the class.
	 */
	public void allocate() throws IPCException {
		if (state != State.DEALLOCATED){
			IPCException ipcException = new IPCException(IPCException.FLOW_IS_ALREADY_ALLOCATED);
			ipcException.setErrorCode(IPCException.FLOW_IS_ALREADY_ALLOCATED_CODE);
			throw ipcException;
		}
		
		initializeRINAInfrastructure();
		
		try{
			log.debug("Attempting to allocate a flow from "+sourceApplication.toString()+ " to "+destinationApplication.toString());
			
			//1 Connect to the local RINA Software, and start the socket reader and the standard sockets implementation
			socket = socketFactory.createSocket(fauxSockets, "localhost", RINAFactory.DEFAULT_PORT);
			
			flowSocketReader = new FlowSocketReader(socket, delimiter, cdapSessionManager, flowQueue, this);
			flowSocketReader.setSDUListener(sduListener);
			RINAFactory.execute(flowSocketReader);
			
			//2 Create the CDAP message with an encoded FlowService object
			FlowService flowService = new FlowService();
			flowService.setSourceAPNamingInfo(sourceApplication);
			flowService.setDestinationAPNamingInfo(destinationApplication);
			flowService.setQoSSpecification(qosSpec);
			ObjectValue objectValue = new ObjectValue();
			objectValue.setByteval(encoder.encode(flowService));
			CDAPMessage cdapMessage = CDAPMessage.getCreateObjectRequestMessage(null, null, FlowService.OBJECT_CLASS, 0, FlowService.OBJECT_NAME, objectValue, 0);
			
			//3 Encode CDAPMessage and send request
			byte[] encodedMessage = cdapSessionManager.encodeCDAPMessage(cdapMessage);
			socket.getOutputStream().write(delimiter.getDelimitedSdu(encodedMessage));
			
			//4 Wait response (until timeout maximum)
			cdapMessage = flowQueue.poll(MAX_WAITTIME_IN_SECONDS, TimeUnit.SECONDS);

			if (cdapMessage == null){
				throw new IPCException("Didn't receive the reply from the local RINA software before "+MAX_WAITTIME_IN_SECONDS+" seconds.");
			}
			
			//5 if the reply is negative throw exception
			if (cdapMessage.getResult() != 0){
				throw new IPCException(cdapMessage.getResultReason());
			}
			
			//6 Response was successful, update the state and return
			setState(State.ALLOCATED);
			flowService = (FlowService) encoder.decode(cdapMessage.getObjValue().getByteval(), FlowService.class);
			this.portId = flowService.getPortId();
			
			log.debug("Flow allocated successfully! PortId: "+getPortId());
		}catch(Exception ex){
			try{
				if (socket != null && !socket.isClosed()){
					socket.close();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			
			IPCException ipcException = new IPCException(IPCException.PROBLEMS_ALLOCATING_FLOW + ex.getMessage());
			ipcException.setErrorCode(IPCException.PROBLEMS_ALLOCATING_FLOW_CODE);
			throw ipcException;
		}	
	}
	
	private void initializeRINAInfrastructure(){
		if(this.delimiter == null){
			delimiter = RINAFactory.getDelimiterInstance();
		}
		
		if (this.cdapSessionManager == null){
			cdapSessionManager = RINAFactory.getCDAPSessionManagerInstance();
		}
		
		if (this.encoder == null) {
			encoder = RINAFactory.getEncoderInstance();
		}
	}
	
	/**
	 * Write length bytes from the buffer, starting from 
	 * the position 0 of the byte array.
	 * @param buffer the data
	 * @param length the number of bytes to write
	 * @throws IPCException
	 */
	public void write(byte[] buffer, int length) throws IPCException{
		byte[] sdu = new byte[length];
		for(int i=0; i<sdu.length; i++){
			sdu[i] = buffer[i];
		}
		
		this.write(sdu);
	}

	/**
	 * Sends an SDU to the flow
	 * @param sdu
	 */
	public void write(byte[] sdu) throws IPCException {
		if (state != State.ALLOCATED){
			IPCException ipcException = new IPCException(IPCException.FLOW_NOT_IN_ALLOCATED_STATE);
			ipcException.setErrorCode(IPCException.FLOW_NOT_IN_ALLOCATED_STATE_CODE);
			throw ipcException;
		}
		
		try{
			if (this.writeObjectValue == null){
				this.writeObjectValue = new ObjectValue();
			}
			if (this.writeCDAPMessage == null){
				this.writeCDAPMessage = CDAPMessage.getWriteObjectRequestMessage(null, null, null, 0, writeObjectValue, null, 0);
			}
			this.writeCDAPMessage.getObjValue().setByteval(sdu);
			this.socket.getOutputStream().write(delimiter.getDelimitedSdu(cdapSessionManager.encodeCDAPMessage(writeCDAPMessage)));
		}catch(Exception ex){
			IPCException ipcException = new IPCException(IPCException.PROBLEMS_WRITING_TO_FLOW + ex.getMessage());
			ipcException.setErrorCode(IPCException.PROBLEMS_WRITING_TO_FLOW_CODE);
			throw ipcException;
		}
	}

	/**
	 * Causes the flow to be terminated. All the resources associated to it will be deallocated 
	 * @throws IPCException
	 */
	public void deallocate() throws IPCException {
		if (state != State.ALLOCATED){
			IPCException ipcException = new IPCException(IPCException.FLOW_NOT_IN_ALLOCATED_STATE);
			ipcException.setErrorCode(IPCException.FLOW_NOT_IN_ALLOCATED_STATE_CODE);
			throw ipcException;
		}
		
		try{
			//1 Create M_DELETE message, encode it and send it through socket
			CDAPMessage cdapMessage = CDAPMessage.getDeleteObjectRequestMessage(null, null, null, 0, null, null, 0);
			byte[] encodedMessage = cdapSessionManager.encodeCDAPMessage(cdapMessage);
			socket.getOutputStream().write(delimiter.getDelimitedSdu(encodedMessage));
			
			//2 Close the socket and return
			try{
				if (socket != null && !socket.isClosed()){
					socket.close();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			socket = null;
			flowSocketReader = null;
			setState(State.DEALLOCATED);
		}catch(Exception ex){
			IPCException ipcException = new IPCException(IPCException.PROBLEMS_DEALLOCATING_FLOW + ex.getMessage());
			ipcException.setErrorCode(IPCException.PROBLEMS_DEALLOCATING_FLOW_CODE);
			throw ipcException;
		}
	}
	
	/**
	 * Invoked by the thread reading the socket when a deallocate request is 
	 * received
	 * @param cdapMessage
	 */
	public void deallocateReceived(CDAPMessage cdapMessage){
		if (state != State.ALLOCATED){
			return;
		}
		
		try{
			if (socket != null && !socket.isClosed()){
				socket.close();
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		socket = null;
		flowSocketReader = null;
		setState(State.DEALLOCATED);
		if (flowListener != null){
			flowListener.flowDeallocated(this.flow);
		}
	}
	
	/**
	 * Invoked when the socketReader detects that the socket 
	 * has been closed
	 */
	public void socketClosed(){
		if (state != State.DEALLOCATED){
			socket = null;
			flowSocketReader = null;
			setState(State.DEALLOCATED);
			if (flowListener != null){
				flowListener.flowDeallocated(this.flow);
			}
		}
	}
	
	/**
	 * Set the socket that the FlowImpl object will use. This operation 
	 * will cause the FlowImpl to start a new SocketReader thread
	 * @param socket
	 * @throws IPCException if the FlowImpl object socket was already created
	 */
	public void setSocket(Socket socket) throws IPCException{
		if (this.socket != null){
			IPCException ipcException = new IPCException(IPCException.SOCKET_IS_ALREADY_PRESENT);
			ipcException.setErrorCode(IPCException.SOCKET_IS_ALREADY_PRESENT_CODE);
			throw ipcException;
		}
		
		if (socket == null){
			IPCException ipcException = new IPCException(IPCException.PROVIDED_SOCKET_NULL);
			ipcException.setErrorCode(IPCException.PROVIDED_SOCKET_NULL_CODE);
			throw ipcException;
		}
		
		if (!socket.isConnected()){
			IPCException ipcException = new IPCException(IPCException.PROVIDED_SOCKET_NOT_CONNECTED);
			ipcException.setErrorCode(IPCException.PROVIDED_SOCKET_NOT_CONNECTED_CODE);
			throw ipcException;
		}
		
		this.socket = socket;
		initializeRINAInfrastructure();
		flowSocketReader = new FlowSocketReader(socket, delimiter, cdapSessionManager, flowQueue, this);
		RINAFactory.execute(flowSocketReader);
		setState(State.ALLOCATED);
	}
	
	public void setFlowListener(FlowListener flowListener){
		this.flowListener = flowListener;
	}
	
	public void setFlow(Flow flow){
		this.flow = flow;
	}

	/**
	 * Get the name of the source application of this flow (i.e. the application that requested
	 * the establishment of this flow)
	 * @return source application process
	 */
	public ApplicationProcessNamingInfo getSourceApplication() {
		return sourceApplication;
	}

	/**
	 * Set the name of the source application of this flow (i.e. the application that requested
	 * the establishment of this flow)
	 * @param source application process
	 */
	public void setSourceApplication(ApplicationProcessNamingInfo sourceApplication) {
		this.sourceApplication = sourceApplication;
	}

	/**
	 * Get the name of the destination application of this flow (i.e. the application that accepted 
	 * the establishment of this flow)
	 * @return destination application process
	 */
	public ApplicationProcessNamingInfo getDestinationApplication() {
		return destinationApplication;
	}

	/**
	 * Set the name of the destination application of this flow (i.e. the application that accepted 
	 * the establishment of this flow)
	 * @param destination application process
	 */
	public void setDestinationApplication(ApplicationProcessNamingInfo destinationApplication) {
		this.destinationApplication = destinationApplication;
	}

	/**
	 * Get the quality of service requested for this flow. This is the service level 
	 * agreement negotiated between the application requesting the flow and the DIF
	 * @return QoS Specification
	 */
	public QualityOfServiceSpecification getQosSpec() {
		return qosSpec;
	}

	/**
	 * Set the quality of service requested for this flow. This is the service level 
	 * agreement negotiated between the application requesting the flow and the DIF
	 * @param QoS Specification
	 */
	public void setQosSpec(QualityOfServiceSpecification qosSpec) {
		this.qosSpec = qosSpec;
	}

	/**
	 * Get the class to be called when a new SDU is received by this flow
	 * @return SDU Listener
	 */
	public SDUListener getSduListener() {
		return flowSocketReader.getSDUListener();
	}

	/**
	 * Set the class to be called when a new SDU is received by this flow
	 * @param SDU Listener
	 */
	public void setSduListener(SDUListener sduListener){
		this.sduListener = sduListener;
		if (flowSocketReader != null){
			flowSocketReader.setSDUListener(sduListener);
		}
	}

	/**
	 * Returns the flow state
	 * @return
	 */
	public State getState(){
		return this.state;
	}
	
	/**
	 * Sets the flow state
	 * @param current state
	 * @param state
	 */
	public void setState(State state){
		this.state = state;
	}
	
	public void setDelimiter(Delimiter delimiter){
		this.delimiter = delimiter;
	}
	
	public void setCDAPSessionManager(CDAPSessionManager cdapSessionManager){
		this.cdapSessionManager = cdapSessionManager;
	}
	
	public void setEncoder(Encoder encoder){
		this.encoder = encoder;
	}
	
	/**
	 * Return the portId associated to this flow
	 * @return
	 */
	public int getPortId(){
		return this.portId;
	}
	
	/**
	 * Set the portId associated to this flow
	 * @param portId
	 */
	public void setPortId(int portId){
		this.portId = portId;
	}
}
