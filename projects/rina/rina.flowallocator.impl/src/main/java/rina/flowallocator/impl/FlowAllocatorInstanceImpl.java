package rina.flowallocator.impl;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPMessageHandler;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.delimiting.api.BaseDelimiter;
import rina.delimiting.api.Delimiter;
import rina.efcp.api.DataTransferAEInstance;
import rina.encoding.api.BaseEncoder;
import rina.encoding.api.Encoder;
import rina.flowallocator.api.BaseFlowAllocator;
import rina.flowallocator.api.Connection;
import rina.flowallocator.api.FlowAllocator;
import rina.flowallocator.api.FlowAllocatorInstance;
import rina.flowallocator.api.message.Flow;
import rina.flowallocator.impl.policies.NewFlowRequestPolicy;
import rina.flowallocator.impl.policies.NewFlowRequestPolicyImpl;
import rina.flowallocator.impl.tcp.TCPSocketReader;
import rina.flowallocator.impl.timertasks.SocketClosedTimerTask;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.IPCService;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObjectNames;
import rina.utils.types.Unsigned;

/**
 * The Flow Allocator is the component of the IPC Process that responds to Allocation API invocations 
 * from Application Processes. It creates and monitors a flow and provides any management over its lifetime.
 * Its only service is to network management.
 * @author eduardgrasa
 *
 */
public class FlowAllocatorInstanceImpl implements FlowAllocatorInstance, CDAPMessageHandler{
	
	private static final Log log = LogFactory.getLog(FlowAllocatorInstanceImpl.class);
	
	/**
	 * The new flow request policy, will translate the allocate request into 
	 * a flow object
	 */
	private NewFlowRequestPolicy newFlowRequestPolicy = null;
	
	/**
	 * The current active connection
	 */
	private Connection activeConnection = null;
	
	/**
	 * All the connections associated to this flow
	 */
	private List<Connection> connections = null;
	
	/**
	 * The data transfer AE instance associated to this flow allocator instance
	 */
	private DataTransferAEInstance dataTransferAEInstance = null;
	
	/**
	 * The Flow Allocator
	 */
	private FlowAllocator flowAllocator = null;
	
	/**
	 * The application process that requested the allocation of the flow
	 */
	private APService apService = null;
	
	/**
	 * The portId associated to this Flow Allocator instance
	 */
	private int portId = 0;
	
	/**
	 * The flow object related to this Flow Allocator Instance
	 */
	private Flow flow = null;
	
	/**
	 * The request message;
	 */
	private CDAPMessage requestMessage = null;
	
	/**
	 * The underlying portId to reply to the request message
	 */
	private int underlyingPortId = 0;
	
	/**
	 * The data socket associated to this flow. For the current prototype a 
	 * flow is directly associated to a TCP socket.
	 */
	private Socket socket = null;
	
	/**
	 * Reads the data coming from the TCP connection that supports the flow
	 */
	private TCPSocketReader tcpSocketReader = null;
	
	/**
	 * Controls if this Flow Allocator instance is operative or not
	 */
	private boolean finished = false;
	
	/**
	 * The timer for this Flow Allocator instance
	 */
	private Timer timer = null;
	
	private CDAPSessionManager cdapSessionManager = null;
	
	private IPCProcess ipcProcess = null;
	
	/**
	 * Tells if this flow is local (same system)
	 */
	private boolean local = false;
	
	/**
	 * If this flow is local (same system), this is the portId of the other Flow Allocator Instance
	 */
	private int remotePortId = 0;
	
	/**
	 * The name of the flow object associated to this FlowAllocatorInstance
	 */
	private String objectName = null;
	
	/**
	 * The RIBDaemon of the IPC process
	 */
	private RIBDaemon ribDaemon = null;
	
	/**
	 * The class used to delimit messages
	 */
	private Delimiter delimiter = null;
	
	/**
	 * The class used to encode and decode objects transmitted through CDAP
	 */
	private Encoder encoder = null;
	
	public FlowAllocatorInstanceImpl(IPCProcess ipcProcess, FlowAllocator flowAllocator, CDAPSessionManager cdapSessionManager, int portId){
		initialize(ipcProcess, flowAllocator, portId);
		this.timer = new Timer();
		this.cdapSessionManager = cdapSessionManager;
		connections = new ArrayList<Connection>();
		//TODO initialize the newFlowRequestPolicy
		newFlowRequestPolicy = new NewFlowRequestPolicyImpl();
		log.debug("Created flow allocator instance to manage the flow identified by portId "+portId);
	}
	
