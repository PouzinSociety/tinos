package rina.configuration;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The global configuration of the RINA software
 * @author eduardgrasa
 *
 */
public class RINAConfiguration {
	
	public static final String CONFIG_FILE_LOCATION = "config/rina/config.rina"; 
	
	private static final Log log = LogFactory.getLog(RINAConfiguration.class);
	
	/**
	 * The local software configuration (ports, timeouts, ...)
	 */
	private LocalConfiguration localConfiguration = null;
	
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
	
	/**
	 * Prevent instantiation
	 */
	protected RINAConfiguration(){
	}
	
	public static RINAConfiguration getInstance(){
		if (instance == null){
			instance = readConfigurationFile();
		}
		
		return instance;
	}
	
	/**
	 * Read the configuration parameters from the properties file
	 */
	private static RINAConfiguration readConfigurationFile(){
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
    		log.debug("Could not find the main configuration file. Stoping the system!");
    		System.exit(-1);
    		return null;
        }
	}
	
	public LocalConfiguration getLocalConfiguration() {
		return localConfiguration;
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
	 * Get the RMT port number of the IPC Process named "apName"
	 * @param apName
	 * @return
	 */
	public int getRMTPortNumber(String apName){
		KnownIPCProcessConfiguration ipcConf = this.getIPCProcessConfiguration(apName);
		if (ipcConf != null){
			return ipcConf.getRmtPortNumber();
		}else{
			return this.getLocalConfiguration().getRmtPort();
		}
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
	
	/**
	 * Get the RMT port number of the IPC Process whose address is "address"
	 * @param address
	 * @return
	 */
	public int getRMTPortNumber(long address){
		KnownIPCProcessConfiguration ipcConf = this.getIPCProcessConfiguration(address);
		if (ipcConf != null){
			return ipcConf.getRmtPortNumber();
		}else{
			return this.getLocalConfiguration().getRmtPort();
		}
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
}
