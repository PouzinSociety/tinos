package rina.ipcservice.impl;

import java.util.HashMap;
import java.util.Map;

import rina.efcp.api.DataTransferAEFactory;
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

	public IPCProcess createIPCProcess(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		IPCProcess ipcProcess = new IPCProcessImpl(ipcProcessNamingInfo);
		
		ipcProcess.setFlowAllocator(flowAllocatorFactory.createFlowAllocator(ipcProcessNamingInfo));
		ipcProcess.setDataTransferAE(dataTransferAEFactory.createDataTransferAE(ipcProcessNamingInfo));
		ipcProcess.setRibDaemon(ribDaemonFactory.createRIBDaemon(ipcProcessNamingInfo));
		ipcProcess.setRmt(rmtFactory.createRMT(ipcProcessNamingInfo));
		
		ipcProcesses.put(ipcProcessNamingInfo, ipcProcess);
		return ipcProcess;
	}

	public void destroyIPCProcess(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		IPCProcess ipcProcess = ipcProcesses.remove(ipcProcessNamingInfo);
		
		flowAllocatorFactory.destroyFlowAllocator(ipcProcess.getIPCProcessNamingInfo());
		ribDaemonFactory.destroyRIBDeamon(ipcProcess.getIPCProcessNamingInfo());
		rmtFactory.destroyRMT(ipcProcess.getIPCProcessNamingInfo());
	}

	public void destroyIPCProcess(IPCProcess ipcProcess) {
		this.destroyIPCProcess(ipcProcess.getIPCProcessNamingInfo());
	}

	public IPCProcess getIPCProcess(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		return ipcProcesses.get(ipcProcessNamingInfo);
	}

}
