package java.net;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applibrary.api.ApplicationRegistrationImpl;
import rina.applibrary.api.Flow;
import rina.applibrary.api.FlowImpl;
import rina.applibrary.api.FlowImpl.State;
import rina.applibrary.api.SDUListener;
import rina.applibrary.impl.DefaultApplicationRegistrationImpl;
import rina.applibrary.impl.DefaultFlowAcceptor;
import rina.applibrary.impl.DefaultFlowImpl;
import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.IPCException;

public class RINASocketImpl extends SocketImpl implements SDUListener{
	
	private static final Log log = LogFactory.getLog(RINASocketImpl.class);
	
	private FlowInputStream inputStream = null;
	private OutputStream outputStream = null;
	private FlowImpl flowImpl = null;
	private ApplicationRegistrationImpl applicationRegistration = null;
	private ApplicationProcessNamingInfo sourceApplicationProcessNamingInfo = null;
	
	private Map<Integer, Object> options = null;
	
	public RINASocketImpl(){
		super();
		options = new HashMap<Integer, Object>();
		log.debug("Implemenation created");
	}
	
	public RINASocketImpl(FlowImpl flowImpl){
		this();
		this.flowImpl = flowImpl;
		this.flowImpl.setSduListener(this);
		this.inputStream = new FlowInputStream();
		this.outputStream = new FlowOutputStream(flowImpl);
	}
	

	public Object getOption(int optID) throws SocketException {
		log.info("Trying to get option : "+optID);
		Object result = options.get(new Integer(optID));
		if (result == null){
			throw new SocketException("invalid option: " + optID);
		}
		
		return result;
	}

	public void setOption(int optID, Object optionValue) throws SocketException {
		log.info("Truing to set option "+optID+" with value "+optionValue.toString());
		options.put(optID, optionValue);
	}

	@Override
	protected void accept(SocketImpl socketImpl) throws IOException {
		throw new IOException("Wrong accept method called, should never be called by the Faux Sockets API");
		
	}
	
	protected SocketImpl accept() throws IOException{
		log.info("Accept called");
		if (isServerSocket()){
			try{
				FlowImpl flowImpl = applicationRegistration.accept();
				RINASocketImpl rinaSocketImpl = new RINASocketImpl(flowImpl);
				return rinaSocketImpl;
			}catch(IPCException ex){
				throw new IOException(ex);
			}
		}else{
			throw new IOException("Accept cannot be called by a socket that is not a server socket");
		}
	}

	@Override
	protected int available() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void bind(InetAddress address, int port) throws IOException {
		log.info("Bind called with parameters. Address: "+address.toString()+". Port: "+port);
		String apName = address.getHostName();
		
		if(apName.equals("0.0.0.0")){
			apName = "localhost";
		}
		sourceApplicationProcessNamingInfo = new ApplicationProcessNamingInfo(apName, ""+port);
		
		if (isServerSocket()){
			try{
				applicationRegistration.register(sourceApplicationProcessNamingInfo, null, 
						new DefaultFlowAcceptor(sourceApplicationProcessNamingInfo), null);
			}catch(IPCException ex){
				throw new IOException(ex);
			}
		}
	}

	@Override
	protected void close() throws IOException {
		log.info("Close called");

		if (isServerSocket()){
			if (applicationRegistration == null || 
					applicationRegistration.getState() == ApplicationRegistrationImpl.State.UNREGISTERED){
				throw new IOException("Socket closed");
			}
			
			try{
				applicationRegistration.unregister();
			}catch(IPCException ex){
				throw new IOException(ex);
			}
		}else{
			if (flowImpl == null || flowImpl.getState() == State.DEALLOCATED){
				throw new IOException("Socket closed");
			}

			try{
				flowImpl.deallocate();
			}catch(IPCException ex){
				throw new IOException(ex);
			}
		}
	}


	@Override
	protected void connect(InetAddress address, int port) throws IOException {
		log.info("Connect called with the following parameters. Address: "+address.toString()+". Port: "+port);
		this.connect(address.getHostName(), port);
		
	}

	@Override
	protected void connect(SocketAddress address, int timeout) throws IOException {
		log.info("Connect called with the following parameters. Address: "+address.toString()+". Timeout: "+timeout);
		InetSocketAddress socketAddress = (InetSocketAddress) address;
		this.connect(socketAddress.getHostName(), socketAddress.getPort());
	}
	
	@Override
	/**
	 * Hostname = application process name
	 * Port = application process instance
	 */
	protected void connect(String hostname, int port) throws IOException {
		log.info("Connect called with the following parameters. Hostname: "+hostname+". Port: "+port);
		if (isServerSocket()){
			throw new IOException("This is a server socket, I cannot connect");
		}
		
		if (sourceApplicationProcessNamingInfo != null){
			flowImpl.setSourceApplication(sourceApplicationProcessNamingInfo);
		}else{
			flowImpl.setSourceApplication(new ApplicationProcessNamingInfo("Faux Sockets API", null));
		}
		flowImpl.setDestinationApplication(new ApplicationProcessNamingInfo(hostname, ""+port));
		
		try{
			flowImpl.allocate();
			flowImpl.setSduListener(this);
			inputStream = new FlowInputStream();
			outputStream = new FlowOutputStream(flowImpl);
		}catch(IPCException ex){
			throw new IOException(ex);
		}
	}

	@Override
	/**
	 * Creites either a stream or a datagram socket
	 * @param stream - if true, create a stream socket; otherwise, create a datagram socket.
	 * @throws 
	 */
	protected void create(boolean stream) throws IOException {
		log.info("Create called. Is stream? "+stream);
		fd = new FileDescriptor();
		if (isServerSocket()){
			applicationRegistration = new DefaultApplicationRegistrationImpl(true);
		}else{
			flowImpl = new DefaultFlowImpl(true);
		}
	}
	
	private boolean isServerSocket() throws IOException{
		if (this.socket != null && this.serverSocket != null){
			throw new IOException("Cannot be a socket and a serverSocket at the same time");
		}
		
		if (this.socket == null && this.serverSocket == null){
			throw new IOException("Both socket and serverSocket are null");
		}
		
		if (this.serverSocket != null){
			return true;
		}else{
			return false;
		}
	}

	@Override
	protected InputStream getInputStream() throws IOException {
		return inputStream;
	}

	@Override
	protected OutputStream getOutputStream() throws IOException {
		return outputStream;
	}

	@Override
	/**
	 * Sets the maximum queue length for incoming connection indications (a request to connect) 
	 * to the count argument. If a connection indication arrives when the queue is full, the 
	 * connection is refused.
	 * @param backlog the maximum length of the queue
	 * @throws IOException - if an I/O error occurs when creating the queue.
	 */
	protected void listen(int backlog) throws IOException {
		log.info("Listen called with arguments: "+backlog);
		log.info("Doing nothing");
	}

	@Override
	protected void sendUrgentData(int arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}

	
	/* SDU Listener API */
	public void sduDelivered(byte[] sdu) {
		inputStream.addSDU(sdu);
	}

}
