package rina.ipcservice.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPSessionManagerFactory;
import rina.delimiting.api.DelimiterFactory;
import rina.efcp.api.DataTransferAEFactory;
import rina.encoding.api.EncoderFactory;
import rina.enrollment.api.EnrollmentTaskFactory;
import rina.flowallocator.api.FlowAllocatorFactory;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcprocess.api.IPCProcessFactory;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonFactory;
import rina.ribdaemon.api.RIBObjectNames;
import rina.rmt.api.RMTFactory;

public class IPCProcessFactoryImpl implements IPCProcessFactory{
	private static final Log log = LogFactory.getLog(IPCProcessFactoryImpl.class);
	
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

	public IPCProcess createIPCProcess(ApplicationProcessNamingInfo ipcProcessNamingInfo) throws Exception{
		if (ipcProcesses.get(ipcProcessNamingInfo.getProcessKey()) != null){
			throw new Exception("An IPC Process with this naming information already exists in this system");
		}
		
		RIBDaemon ribDaemon = ribDaemonFactory.createRIBDaemon(ipcProcessNamingInfo);
		IPCProcess ipcProcess = new IPCProcessImpl(ipcProcessNamingInfo.getApplicationProcessName(), 
				ipcProcessNamingInfo.getApplicationProcessInstance(), ribDaemon);
		
		ipcProcess.addIPCProcessComponent(ribDaemon);
		ipcProcess.addIPCProcessComponent(delimiterFactory.createDelimiter(DelimiterFactory.DIF));
		ipcProcess.addIPCProcessComponent(encoderFactory.createEncoderInstance());
		ipcProcess.addIPCProcessComponent(rmtFactory.createRMT(ipcProcessNamingInfo));
		ipcProcess.addIPCProcessComponent(cdapSessionManagerFactory.createCDAPSessionManager());
		ipcProcess.addIPCProcessComponent(enrollmentTaskFactory.createEnrollmentTask(ipcProcessNamingInfo));
		ipcProcess.addIPCProcessComponent(dataTransferAEFactory.createDataTransferAE(ipcProcessNamingInfo));
		ipcProcess.addIPCProcessComponent(flowAllocatorFactory.createFlowAllocator(ipcProcessNamingInfo));
		
		ipcProcesses.put(ipcProcessNamingInfo.getProcessKey(), ipcProcess);
		return ipcProcess;
	}

	public void destroyIPCProcess(ApplicationProcessNamingInfo ipcProcessNamingInfo) throws Exception{
		if (ipcProcesses.get(ipcProcessNamingInfo.getProcessKey()) == null){
			throw new Exception("An IPC Process with this naming information does not exist in this system");
		}
		
		IPCProcess ipcProcess = ipcProcesses.remove(ipcProcessNamingInfo.getProcessKey());
		
		//flowAllocatorFactory.destroyFlowAllocator(ipcProcessNamingInfo);
		ribDaemonFactory.destroyRIBDaemon(ipcProcessNamingInfo);
		rmtFactory.destroyRMT(ipcProcessNamingInfo);
		enrollmentTaskFactory.destroyEnrollmentTask(ipcProcessNamingInfo);
		dataTransferAEFactory.destroyDataTransferAE(ipcProcessNamingInfo);
		flowAllocatorFactory.destroyFlowAllocator(ipcProcessNamingInfo);
		ipcProcess.destroy();
	}

	public void destroyIPCProcess(IPCProcess ipcProcess) {
		RIBDaemon ribDaemon = (RIBDaemon) ipcProcess.getIPCProcessComponent(BaseRIBDaemon.getComponentName());

		try{
			ApplicationProcessNamingInfo apNamingInfo = (ApplicationProcessNamingInfo) ribDaemon.read(null, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + 
					RIBObjectNames.APNAME, 0);
			this.destroyIPCProcess(apNamingInfo);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public IPCProcess getIPCProcess(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		return ipcProcesses.get(ipcProcessNamingInfo.getProcessKey());
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

}
