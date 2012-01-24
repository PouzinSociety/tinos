package rina.ipcmanager.impl.test;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.delimiting.api.Delimiter;
import rina.delimiting.api.DelimiterFactory;
import rina.encoding.api.Encoder;
import rina.ipcmanager.api.InterDIFDirectory;
import rina.ipcmanager.impl.IPCManagerImpl;
import rina.ipcmanager.impl.apservice.APServiceTCPServer;
import rina.ipcprocess.api.IPCProcessFactory;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.FlowService;

public class IPCManagerAppInteractionTest {
	
	private IPCManagerImpl ipcManager = null;
	private IPCProcessFactory mockIPCProcessFactory = null;
	private CDAPSessionManager cdapSessionManager = null;
	private Delimiter delimiter = null;
	private Encoder encoder = null;
	private InterDIFDirectory idd = null;
	private ExecutorService executorService = null;
	
	@Before
	public void setup(){
		ipcManager = new IPCManagerImpl();
		mockIPCProcessFactory = new MockIPCProcessFactory();
		ipcManager.setIPCProcessFactory(mockIPCProcessFactory);
		idd = new MockInterDIFDirectory();
		ipcManager.setInterDIFDirectory(idd);
		cdapSessionManager = mockIPCProcessFactory.getCDAPSessionManagerFactory().createCDAPSessionManager();
		delimiter = mockIPCProcessFactory.getDelimiterFactory().createDelimiter(DelimiterFactory.DIF);
		encoder = mockIPCProcessFactory.getEncoderFactory().createEncoderInstance();
		executorService = Executors.newFixedThreadPool(10);
	}
	
	@Test
	public void testFlowAllocation(){
		try{
			//1 Connect to the IPC Manager
			Socket rinaLibrarySocket = new Socket("localhost", APServiceTCPServer.DEFAULT_PORT);
			Assert.assertTrue(rinaLibrarySocket.isConnected());
			
			//2 Start a thread that will continuously listen to the socket waiting for IPC Manager responses
			SocketReader socketReader = new SocketReader(rinaLibrarySocket, delimiter, cdapSessionManager, this);
			executorService.execute(socketReader);
			
			//3 Get an allocate object and send the request to the IPC Manager
			CDAPMessage allocateRequest = getAllocateRequest();
			sendCDAPMessage(rinaLibrarySocket, allocateRequest);
			wait2Seconds();
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	private void wait2Seconds() throws Exception{
			Thread.sleep(2000);
	}
	
	private CDAPMessage getAllocateRequest() throws Exception{
		FlowService flowService = new FlowService();
		ApplicationProcessNamingInfo apNamingInfo = new ApplicationProcessNamingInfo("A", "1");
		flowService.setSourceAPNamingInfo(apNamingInfo);
		apNamingInfo = new ApplicationProcessNamingInfo("B", "1");
		flowService.setDestinationAPNamingInfo(apNamingInfo);
		byte[] encodedObject = encoder.encode(flowService);
		
		ObjectValue objectValue = new ObjectValue();
		objectValue.setByteval(encodedObject);
		CDAPMessage cdapMessage = 
			CDAPMessage.getCreateObjectRequestMessage(null, null, FlowService.OBJECT_CLASS, 0, FlowService.OBJECT_NAME, objectValue, 0);
		
		return cdapMessage;
	}
	
	/**
	 * Write a delimited CDAP message to the socket output stream
	 * @param socket
	 * @param cdapMessage
	 * @throws Exception
	 */
	private void sendCDAPMessage(Socket socket, CDAPMessage cdapMessage) throws Exception{
		byte[] encodedMessage = cdapSessionManager.encodeCDAPMessage(cdapMessage);
		byte[] delimitedMessage = delimiter.getDelimitedSdu(encodedMessage);
		socket.getOutputStream().write(delimitedMessage);
	}

}
