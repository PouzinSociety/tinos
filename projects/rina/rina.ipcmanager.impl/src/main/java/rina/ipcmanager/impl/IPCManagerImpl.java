package rina.ipcmanager.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.ApplicationProcessNameSynonym;
import rina.applicationprocess.api.WhatevercastName;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.efcp.api.DataTransferConstants;
import rina.encoding.api.BaseEncoder;
import rina.encoding.api.Encoder;
import rina.enrollment.api.BaseEnrollmentTask;
import rina.enrollment.api.EnrollmentTask;
import rina.flowallocator.api.QoSCube;
import rina.ipcmanager.impl.console.IPCManagerConsole;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcprocess.api.IPCProcessFactory;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;

public class IPCManagerImpl {
	private static final Log log = LogFactory.getLog(IPCManagerImpl.class);
	
	private IPCManagerConsole console = null;
	
	/**
	 * The thread pool implementation
	 */
	private ExecutorService executorService = null;
	private static int MAXWORKERTHREADS = 10;
	
	/**
	 * The IPC Process factory
	 */
	private IPCProcessFactory ipcProcessFactory = null;
	
	public IPCManagerImpl(){
		console = new IPCManagerConsole(this);
		this.executorService = Executors.newFixedThreadPool(MAXWORKERTHREADS);
		executorService.execute(console);
		log.debug("IPC Manager started");
	}
	
	public void setIPCProcessFactory(IPCProcessFactory ipcProcessFactory){
		this.ipcProcessFactory = ipcProcessFactory;
	}
	
	public void createIPCProcess(String applicationProcessName, String applicationProcessInstance, String difName) throws Exception{
		ApplicationProcessNamingInfo apNamingInfo = new ApplicationProcessNamingInfo(applicationProcessName, applicationProcessInstance, null, null);
		IPCProcess ipcProcess = ipcProcessFactory.createIPCProcess(apNamingInfo);
		if (difName != null){
			WhatevercastName dan = new WhatevercastName();
			dan.setName(difName);
			dan.setRule("All members");

			RIBDaemon ribDaemon = (RIBDaemon) ipcProcess.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
			ribDaemon.create(null, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + 
					RIBObjectNames.WHATEVERCAST_NAMES + RIBObjectNames.SEPARATOR + "all", 0, dan);
			
			ApplicationProcessNameSynonym synonym = new ApplicationProcessNameSynonym();
			synonym.setApplicationProcessName(applicationProcessName);
			synonym.setApplicationProcessInstance(applicationProcessInstance);
			synonym.setSynonym(new byte[]{0x01});
			ribDaemon.write(null, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.CURRENT_SYNONYM, 0, synonym);
			
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
			ribDaemon.write(null, RIBObjectNames.SEPARATOR + RIBObjectNames.DIF + RIBObjectNames.SEPARATOR + RIBObjectNames.IPC + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.DATA_TRANSFER+ RIBObjectNames.SEPARATOR + RIBObjectNames.CONSTANTS, 0, dataTransferConstants);
			
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
			ribDaemon.create(null, RIBObjectNames.SEPARATOR + RIBObjectNames.DIF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.FLOW_ALLOCATOR + RIBObjectNames.SEPARATOR + RIBObjectNames.QOS_CUBES 
					+ RIBObjectNames.SEPARATOR + "unreliable", 0, qosCube);
			
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
			ribDaemon.create(null, RIBObjectNames.SEPARATOR + RIBObjectNames.DIF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.FLOW_ALLOCATOR + RIBObjectNames.SEPARATOR + RIBObjectNames.QOS_CUBES 
					+ RIBObjectNames.SEPARATOR + "reliable", 0, qosCube);
		}
	}
	
	public void destroyIPCProcesses(String applicationProcessName, String applicationProcessInstance) throws Exception{
		ApplicationProcessNamingInfo apNamingInfo = new ApplicationProcessNamingInfo(applicationProcessName, applicationProcessInstance, null, null);
		ipcProcessFactory.destroyIPCProcess(apNamingInfo);
	}
	
	public List<String> listIPCProcessesInformation(){
		List<String> ipcProcessesInformation = new ArrayList<String>();
		List<IPCProcess> ipcProcesses = ipcProcessFactory.listIPCProcesses();
		RIBDaemon ribDaemon = null;
		ApplicationProcessNamingInfo apNamingInfo = null;
		WhatevercastName difName = null;
		String information = null;
		
		for(int i=0; i<ipcProcesses.size(); i++){
			try{
				ribDaemon = (RIBDaemon) ipcProcesses.get(i).getIPCProcessComponent(BaseRIBDaemon.getComponentName());
				apNamingInfo = (ApplicationProcessNamingInfo) ribDaemon.read(null, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + 
						RIBObjectNames.MANAGEMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.APNAME, 0);
				difName = (WhatevercastName) ribDaemon.read(null, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
						RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.WHATEVERCAST_NAMES + RIBObjectNames.SEPARATOR + "1" , 0);
				information = "\n";
				information = information + "DIF name: " + difName.getName() + "\n";
				information = information + "Application process name: "+apNamingInfo.getApplicationProcessName() + "\n";
				information = information + "Application process instance: "+apNamingInfo.getApplicationProcessInstance() + "\n";
				ipcProcessesInformation.add(information);
			}catch(RIBDaemonException ex){
				log.error(ex);
			}
		}
		
		return ipcProcessesInformation;
	}
	
	public List<String> getPrintedRIB(String applicationProcessName, String applicationProcessInstance) throws Exception{
		IPCProcess ipcProcess = ipcProcessFactory.getIPCProcess(new ApplicationProcessNamingInfo(applicationProcessName, applicationProcessInstance, null, null));
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
				new ApplicationProcessNamingInfo(sourceApplicationProcessName, sourceApplicationProcessInstance, null, null));
		EnrollmentTask enrollmentTask = (EnrollmentTask) ipcProcess.getIPCProcessComponent(BaseEnrollmentTask.getComponentName());
		Encoder encoder = (Encoder) ipcProcess.getIPCProcessComponent(BaseEncoder.getComponentName());
		
		ApplicationProcessNameSynonym apNameSynonym = new ApplicationProcessNameSynonym();
		apNameSynonym.setApplicationProcessName(destinationApplicationProcessName);
		apNameSynonym.setApplicationProcessInstance(destinationApplicationProcessInstance);
		byte[] encodedApNamesynonym = encoder.encode(apNameSynonym);
		ObjectValue objectValue = new ObjectValue();
		objectValue.setByteval(encodedApNamesynonym);
		
		
		CDAPMessage cdapMessage = CDAPMessage.getCreateObjectRequestMessage(null, null, 3, "", 0, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.ENROLLMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.MEMBERS + RIBObjectNames.SEPARATOR + destinationApplicationProcessName + "-" + 
				destinationApplicationProcessInstance, objectValue, 0);
		CDAPSessionDescriptor cdapSessionDescriptor = new CDAPSessionDescriptor();
		enrollmentTask.initiateEnrollment(cdapMessage, cdapSessionDescriptor);
	}

}
