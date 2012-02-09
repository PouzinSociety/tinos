package rina.applibrary.impl.test;

import java.net.Socket;

import junit.framework.Assert;

import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.delimiting.api.BaseSocketReader;
import rina.delimiting.api.Delimiter;

public class ClientSocketReader extends BaseSocketReader{
	
	private CDAPSessionManager cdapSessionManager = null;
	private String lastSDU = null;
	private int sdusSent = 0;

	public ClientSocketReader(Socket socket, Delimiter delimiter, CDAPSessionManager cdapSessionManager) {
		super(socket, delimiter);
		this.cdapSessionManager = cdapSessionManager;
	}

	@Override
	public void processPDU(byte[] pdu) {
		CDAPMessage cdapMessage = null;

		try{
			cdapMessage = cdapSessionManager.decodeCDAPMessage(pdu);
			System.out.println(cdapMessage.toString());

			switch(cdapMessage.getOpCode()){
			case M_CREATE_R:
				handleMCreateResponseReceived(cdapMessage);
				break;
			case M_WRITE:
				handleMWriteReceived(cdapMessage);
				break;
			case M_DELETE_R:
				handleMDeleteResponseReceived(cdapMessage);
				break;
			default:
				System.out.println("Received invalid message");
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}

	}
	
	/**
	 * The flow has been established, ensure that the response is successful and 
	 * read the socket number. The issue a write command.
	 * @param cdapMessage
	 */
	private void handleMCreateResponseReceived(CDAPMessage cdapMessage){
		Assert.assertEquals(0, cdapMessage.getResult());
		try{
			System.out.println("ClientSocketReader: Flow allocated!");
			
			//Send data through the flow
			ObjectValue objectValue = new ObjectValue();
		    lastSDU = "Switzerland is a good country if you like mountains";
			objectValue.setByteval(lastSDU.getBytes());
			cdapMessage = CDAPMessage.getWriteObjectRequestMessage(null, null, null, 0, objectValue, null, 0);
			getSocket().getOutputStream().write(getDelimiter().getDelimitedSdu(cdapSessionManager.encodeCDAPMessage(cdapMessage)));
			sdusSent = sdusSent + 1;
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/**
	 * The server is just echoing back the requests, therefore the string received should be the same as the 
	 * last SDU. Do it 
	 * @param cdapMessage
	 */
	private void handleMWriteReceived(CDAPMessage cdapMessage){
		try{
			Assert.assertEquals(lastSDU, new String(cdapMessage.getObjValue().getByteval()));
			System.out.println("ClientSocketReader: Received SDU! "+lastSDU);
			
			//If we've just sent an SDU send another one, otherwise request the flow deallocation
			if (sdusSent == 1){
				ObjectValue objectValue = new ObjectValue();
			    lastSDU = "And it can be even a better country if you also like cheese";
				objectValue.setByteval(lastSDU.getBytes());
				cdapMessage = CDAPMessage.getWriteObjectRequestMessage(null, null, null, 0, objectValue, null, 0);
				getSocket().getOutputStream().write(getDelimiter().getDelimitedSdu(cdapSessionManager.encodeCDAPMessage(cdapMessage)));
				sdusSent = sdusSent + 1;
			}else{
				cdapMessage = CDAPMessage.getDeleteObjectRequestMessage(null, null, null, 0, null, 0);
				getSocket().getOutputStream().write(getDelimiter().getDelimitedSdu(cdapSessionManager.encodeCDAPMessage(cdapMessage)));
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/**
	 * Just make sure the response is successful
	 * @param cdapMessage
	 */
	private void handleMDeleteResponseReceived(CDAPMessage cdapMessage){
		try{
			Assert.assertEquals(0, cdapMessage.getResult());
			System.out.println("ClientSocketReader: Flow deallocated!");
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	@Override
	public void socketDisconnected() {
		System.out.println("ClientSocketReader: Socket disconnected!");
	}

}