	/**
	 * The flow allocator instance will manage a local flow
	 * @param ipcProcess
	 * @param flowAllocator
	 * @param portId
	 */
	public FlowAllocatorInstanceImpl(IPCProcess ipcProcess, FlowAllocator flowAllocator, int portId){
		initialize(ipcProcess, flowAllocator, portId);
		this.local = true;
		log.debug("Created flow allocator instance to manage the flow identified by portId "+portId);
	}
	
	private void initialize(IPCProcess ipcProcess, FlowAllocator flowAllocator, int portId){
		this.flowAllocator = flowAllocator;
		this.ipcProcess = ipcProcess;
		this.apService = ipcProcess.getAPService();
		this.portId = portId;
		this.ribDaemon = (RIBDaemon) ipcProcess.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		this.delimiter = (Delimiter) ipcProcess.getIPCProcessComponent(BaseDelimiter.getComponentName());
		this.encoder = (Encoder) ipcProcess.getIPCProcessComponent(BaseEncoder.getComponentName());
	}
	
	public Socket getSocket(){
		return this.socket;
	}
	
	public int getPortId(){
		return portId;
	}
	
	public Flow getFlow(){
		return this.flow;
	}
	
	public boolean isFinished(){
		return finished;
	}

	/**
	 * Generate the flow object, create the local DTP and optionally DTCP instances, generate a CDAP 
	 * M_CREATE request with the flow object and send it to the appropriate IPC process (search the 
	 * directory and the directory forwarding table if needed)
	 * @param allocateRequest the allocate request
	 * @param portId the local port id associated to this flow
	 * @throws IPCException if there are not enough resources to fulfill the allocate request
	 */
	public void submitAllocateRequest(FlowService flowService) throws IPCException {
		flow = newFlowRequestPolicy.generateFlowObject(flowService);
		log.debug("Generated flow object: "+flow.toString());
		ObjectValue objectValue = null;
		CDAPMessage cdapMessage = null;
		
		//1 Check directory to see to what IPC process the CDAP M_CREATE request has to be delivered
		long destinationAddress = flowAllocator.getDirectoryForwardingTable().getAddress(flowService.getDestinationAPNamingInfo());
		flow.setDestinationAddress(destinationAddress);
		if (destinationAddress == 0){
			throw new IPCException(IPCException.COULD_NOT_FIND_ENTRY_IN_DIRECTORY_FORWARDING_TABLE_CODE, 
					IPCException.COULD_NOT_FIND_ENTRY_IN_DIRECTORY_FORWARDING_TABLE + flowService.getDestinationAPNamingInfo().toString());
		}
		
		//2 Check if the destination address is this IPC process (then invoke degenerated form of IPC)
		long sourceAddress = flowAllocator.getIPCProcess().getAddress().longValue();
		flow.setSourceAddress(sourceAddress);
		String flowName = ""+sourceAddress+"-"+this.portId;
		this.objectName = Flow.FLOW_SET_RIB_OBJECT_NAME + RIBObjectNames.SEPARATOR + flowName;
		if (destinationAddress == sourceAddress){
			local = true;
			this.flowAllocator.receivedLocalFlowRequest(flowService, this.objectName);
			return;
		}
		
		//3 Start the flow allocation sequence by opening a socket to the remote flow allocator
		this.socket = connectToRemoteFlowAllocator(destinationAddress);
		flow.setSourcePortId(portId);
		flow.setTcpRendezvousId(socket.getLocalPort());
		
		//4 Map the address to the port id through which I can reach the destination application process name
		int rmtPortId = Utils.mapAddressToPortId(destinationAddress, flowAllocator.getIPCProcess());
		
		//5 Encode the flow object and send it to the destination IPC process
		try{
			objectValue = new ObjectValue();
			objectValue.setByteval(this.encoder.encode(flow));
			cdapMessage = cdapSessionManager.getCreateObjectRequestMessage(rmtPortId, null, null, "flow", 0, objectName, objectValue, 0, true);
			this.ribDaemon.sendMessage(cdapMessage, rmtPortId, this);
			this.underlyingPortId = rmtPortId;
			this.requestMessage = cdapMessage;
		}catch(Exception ex){
			log.error(ex);
			throw new IPCException(IPCException.PROBLEMS_ALLOCATING_FLOW_CODE, 
					IPCException.PROBLEMS_ALLOCATING_FLOW + ex.getMessage());
		}
	}
	
