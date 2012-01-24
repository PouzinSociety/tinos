package rina.ipcmanager.impl.test;

import java.util.List;

import rina.cdap.api.CDAPSessionManagerFactory;
import rina.cdap.impl.CDAPSessionManagerFactoryImpl;
import rina.cdap.impl.googleprotobuf.GoogleProtocolBufWireMessageProviderFactory;
import rina.delimiting.api.DelimiterFactory;
import rina.delimiting.impl.DelimiterFactoryImpl;
import rina.encoding.api.EncoderFactory;
import rina.encoding.impl.googleprotobuf.GPBEncoderFactory;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcprocess.api.IPCProcessFactory;
import rina.ipcservice.api.ApplicationProcessNamingInfo;

public class MockIPCProcessFactory implements IPCProcessFactory{
	
	private IPCProcess ipcProcess = null;
	private CDAPSessionManagerFactoryImpl cdapSessionManagerFactory = null;
	private EncoderFactory encoderFactory = null;
	private DelimiterFactory delimiterFactory = null;
	
	public MockIPCProcessFactory(){
		ipcProcess = new MockIPCProcess();
	}

	public IPCProcess createIPCProcess(ApplicationProcessNamingInfo arg0)
			throws Exception {
		ipcProcess = new MockIPCProcess();
		return ipcProcess;
	}

	public void destroyIPCProcess(ApplicationProcessNamingInfo arg0)
			throws Exception {
		ipcProcess = null;
	}

	public void destroyIPCProcess(IPCProcess arg0) throws Exception {
		ipcProcess = null;
	}

	public CDAPSessionManagerFactory getCDAPSessionManagerFactory() {
		if (cdapSessionManagerFactory == null){
			cdapSessionManagerFactory = new CDAPSessionManagerFactoryImpl();
			cdapSessionManagerFactory.setWireMessageProviderFactory(new GoogleProtocolBufWireMessageProviderFactory());
		}
		
		return cdapSessionManagerFactory;
	}

	public DelimiterFactory getDelimiterFactory() {
		if (delimiterFactory == null){
			delimiterFactory = new DelimiterFactoryImpl();
		}
		
		return delimiterFactory;
	}

	public EncoderFactory getEncoderFactory() {
		if (encoderFactory == null){
			encoderFactory = new GPBEncoderFactory();
		}
		
		return encoderFactory;
	}

	public IPCProcess getIPCProcess(ApplicationProcessNamingInfo arg0) {
		return ipcProcess;
	}
	
	/**
	 * Return the IPC process that is a member of the DIF called "difname"
	 * @param difname The name of the DIF
	 * @return
	 */
	public IPCProcess getIPCProcessBelongingToDIF(String difname){
		return ipcProcess;
	}

	public List<IPCProcess> listIPCProcesses() {
		// TODO Auto-generated method stub
		return null;
	}
}
