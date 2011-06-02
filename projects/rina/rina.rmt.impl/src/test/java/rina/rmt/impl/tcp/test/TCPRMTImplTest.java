package rina.rmt.impl.tcp.test;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import rina.delimiting.api.Delimiter;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.RIBDaemon;
import rina.rmt.impl.tcp.TCPRMTImpl;

public class TCPRMTImplTest {
	
	private TCPRMTImpl rmt = null;
	private Delimiter delimiter = null;
	private FakeRIBDaemon ribdaemon = null;
	
	public void setup(){
		this.rmt = new TCPRMTImpl();
		IPCProcess fakeIPCProcess = new FakeIPCProcess();
		this.rmt.setIPCProcess(fakeIPCProcess);
		fakeIPCProcess.setRmt(rmt);
		this.delimiter = fakeIPCProcess.getDelimiter();
		this.ribdaemon = (FakeRIBDaemon) fakeIPCProcess.getRibDaemon();
	}
	
	public Delimiter getDelimiter(){
		return delimiter;
	}
	
	public static void main(String[] args){
		TCPRMTImplTest test = new TCPRMTImplTest();
		test.setup();
		
		try {
			Socket clientSocket = new Socket("localhost", 32769);
			byte[] delimitedSdu = test.getDelimiter().getDelimitedSdu("CDAP message coming".getBytes());
			clientSocket.getOutputStream().write(delimitedSdu);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