	/**
	 * Establishes a TCP flow to a remote flow allocator and sends the portid as data over the flow
	 * @param applicationProcessName
	 * @param applicationProcessInstance
	 * @return
	 */
	private Socket connectToRemoteFlowAllocator(long address) throws IPCException{
		String ipAddress = null;
		Socket socket = null;
		int port = 0;
		
		
		try{
			port = Integer.parseInt(System.getProperty(BaseFlowAllocator.FLOW_ALLOCATOR_PORT_PROPERTY));
		}catch(Exception ex){
			port = BaseFlowAllocator.DEFAULT_PORT;
		}
		
		try{
			ipAddress = Utils.getRemoteIPCProcessIPAddress(address, flowAllocator.getIPCProcess());
			socket = new Socket(ipAddress, port);
			Unsigned unsigned = new Unsigned(2);
			unsigned.setValue(new Integer(socket.getLocalPort()).longValue());
			socket.getOutputStream().write(unsigned.getBytes());
			log.debug("Started a socket to the Flow Allocator at "+ipAddress+":"+port+". The local socket number is "+socket.getLocalPort());
			return socket;
		}catch(Exception ex){
			throw new IPCException(5, "Problems connecting to remote Flow Allocator. "+ex.getMessage());
		}
	}

	/**
	 * When an FAI is created with a Create_Request(Flow) as input, it will inspect the parameters 
	 * first to determine if the requesting Application (Source_Naming_Info) has access to the requested 
	 * Application (Destination_Naming_Info) by inspecting the Access Control parameter.  If not, a 
	 * negative Create_Response primitive will be returned to the requesting FAI. If it does have access, 
	 * the FAI will determine if the policies proposed are acceptable, invoking the NewFlowRequestPolicy.  
	 * If not, a negative Create_Response is sent.  If they are acceptable, the FAI will invoke a 
	 * Allocate_Request.deliver primitive to notify the requested Application that it has an outstanding 
	 * allocation request.  (If the application is not executing, the FAI will cause the application
	 * to be instantiated.)
	 * @param flow
	 * @param portId
	 * @param invokeId
	 * @param flowObjectName
	 */
	public void createFlowRequestMessageReceived(Flow flow, CDAPMessage requestMessage, int underlyingPortId) {
		log.debug("Create flow request received.\n  "+flow.toString());
		this.flow = flow;
		this.requestMessage = requestMessage;
		this.underlyingPortId = underlyingPortId;
		flow.setDestinationPortId(portId);
	}
	
	/**
	 * Then this happens both the M_CREATE Flow request and the TCP connection have been established, therefore 
	 * we can continue with the Flow Allocation sequence.
	 * @param socket
	 */
	public synchronized void setSocket(Socket socket){
		if (this.socket != null){
			return;
		}
		this.socket = socket;
		
		//1 TODO Check if the source application process has access to the destination application process. If not send negative M_CREATE_R 
		//back to the sender IPC process, and housekeeping.
		//Not done in this version, this decision is left to the application 
		//2 TODO If it has, determine if the proposed policies for the flow are acceptable (invoke NewFlowREquestPolicy)
		//Not done in this version, it is assumed that the proposed policies for the flow are acceptable.
		//3 If they are acceptable, the FAI will invoke the Allocate_Request.deliver operation of the destination application process. To do so it will find the
		//IPC Manager and pass it the allocation request.
		FlowService flowService = new FlowService();
		flowService.setSourceAPNamingInfo(flow.getSourceNamingInfo());
		flowService.setDestinationAPNamingInfo(flow.getDestinationNamingInfo());
		flowService.setQoSSpecification(flow.getQosParameters());
		flowService.setPortId(this.portId);
		apService.deliverAllocateRequest(flowService, (IPCService) ipcProcess);
	}
	
