package rina.configuration;

import java.util.List;

/**
 * The global configuration of the RINA software
 * @author eduardgrasa
 *
 */
public class RINAConfiguration {
	
	/**
	 * The local software configuration (ports, timeouts, ...)
	 */
	private LocalConfiguration localConfiguration = null;
	
	/**
	 * The list of IPC Process to create when the software starts
	 */
	private List<IPCProcessToCreate> ipcProcessesToCreate = null;
	
	/**
	 * The configurations of zero or more DIFs
	 */
	private List<DIFConfiguration> difConfigurations = null;
	
	/**
	 * The data of the known IPC Process (hostname, ports, apname, address) 
	 * that can potentially be members of the DIFs I know
	 */
	private List<KnownIPCProcessConfiguration> knownIPCProcessConfigurations = null;
	
	/**
	 * The address prefixes, assigned to different organizations
	 */
	private List<AddressPrefixConfiguration> addressPrefixes = null;

	/**
	 * The single instance of this class
	 */
	private static RINAConfiguration instance = null;
	
	public static void setConfiguration(RINAConfiguration rinaConfiguration){
		instance = rinaConfiguration;
	}
	
	public static RINAConfiguration getInstance(){
		return instance;
	}
	
	public LocalConfiguration getLocalConfiguration() {
		return localConfiguration;
	}

	public List<IPCProcessToCreate> getIpcProcessesToCreate() {
		return ipcProcessesToCreate;
	}

	public void setIpcProcessesToCreate(
			List<IPCProcessToCreate> ipcProcessesToCreate) {
		this.ipcProcessesToCreate = ipcProcessesToCreate;
	}

	public void setLocalConfiguration(LocalConfiguration localConfiguration) {
		this.localConfiguration = localConfiguration;
	}

	public List<DIFConfiguration> getDifConfigurations() {
		return difConfigurations;
	}

	public void setDifConfigurations(List<DIFConfiguration> difConfigurations) {
		this.difConfigurations = difConfigurations;
	}

	public List<KnownIPCProcessConfiguration> getKnownIPCProcessConfigurations() {
		return knownIPCProcessConfigurations;
	}

	public void setKnownIPCProcessConfigurations(
			List<KnownIPCProcessConfiguration> knownIPCProcessConfigurations) {
		this.knownIPCProcessConfigurations = knownIPCProcessConfigurations;
	}

	public List<AddressPrefixConfiguration> getAddressPrefixes() {
		return addressPrefixes;
	}

	public void setAddressPrefixes(List<AddressPrefixConfiguration> addressPrefixes) {
		this.addressPrefixes = addressPrefixes;
	}
	
	/**
	 * Return the configuration of the DIF named "difName" if it is known, null 
	 * otherwise
	 * @param difName
	 * @return
	 */
	public DIFConfiguration getDIFConfiguration(String difName){
		if (difConfigurations == null){
			return null;
		}
		
		for(int i=0; i<difConfigurations.size(); i++){
			if (difConfigurations.get(i).getDifName().equals(difName)){
				return difConfigurations.get(i);
			}
		}
		
		return null;
	}
	
	/**
	 * Return the configuration of the IPC process named "apName" if it is known, 
	 * null otherwise
	 * @param apName
	 * @return
	 */
	public KnownIPCProcessConfiguration getIPCProcessConfiguration(String apName){
		if (knownIPCProcessConfigurations == null){
			return null;
		}
		
		for(int i=0; i<knownIPCProcessConfigurations.size(); i++){
			if (knownIPCProcessConfigurations.get(i).getApName().equals(apName)){
				return knownIPCProcessConfigurations.get(i);
			}
		}
		
		return null;
	}
	
	/**
	 * Get the Flow Allocator port number of the IPC Process named "apName"
	 * @param apName
	 * @return
	 */
	public int getFlowAllocatorPortNumber(String apName){
		KnownIPCProcessConfiguration ipcConf = this.getIPCProcessConfiguration(apName);
		if (ipcConf != null){
			return ipcConf.getFlowAllocatorPortNumber();
		}else{
			return this.getLocalConfiguration().getFlowAllocatorPort();
		}
	}
	
	/**
	 * Return the configuration of the IPC process whose address is "address" if it is known, 
	 * null otherwise
	 * @param address
	 * @return
	 */
	public KnownIPCProcessConfiguration getIPCProcessConfiguration(long address){
		if (knownIPCProcessConfigurations == null){
			return null;
		}
		
		for(int i=0; i<knownIPCProcessConfigurations.size(); i++){
			if (knownIPCProcessConfigurations.get(i).getAddress() == address){
				return knownIPCProcessConfigurations.get(i);
			}
		}
		
		return null;
	}
	
	public IPCProcessToCreate getIPCProcessToCreate(String apName, String apInstance){
		IPCProcessToCreate result = null;
		
		for(int i=0; i<this.getIpcProcessesToCreate().size(); i++){
			result = this.getIpcProcessesToCreate().get(i);
			if (result.getApplicationProcessName().equals(apName) && 
					result.getApplicationProcessInstance().equals(apInstance)){
				return result;
			}
		}
		
		return null;
	}
	
	/**
	 * Get the Flow Allocator port number of the IPC Process whose address is "address"
	 * @param address
	 * @return
	 */
	public int getFlowAllocatorPortNumber(long address){
		KnownIPCProcessConfiguration ipcConf = this.getIPCProcessConfiguration(address);
		if (ipcConf != null){
			return ipcConf.getFlowAllocatorPortNumber();
		}else{
			return this.getLocalConfiguration().getFlowAllocatorPort();
		}
	}
	
	/**
	 * Get the address prefix that corresponds to the application process name of the 
	 * IPC Process. Return -1 if there is no matching.
	 * @param ipcProcessName
	 * @return the address prefix
	 */
	public long getAddressPrefixConfiguration(String apName){
		if (this.getAddressPrefixes() == null){
			return -1;
		}
		
		for(int i=0; i<this.getAddressPrefixes().size(); i++){
			if (apName.indexOf(this.getAddressPrefixes().get(i).getOrganization()) != -1){
				return this.getAddressPrefixes().get(i).getAddressPrefix();
			}
		}
		
		return -1;
	}
}
