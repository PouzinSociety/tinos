package rina.ipcprocess.api;

import java.util.List;

import rina.cdap.api.CDAPSessionManagerFactory;
import rina.delimiting.api.DelimiterFactory;
import rina.encoding.api.EncoderFactory;
import rina.ipcservice.api.ApplicationProcessNamingInfo;

/**
 * Creates, stores and destroys instances of IPC processes
 * @author eduardgrasa
 *
 */
public interface IPCProcessFactory {
	
	/**
	 * Creates a new IPC process
	 * TODO add more stuff probably
	 * @param namingInfo the name of this new IPC process
	 */
	public IPCProcess createIPCProcess(ApplicationProcessNamingInfo namingInfo) throws Exception;
	
	/**
	 * Destroys an existing IPC process
	 * TODO add more stuff probably
	 * @param namingInfo the name of this IPC process
	 */
	public void destroyIPCProcess(ApplicationProcessNamingInfo namingInfo) throws Exception;
	
	/**
	 * Destroys an existing IPC process
	 * TODO add more stuff probably
	 * @param IPCService ipcProcess
	 */
	public void destroyIPCProcess(IPCProcess ipcProcess) throws Exception;
	
	/**
	 * Get an existing IPC process
	 * TODO add more stuff probably
	 * @param namingInfo the name of this new IPC process
	 */
	public IPCProcess getIPCProcess(ApplicationProcessNamingInfo namingInfo);
	
	/**
	 * Return the IPC process that is a member of the DIF called "difname"
	 * @param difname The name of the DIF
	 * @return
	 */
	public IPCProcess getIPCProcessBelongingToDIF(String difname);
	
	/**
	 * Return a list of the existing IPC processes
	 * @return
	 */
	public List<IPCProcess> listIPCProcesses();
	
	/**
	 * Return a list of the names of the DIFs currently available in the system
	 * @return
	 */
	public List<String> listDIFNames();
	
	public CDAPSessionManagerFactory getCDAPSessionManagerFactory();
	public EncoderFactory getEncoderFactory();
	public DelimiterFactory getDelimiterFactory();
}