	/**
	 * Called when the Flow Allocator receives a request for a local flow
	 * @param flowService
	 * @param objectName
	 * @throws IPCException
	 */
	public void receivedLocalFlowRequest(FlowService flowService, String objectName) throws IPCException{
		if (!local){
			throw new IPCException(IPCException.PROBLEMS_ALLOCATING_FLOW_CODE, 
					IPCException.PROBLEMS_ALLOCATING_FLOW + "This Flow Allocator instance cannot deal with local flows.");
		}
		
		this.remotePortId = flowService.getPortId();
		this.objectName = objectName;
		flowService.setPortId(portId);
		apService.deliverAllocateRequest(flowService, (IPCService) ipcProcess);
	}

	/**
	 * When the FAI gets a Allocate_Response from the destination application, it formulates a Create_Response 
	 * on the flow object requested.If the response was positive, the FAI will cause DTP and if required DTCP 
	 * instances to be created to support this allocation. A positive Create_Response Flow is sent to the 
	 * requesting FAI with the connection-endpoint-id and other information provided by the destination FAI. 
	 * The Create_Response is sent to requesting FAI with the necessary information reflecting the existing flow, 
	 * or an indication as to why the flow was refused.  
	 * If the response was negative, the FAI does any necessary housekeeping and terminates.
	 * @param portId
	 * @param reason
	 * @throws IPCException
	 */
	public void submitAllocateResponse(boolean success, String reason) throws IPCException{
		CDAPMessage cdapMessage = null;
		
		//If the IPC process is local, just call the Flow Allocator directly
		if (local){
			this.flowAllocator.receivedLocalFlowResponse(remotePortId, portId, success, reason);
			if (success){
				try{
					this.ribDaemon.create("flow", this.objectName, 0, this, null);
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
			return;
		}
		
		if (success){
			//1 TODO Create DTP and DTCP instances
			//2 Create CDAP response message
			try{
				ObjectValue objectValue = new ObjectValue();
				objectValue.setByteval(this.encoder.encode(flow));
				cdapMessage = cdapSessionManager.getCreateObjectResponseMessage(underlyingPortId, null, requestMessage.getObjClass(), 
						0, requestMessage.getObjName(), objectValue, 0, null, requestMessage.getInvokeID());
				this.ribDaemon.sendMessage(cdapMessage, underlyingPortId, null);
				this.ribDaemon.create(requestMessage.getObjClass(), requestMessage.getObjName(), 0, this, null);
				tcpSocketReader = new TCPSocketReader(socket, this.delimiter, this.apService, this.portId);
				this.ipcProcess.execute(tcpSocketReader);
			}catch(Exception ex){
				log.error(ex);
				throw new IPCException(IPCException.PROBLEMS_ALLOCATING_FLOW_CODE, 
						IPCException.PROBLEMS_ALLOCATING_FLOW + ex.getMessage());
			}
		}else{
			//Create CDAP response message
			try{
				cdapMessage = cdapSessionManager.getCreateObjectResponseMessage(underlyingPortId, null, requestMessage.getObjClass(), 
						0, requestMessage.getObjName(), null, -1, reason, requestMessage.getInvokeID());
				this.ribDaemon.sendMessage(cdapMessage, underlyingPortId, null);
			}catch(Exception ex){
				log.error(ex);
				throw new IPCException(IPCException.PROBLEMS_ALLOCATING_FLOW_CODE, 
						IPCException.PROBLEMS_ALLOCATING_FLOW + ex.getMessage());
			}
		}
	}

	/**
	 * When a deallocate primitive is invoked, it is passed to the FAI responsible for that port-id.  
	 * The FAI sends an M_DELETE request CDAP PDU on the Flow object referencing the destination port-id, deletes the local 
	 * binding between the Application and the DTP-instance and waits for a response.  (Note that 
	 * the DTP and DTCP if it exists will be deleted automatically after 2MPL)
	 * @throws IPCException
	 */
	public void submitDeallocate() throws IPCException{
		if (local){
			this.flowAllocator.receivedDeallocateLocalFlowRequest(this.remotePortId);
			try{
				this.ribDaemon.delete("flow", objectName, 0, null, null);
				this.flowAllocator.removeFlowAllocatorInstance(this.portId);
			}catch(Exception ex){
				ex.printStackTrace();
			}
			return;
		}
		
		//1 Send the M_DELETE Flow message
		try{
			ObjectValue objectValue = new ObjectValue();
			objectValue.setByteval(this.encoder.encode(flow));
			requestMessage = cdapSessionManager.getDeleteObjectRequestMessage(
					underlyingPortId, null, null, "flow", 0, requestMessage.getObjName(), null, 0, true); 
			this.ribDaemon.sendMessage(requestMessage, underlyingPortId, this);
			//TODO set timer to wait for M_DELETE_R message. If the message is not received before timer expiration, remove
			//the Flow object from the RIB
		}catch(Exception ex){
			log.error(ex);
			throw new IPCException(IPCException.PROBLEMS_DEALLOCATING_FLOW_CODE, 
					IPCException.PROBLEMS_DEALLOCATING_FLOW + ex.getMessage());
		}
		
		//2 Delete the binding between the TCP connection and the portId, and terminate the TCP connection
		destroyFlowAllocatorInstance();
	}

	/**
	 * When this PDU is received by the FAI with this port-id, the FAI invokes a Deallocate.deliver to notify the local Application, 
	 * deletes the binding between the Application and the local DTP-instance, and sends a Delete_Response indicating the result.
	 * @param cdapMessage
	 * @param underlyingPortId
	 */
	public void deleteFlowRequestMessageReceived(CDAPMessage cdapMessage, int underlyingPortId){
		//1 Notify application
		this.apService.deliverDeallocate(portId);
		
		//2 Reply back
		try{
			CDAPMessage responseMessage = cdapSessionManager.getDeleteObjectResponseMessage(underlyingPortId, null, 
					cdapMessage.getObjClass(), cdapMessage.getObjInst(), cdapMessage.getObjName(), 0, null, cdapMessage.getInvokeID());
			this.ribDaemon.sendMessage(responseMessage, underlyingPortId, null);
			this.ribDaemon.delete(cdapMessage.getObjClass(), cdapMessage.getObjName(), cdapMessage.getObjInst(), null, null);
		}catch(Exception ex){
			ex.printStackTrace(); 
		}
		
		//3 Destroy flow allocator instance
		destroyFlowAllocatorInstance();
	}
	
	/**
	 * Request to deallocate a local flow
	 * @throws IPCException
	 */
	public void receivedDeallocateLocalFlowRequest() throws IPCException{
		//1 Notify application
		this.apService.deliverDeallocate(portId);
		
		//2 Cleanup
		try{
			this.ribDaemon.delete("flow", objectName, 0, null, null);
			this.flowAllocator.removeFlowAllocatorInstance(this.portId);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/**
	 * If the response to the allocate request is negative 
	 * the Allocation invokes the AllocateRetryPolicy. If the AllocateRetryPolicy returns a positive result, a new Create_Flow Request 
	 * is sent and the CreateFlowTimer is reset.  Otherwise, if the AllocateRetryPolicy returns a negative result or the MaxCreateRetries has been exceeded, 
	 * an Allocate_Request.deliver primitive to notify the Application that the flow could not be created. (If the reason was 
	 * ÒApplication Not Found,Ó the primitive will be delivered to the Inter-DIF Directory to search elsewhere.The FAI deletes the DTP and DTCP instances 
	 * it created and does any other housekeeping necessary, before terminating.  If the response is positive, it completes the binding of the DTP-instance 
	 * with this connection-endpoint-id to the requesting Application and invokes a Allocate_Request.submit primitive to notify the requesting Application 
	 * that its allocation request has been satisfied.
	 * @param CDAPMessage
	 * @param CDAPSessionDescriptor
	 */
	public void createResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		if (!cdapMessage.getObjName().equals(requestMessage.getObjName())){
			log.error("Expected create flow response message for flow "+requestMessage.getObjName()+
					", but received create flow response message for flow "+cdapMessage.getObjName());
			//TODO, what to do?
			return;
		}
		
		if (cdapMessage.getResult() != 0){
			log.debug("Unsuccessful create flow response message received for flow "+cdapMessage.getObjName());
			destroyFlowAllocatorInstance();
			this.apService.deliverAllocateResponse(portId, cdapMessage.getResult(), cdapMessage.getResultReason());
			return;
		}

		try{
			this.flow = (Flow) this.encoder.decode(cdapMessage.getObjValue().getByteval(), Flow.class);
			log.debug("Successfull create flow message response received for flow "+cdapMessage.getObjName()+".\n "+this.flow.toString());
			this.ribDaemon.create("flow", objectName, 0, this, null);
			this.tcpSocketReader = new TCPSocketReader(socket, this.delimiter, this.apService, this.portId);
			this.ipcProcess.execute(tcpSocketReader);
			this.apService.deliverAllocateResponse(portId, 0, null);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/**
	 * Called when the Flow Allocator receives a response to a request for a local flow
	 * @param remotePortId
	 * @param result
	 * @param resultReason
	 * @throws IPCException
	 */
	public void receivedLocalFlowResponse(int remotePortId, boolean result, String resultReason) throws IPCException{
		if (!local){
			throw new IPCException(IPCException.PROBLEMS_ALLOCATING_FLOW_CODE, 
					IPCException.PROBLEMS_ALLOCATING_FLOW + "This Flow Allocator instance cannot deal with local flows.");
		}
		
		if (result){
			try{
				this.remotePortId = remotePortId;
				this.apService.deliverAllocateResponse(portId, 0, null);
				this.ribDaemon.create("flow", objectName, 0, this, null);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}else{
			this.apService.deliverAllocateResponse(portId, -1, resultReason);
		}
	}
	
	/**
	 * Delimits and sends an SDU through the flow managed by this flow allocator instance.
	 * This function is just for the RINA prototype over TCP. When DTP and DTCP are implemented
	 * this operation will be removed from here.
	 * @param sdu
	 * @throws IPCException
	 */
	public void submitTransfer(byte[] sdu) throws IPCException{
		if (local){
			this.apService.deliverTransfer(this.remotePortId, sdu);
			return;
		}
		
		try{
			this.socket.getOutputStream().write(this.delimiter.getDelimitedSdu(sdu));
		}catch(IOException ex){
			log.error(ex);
			throw new IPCException(IPCException.PROBLEMS_SENDING_SDU_CODE, 
					IPCException.PROBLEMS_SENDING_SDU + ex.getMessage());
		}
	}
	
	public void destroyFlowAllocatorInstance(){
		//Close the socket if it is still open
		if (!this.socket.isClosed()){
			try{
				this.socket.close();
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		
		this.flowAllocator.removeFlowAllocatorInstance(this.portId);
		this.finished = true;
		timer.cancel();
	}

	/**
	 * When a Delete_Response PDU is received, the FAI completes any housekeeping and terminates.
	 * @param cdapMessage
	 * @param cdapSessionDescriptor
	 */
	public void deleteResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException{
		if (!cdapMessage.getObjName().equals(requestMessage.getObjName())){
			log.error("Expected delete flow response message for flow "+requestMessage.getObjName()+
					", but received delete flow response message for flow "+cdapMessage.getObjName());
			//TODO, what to do?
			return;
		}
		
		//TODO Cancel timer
		
		RIBDaemon ribDaemon = (RIBDaemon) flowAllocator.getIPCProcess().getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		try{
			ribDaemon.delete(cdapMessage.getObjClass(), cdapMessage.getObjName(), cdapMessage.getObjInst(), null, null);
		}catch(RIBDaemonException ex){
			ex.printStackTrace();
		}
	}
	
	/**
	 * When the TCP Socket Reader detects that the socket is closed, 
	 * it will notify the Flow Allocator instance
	 */
	public void socketClosed(){
		SocketClosedTimerTask task = new SocketClosedTimerTask(this, ribDaemon, requestMessage.getObjClass(), requestMessage.getObjName());
		timer.schedule(task, SocketClosedTimerTask.DELAY);
	}
	
	public void cancelReadResponse(CDAPMessage arg0, CDAPSessionDescriptor arg1) throws RIBDaemonException {
		// TODO Auto-generated method stub
	}

	public void readResponse(CDAPMessage arg0, CDAPSessionDescriptor arg1)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void startResponse(CDAPMessage arg0, CDAPSessionDescriptor arg1)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void stopResponse(CDAPMessage arg0, CDAPSessionDescriptor arg1)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void writeResponse(CDAPMessage arg0, CDAPSessionDescriptor arg1)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}
}
