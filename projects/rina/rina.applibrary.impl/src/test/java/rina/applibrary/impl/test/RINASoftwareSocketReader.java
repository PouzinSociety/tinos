package rina.applibrary.impl.test;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.delimiting.api.BaseSocketReader;
import rina.delimiting.api.Delimiter;
import rina.encoding.api.Encoder;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.ApplicationRegistration;
import rina.ipcservice.api.FlowService;

public class RINASoftwareSocketReader extends BaseSocketReader{
	
	private CDAPSessionManager cdapSessionManager = null;
	private Encoder encoder = null;
	private ExecutorService executorService = null;

	public RINASoftwareSocketReader(Socket socket, Delimiter delimiter, CDAPSessionManager cdapSessionManager, Encoder encoder) {
		super(socket, delimiter);
		this.cdapSessionManager = cdapSessionManager;
		this.encoder = encoder;
		executorService = Executors.newFixedThreadPool(2);
	}

	@Override
	public void processPDU(byte[] pdu) {
		CDAPMessage cdapMessage = null;

		try{
			cdapMessage = cdapSessionManager.decodeCDAPMessage(pdu);
			System.out.println(cdapMessage.toString());
			
			switch(cdapMessage.getOpCode()){
			case M_CREATE:
				handleMCreateReceived(cdapMessage);
				break;
			case M_WRITE:
				handleMWriteReceived(cdapMessage);
				break;
			case M_DELETE:
				handleMDeleteReceived(cdapMessage);
				break;
			case M_START:
				handleMStartReceived(cdapMessage);
				break;
			case M_STOP:
				handleMStopReceived(cdapMessage);
				break;
			default:
				System.out.println("Received invalid message");
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
	
	/**
	 * Simulate accepting the request = assign a port id and return successfull M_CREATE_R message.
	 * @param cdapMessage
	 */
	private void handleMCreateReceived(CDAPMessage cdapMessage){
		try{
			FlowService flowService = (FlowService) encoder.decode(cdapMessage.getObjValue().getByteval(), FlowService.class.toString());
			System.out.println("RINASoftware: Flow allocation request received.");
			System.out.println("RINASoftware: Source application: "+flowService.getSourceAPNamingInfo().toString());
			System.out.println("RINASoftware: Destination application: "+flowService.getDestinationAPNamingInfo().toString());
			
			int portId = new Double(65.000*Math.random()).intValue();
			System.out.println("RINASoftware: Flow request accepted, assigning portId " + portId);
			flowService.setPortId(portId);
			ObjectValue objectValue = new ObjectValue();
			objectValue.setByteval(encoder.encode(flowService));
			CDAPMessage response = CDAPMessage.getCreateObjectResponseMessage(null, cdapMessage.getObjClass(), 0, 
					cdapMessage.getObjName(), objectValue, 0, null, 1);
			getSocket().getOutputStream().write(getDelimiter().getDelimitedSdu(cdapSessionManager.encodeCDAPMessage(response)));
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/**
	 * Print the received String and send it back (simulates a remote echo server application)
	 * @param cdapMessage
	 */
	private void handleMWriteReceived(CDAPMessage cdapMessage){
		try{
			String received = new String(cdapMessage.getObjValue().getByteval());
			System.out.println("Echo Server: Received the following SDU: " + received + ". Sending it back!");
			getSocket().getOutputStream().write(getDelimiter().getDelimitedSdu(cdapSessionManager.encodeCDAPMessage(cdapMessage)));
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/**
	 * Accept flow deletion, return successful M_DELETE_R
	 * @param cdapMessage
	 */
	private void handleMDeleteReceived(CDAPMessage cdapMessage){
		try{
			System.out.println("RINASoftware: Deallocating flow");
			CDAPMessage response = CDAPMessage.getDeleteObjectResponseMessage(null, cdapMessage.getObjClass(), 0,
					cdapMessage.getObjName(), 0, null, 1);
			getSocket().getOutputStream().write(getDelimiter().getDelimitedSdu(cdapSessionManager.encodeCDAPMessage(response)));
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/**
	 * Accept application registration, return successful M_START_R. After that wait 1 second, and then start a Thread that will request
	 * the allocation of a flow.
	 * @param cdapMessage
	 */
	private void handleMStartReceived(CDAPMessage cdapMessage){
		try{
			ApplicationRegistration apReg = (ApplicationRegistration) encoder.decode(cdapMessage.getObjValue().getByteval(), ApplicationRegistration.class.toString());
			System.out.println("RINASoftware: Registered application.");
			System.out.println("RINASoftware: Application: "+apReg.getApNamingInfo().toString());
			System.out.println("RINASoftware: Contact socket number: "+apReg.getSocketNumber());
			
			cdapMessage = CDAPMessage.getStartObjectResponseMessage(null, 0, null, 1);
			getSocket().getOutputStream().write(getDelimiter().getDelimitedSdu(cdapSessionManager.encodeCDAPMessage(cdapMessage)));
			System.out.println("RINASoftware: Sent registration reply!");
			
			//Sleep 1 second
			try{
				Thread.sleep(1000);
			}catch(Exception ex){
				ex.printStackTrace();
			}
			
			//Create a CDAPMessage to request the allocation of a new flow
			FlowService flowService = new FlowService();
			ApplicationProcessNamingInfo sourceAPNamingInfo = new ApplicationProcessNamingInfo("junit-test", "1");
			ApplicationProcessNamingInfo destinationAPNamingInfo = 
				new ApplicationProcessNamingInfo(apReg.getApNamingInfo().getApplicationProcessName(), apReg.getApNamingInfo().getApplicationProcessInstance());
			flowService.setSourceAPNamingInfo(sourceAPNamingInfo);
			flowService.setDestinationAPNamingInfo(destinationAPNamingInfo);
			flowService.setPortId(new Double(65.000*Math.random()).intValue());
			ObjectValue objectValue = new ObjectValue();
			objectValue.setByteval(encoder.encode(flowService));
			cdapMessage = CDAPMessage.getCreateObjectRequestMessage(null, null, 
					FlowService.OBJECT_CLASS, 0, FlowService.OBJECT_NAME, objectValue, 0);
			
			//2 Contact the server and send the message
			Socket socket = new Socket("localhost", apReg.getSocketNumber());
			socket.getOutputStream().write(getDelimiter().getDelimitedSdu(cdapSessionManager.encodeCDAPMessage(cdapMessage)));
			
			//3 Start a socketReader thread that continues the interaction
			ClientSocketReader clientSocketReader = new ClientSocketReader(socket, getDelimiter(), cdapSessionManager);
			executorService.execute(clientSocketReader);			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/**
	 * Accept flow deletion, return successful M_DELETE_R
	 * @param cdapMessage
	 */
	private void handleMStopReceived(CDAPMessage cdapMessage){
		try{
			System.out.println("RINASoftware: Unregistered application.");
			cdapMessage = CDAPMessage.getStopObjectResponseMessage(null, 0, null, 1);
			getSocket().getOutputStream().write(getDelimiter().getDelimitedSdu(cdapSessionManager.encodeCDAPMessage(cdapMessage)));
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	@Override
	public void socketDisconnected() {
		System.out.println("Socket disconnected");
	}

}
