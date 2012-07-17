package rina.applibrary.impl;

import java.net.FauxSocketFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applibrary.api.ApplicationRegistrationImpl;
import rina.applibrary.api.FlowAcceptor;
import rina.applibrary.api.FlowImpl;
import rina.applibrary.api.FlowListener;
import rina.applibrary.api.SocketFactory;
import rina.applibrary.api.FlowImpl.State;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.cdap.api.message.CDAPMessage.Opcode;
import rina.delimiting.api.Delimiter;
import rina.encoding.api.Encoder;
import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.ApplicationRegistration;
import rina.ipcservice.api.IPCException;

/**
 * Default implementaiton of the applicationRegistrationImpl interface, used unless the user of 
 * the ApplicationRegistration class provides an applicationRegistrationImplFactory.
 * @author eduardgrasa
 *
 */
public class DefaultApplicationRegistrationImpl implements ApplicationRegistrationImpl{
	private static final Log log = LogFactory.getLog(DefaultApplicationRegistrationImpl.class);

	private static final int MAX_WAITTIME_IN_SECONDS = 3;
	
	private FlowListener flowListener = null;
	
	private Delimiter delimiter = null;
	
	private CDAPSessionManager cdapSessionManager = null;
	
	private Encoder encoder = null;
	
	private Socket socket = null;
	
	/**
	 * This queue will be used for communication between 
	 * the ApplicationRegistrationSocketReader and this class
	 */
	private BlockingQueue<byte[]> registrationQueue = null;
	
	/**
	 * The socket reader that will read the socket used to 
	 * communicate application registration information to the 
	 * local RINA software
	 */
	private ApplicationRegistrationSocketReader arSocketReader = null;
	
	/**
	 * The socket that will be listening for incoming flow requests
	 */
	private ServerSocket serverSocket = null;
	
	/**
	 * This queue will be used for communication between 
	 * the ApplicationRegistrationSocketReader and this class
	 */
	private BlockingQueue<FlowImpl> acceptedFlowsQueue = null;
	
	/**
	 * The TCP server that will process incoming flow 
	 * requests
	 */
	private FlowRequestsServer flowRequestsServer = null;
	
	/**
	 * The naming information of the registered application
	 */
	private ApplicationProcessNamingInfo registeredApp = null;
	
	/**
	 * The registration state
	 */
	private State state = State.UNREGISTERED;
	
	/**
	 * Controls if this object was created by the fauxSockets implementation and 
	 * therefore needs to use the faux Sockets constructors
	 */
	private boolean fauxSockets = false;
	
	/**
	 * The class that creates the socket instances
	 */
	private SocketFactory socketFactory = null;
	
	public DefaultApplicationRegistrationImpl(){
		this.cdapSessionManager = RINAFactory.getCDAPSessionManagerInstance();
		this.delimiter = RINAFactory.getDelimiterInstance();
		this.encoder = RINAFactory.getEncoderInstance();
		this.registrationQueue = new LinkedBlockingQueue<byte[]>();
		this.socketFactory = new StandardSocketFactory();
	}
	
	public DefaultApplicationRegistrationImpl(boolean fauxSockets){
		this();
		this.fauxSockets = fauxSockets;
		if (fauxSockets){
			this.socketFactory = new FauxSocketFactory();
		}else{
			this.socketFactory = new StandardSocketFactory();
		}
	}
	
