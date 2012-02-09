package rina.examples.apps.echoserver.test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import rina.applibrary.impl.RINAFactory;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.Opcode;
import rina.cdap.api.message.ObjectValue;
import rina.delimiting.api.Delimiter;
import rina.encoding.api.Encoder;
import rina.examples.apps.echoserver.EchoServer;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.ApplicationRegistration;
import rina.ipcservice.api.FlowService;

public class TestEchoServer {

	private ServerSocket rinaServer = null;
	private Delimiter delimiter = null;
	private CDAPSessionManager cdapSessionManager = null;
	private Encoder encoder = null;
	private ExecutorService executorService = null;
	
	@Before
	public void setup(){
		delimiter = RINAFactory.getDelimiterInstance();
		cdapSessionManager = RINAFactory.getCDAPSessionManagerInstance();
		encoder = RINAFactory.getEncoderInstance();
		executorService = Executors.newFixedThreadPool(2);
		try {
			rinaServer = new ServerSocket(RINAFactory.DEFAULT_PORT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@After
	public void tearDown(){
		try{
			rinaServer.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void testEchoServer() throws Exception{
		byte[] sdu = null;
		CDAPMessage cdapMessage = null;
		ApplicationRegistration applicationRegistration = null;
		int serverPort = 0;
		FlowService flowService = null;
		ObjectValue objectValue = null;
		Socket flowSocket = null;
		
		//0 Start Echo server
		EchoServerRunner echoServerRunner = new EchoServerRunner();
		executorService.execute(echoServerRunner);
		
		//1 Wait for registration attempt and reply
		Socket registerSocket = rinaServer.accept();
		Assert.assertTrue(registerSocket.isConnected());
		sdu = getNextSDU(registerSocket);
		cdapMessage = cdapSessionManager.decodeCDAPMessage(sdu);
		Assert.assertEquals(Opcode.M_START, cdapMessage.getOpCode());
		applicationRegistration = (ApplicationRegistration) encoder.decode(cdapMessage.getObjValue().getByteval(), ApplicationRegistration.class.toString());
		Assert.assertEquals(EchoServer.APPLICATION_PROCESS_NAME, applicationRegistration.getApNamingInfo().getApplicationProcessName());
		serverPort = applicationRegistration.getSocketNumber();
		cdapMessage = cdapMessage.getReplyMessage();
		registerSocket.getOutputStream().write(delimiter.getDelimitedSdu(cdapSessionManager.encodeCDAPMessage(cdapMessage)));
		
		//Wait 1 second and send a new flow request
		wait1Second();
		flowSocket = new Socket("localhost", serverPort);
		flowService = new FlowService();
		flowService.setSourceAPNamingInfo(new ApplicationProcessNamingInfo("junit-test", "1"));
		flowService.setDestinationAPNamingInfo(new ApplicationProcessNamingInfo(EchoServer.APPLICATION_PROCESS_NAME, null));
		flowService.setPortId(new Double(65.000*Math.random()).intValue());
		objectValue = new ObjectValue();
		objectValue.setByteval(encoder.encode(flowService));
		cdapMessage = CDAPMessage.getCreateObjectRequestMessage(null, null, FlowService.OBJECT_CLASS, 0, FlowService.OBJECT_NAME, objectValue, 0);
		flowSocket.getOutputStream().write(delimiter.getDelimitedSdu(cdapSessionManager.encodeCDAPMessage(cdapMessage)));
		
		//2 Get flow request response
		sdu = getNextSDU(flowSocket);
		cdapMessage = cdapSessionManager.decodeCDAPMessage(sdu);
		Assert.assertEquals(Opcode.M_CREATE_R, cdapMessage.getOpCode());
		Assert.assertEquals(0, cdapMessage.getResult());
		
		//3 Send an SDU and get the response
		sdu = "Today it's sunny but we are still enjoying the cold air from Siberia".getBytes();
		objectValue = new ObjectValue();
		objectValue.setByteval(sdu);
		cdapMessage = CDAPMessage.getWriteObjectRequestMessage(null, null, null, 0, objectValue, null, 0);
		flowSocket.getOutputStream().write(delimiter.getDelimitedSdu(cdapSessionManager.encodeCDAPMessage(cdapMessage)));
		sdu = getNextSDU(flowSocket);
		cdapMessage = cdapSessionManager.decodeCDAPMessage(sdu);
		Assert.assertEquals("Today it's sunny but we are still enjoying the cold air from Siberia", new String(cdapMessage.getObjValue().getByteval()));
		
		//4 disconnect
		cdapMessage = CDAPMessage.getDeleteObjectRequestMessage(null, null, null, 0, null, 0);
		flowSocket.getOutputStream().write(delimiter.getDelimitedSdu(cdapSessionManager.encodeCDAPMessage(cdapMessage)));
		wait1Second();
		
		//5 Stop Echo Server and wait 1 second
		echoServerRunner.stop();
		sdu = getNextSDU(registerSocket);
		cdapMessage = cdapSessionManager.decodeCDAPMessage(sdu);
		Assert.assertEquals(Opcode.M_STOP, cdapMessage.getOpCode());
		cdapMessage = cdapMessage.getReplyMessage();
		registerSocket.getOutputStream().write(delimiter.getDelimitedSdu(cdapSessionManager.encodeCDAPMessage(cdapMessage)));
		wait1Second();
	}
	
	private final byte[] getNextSDU(Socket socket) throws Exception{
		boolean lookingForSduLength = true;
		int length = 0;
		int index = 0;
		byte[] lastSduLengthCandidate = new byte[0];
		byte[] currentSduLengthCandidate = null;
		byte[] pdu = null;
		byte nextByte = 0;
		int value = 0;

		while(true){
			//Delimit the byte array that contains a serialized CDAP message
			value = socket.getInputStream().read();
			if (value == -1){
				break;
			}

			nextByte = (byte) value;
			if (lookingForSduLength){
				currentSduLengthCandidate = new byte[lastSduLengthCandidate.length + 1];
				for(int i=0; i<lastSduLengthCandidate.length; i++){
					currentSduLengthCandidate[i] = lastSduLengthCandidate[i];
				}
				currentSduLengthCandidate[lastSduLengthCandidate.length] = nextByte;
				length = delimiter.readVarint32(currentSduLengthCandidate);
				if (length == -2){
					lastSduLengthCandidate = currentSduLengthCandidate;
				}else{
					lastSduLengthCandidate = new byte[0];
					if (length > 0){
						lookingForSduLength = false;
					}
				}
			}else{
				if (index < length){
					if (pdu == null){
						pdu = new byte[length];
					}
					pdu[index] = nextByte;
					index ++;
					if (index == length){
						return pdu;
					}
				}
			}
		}
		
		return pdu;
	}
	
	private final void wait1Second(){
		try{
			Thread.sleep(1000);
		}catch(InterruptedException ex){
			ex.printStackTrace();
		}
	}
}
