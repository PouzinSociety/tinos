package rina.flowallocator.impl;

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
import rina.efcp.api.DataTransferAEInstance;
import rina.encoding.api.BaseEncoder;
import rina.encoding.api.Encoder;
import rina.flowallocator.api.Connection;
import rina.flowallocator.api.FlowAllocator;
import rina.flowallocator.api.FlowAllocatorInstance;
import rina.flowallocator.api.message.Flow;
import rina.flowallocator.impl.policies.NewFlowRequestPolicy;
import rina.flowallocator.impl.policies.NewFlowRequestPolicyImpl;
import rina.flowallocator.impl.tcp.TCPServer;
import rina.flowallocator.impl.tcp.TCPSocketReader;
import rina.flowallocator.impl.timertasks.SocketClosedTimerTask;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObjectNames;
import rina.utils.types.Unsigned;

/**
 * A flow allocator instance implementation. Its task is to manage the 
 * lifecycle of an EFCP connection
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
	private APService applicationProcess = null;
	
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
	
	public FlowAllocatorInstanceImpl(APService applicationProcess, FlowAllocator flowAllocator, CDAPSessionManager cdapSessionManager){
		this.applicationProcess = applicationProcess;
		this.flowAllocator = flowAllocator;
		this.timer = new Timer();
		this.cdapSessionManager = cdapSessionManager;
		connections = new ArrayList<Connection>();
		//TODO initialize the newFlowRequestPolicy
		newFlowRequestPolicy = new NewFlowRequestPolicyImpl();
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
		createDataTransferAEInstance(flow);
		
		//Check directory to see to what IPC process the CDAP M_CREATE request has to be delivered
		long destinationAddress = flowAllocator.getDirectoryForwardingTable().getAddress(flowService.getDestinationAPNamingInfo());
		flow.setDestinationAddress(destinationAddress);
		if (destinationAddress == 0){
			//error, the table should have at least returned a default IPC process address to continue looking for the application process
			String message = "The directory forwarding table returned no entries when looking up " + flowService.getDestinationAPNamingInfo().toString();
			throw new IPCException(5, message);
		}
		
		//Check if the destination address is this IPC process (then invoke degenerated form of IPC)
		RIBDaemon ribDaemon = (RIBDaemon) flowAllocator.getIPCProcess().getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		long sourceAddress = 0;
		
		try{
			sourceAddress = (Long) ribDaemon.read(null, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT +
					RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.CURRENT_SYNONYM, 0).getObjectValue();
			flow.setSourceAddress(sourceAddress);
			if (destinationAddress == sourceAddress){
				//TODO The destination application is here, special case
				return;
			}
		}catch(RIBDaemonException ex){
			ex.printStackTrace();
			log.error(ex);
			throw new IPCException(5, ex.getMessage());
		}
		
		//Start the flow allocation sequence by opening a socket to the remote flow allocator
		this.socket = connectToRemoteFlowAllocator(destinationAddress);
		this.portId = socket.getLocalPort();
		flow.setSourcePortId(portId);
		flow.setTcpRendezvousId(portId);
		
		//Map the address to the port id through which I can reach the destination application process name
		int rmtPortId = Utils.mapAddressToPortId(destinationAddress, flowAllocator.getIPCProcess());
		
		//Encode the flow object and send it to the destination IPC process
		Encoder encoder = (Encoder) flowAllocator.getIPCProcess().getIPCProcessComponent(BaseEncoder.getComponentName());
		ObjectValue objectValue = null;
		CDAPMessage cdapMessage = null;
		String flowName = ""+sourceAddress+"-"+socket.getLocalPort();
		
		try{
			objectValue = new ObjectValue();
			objectValue.setByteval(encoder.encode(flow));
			cdapMessage = cdapSessionManager.getCreateObjectRequestMessage(rmtPortId, null, null, "flow", 0, RIBObjectNames.SEPARATOR + 
					RIBObjectNames.DIF + RIBObjectNames.SEPARATOR + RIBObjectNames.RESOURCE_ALLOCATION + RIBObjectNames.SEPARATOR +
					RIBObjectNames.FLOW_ALLOCATOR + RIBObjectNames.SEPARATOR + RIBObjectNames.FLOWS + RIBObjectNames.SEPARATOR + flowName, objectValue, 0, true);
			ribDaemon.sendMessage(cdapMessage, rmtPortId, this);
			this.underlyingPortId = rmtPortId;
			this.requestMessage = cdapMessage;
		}catch(Exception ex){
			ex.printStackTrace();
			log.error(ex);
			throw new IPCException(5, ex.getMessage());
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
		
		try{
			ipAddress = Utils.getRemoteIPCProcessIPAddress(address, flowAllocator.getIPCProcess());
			socket = new Socket(ipAddress, TCPServer.DEFAULT_PORT);
			Unsigned unsigned = new Unsigned(2);
			unsigned.setValue(new Integer(socket.getLocalPort()).longValue());
			socket.getOutputStream().write(unsigned.getBytes());
			log.debug("Started a socket to the Flow Allocator at "+ipAddress+":"+TCPServer.DEFAULT_PORT+". The local socket number is "+socket.getLocalPort());
			return socket;
		}catch(Exception ex){
			ex.printStackTrace();
			log.error("Problems connecting to remote Flow Allocator at: "+ipAddress+":"+TCPServer.DEFAULT_PORT);
			throw new IPCException(5, ex.getMessage());
		}
	}
	
	/**
	 * Create the local instances of DTP and optionally DTCP
	 * @param flow
	 */
	private void createDataTransferAEInstance(Flow flow){
		//TODO, when we have DTP
		/*Connection connection = new Connection(flow);
		DataTransferAE dataTransferAE = (DataTransferAE) ipcProcess.getIPCProcessComponent(DataTransferAE.class.getName());
		dataTransferAEInstance = dataTransferAE.createDataTransferAEInstance(connection);
		activeConnection = connection;
		connections.add(connection);*/
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
	public void createFlowRequestMessageReceived(Flow flow, int portId, CDAPMessage requestMessage, int underlyingPortId) {
		log.debug("Create flow request received.\n  "+flow.toString());
		this.flow = flow;
		this.portId = portId;
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
		
		//1 TODO Check if the source application process has access to the destination application process
		//2 TODO If not send negative M_CREATE_R back to the sender IPC process, and housekeeping
		//3 TODO If it has, determine if the proposed policies for the flow are acceptable (invoke NewFlowREquestPolicy)
		//4 TODO If they are acceptable, the FAI will invoke the Allocate_Request.deliver operation of the destination application process. To do so it will find the
		//IPC Manager and pass it the allocation request.
		//TODO:Fix this. Right now this is a hack. I've short-circuited this and called submitAllocateResponse to avoid calling the destination App
		this.submitAllocateResponse(portId, true, null);
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
	 * @param success
	 */
	public void submitAllocateResponse(int portId, boolean success, String reason){
		Encoder encoder = (Encoder) flowAllocator.getIPCProcess().getIPCProcessComponent(BaseEncoder.getComponentName());
		RIBDaemon ribDaemon = (RIBDaemon) flowAllocator.getIPCProcess().getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		CDAPMessage cdapMessage = null;
		
		if (success){
			//1 TODO Create DTP and DTCP instances
			//2 Create CDAP response message
			try{
				ObjectValue objectValue = new ObjectValue();
				objectValue.setByteval(encoder.encode(flow));
				cdapMessage = cdapSessionManager.getCreateObjectResponseMessage(underlyingPortId, null, requestMessage.getObjClass(), 
						0, requestMessage.getObjName(), objectValue, 0, null, requestMessage.getInvokeID());
				ribDaemon.sendMessage(cdapMessage, underlyingPortId, null);
				ribDaemon.create(requestMessage.getObjClass(), requestMessage.getObjName(), 0, this);
				tcpSocketReader = new TCPSocketReader(socket);
				flowAllocator.executeRunnable(tcpSocketReader);
			}catch(Exception ex){
				ex.printStackTrace();
				log.error("Error when allocating flow: "+ex.getMessage());
			}
		}else{
			//Create CDAP response message
			try{
				cdapMessage = cdapSessionManager.getCreateObjectResponseMessage(underlyingPortId, null, requestMessage.getObjClass(), 
						0, requestMessage.getObjName(), null, -1, reason, requestMessage.getInvokeID());
				ribDaemon.sendMessage(cdapMessage, underlyingPortId, null);
			}catch(Exception ex){
				ex.printStackTrace();
				log.error("Error when allocating flow: "+ex.getMessage());
			}
		}
	}

	/**
	 * When a deallocate primitive is invoked, it is passed to the FAI responsible for that port-id.  
	 * The FAI sends an M_DELETE request CDAP PDU on the Flow object referencing the destination port-id, deletes the local 
	 * binding between the Application and the DTP-instance and waits for a response.  (Note that 
	 * the DTP and DTCP if it exists will be deleted automatically after 2MPL)
	 * @param portId
	 * @param applicationProcess
	 */
	public void submitDeallocateRequest(int portId, APService applicationProcess){
		Encoder encoder = (Encoder) flowAllocator.getIPCProcess().getIPCProcessComponent(BaseEncoder.getComponentName());
		RIBDaemon ribDaemon = (RIBDaemon) flowAllocator.getIPCProcess().getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		
		//1 Send the M_DELETE Flow message
		try{
			ObjectValue objectValue = new ObjectValue();
			objectValue.setByteval(encoder.encode(flow));
			requestMessage = cdapSessionManager.getDeleteObjectRequestMessage(underlyingPortId, null, null, "flow", 0, requestMessage.getObjName(), 0, true); 
			ribDaemon.sendMessage(requestMessage, underlyingPortId, this);
			//TODO set timer to wait for M_DELETE_R message. If the message is not reveived before timer expiration, remove
			//the Flow object from the RIB and notify the application process (.deliverDeallocateResponse)
		}catch(Exception ex){
			ex.printStackTrace();
			log.error(ex);
			//TODO reply the source applicationprocess?
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
		RIBDaemon ribDaemon = (RIBDaemon) flowAllocator.getIPCProcess().getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		
		try{
			CDAPMessage responseMessage = cdapSessionManager.getDeleteObjectResponseMessage(underlyingPortId, null, 
					cdapMessage.getObjClass(), cdapMessage.getObjInst(), cdapMessage.getObjName(), 0, null, cdapMessage.getInvokeID());
			ribDaemon.sendMessage(responseMessage, underlyingPortId, null);
			ribDaemon.delete(cdapMessage.getObjClass(), cdapMessage.getObjName(), cdapMessage.getObjInst(), null);
		}catch(Exception ex){
			ex.printStackTrace(); 
		}
		
		destroyFlowAllocatorInstance();
		
		//TODO notify the application process
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
			//TODO reply the Application process with a negative allocate response
			return;
		}
		
		Encoder encoder = (Encoder) flowAllocator.getIPCProcess().getIPCProcessComponent(BaseEncoder.getComponentName());
		RIBDaemon ribDaemon = (RIBDaemon) flowAllocator.getIPCProcess().getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		try{
			this.flow = (Flow) encoder.decode(cdapMessage.getObjValue().getByteval(), Flow.class.toString());
			log.debug("Successfull create flow message response received for flow "+cdapMessage.getObjName()+".\n "+this.flow.toString());
			ribDaemon.create(requestMessage.getObjClass(), requestMessage.getObjName(), 0, this);
			tcpSocketReader = new TCPSocketReader(socket);
			flowAllocator.executeRunnable(tcpSocketReader);
			//TODO reply the application process with a positive allocate response
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
	
	public void destroyFlowAllocatorInstance(){
		//Make the TCP socket reader complete its execution if it is executing
		if (tcpSocketReader != null){
			tcpSocketReader.setEnd();
		}
		
		//Close the socket if it is still open
		if (!socket.isClosed()){
			try{
				socket.close();
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		
		flowAllocator.removeFlowAllocatorInstance(this.portId);
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
			ribDaemon.delete(cdapMessage.getObjClass(), cdapMessage.getObjName(), cdapMessage.getObjInst(), null);
		}catch(RIBDaemonException ex){
			ex.printStackTrace();
		}
		
		//TODO call application process (.deliverDeallocateResponse())
	}
	
	/**
	 * When the TCP Socket Reader detects that the socket is closed, 
	 * it will notify the Flow Allocator instance
	 */
	public void socketClosed(){
		RIBDaemon ribDaemon = (RIBDaemon) flowAllocator.getIPCProcess().getIPCProcessComponent(BaseRIBDaemon.getComponentName());
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
