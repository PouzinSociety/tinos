package rina.ipcmanager.impl.apservice;

import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPSessionManager;
import rina.delimiting.api.DelimiterFactory;
import rina.encoding.api.Encoder;
import rina.ipcmanager.api.InterDIFDirectory;
import rina.ipcmanager.impl.IPCManagerImpl;
import rina.ipcmanager.impl.apservice.FlowServiceState.Status;
import rina.ipcprocess.api.IPCProcessFactory;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.IPCService;

/**
 * Implements the part of the IPCManager that deals with applications
 * @author eduardgrasa
 *
 */
public class APServiceImpl implements APService{
	private static final Log log = LogFactory.getLog(APServiceImpl.class);
	private static final int MAXWORKERTHREADS = 10;
	
	/**
	 * The thread pool implementation
	 */
	private ExecutorService executorService = null;
	
	/**
	 * The IPC Process factory
	 */
	private IPCProcessFactory ipcProcessFactory = null;

	private InterDIFDirectory interDIFDirectory = null;
	
	private CDAPSessionManager cdapSessionManager = null;
	
	private Encoder encoder = null;
	
	/**
	 * Server listening for incoming connections from applications
	 */
	private APServiceTCPServer tcpServer = null;
	
	/**
	 * The state of all the requested flow services, hashed by portId
	 */
	private Map<Integer, FlowServiceState> flowServices = null;
	
	public APServiceImpl(){
		tcpServer = new APServiceTCPServer(this);
		executorService = Executors.newFixedThreadPool(MAXWORKERTHREADS);
		executorService.execute(tcpServer);
		flowServices = new Hashtable<Integer, FlowServiceState>();
	}
	
	public void setInterDIFDirectory(InterDIFDirectory interDIFDirectory){
		this.interDIFDirectory = interDIFDirectory;
	}
	
	public void setIPCProcessFactory(IPCProcessFactory ipcProcessFactory){
		this.ipcProcessFactory = ipcProcessFactory;
		cdapSessionManager = ipcProcessFactory.getCDAPSessionManagerFactory().createCDAPSessionManager();
		encoder = ipcProcessFactory.getEncoderFactory().createEncoderInstance();
	}
	
	
	
	/**
	 * Start a new thread to read from the socket
	 * @param socket
	 */
	public synchronized void newConnectionAccepted(Socket socket){
		TCPSocketReader socketReader = new TCPSocketReader(socket, ipcProcessFactory.getDelimiterFactory().createDelimiter(DelimiterFactory.DIF),
				ipcProcessFactory.getEncoderFactory().createEncoderInstance(), ipcProcessFactory.getCDAPSessionManagerFactory().createCDAPSessionManager(), 
				this);
		executorService.execute(socketReader);
		
		//TODO, keep a list of active socketReaders in this class? Will this ever be needed?
	}
	
	public IPCService processAllocateRequest(FlowService flowService, Socket socket){
		String difName = interDIFDirectory.mapApplicationProcessNamingInfoToDIFName(flowService.getDestinationAPNamingInfo());
		
		//Look for the local IPC Process that is a member of difName
		IPCService ipcService = (IPCService) ipcProcessFactory.getIPCProcessBelongingToDIF(difName);
		if (ipcService == null){
			discardRequest("Could not find an IPC Process belonging to DIF " + difName + " in this system", socket);
			return null;
		}
		
		//Once we have the IPCService, invoke allocate request
		try{
			ipcService.submitAllocateRequest(flowService, this);
		}catch(IPCException ex){
			ex.printStackTrace();
			discardRequest(ex.getMessage(), socket);
		}
		
		//Store the state of the flow service
		FlowServiceState flowServiceState = new FlowServiceState();
		flowServiceState.setFlowService(flowService);
		flowServiceState.setSocket(socket);
		flowServiceState.setStatus(Status.ALLOCATION_REQUESTED);
		flowServices.put(new Integer(socket.getPort()), flowServiceState);
		
		return ipcService;
	}
	
	/**
	 * An error has occurred, treat it
	 * @param message
	 * @param socket
	 */
	private void discardRequest(String message, Socket socket){
		log.error(message);
		try{
			//TODO send reply message to the sender, right now closing the socket, maybe it is too much?
			socket.close();
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}
	

	public synchronized void deliverAllocateRequest(FlowService flowService) {
		// TODO Auto-generated method stub
		
	}

	public synchronized void deliverAllocateResponse(ApplicationProcessNamingInfo arg0,
			int arg1, int arg2, String arg3) {
		// TODO Auto-generated method stub
		
	}

	public synchronized void deliverDeallocateResponse(int arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	public synchronized void deliverStatus(int arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}

	public synchronized void deliverTransfer(int arg0, byte[] arg1, boolean arg2) {
		// TODO Auto-generated method stub
		
	}

}
