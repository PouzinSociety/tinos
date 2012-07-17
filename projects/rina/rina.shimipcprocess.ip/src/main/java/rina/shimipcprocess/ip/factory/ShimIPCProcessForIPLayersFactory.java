package rina.shimipcprocess.ip.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.cdap.api.CDAPSessionManagerFactory;
import rina.configuration.DirectoryEntry;
import rina.configuration.ExpectedApplicationRegistration;
import rina.configuration.IPCProcessToCreate;
import rina.configuration.RINAConfiguration;
import rina.configuration.DIFConfiguration;
import rina.delimiting.api.DelimiterFactory;
import rina.encoding.api.EncoderFactory;
import rina.ipcmanager.api.IPCManager;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcprocess.api.IPCProcessFactory;
import rina.shimipcprocess.ip.ShimIPCProcessForIPLayers;

public class ShimIPCProcessForIPLayersFactory implements IPCProcessFactory{
	
	public static final String HOSTNAME = "hostname";
	public static final String DIFNAME = "difname";
	
	private IPCManager ipcManager = null;
	
	/**
	 * All the existing IPC processes in this system
	 */
	private Map<String, IPCProcess> ipcProcesses = null;
	
	/**
	 * Factory of delimiters
	 */
	private DelimiterFactory delimiterFactory = null;
	
	public ShimIPCProcessForIPLayersFactory(){
		this.ipcProcesses = new HashMap<String, IPCProcess>();
	}
	
	public void setDelimiterFactory(DelimiterFactory delimiterFactory) {
		this.delimiterFactory = delimiterFactory;
	}
	
	public DelimiterFactory getDelimiterFactory(){
		return this.delimiterFactory;
	}

	/**
	 * Creates a new IPC process
	 * @param apName the application process name of this IPC process
	 * @param apInstance the application process instance of this IPC process
	 */
	public IPCProcess createIPCProcess(String apName, String apInstance, RINAConfiguration config) throws Exception{
		try{
		if (ipcProcesses.get(apName+"-"+apInstance) != null){
			throw new Exception("An IPC Process with this name/instance pair already exists in this system");
		}

		ApplicationProcessNamingInfo apNamingInfo = new ApplicationProcessNamingInfo();
		apNamingInfo.setApplicationProcessName(apName);
		apNamingInfo.setApplicationProcessInstance(apInstance);
		ShimIPCProcessForIPLayers ipcProcess = null;
		
		IPCProcessToCreate ipcProcessToCreate = config.getIPCProcessToCreate(apName, apInstance);
		if (ipcProcessToCreate == null){
			throw new Exception("Could not find the configuration data to create IPC Process "+apName+" "+apInstance);
		}
		
		if (this.ipcManager != null && this.delimiterFactory != null){
			ipcProcess = new ShimIPCProcessForIPLayers(apNamingInfo, ipcProcessToCreate.getHostname(), ipcProcessToCreate.getDifName(), 
					delimiterFactory.createDelimiter(DelimiterFactory.DIF), this.ipcManager);
			ipcProcess.setIPCManager(this.ipcManager);
			
			DIFConfiguration shimIPDIFconfiguration = (DIFConfiguration) config.getDIFConfiguration(ipcProcessToCreate.getDifName());
			ExpectedApplicationRegistration expectedRegistration = null;
			for(int i=0; i<shimIPDIFconfiguration.getExpectedApplicationRegistrations().size(); i++){
				expectedRegistration = shimIPDIFconfiguration.getExpectedApplicationRegistrations().get(i);
				ipcProcess.addExpectedApplicationRegistration(
						new ApplicationProcessNamingInfo(expectedRegistration.getApplicationProcessName(), expectedRegistration.getApplicationProcessInstance()), 
						expectedRegistration.getSocketPortNumber());
			}
			
			DirectoryEntry directoryEntry = null;
			for(int i=0; i<shimIPDIFconfiguration.getDirectory().size(); i++){
				directoryEntry = shimIPDIFconfiguration.getDirectory().get(i);
				ipcProcess.addOrModifyDirectoryEntry(
						new ApplicationProcessNamingInfo(directoryEntry.getApplicationProcessName(), directoryEntry.getApplicationProcessInstance()), 
						directoryEntry.getHostname(), directoryEntry.getSocketPortNumber());
			}
		}else{
			throw new Exception("IPC Manager or Delimiter factory are null");
		}
		
		ipcProcesses.put(apName+"-"+apInstance, ipcProcess);
		return ipcProcess;
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}
	}

	public void destroyIPCProcess(IPCProcess ipcProcess) throws Exception {
		try{
			this.destroyIPCProcess(ipcProcess.getApplicationProcessName(), 
					ipcProcess.getApplicationProcessInstance());
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void destroyIPCProcess(String apName, String apInstance) throws Exception {
		if (ipcProcesses.get(apName+"-"+apInstance) == null){
			throw new Exception("An IPC Process with this naming information does not exist in this system");
		}
		
		IPCProcess ipcProcess = ipcProcesses.remove(apName+"-"+apInstance);
		ipcProcess.destroy();
	}

	public IPCProcess getIPCProcess(String apName, String apInstance) {
		return ipcProcesses.get(apName+"-"+apInstance);
	}

	public IPCProcess getIPCProcessBelongingToDIF(String difName) {
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

	public List<String> listDIFNames() {
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

	public List<IPCProcess> listIPCProcesses() {
		List<IPCProcess> result = new ArrayList<IPCProcess>();
		Iterator<String> iterator = ipcProcesses.keySet().iterator();
		while(iterator.hasNext()){
			result.add(ipcProcesses.get(iterator.next()));
		}
		
		return result;
	}

	public void setIPCManager(IPCManager ipcManager) {
		this.ipcManager = ipcManager;
	}

	public CDAPSessionManagerFactory getCDAPSessionManagerFactory() {
		return null;
	}

	public EncoderFactory getEncoderFactory() {
		return null;
	}

}
