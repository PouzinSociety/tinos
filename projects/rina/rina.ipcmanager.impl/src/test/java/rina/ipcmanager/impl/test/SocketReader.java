package rina.ipcmanager.impl.test;

import java.net.Socket;

import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.delimiting.api.BaseSocketReader;
import rina.delimiting.api.Delimiter;

public class SocketReader extends BaseSocketReader{
	
	private CDAPSessionManager cdapSessionManager = null;
	
	private IPCManagerAppInteractionTest test = null;
	
	public SocketReader(Socket socket, Delimiter delimiter, CDAPSessionManager cdapSessionManager, IPCManagerAppInteractionTest test){
		super(socket, delimiter);
		this.cdapSessionManager = cdapSessionManager;
		this.test = test;
	}
	
	/**
	 * process the pdu that has been found
	 * @param pdu
	 */
	public void processPDU(byte[] pdu){
		try{
			CDAPMessage cdapMessage = cdapSessionManager.decodeCDAPMessage(pdu);
			switch(cdapMessage.getOpCode()){
			case M_CREATE_R:
				break;
			default:
				//TODO
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
	
	/**
	 * Invoked when the socket is disconnected
	 */
	public void socketDisconnected(){
		System.out.println("Socket disconnected!!");
	}

}