	/**
	 * Registers the application to the specified list of DIFs. If the list is null, the application is 
	 * registered in all the DIFs that currently exist in this system.
	 * @param applicationProcess The naming information of the application process that is registering
	 * @param difNames The list of difNames to which the application process is registering. If the list is null it will 
	 * register to all the DIFs available in the system
	 * @param flowAcceptor Decides what flows will be accepted and what flows will be rejected. If it is null, all the 
	 * incoming flows are accepted
	 * @param flowListener If provided, every time a new flow is accepted the flowListener will be called (non-blocking
	 * mode). In non-blocking mode calls to "accept" will throw an Exception. If it is null, users of this class will have 
	 * to call the "accept" blocking operation in order to get the accepted flows.
	 * @throws IPCException
	 */
	public void register(ApplicationProcessNamingInfo applicationProcess, List<String> difNames, 
			FlowAcceptor flowAcceptor, FlowListener flowListener) throws IPCException{
		if (state != State.UNREGISTERED){
			IPCException ipcException = new IPCException(IPCException.APPLICATION_ALREADY_REGISTERED);
			ipcException.setErrorCode(IPCException.APPLICATION_ALREADY_REGISTERED_CODE);
			throw ipcException;
		}
		
		this.flowListener = flowListener;
		if (this.flowListener == null){
			this.acceptedFlowsQueue = new LinkedBlockingQueue<FlowImpl>();
		}
		CDAPMessage cdapMessage = null;
		byte[] message = null;
		
		try{
			//1 Connect to the local RINA software, using the standard Sockets implementation
			socket = socketFactory.createSocket(fauxSockets, "localhost", RINAFactory.DEFAULT_PORT);
			
			//2 Start a server socket to listen for incoming flow establishment attempts, using the 
			//standard Sockets implementation
			serverSocket = socketFactory.createServerSocket(fauxSockets, 0);
			
			//3 Create and start the registration socket reader
			this.arSocketReader = new ApplicationRegistrationSocketReader(socket, delimiter, registrationQueue, this);
			RINAFactory.execute(arSocketReader);
			
			//4 Populate the application registration object
			ApplicationRegistration applicationRegistration = new ApplicationRegistration();
			applicationRegistration.setApNamingInfo(applicationProcess);
			applicationRegistration.setDifNames(difNames);
			applicationRegistration.setSocketNumber(serverSocket.getLocalPort());
			
			//5 Construct the CDAP Message, encode it, delimit it and send it through the socket
			ObjectValue objectValue = new ObjectValue();
			objectValue.setByteval(encoder.encode(applicationRegistration));
			cdapMessage = CDAPMessage.getStartObjectRequestMessage(null, null,
					ApplicationRegistration.OBJECT_CLASS, objectValue, 0, ApplicationRegistration.OBJECT_NAME, 0);
			message = cdapSessionManager.encodeCDAPMessage(cdapMessage);
			socket.getOutputStream().write(delimiter.getDelimitedSdu(message));
			
			//5 Block and wait (maximum deadline) for the answer
			message = registrationQueue.poll(MAX_WAITTIME_IN_SECONDS, TimeUnit.SECONDS);
			if (message == null){
				throw new IPCException("Didn't receive the reply from the local RINA software before "+MAX_WAITTIME_IN_SECONDS+" seconds.");
			}
			
			//6 Decode the CDAP Message and see if registration was successful
			cdapMessage = cdapSessionManager.decodeCDAPMessage(message);
			if (cdapMessage.getOpCode() != Opcode.M_START_R){
				throw new IPCException("Got a CDAP message with the wrong opcode: "+cdapMessage.getOpCode());
			}
			if (cdapMessage.getResult() != 0){
				throw new IPCException("Got a negative response from the local RINA software: "+cdapMessage.getResultReason());
			}
			
			//7 If the registration was successful, start the server thread that will wait for incoming Flow establishment attempts
			if (this.flowListener == null){
				this.flowRequestsServer = new FlowRequestsServer(serverSocket, flowAcceptor, acceptedFlowsQueue);
			}else{
				this.flowRequestsServer = new FlowRequestsServer(serverSocket, flowAcceptor, flowListener);
			}
			this.flowRequestsServer.setCDAPSessionManager(cdapSessionManager);
			this.flowRequestsServer.setDelimiter(delimiter);
			this.flowRequestsServer.setEncoder(encoder);
			this.flowRequestsServer.setFauxSockets(fauxSockets);
			RINAFactory.execute(this.flowRequestsServer);
			this.registeredApp = applicationProcess;
			this.state = State.REGISTERED;
		}catch(Exception ex){
			if (socket != null && !socket.isClosed()){
				try{
					socket.close();
				}catch(Exception e){
				}
			}
			
			if (serverSocket != null && !serverSocket.isClosed()){
				try{
					serverSocket.close();
				}catch(Exception e){
				}
			}
			
			IPCException ipcException = new IPCException(IPCException.PROBLEMS_REGISTERING_APPLICATION + " " + ex.getMessage());
			ipcException.setErrorCode(IPCException.PROBLEMS_REGISTERING_APPLICATION_CODE);
			throw ipcException;
		}
	}
	
	/**
	 * This operation will block until a new incoming flow is accepted.
	 * @param SDUListener the SDUs received by this flow will be sent to the SDUListener
	 * @return the accepted Flow
	 */
	public FlowImpl accept() throws IPCException{
		try{
			FlowImpl flowImpl =  acceptedFlowsQueue.take();
			if (flowImpl.getState() == FlowImpl.State.ALLOCATED){
				return flowImpl;
			}
			
			try{
				if (!this.socket.isClosed()){
					this.socket.close();
				}
				if (this.state == State.REGISTERED){
					flowRequestsServer.setEnd(true);
					this.state = State.UNREGISTERED;
				}
				this.registeredApp = null;
			}catch(Exception e){
				e.printStackTrace();
			}
			
			throw new IPCException("Registration cancelled");
		}catch(InterruptedException ex){
			IPCException ipcException = new IPCException(IPCException.PROBLEMS_ACCEPTING_FLOW + " " + ex.getMessage());
			ipcException.setErrorCode(IPCException.PROBLEMS_ACCEPTING_FLOW_CODE);
			throw ipcException;
		}
	}
	
	/**
	 * Cancel the registration
	 * @throws IPCException
	 */
	public void unregister() throws IPCException{
		if (state != State.REGISTERED){
			IPCException ipcException = new IPCException(IPCException.APPLICATION_ALREADY_UNREGISTERED);
			ipcException.setErrorCode(IPCException.APPLICATION_ALREADY_UNREGISTERED_CODE);
			throw ipcException;
		}
		
		//Stop serverSocket, socket and execution service
		try{
			socket.close();
		}catch(Exception ex){
		}
		flowRequestsServer.setEnd(true);
		this.state = State.UNREGISTERED;
	}
	
	public void registrationSocketClosed(){
		log.info("Registration socket closed, application "+this.registeredApp.getEncodedString()+" is no longer registered");
		if (this.state == State.REGISTERED){
			try{
				socket.close();
			}catch(Exception ex){
			}
			flowRequestsServer.setEnd(true);
			this.state = State.UNREGISTERED;
		}
		this.registeredApp = null;
	}
	
	/**
	 * Returns the registration state
	 * @return
	 */
	public State getState(){
		return this.state;
	}
	
	/**
	 * Sets the registration state
	 * @param current state
	 * @param state
	 */
	public void setState(State state){
		this.state = state;
	}
}