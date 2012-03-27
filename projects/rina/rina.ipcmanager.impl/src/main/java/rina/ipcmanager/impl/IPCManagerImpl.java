package rina.ipcmanager.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.WhatevercastName;
import rina.configuration.DIFConfiguration;
import rina.configuration.KnownIPCProcessConfiguration;
import rina.configuration.RINAConfiguration;
import rina.efcp.api.DataTransferConstants;
import rina.enrollment.api.BaseEnrollmentTask;
import rina.enrollment.api.EnrollmentTask;
import rina.enrollment.api.Neighbor;
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
import rina.rmt.api.BaseRMT;
import rina.rmt.api.RMT;

/**
 * The IPC Manager is the component of a DAF that manages the local IPC resources. In its current implementation it 
 * manages IPC Processes (creates/destroys them), and serves as a broker between applications and IPC Processes. Applications 
 * can use the RINA library to establish a connection to the IPC Manager and interact with the RINA stack.
 * @author eduardgrasa
 *
 */
public class IPCManagerImpl implements IPCManager{
	
	private static final Log log = LogFactory.getLog(IPCManagerImpl.class);
	
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
		executorService = Executors.newCachedThreadPool();
		console = new IPCManagerConsole(this);
		apService = new APServiceImpl(this);
		executorService.execute(console);
		log.debug("IPC Manager started");
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

	public void createIPCProcess(String applicationProcessName, String difName) throws Exception{
		IPCProcess ipcProcess = ipcProcessFactory.createIPCProcess(applicationProcessName);
		ipcProcess.setIPCManager(this);
		if (difName != null){
			DIFConfiguration difConfiguration = RINAConfiguration.getInstance().getDIFConfiguration(difName);
			if (difConfiguration == null){
				throw new Exception("Unrecognized DIF name: "+difName);
			}
			
			KnownIPCProcessConfiguration ipcProcessConfiguration = 
				RINAConfiguration.getInstance().getIPCProcessConfiguration(applicationProcessName);
			if (ipcProcessConfiguration == null){
				throw new Exception("Unrecoginzed IPC Process Name: "+applicationProcessName);
			}
			
			WhatevercastName dan = new WhatevercastName();
			dan.setName(difName);
			dan.setRule(WhatevercastName.DIF_NAME_WHATEVERCAST_RULE);

			RIBDaemon ribDaemon = (RIBDaemon) ipcProcess.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
			ribDaemon.create(WhatevercastName.WHATEVERCAST_NAME_RIB_OBJECT_CLASS, 
					WhatevercastName.WHATEVERCAST_NAME_SET_RIB_OBJECT_NAME + RIBObjectNames.SEPARATOR + 
					WhatevercastName.DIF_NAME_WHATEVERCAST_RULE, 
					dan);
			
			ribDaemon.write(RIBObjectNames.ADDRESS_RIB_OBJECT_CLASS, 
					RIBObjectNames.ADDRESS_RIB_OBJECT_NAME, 
					ipcProcessConfiguration.getAddress());
			
			ribDaemon.write(DataTransferConstants.DATA_TRANSFER_CONSTANTS_RIB_OBJECT_CLASS, 
					DataTransferConstants.DATA_TRANSFER_CONSTANTS_RIB_OBJECT_NAME, 
					difConfiguration.getDataTransferConstants());
			
			ribDaemon.create(QoSCube.QOSCUBE_SET_RIB_OBJECT_CLASS,
					QoSCube.QOSCUBE_SET_RIB_OBJECT_NAME, 
					(QoSCube[]) difConfiguration.getQosCubes().toArray(new QoSCube[difConfiguration.getQosCubes().size()]));
			
			ribDaemon.start(RIBObjectNames.OPERATIONAL_STATUS_RIB_OBJECT_CLASS, 
					RIBObjectNames.OPERATIONAL_STATUS_RIB_OBJECT_NAME);

			RMT rmt = (RMT) ipcProcess.getIPCProcessComponent(BaseRMT.getComponentName());
			rmt.startListening();
		}
	}
	
	public void destroyIPCProcesses(String applicationProcessName) throws Exception{
		ipcProcessFactory.destroyIPCProcess(applicationProcessName);
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

	public List<String> getPrintedRIB(String applicationProcessName) throws Exception{
		IPCProcess ipcProcess = ipcProcessFactory.getIPCProcess(applicationProcessName);
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
	
	public void enroll(String sourceApplicationProcessName, String destinationApplicationProcessName) throws Exception{
		IPCProcess ipcProcess = ipcProcessFactory.getIPCProcess(sourceApplicationProcessName);
		EnrollmentTask enrollmentTask = (EnrollmentTask) ipcProcess.getIPCProcessComponent(BaseEnrollmentTask.getComponentName());
		
		Neighbor neighbor = new Neighbor();
		neighbor.setApplicationProcessName(destinationApplicationProcessName);

		enrollmentTask.initiateEnrollment(neighbor);
	}
	
	public void allocateFlow(String sourceIPCProcessName, String destinationApplicationProcessName) throws Exception{
		IPCProcess ipcProcess = ipcProcessFactory.getIPCProcess(sourceIPCProcessName);
		IPCService ipcService = (IPCService) ipcProcess;
		FlowService flowService = new FlowService();
		flowService.setDestinationAPNamingInfo(new ApplicationProcessNamingInfo(destinationApplicationProcessName));
		flowService.setSourceAPNamingInfo(new ApplicationProcessNamingInfo("console", "1"));
		ipcService.submitAllocateRequest(flowService);
	}
	
	public void deallocateFlow(String sourceIPCProcessName, int portId) throws Exception{
		IPCProcess ipcProcess = ipcProcessFactory.getIPCProcess(sourceIPCProcessName);
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
