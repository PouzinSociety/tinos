package rina.ipcmanager.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.DAFMember;
import rina.applicationprocess.api.WhatevercastName;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.efcp.api.DataTransferConstants;
import rina.encoding.api.BaseEncoder;
import rina.encoding.api.Encoder;
import rina.enrollment.api.BaseEnrollmentTask;
import rina.enrollment.api.EnrollmentTask;
import rina.flowallocator.api.FlowAllocatorInstance;
import rina.flowallocator.api.QoSCube;
import rina.flowallocator.api.message.Flow;
import rina.idd.api.InterDIFDirectoryFactory;
import rina.ipcmanager.api.IPCManager;
import rina.ipcmanager.impl.apservice.APServiceImpl;
import rina.ipcmanager.impl.console.IPCManagerConsole;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcprocess.api.IPCProcessFactory;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCService;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * The IPC Manager is the component of a DAF that manages the local IPC resources. In its current implementation it 
 * manages IPC Processes (creates/destroys them), and serves as a broker between applications and IPC Processes. Applications 
 * can use the RINA library to establish a connection to the IPC Manager and interact with the RINA stack.
 * @author eduardgrasa
 *
 */
public class IPCManagerImpl implements IPCManager{
	private static final Log log = LogFactory.getLog(IPCManagerImpl.class);
	
	public static final String CONFIG_FILE_LOCATION = "config/rina/config.rina"; 
	
	private IPCManagerConsole console = null;
	
	/**
	 * The thread pool implementation
	 */
	private ExecutorService executorService = null;
	
	/**
	 * The IPC Process factory
	 */
	private IPCProcessFactory ipcProcessFactory = null;
	
	private APServiceImpl apService = null;
	
	public IPCManagerImpl(){
		readConfigurationFile();
		executorService = Executors.newCachedThreadPool();
		console = new IPCManagerConsole(this);
		apService = new APServiceImpl(this);
		executorService.execute(console);
		log.debug("IPC Manager started");
	}
	
	/**
	 * Read the configuration parameters from the properties file
	 */
	private void readConfigurationFile(){
		Properties properties = new Properties();
		 
    	try {
    		properties.load(new FileInputStream(CONFIG_FILE_LOCATION));
    		Iterator<Entry<Object, Object>> iterator = properties.entrySet().iterator();
    		Entry<Object, Object> currentEntry = null;
    		while (iterator.hasNext()){
    			currentEntry = iterator.next();
    			System.setProperty((String) currentEntry.getKey(), (String) currentEntry.getValue());
    			log.debug("Added the property: "+currentEntry.getKey()+"="+currentEntry.getValue());
    		}
    	} catch (IOException ex) {
    		log.debug("Could not find the main configuration file. Using default values!");
        }
	}
	
	/**
	 * Executes a runnable in a thread. The IPCManager maintains a single thread pool 
	 * for all the RINA prototype
	 * @param runnable
	 */
	public synchronized void execute(Runnable runnable){
		executorService.execute(runnable);
	}
	
	public void stop(){
		apService.stop();
		console.stop();
		executorService.shutdownNow();
	}
	
	public void setInterDIFDirectoryFactory(InterDIFDirectoryFactory iddFactory){
		apService.setInterDIFDirectory(iddFactory.createIDD(this));
	}
	
	public void setIPCProcessFactory(IPCProcessFactory ipcProcessFactory){
		this.ipcProcessFactory = ipcProcessFactory;
		ipcProcessFactory.setIPCManager(this);
		apService.setIPCProcessFactory(ipcProcessFactory);
	}

