package rina.ipcservice.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rina.cdap.api.CDAPSessionManagerFactory;
import rina.configuration.RINAConfiguration;
import rina.delimiting.api.DelimiterFactory;
import rina.efcp.api.DataTransferAEFactory;
import rina.encoding.api.EncoderFactory;
import rina.enrollment.api.EnrollmentTaskFactory;
import rina.flowallocator.api.FlowAllocatorFactory;
import rina.ipcmanager.api.IPCManager;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcprocess.api.IPCProcessFactory;
import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.resourceallocator.api.ResourceAllocatorFactory;
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
	 * Factory of resource allocators
	 */
	private ResourceAllocatorFactory resourceAllocatorFactory = null;
	
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
	
	public void setResourceAllocatorFactory(ResourceAllocatorFactory resourceAllocatorFactory){
		this.resourceAllocatorFactory = resourceAllocatorFactory;
	}
	
	public ResourceAllocatorFactory getResourceAllocatorFactory(){
		return this.resourceAllocatorFactory;
	}
	
	public EncoderFactory getEncoderFactory(){
		return this.encoderFactory;
	}
	
	public DelimiterFactory getDelimiterFactory(){
		return this.delimiterFactory;
	}

	/**
	 * Creates a new IPC process
	 * @param apName the application process name of this IPC process
	 * @param apInstance the application process instance of this IPC process
	 * @param parameters optional extra parameters
	 * @param config the configuration
	 */
	public IPCProcess createIPCProcess(String apName, String apInstance, RINAConfiguration config) throws Exception{
		if (ipcProcesses.get(apName+"-"+apInstance) != null){
			throw new Exception("An IPC Process with this name/instance pair already exists in this system");
		}

		ApplicationProcessNamingInfo apNamingInfo = new ApplicationProcessNamingInfo();
		apNamingInfo.setApplicationProcessName(apName);
		apNamingInfo.setApplicationProcessInstance(apInstance);

		RIBDaemon ribDaemon = null;
		IPCProcess ipcProcess = null;

		if (this.ribDaemonFactory != null){
			ribDaemon = this.ribDaemonFactory.createRIBDaemon(apNamingInfo);
		}else{
			throw new Exception("RIB Daemon Factory is null");
		}

		if (this.ipcManager != null){
			ipcProcess = new IPCProcessImpl(apNamingInfo.getApplicationProcessName(), 
					apNamingInfo.getApplicationProcessInstance(), ribDaemon);
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
		
		if (this.resourceAllocatorFactory != null){
			ipcProcess.addIPCProcessComponent(this.resourceAllocatorFactory.createResourceAllocator(apNamingInfo));
		}else{
			throw new Exception("Resource Allocator Factory is null");
		}

		if (this.dataTransferAEFactory != null){
			ipcProcess.addIPCProcessComponent(dataTransferAEFactory.createDataTransferAE(apNamingInfo));
		}else{
			throw new Exception("Data Transfer AE Factory is null");
		}
		
		if (this.rmtFactory != null){
			ipcProcess.addIPCProcessComponent(this.rmtFactory.createRMT(apNamingInfo));
		}else{
			throw new Exception("RMT Factory is null");
		}

		if (this.flowAllocatorFactory != null){
			ipcProcess.addIPCProcessComponent(this.flowAllocatorFactory.createFlowAllocator(apNamingInfo));
		}else{
			throw new Exception("Flow Allocator Factory is null");
		}
		
		if (this.enrollmentTaskFactory != null){
			ipcProcess.addIPCProcessComponent(this.enrollmentTaskFactory.createEnrollmentTask(apNamingInfo));
		}else{
			throw new Exception("Enrollment Task Factory is null");
		}

		ipcProcesses.put(apName+"-"+apInstance, ipcProcess);
		return ipcProcess;
	}

	/**
	 * Destroys an existing IPC process
	 * @param applicationProcessName the application process name of this IPC process
	 * @param applicationProcessInstance the application process instance of this IPC process
	 */
	public void destroyIPCProcess(String apName, String apInstance) throws Exception{
		if (ipcProcesses.get(apName+"-"+apInstance) == null){
			throw new Exception("An IPC Process with this naming information does not exist in this system");
		}
		
		IPCProcess ipcProcess = ipcProcesses.remove(apName+"-"+apInstance);
		ApplicationProcessNamingInfo apNamingInfo = ipcProcess.getApplicationProcessNamingInfo();
		
		ribDaemonFactory.destroyRIBDaemon(apNamingInfo);
		rmtFactory.destroyRMT(apNamingInfo);
		enrollmentTaskFactory.destroyEnrollmentTask(apNamingInfo);
		dataTransferAEFactory.destroyDataTransferAE(apNamingInfo);
		flowAllocatorFactory.destroyFlowAllocator(apNamingInfo);
		resourceAllocatorFactory.destroyResourceAllocator(apNamingInfo);
		ipcProcess.destroy();
	}

	public void destroyIPCProcess(IPCProcess ipcProcess){
		try{
			this.destroyIPCProcess(ipcProcess.getApplicationProcessName(), 
					ipcProcess.getApplicationProcessInstance());
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	/**
	 * Get an existing IPC process
	 * @param applicationProcessName the application process name of this IPC process
	 * @param applicationProcessInstance the application process instance of this IPC process
	 */
	public IPCProcess getIPCProcess(String apName, String apInstance){
		return ipcProcesses.get(apName+"-"+apInstance);
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
