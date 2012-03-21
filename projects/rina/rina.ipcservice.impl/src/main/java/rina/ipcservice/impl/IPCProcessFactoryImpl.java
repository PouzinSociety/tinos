package rina.ipcservice.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rina.cdap.api.CDAPSessionManagerFactory;
import rina.delimiting.api.DelimiterFactory;
import rina.efcp.api.DataTransferAEFactory;
import rina.encoding.api.EncoderFactory;
import rina.enrollment.api.EnrollmentTaskFactory;
import rina.flowallocator.api.FlowAllocatorFactory;
import rina.ipcmanager.api.IPCManager;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcprocess.api.IPCProcessFactory;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonFactory;
import rina.rmt.api.RMTFactory;

public class IPCProcessFactoryImpl implements IPCProcessFactory{
	/**
	 * All the existing IPC processes in this system
	 */
	private Map<String, IPCProcess> ipcProcesses = null;
	
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
	
	/**
	 * Factory of enrollment tasks
	 */
	private EnrollmentTaskFactory enrollmentTaskFactory = null;
	
	/**
	 * The IPCManager of this system
	 */
	private IPCManager ipcManager = null;
	
	public IPCProcessFactoryImpl(){
		ipcProcesses = new HashMap<String, IPCProcess>();
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
	
	public void setEnrollmentTaskFactory(EnrollmentTaskFactory enrollmentTaskFactory){
		this.enrollmentTaskFactory = enrollmentTaskFactory;
	}
	
	public CDAPSessionManagerFactory getCDAPSessionManagerFactory(){
		return this.cdapSessionManagerFactory;
	}
	
	public EncoderFactory getEncoderFactory(){
		return this.encoderFactory;
	}
	
	public DelimiterFactory getDelimiterFactory(){
		return this.delimiterFactory;
	}

	public IPCProcess createIPCProcess(ApplicationProcessNamingInfo ipcProcessNamingInfo) throws Exception{
		if (ipcProcesses.get(ipcProcessNamingInfo.getProcessKey()) != null){
			throw new Exception("An IPC Process with this naming information already exists in this system");
		}
		
		RIBDaemon ribDaemon = null;
		IPCProcess ipcProcess = null;
		
		if (this.ribDaemonFactory != null){
			ribDaemon = this.ribDaemonFactory.createRIBDaemon(ipcProcessNamingInfo);
		}else{
			throw new Exception("RIB Daemon Factory is null");
		}

		if (this.ipcManager != null){
			ipcProcess = new IPCProcessImpl(ipcProcessNamingInfo.getApplicationProcessName(), 
					ipcProcessNamingInfo.getApplicationProcessInstance(), ribDaemon);
			ipcProcess.setIPCManager(this.ipcManager);
		}else{
			throw new Exception("IPC Manager is null");
		}
		
		if (this.cdapSessionManagerFactory != null){
			ipcProcess.addIPCProcessComponent(this.cdapSessionManagerFactory.createCDAPSessionManager());
		}else{
			throw new Exception("CDAP Session Manager Factory is null");
		}
		
		if (this.delimiterFactory != null){
			ipcProcess.addIPCProcessComponent(this.delimiterFactory.createDelimiter(DelimiterFactory.DIF));
		}else{
			throw new Exception("Delimiter Factory is null");
		}
		
		if (this.encoderFactory != null){
			ipcProcess.addIPCProcessComponent(this.encoderFactory.createEncoderInstance());
		}else{
			throw new Exception("Encoder Factory is null");
		}
		
		ipcProcess.addIPCProcessComponent(ribDaemon);
		
		if (this.rmtFactory != null){
			ipcProcess.addIPCProcessComponent(this.rmtFactory.createRMT(ipcProcessNamingInfo));
		}else{
			throw new Exception("RMT Factory is null");
		}
		
		if (this.enrollmentTaskFactory != null){
			ipcProcess.addIPCProcessComponent(this.enrollmentTaskFactory.createEnrollmentTask(ipcProcessNamingInfo));
		}else{
			throw new Exception("Enrollment Task Factory is null");
		}
		
		if (this.dataTransferAEFactory != null){
			ipcProcess.addIPCProcessComponent(dataTransferAEFactory.createDataTransferAE(ipcProcessNamingInfo));
		}else{
			throw new Exception("Data Transfer AE Factory is null");
		}
		
		if (this.flowAllocatorFactory != null){
			ipcProcess.addIPCProcessComponent(this.flowAllocatorFactory.createFlowAllocator(ipcProcessNamingInfo));
		}else{
			throw new Exception("Flow Allocator Factory is null");
		}
		
		ipcProcesses.put(ipcProcessNamingInfo.getProcessKey(), ipcProcess);
		return ipcProcess;
	}

	public void destroyIPCProcess(ApplicationProcessNamingInfo ipcProcessNamingInfo) throws Exception{
		if (ipcProcesses.get(ipcProcessNamingInfo.getProcessKey()) == null){
			throw new Exception("An IPC Process with this naming information does not exist in this system");
		}
		
		IPCProcess ipcProcess = ipcProcesses.remove(ipcProcessNamingInfo.getProcessKey());
		
		ribDaemonFactory.destroyRIBDaemon(ipcProcessNamingInfo);
		rmtFactory.destroyRMT(ipcProcessNamingInfo);
		enrollmentTaskFactory.destroyEnrollmentTask(ipcProcessNamingInfo);
		dataTransferAEFactory.destroyDataTransferAE(ipcProcessNamingInfo);
		flowAllocatorFactory.destroyFlowAllocator(ipcProcessNamingInfo);
		ipcProcess.destroy();
	}

	public void destroyIPCProcess(IPCProcess ipcProcess){
		try{
			this.destroyIPCProcess(ipcProcess.getApplicationProcessNamingInfo());
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public IPCProcess getIPCProcess(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		return ipcProcesses.get(ipcProcessNamingInfo.getProcessKey());
	}
	
	/**
	 * Return the IPC process that is a member of the DIF called "difname"
	 * @param difname The name of the DIF
	 * @return
	 */
	public IPCProcess getIPCProcessBelongingToDIF(String difName){
		IPCProcess currentIPCProcess = null;
		String candidateName = null;

		Iterator<String> iterator = ipcProcesses.keySet().iterator();
		while(iterator.hasNext()){
			currentIPCProcess = ipcProcesses.get(iterator.next());
			candidateName = currentIPCProcess.getDIFName();
			if (candidateName != null && candidateName.equals(difName)){
				return currentIPCProcess;
			}
		}

		return null;
	}
	
	/**
	 * Return a list of the names of the DIFs currently available in the system
	 * @return
	 */
	public List<String> listDIFNames(){
		List<String> difNames = new ArrayList<String>();
		String difName = null;
		
		Iterator<String> iterator = ipcProcesses.keySet().iterator();
		while(iterator.hasNext()){
			difName = ipcProcesses.get(iterator.next()).getDIFName();
			if (difName != null){
				difNames.add(difName);
			}
		}
		
		return difNames;
		
	}
	
	/**
	 * Return a list of the existing IPC processes
	 * @return
	 */
	public List<IPCProcess> listIPCProcesses(){
		List<IPCProcess> result = new ArrayList<IPCProcess>();
		Iterator<String> iterator = ipcProcesses.keySet().iterator();
		while(iterator.hasNext()){
			result.add(ipcProcesses.get(iterator.next()));
		}
		
		return result;
	}

	/**
	 * Set the IPCManager of this system
	 * @param ipcManager
	 */
	public void setIPCManager(IPCManager ipcManager) {
		this.ipcManager = ipcManager;
	}

}