	public void createIPCProcess(String applicationProcessName, String applicationProcessInstance, String difName) throws Exception{
		ApplicationProcessNamingInfo apNamingInfo = new ApplicationProcessNamingInfo(applicationProcessName, applicationProcessInstance);
		IPCProcess ipcProcess = ipcProcessFactory.createIPCProcess(apNamingInfo);
		ipcProcess.setIPCManager(this);
		if (difName != null){
			WhatevercastName dan = new WhatevercastName();
			dan.setName(difName);
			dan.setRule("Any member");

			RIBDaemon ribDaemon = (RIBDaemon) ipcProcess.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
			ribDaemon.create(null, WhatevercastName.DIF_NAME_WHATEVERCAST_OBJECT_NAME, 0, dan);
			ribDaemon.write(null, RIBObjectNames.CURRENT_SYNONYM_RIB_OBJECT_NAME, 0, new Long(1));
			
			DataTransferConstants dataTransferConstants = new DataTransferConstants();
			dataTransferConstants.setAddressLength(2);
			dataTransferConstants.setCepIdLength(2);
			dataTransferConstants.setDIFConcatenation(true);
			dataTransferConstants.setDIFFragmentation(false);
			dataTransferConstants.setDIFIntegrity(false);
			dataTransferConstants.setLengthLength(2);
			dataTransferConstants.setMaxPDUSize(1950);
			dataTransferConstants.setPortIdLength(2);
			dataTransferConstants.setQosIdLength(1);
			dataTransferConstants.setSequenceNumberLength(2);
			ribDaemon.write(null, DataTransferConstants.DATA_TRANSFER_CONSTANTS_RIB_OBJECT_NAME, 0, dataTransferConstants);
			
			QoSCube qosCube = new QoSCube();
			qosCube.setAverageBandwidth(0);
			qosCube.setAverageSDUBandwidth(0);
			qosCube.setDelay(0);
			qosCube.setJitter(0);
			qosCube.setMaxAllowableGapSdu(-1);
			qosCube.setOrder(false);
			qosCube.setPartialDelivery(true);
			qosCube.setPeakBandwidthDuration(0);
			qosCube.setPeakSDUBandwidthDuration(0);
			qosCube.setQosId(new byte[]{0x01});
			qosCube.setUndetectedBitErrorRate(Double.valueOf("1E-09"));
			ribDaemon.create(null, QoSCube.QOSCUBE_SET_RIB_OBJECT_NAME + RIBObjectNames.SEPARATOR + "unreliable", 0, qosCube);
			
			qosCube = new QoSCube();
			qosCube.setAverageBandwidth(0);
			qosCube.setAverageSDUBandwidth(0);
			qosCube.setDelay(0);
			qosCube.setJitter(0);
			qosCube.setMaxAllowableGapSdu(0);
			qosCube.setOrder(true);
			qosCube.setPartialDelivery(false);
			qosCube.setPeakBandwidthDuration(0);
			qosCube.setPeakSDUBandwidthDuration(0);
			qosCube.setQosId(new byte[]{0x02});
			qosCube.setUndetectedBitErrorRate(Double.valueOf("1E-09"));
			ribDaemon.create(null, QoSCube.QOSCUBE_SET_RIB_OBJECT_NAME + RIBObjectNames.SEPARATOR + "reliable", 0, qosCube);
		}
	}
	
	public void destroyIPCProcesses(String applicationProcessName, String applicationProcessInstance) throws Exception{
		ApplicationProcessNamingInfo apNamingInfo = new ApplicationProcessNamingInfo(applicationProcessName, applicationProcessInstance);
		ipcProcessFactory.destroyIPCProcess(apNamingInfo);
	}
	
	public List<String> listIPCProcessesInformation(){
		List<String> ipcProcessesInformation = new ArrayList<String>();
		List<IPCProcess> ipcProcesses = ipcProcessFactory.listIPCProcesses();
		ApplicationProcessNamingInfo apNamingInfo = null;
		String difName = null;
		String information = null;

		for(int i=0; i<ipcProcesses.size(); i++){
			apNamingInfo = ipcProcesses.get(i).getApplicationProcessNamingInfo();
			difName = ipcProcesses.get(i).getDIFName();
			information = "\n";
			information = information + "DIF name: " + difName + "\n";
			information = information + "Application process name: "+apNamingInfo.getApplicationProcessName() + "\n";
			information = information + "Application process instance: "+apNamingInfo.getApplicationProcessInstance() + "\n";
			information = information + "Address: "+ipcProcesses.get(i).getAddress().longValue() + "\n";
			ipcProcessesInformation.add(information);
		}

		return ipcProcessesInformation;
	}

