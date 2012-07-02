package rina.ipcmanager.api;

import java.util.List;

import rina.cdap.api.CDAPSessionManagerFactory;
import rina.delimiting.api.DelimiterFactory;
import rina.encoding.api.EncoderFactory;
import rina.flowallocator.api.Flow;
import rina.flowallocator.api.FlowAllocatorInstance;
import rina.idd.api.InterDIFDirectoryFactory;
import rina.ipcprocess.api.IPCProcess;

/**
 * The IPC Manager is the component of a DAF that manages the local IPC resources. In its current implementation it 
 * manages IPC Processes (creates/destroys them), and serves as a broker between applications and IPC Processes. Applications 
 * can use the RINA library to establish a connection to the IPC Manager and interact with the RINA stack.
 * @author eduardgrasa
 *
 */
public interface IPCManager {
	
	/**
	 * Invoked by the Flow Allocator Instance when it receives a new create flow request. The IPCManager has to call the destination application 
	 * to see wether it accepts or not the flow allocation request.
	 * @param flow
	 * @param ipcProcess
	 */
	public void createFlowRequestMessageReceived(Flow flow, FlowAllocatorInstance flowAllocatorInstance);
	
	/**
	 * Setter for the InterDIFDirectoryFactory
	 * @param iddFactory
	 */
	public void setInterDIFDirectoryFactory(InterDIFDirectoryFactory idd);
	
	/**
	 * Executes a runnable in a thread. The IPCManager maintains a single thread pool 
	 * for all the RINA prototype
	 * @param runnable
	 */
	public void execute(Runnable runnable);
	
	public CDAPSessionManagerFactory getCDAPSessionManagerFactory();
	public DelimiterFactory getDelimiterFactory();
	public EncoderFactory getEncoderFactory();
	
	public List<IPCProcess> listIPCProcesses();
	public List<String> listDIFNames();
	public IPCProcess getIPCProcessBelongingToDIF(String difName);
}
