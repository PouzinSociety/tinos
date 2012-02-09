package rina.flowallocator.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.DAFMember;
import rina.cdap.api.BaseCDAPSessionManager;
import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSessionManager;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.IPCException;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;
import rina.rmt.api.BaseRMT;
import rina.rmt.api.RMT;

/**
 * Different usefull functions
 * @author eduardgrasa
 *
 */
public class Utils {
	
	private static final Log log = LogFactory.getLog(Utils.class);
	
	/**
	 * Maps the destination address to the port id of the N-1 flow that this IPC process can use
	 * to reach the IPC process identified by the address
	 * @return
	 */
	public static synchronized int mapAddressToPortId(long address, IPCProcess ipcProcess) throws IPCException{
		RIBDaemon ribDaemon = (RIBDaemon) ipcProcess.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		CDAPSessionManager cdapSessionManager = (CDAPSessionManager) ipcProcess.getIPCProcessComponent(BaseCDAPSessionManager.getComponentName());
		List<RIBObject> members = null;
		DAFMember dafMember = null;
		int rmtPortId = 0;
		
		try{
			members = ribDaemon.read(null, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.ENROLLMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.MEMBERS ,0).getChildren();
			
			for(int i=0; i<members.size(); i++){
				dafMember = (DAFMember) members.get(i).getObjectValue();
				if (dafMember.getSynonym() == address){
					rmtPortId = cdapSessionManager.getPortId(dafMember.getApplicationProcessName(), dafMember.getApplicationProcessInstance());
					break;
				}
			}
			
			if (rmtPortId == 0){
				String message = "Could not find the application process name of the IPC process whose synonym is "+address;
				log.error(message);
				throw new IPCException(5, message);
			}
		}catch(RIBDaemonException ex){
			log.error(ex);
			throw new IPCException(5, ex.getMessage());
		}catch(CDAPException ex){
			log.error(ex);
			throw new IPCException(5, ex.getMessage());
		}
		
		return rmtPortId;
	}
	
	/**
	 * Returns the IP Address of a remote IPC Process, given its address (only valid for the RINA on top of TCP/IP prototype).
	 * @param ipcProcessAddress
	 * @param ipcProcess
	 * @return the ip address of the remote IPC process
	 * @throws IPCException if no matching entry is found
	 */
	public static synchronized String getRemoteIPCProcessIPAddress(long ipcProcessAddress, IPCProcess ipcProcess) throws IPCException{
		ApplicationProcessNamingInfo namingInfo = Utils.getRemoteIPCProcessNamingInfo(ipcProcessAddress, ipcProcess);
		RMT rmt = (RMT) ipcProcess.getIPCProcessComponent(BaseRMT.getComponentName());
		return rmt.getIPAddressFromApplicationNamingInformation(namingInfo.getApplicationProcessName(), namingInfo.getApplicationProcessInstance());
	}
	
	/**
	 * Returns the application process name and application process instance of a remote IPC process, given its address.
	 * @param ipcProcessAddress
	 * @param ipcProcess
	 * @return the application process naming information
	 * @throws IPCException if no matching entry is found
	 */
	public static synchronized ApplicationProcessNamingInfo getRemoteIPCProcessNamingInfo(long ipcProcessAddress, IPCProcess ipcProcess) throws IPCException{
		RIBDaemon ribDaemon = (RIBDaemon) ipcProcess.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		List<RIBObject> members = null;
		DAFMember dafMember = null;
		ApplicationProcessNamingInfo result = null;
		
		try{
			members = ribDaemon.read(null, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.ENROLLMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.MEMBERS ,0).getChildren();
			
			for(int i=0; i<members.size(); i++){
				dafMember = (DAFMember) members.get(i).getObjectValue();
				if (dafMember.getSynonym() == ipcProcessAddress){
					result = new ApplicationProcessNamingInfo(dafMember.getApplicationProcessName(), dafMember.getApplicationProcessInstance());
					return result;
				}
			}
			
			String message = "Could not find the application process name of the IPC process whose synonym is "+ipcProcessAddress;
			log.error(message);
			throw new IPCException(5, message);
		}catch(RIBDaemonException ex){
			log.error(ex);
			throw new IPCException(5, ex.getMessage());
		}
	}

}