	public List<String> getPrintedRIB(String applicationProcessName, String applicationProcessInstance) throws Exception{
		IPCProcess ipcProcess = ipcProcessFactory.getIPCProcess(new ApplicationProcessNamingInfo(applicationProcessName, applicationProcessInstance));
		RIBDaemon ribDaemon = (RIBDaemon) ipcProcess.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		List<RIBObject> ribObjects = ribDaemon.getRIBObjects();
		List<String> result = new ArrayList<String>();
		RIBObject currentRIBObject = null;
		String object = null;
		
		for(int i=0; i<ribObjects.size(); i++){
			currentRIBObject = ribObjects.get(i);
			object = "\nObject name: " + currentRIBObject.getObjectName() + "\n";
			object = object + "Object class: " + currentRIBObject.getObjectClass() + "\n";
			object = object + "Object instance: " + currentRIBObject.getObjectInstance() + "\n";
			object = object + "Object value: " + currentRIBObject.getObjectValue() + "\n";
			result.add(object);
		}
		
		return result;
	}
	
	public void enroll(String sourceApplicationProcessName, String sourceApplicationProcessInstance, 
			String destinationApplicationProcessName, String destinationApplicationProcessInstance) throws Exception{
		IPCProcess ipcProcess = ipcProcessFactory.getIPCProcess(
				new ApplicationProcessNamingInfo(sourceApplicationProcessName, sourceApplicationProcessInstance));
		EnrollmentTask enrollmentTask = (EnrollmentTask) ipcProcess.getIPCProcessComponent(BaseEnrollmentTask.getComponentName());
		Encoder encoder = (Encoder) ipcProcess.getIPCProcessComponent(BaseEncoder.getComponentName());
		
		DAFMember dafMember = new DAFMember();
		dafMember.setApplicationProcessName(destinationApplicationProcessName);
		dafMember.setApplicationProcessInstance(destinationApplicationProcessInstance);
		byte[] encodedDafMember = encoder.encode(dafMember);
		ObjectValue objectValue = new ObjectValue();
		objectValue.setByteval(encodedDafMember);
		
		CDAPMessage cdapMessage = CDAPMessage.getCreateObjectRequestMessage(null, null, "", 0, DAFMember.DAF_MEMBER_SET_RIB_OBJECT_NAME 
				+ RIBObjectNames.SEPARATOR + destinationApplicationProcessName + "-" + destinationApplicationProcessInstance, objectValue, 0);
		cdapMessage.setInvokeID(3);
		CDAPSessionDescriptor cdapSessionDescriptor = new CDAPSessionDescriptor();
		enrollmentTask.initiateEnrollment(cdapMessage, cdapSessionDescriptor);
	}
	
	public void allocateFlow(String sourceIPCProcessName, String sourceIPCProcessInstance, 
			String destinationApplicationProcessName, String destinationApplicationProcessInstance) throws Exception{
		IPCProcess ipcProcess = ipcProcessFactory.getIPCProcess(
				new ApplicationProcessNamingInfo(sourceIPCProcessName, sourceIPCProcessInstance));
		IPCService ipcService = (IPCService) ipcProcess;
		FlowService flowService = new FlowService();
		flowService.setDestinationAPNamingInfo(new ApplicationProcessNamingInfo(destinationApplicationProcessName, destinationApplicationProcessInstance));
		flowService.setSourceAPNamingInfo(new ApplicationProcessNamingInfo("console", "1"));
		ipcService.submitAllocateRequest(flowService);
	}
	
	public void deallocateFlow(String sourceIPCProcessName, String sourceIPCProcessInstance, int portId) throws Exception{
		IPCProcess ipcProcess = ipcProcessFactory.getIPCProcess(
				new ApplicationProcessNamingInfo(sourceIPCProcessName, sourceIPCProcessInstance));
		IPCService ipcService = (IPCService) ipcProcess;
		ipcService.submitDeallocate(portId);
	}

	public void createFlowRequestMessageReceived(Flow arg0,
			FlowAllocatorInstance arg1) {
		// TODO Auto-generated method stub
		
	}

	public APService getAPService() {
		return apService;
	}

}
