package rina.ipcprocess.api;

import java.util.List;

import rina.cdap.api.CDAPSessionFactory;
import rina.efcp.api.DataTransferAE;
import rina.flowallocator.api.FlowAllocator;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.RIBDaemon;
import rina.rmt.api.RMT;

/**
 * Represents an IPC Process. Holds together the different components of the IPC 
 * process
 * @author eduardgrasa
 *
 */
public interface IPCProcess {

	/**
	 * Return the naming information associated to this IPC process
	 * @return
	 */
	public ApplicationProcessNamingInfo getIPCProcessNamingInfo();
	
	/**
	 * The address of this IPC Process
	 * @return
	 */
	public byte[] getIPCProcessAddress();
	
	/**
	 * Set the naming information associated to this IPC process
	 * @return
	 */
	public void setIPCProcessNamingInfo(ApplicationProcessNamingInfo mamingInfo);
	
	public FlowAllocator getFlowAllocator();

	public void setFlowAllocator(FlowAllocator flowAllocator);
	
	public DataTransferAE getDataTransferAE();
	
	public void setDataTransferAE(DataTransferAE dataTransferAE);
	
	public RIBDaemon getRibDaemon();

	public void setRibDaemon(RIBDaemon ribDaemon);

	public RMT getRmt();
	
	public void setRmt(RMT rmt);
	
	public CDAPSessionFactory getCDAPSessionFactory();
	
	public void setCDAPSessionFactory(CDAPSessionFactory cdapSessionFactory);
	
	/**
	 * Lifecicle event, invoked to tell the IPC process it is about to be destroyed.
	 * The IPC Process implementation must do any necessary cleanup inside this 
	 * operation.
	 */
	public void destroy();
	
	/**
	 * Deliver a set of sdus to the application process bound to portId
	 * @param sdus
	 * @param portId
	 */
	public void deliverSDUsToApplicationProcess(List<byte[]> sdus, int portId);
	
	/**
	 * Call the applicationProcess deallocate.deliver operation
	 * @param portId
	 */
	public void deliverDeallocateRequestToApplicationProcess(int portId);
}
