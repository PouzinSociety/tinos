package rina.apps.proxy.impl;

import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applibrary.api.Flow;
import rina.applibrary.api.SDUListener;
import rina.applicationprocess.api.ApplicationProcessNamingInfo;

/**
 * Every time a new SDU is delivered, it logs it and echoes it back
 * @author eduardgrasa
 */
public class ProxyWorker implements Runnable, SDUListener{

	public static final int BUFFER_LENGTH = 1024;
	public static final String HTTP = "HTTP";
	
	private static final Log log = LogFactory.getLog(ProxyWorker.class);
	
	private Socket socket = null;
	private String encodedDestAPNamingInfo = null;
	private Flow flow = null;
	
	public ProxyWorker(Socket socket){
		this.socket = socket;
	}

	public void run() {
		byte[] buffer = new byte[BUFFER_LENGTH];
		int numberOfBytesRead = 0;
		String payload = null;

		try{
			//1 Read the first bytes and detect the application protocol
			numberOfBytesRead = socket.getInputStream().read(buffer);
			payload = new String(buffer);
			log.info("Read " + numberOfBytesRead+ " bytes. \n" + payload);
			if (payload.indexOf(HTTP) != -1){
				handleHTTP(payload);
			}else{
				log.error("Unrecognized application protocol, exiting");
				return;
			}
			
			//2 Allocate a flow to the destination application process, 
			//and write the first bytes read
			log.info("The destination application process name is " 
					+ encodedDestAPNamingInfo);
			ApplicationProcessNamingInfo sourceAPNamingInfo = new ApplicationProcessNamingInfo(
					socket.getInetAddress().getHostAddress(), ""+socket.getPort());
			ApplicationProcessNamingInfo destAPNamingInfo = new ApplicationProcessNamingInfo(encodedDestAPNamingInfo);
			flow = new Flow(sourceAPNamingInfo, destAPNamingInfo, null, this);
			flow.write(buffer, numberOfBytesRead);
			
			//While the socket doesn't end, deallocate the flow
			numberOfBytesRead = socket.getInputStream().read(buffer);
			while(numberOfBytesRead != -1){
				flow.write(buffer, numberOfBytesRead);
				numberOfBytesRead = socket.getInputStream().read(buffer);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			log.error(ex);
		}finally{
			//Close the socket
			if (!socket.isClosed()){
				try{
					socket.close();
				}catch(Exception ex){
					ex.printStackTrace();
					log.error(ex);
				}
			}
			
			//Close the flow
			if (flow != null && flow.isAllocated()){
				try{
					flow.deallocate();
				}catch(Exception ex){
					ex.printStackTrace();
					log.error(ex);
				}
			}
		}
	}
	
	/**
	 * Extract application process information from 
	 * the HTTP request (it is the resource name except for the 
	 * '/' character)
	 * @param payload
	 */
	private void handleHTTP(String payload){
		String[] aux = null;
		String[] aux2 = null;
		
		aux = payload.split("\n");
		aux2 = aux[0].split(" ")[1].split("/");
		if (aux2.length < 2){
			encodedDestAPNamingInfo = "";
		}else{
			encodedDestAPNamingInfo = aux2[1];
		}
	}

	/**
	 * A PDU has been delivered through the flow
	 */
	public void sduDelivered(byte[] pdu) {
		try{
			socket.getOutputStream().write(pdu);
		}catch(Exception ex){
			ex.printStackTrace();
			log.error(ex);
		}
	}
}
