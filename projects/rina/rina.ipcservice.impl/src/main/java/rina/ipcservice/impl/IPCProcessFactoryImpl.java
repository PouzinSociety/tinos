package rina.ipcservice.impl;

import java.util.HashMap;
import java.util.Map;

import rina.cdap.api.CDAPSessionManagerFactory;
import rina.delimiting.api.DelimiterFactory;
import rina.efcp.api.DataTransferAEFactory;
import rina.encoding.api.EncoderFactory;
import rina.flowallocator.api.FlowAllocatorFactory;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcprocess.api.IPCProcessFactory;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.RIBDaemonFactory;
import rina.rmt.api.RMTFactory;

public class IPCProcessFactoryImpl implements IPCProcessFactory{
	
	/**
	 * All the existing IPC processes in this system
	 */
	private Map<ApplicationProcessNamingInfo, IPCProcess> ipcProcesses = null;
	
	/**
	 * Creates instances of rib daemons
	 */
	private RIBDaemonFactory ribDaemonFactory = null;
	
	/**
	 * Creates instances of rmts
	 */
	private RMTFactory rmtFactory = null;
	
	/**
	 * Creates instances of flow allocators
	 */
	private FlowAllocatorFactory flowAllocatorFactory = null;
	
	/**
	 * Creates instances of data transfer AE factories
	 */
	private DataTransferAEFactory dataTransferAEFactory = null;
	
	/**
	 * Factory of CDAP session managers
	 */
	private CDAPSessionManagerFactory cdapSessionManagerFactory = null;
	
	/**
	 * Factory of serializers
	 */
	private EncoderFactory encoderFactory = null;
	
	/**
	 * Factory of delimiters
	 */
	private DelimiterFactory delimiterFactory = null;
	
	public IPCProcessFactoryImpl(){
		ipcProcesses = new HashMap<ApplicationProcessNamingInfo, IPCProcess>();
	}

	public void setRibDaemonFactory(RIBDaemonFactory ribDaemonFactory) {
		this.ribDaemonFactory = ribDaemonFactory;
	}

	public void setRmtFactory(RMTFactory rmtFactory) {
		this.rmtFactory = rmtFactory;
	}

	public void setFlowAllocatorFactory(FlowAllocatorFactory flowAllocatorFactory) {
		this.flowAllocatorFactory = flowAllocatorFactory;
	}

	public void setDataTransferAEFactory(DataTransferAEFactory dataTransferAEFactory) {
		this.dataTransferAEFactory = dataTransferAEFactory;
	}
	
	public void setCDAPSessionManagerFactory(CDAPSessionManagerFactory cdapSessionManagerFactory){
		this.cdapSessionManagerFactory = cdapSessionManagerFactory;
	}
	
	public void setEncoderFactory(EncoderFactory encoderFactory){
		this.encoderFactory = encoderFactory;
	}

	public void setDelimiterFactory(DelimiterFactory delimiterFactory) {
		this.delimiterFactory = delimiterFactory;
	}

	public IPCProcess createIPCProcess(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		IPCProcess ipcProcess = new IPCProcessImpl(ipcProcessNamingInfo.getApplicationProcessName(), 
				ipcProcessNamingInfo.getApplicationProcessInstance());
		
		//ipcProcess.addIPCProcessComponent(flowAllocatorFactory.createFlowAllocator(ipcProcessNamingInfo));
		//ipcProcess.addIPCProcessComponent(dataTransferAEFactory.createDataTransferAE(ipcProcessNamingInfo));
		ipcProcess.addIPCProcessComponent(ribDaemonFactory.createRIBDaemon(ipcProcessNamingInfo));
		ipcProcess.addIPCProcessComponent(rmtFactory.createRMT(ipcProcessNamingInfo));
		ipcProcess.addIPCProcessComponent(cdapSessionManagerFactory.createCDAPSessionManager());
		ipcProcess.addIPCProcessComponent(encoderFactory.createEncoderInstance());
		ipcProcess.addIPCProcessComponent(delimiterFactory.createDelimiter(DelimiterFactory.DIF));
		
		ipcProcesses.put(ipcProcessNamingInfo, ipcProcess);
		return ipcProcess;
	}

	public void destroyIPCProcess(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		IPCProcess ipcProcess = ipcProcesses.remove(ipcProcessNamingInfo);
		
		//flowAllocatorFactory.destroyFlowAllocator(ipcProcessNamingInfo);
		ribDaemonFactory.destroyRIBDaemon(ipcProcessNamingInfo);
		rmtFactory.destroyRMT(ipcProcessNamingInfo);
		ipcProcess.destroy();
	}

	public void destroyIPCProcess(IPCProcess ipcProcess) {
		ApplicationProcessNamingInfo apNamingInfo = 
			new ApplicationProcessNamingInfo(ipcProcess.getApplicationProcessName(), ipcProcess.getApplicationProcessInstance(), null, null);
		this.destroyIPCProcess(apNamingInfo);
	}

	public IPCProcess getIPCProcess(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		return ipcProcesses.get(ipcProcessNamingInfo);
	}

}
