package rina.applibrary.impl.test;

import java.net.Socket;

import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.delimiting.api.BaseSocketReader;
import rina.delimiting.api.Delimiter;
import rina.encoding.api.Encoder;
import rina.ipcservice.api.FlowService;

public class RINASoftwareSocketReader extends BaseSocketReader{
	
	private CDAPSessionManager cdapSessionManager = null;
	private Encoder encoder = null;

	public RINASoftwareSocketReader(Socket socket, Delimiter delimiter, CDAPSessionManager cdapSessionManager, Encoder encoder) {
		super(socket, delimiter);
		this.cdapSessionManager = cdapSessionManager;
		this.encoder = encoder;
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
			System.out.println("Source application: "+flowService.getSourceAPNamingInfo().toString());
			System.out.println("Destination application: "+flowService.getDestinationAPNamingInfo().toString());
			
			int portId = new Double(65.000*Math.random()).intValue();
			System.out.println("Flow request accepted, assigning portId " + portId);
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
			System.out.println("Deallocating flow");
			CDAPMessage response = CDAPMessage.getDeleteObjectResponseMessage(null, cdapMessage.getObjClass(), 0,
					cdapMessage.getObjName(), 0, null, 1);
			getSocket().getOutputStream().write(getDelimiter().getDelimitedSdu(cdapSessionManager.encodeCDAPMessage(response)));
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	@Override
	public void socketDisconnected() {
		System.out.println("Socket disconnected");
	}

}
