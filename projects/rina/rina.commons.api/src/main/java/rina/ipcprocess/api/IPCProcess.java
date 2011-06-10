package rina.ipcprocess.api;

import java.util.List;

import rina.applicationprocess.api.ApplicationProcess;
import rina.cdap.api.CDAPSessionManager;
import rina.delimiting.api.Delimiter;
import rina.efcp.api.DataTransferAE;
import rina.flowallocator.api.FlowAllocator;
import rina.ribdaemon.api.RIBDaemon;
import rina.rmt.api.RMT;
import rina.serialization.api.Serializer;

/**
 * Represents an IPC Process. Holds together the different components of the IPC 
 * process
 * @author eduardgrasa
 *
 */
public interface IPCProcess extends ApplicationProcess{
	
	public FlowAllocator getFlowAllocator();

	public void setFlowAllocator(FlowAllocator flowAllocator);
	
	public DataTransferAE getDataTransferAE();
	
	public void setDataTransferAE(DataTransferAE dataTransferAE);
	
	public RIBDaemon getRibDaemon();

	public void setRibDaemon(RIBDaemon ribDaemon);

	public RMT getRmt();
	
	public void setRmt(RMT rmt);
	
	public CDAPSessionManager getCDAPSessionManager();
	
	public void setCDAPSessionManager(CDAPSessionManager cdapSessionManager);
	
	public Serializer getSerializer();
	
	public void setSerializer(Serializer serializer);
	
	public Delimiter getDelimiter();
	
	public void setDelimiter(Delimiter delimiter);
	
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
