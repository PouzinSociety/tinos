package rina.ipcmanager.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import rina.applicationprocess.api.WhatevercastName;
import rina.configuration.DIFConfiguration;
import rina.configuration.IPCProcessToCreate;
import rina.configuration.KnownIPCProcessConfiguration;
import rina.configuration.RINAConfiguration;
import rina.efcp.api.DataTransferConstants;
import rina.enrollment.api.BaseEnrollmentTask;
import rina.enrollment.api.EnrollmentTask;
import rina.enrollment.api.Neighbor;
import rina.flowallocator.api.FlowAllocatorInstance;
import rina.flowallocator.api.QoSCube;
import rina.flowallocator.api.Flow;
import rina.idd.api.InterDIFDirectoryFactory;
import rina.ipcmanager.api.IPCManager;
import rina.ipcmanager.impl.apservice.APServiceImpl;
import rina.ipcmanager.impl.console.IPCManagerConsole;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcprocess.api.IPCProcessFactory;
import rina.ipcservice.api.APService;
import rina.applicationprocess.api.ApplicationProcessNamingInfo;
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
	
	public static final String CONFIG_FILE_LOCATION = "config/rina/config.rina"; 
	public static final long CONFIG_FILE_POLL_PERIOD_IN_MS = 5000;
	
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
		initializeConfiguration();
		console = new IPCManagerConsole(this);
		apService = new APServiceImpl(this);
		executorService.execute(console);
		log.debug("IPC Manager started");
	}
	
	private void initializeConfiguration(){
		//Read config file
		RINAConfiguration rinaConfiguration = readConfigurationFile();
		RINAConfiguration.setConfiguration(rinaConfiguration);
		
		//Start thread that will look for config file changes
		Runnable configFileChangeRunnable = new Runnable(){
			private long currentLastModified = 0;
			private RINAConfiguration rinaConfiguration = null;

			public void run(){
				File file = new File(CONFIG_FILE_LOCATION);

				while(true){
					if (file.lastModified() > currentLastModified) {
						log.debug("Configuration file changed, loading the new content");
						currentLastModified = file.lastModified();
						rinaConfiguration = readConfigurationFile();
						RINAConfiguration.setConfiguration(rinaConfiguration);
					}
					try {
						Thread.sleep(CONFIG_FILE_POLL_PERIOD_IN_MS);
					}catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		};

		executorService.execute(configFileChangeRunnable);
	}
	
	private RINAConfiguration readConfigurationFile(){
		try {
    		ObjectMapper objectMapper = new ObjectMapper();
    		RINAConfiguration rinaConfiguration = (RINAConfiguration) 
    			objectMapper.readValue(new FileInputStream(CONFIG_FILE_LOCATION), RINAConfiguration.class);
    		log.info("Read configuration file");
    		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    		objectMapper.writer(new DefaultPrettyPrinter()).writeValue(outputStream, rinaConfiguration);
    		log.info(outputStream.toString());
    		return rinaConfiguration;
    	} catch (Exception ex) {
    		log.error(ex);
    		ex.printStackTrace();
    		log.debug("Could not find the main configuration file.");
    		return null;
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
		createInitialProcesses();
	}
	
	/**
	 * Create the initial IPC Processes as specified in the configuration file (if any)
	 */
	private void createInitialProcesses(){
		RINAConfiguration rinaConfiguration = RINAConfiguration.getInstance();
		if (rinaConfiguration.getIpcProcessesToCreate() == null){
			return;
		}
		
		IPCProcessToCreate currentProcess = null;
		for(int i=0; i<rinaConfiguration.getIpcProcessesToCreate().size(); i++){
			currentProcess = rinaConfiguration.getIpcProcessesToCreate().get(i);
			try{
				this.createIPCProcess(currentProcess.getApplicationProcessName(), 
						currentProcess.getApplicationProcessInstance(), 
						currentProcess.getDifName(), currentProcess.getNeighbors());
			}catch(Exception ex){
				log.error(ex);
			}
		}
	}

	public void createIPCProcess(String apName, String apInstance, String difName, List<Neighbor> neighbors) throws Exception{
		IPCProcess ipcProcess = ipcProcessFactory.createIPCProcess(apName, apInstance);
		ipcProcess.setIPCManager(this);
		if (difName != null){
			DIFConfiguration difConfiguration = RINAConfiguration.getInstance().getDIFConfiguration(difName);
			if (difConfiguration == null){
				throw new Exception("Unrecognized DIF name: "+difName);
			}
			
			KnownIPCProcessConfiguration ipcProcessConfiguration = 
				RINAConfiguration.getInstance().getIPCProcessConfiguration(apName);
			if (ipcProcessConfiguration == null){
				throw new Exception("Unrecoginzed IPC Process Name: "+apName);
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
			
			if (neighbors != null){
				ribDaemon.create(Neighbor.NEIGHBOR_SET_RIB_OBJECT_CLASS, 
						Neighbor.NEIGHBOR_SET_RIB_OBJECT_NAME, 
						(Neighbor[]) neighbors.toArray(new Neighbor[neighbors.size()]));
			}
			
			ribDaemon.start(RIBObjectNames.OPERATIONAL_STATUS_RIB_OBJECT_CLASS, 
					RIBObjectNames.OPERATIONAL_STATUS_RIB_OBJECT_NAME);

			RMT rmt = (RMT) ipcProcess.getIPCProcessComponent(BaseRMT.getComponentName());
			rmt.startListening();
		}
	}
	
	public void destroyIPCProcesses(String apName, String apInstance) throws Exception{
		ipcProcessFactory.destroyIPCProcess(apName, apInstance);
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

	public List<String> getPrintedRIB(String apName, String apInstance) throws Exception{
		IPCProcess ipcProcess = ipcProcessFactory.getIPCProcess(apName, apInstance);
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
	
	public void enroll(String sourceAPName, String sourceAPInstance, String destAPName, String destAPInstance) throws Exception{
		IPCProcess ipcProcess = ipcProcessFactory.getIPCProcess(sourceAPName, sourceAPInstance);
		EnrollmentTask enrollmentTask = (EnrollmentTask) ipcProcess.getIPCProcessComponent(BaseEnrollmentTask.getComponentName());
		
		Neighbor neighbor = new Neighbor();
		neighbor.setApplicationProcessName(destAPName);
		neighbor.setApplicationProcessInstance(destAPInstance);

		enrollmentTask.initiateEnrollment(neighbor);
	}
	
	public void allocateFlow(String sourceAPName, String sourceAPInstance, String destAPName, String destAPInstance) throws Exception{
		IPCProcess ipcProcess = ipcProcessFactory.getIPCProcess(sourceAPName, sourceAPInstance);
		IPCService ipcService = (IPCService) ipcProcess;
		FlowService flowService = new FlowService();
		flowService.setDestinationAPNamingInfo(new ApplicationProcessNamingInfo(destAPName, destAPInstance));
		flowService.setSourceAPNamingInfo(new ApplicationProcessNamingInfo("console", "1"));
		ipcService.submitAllocateRequest(flowService);
	}
	
	public void writeDataToFlow(String sourceAPName, String sourceAPInstance, int portId, String data) throws Exception{
		IPCProcess ipcProcess = ipcProcessFactory.getIPCProcess(sourceAPName, sourceAPInstance);
		IPCService ipcService = (IPCService) ipcProcess;
		ipcService.submitTransfer(portId, data.getBytes());
	}
	
	public void deallocateFlow(String sourceAPName, String sourceAPInstance, int portId) throws Exception{
		IPCProcess ipcProcess = ipcProcessFactory.getIPCProcess(sourceAPName, sourceAPInstance);
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
