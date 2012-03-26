package rina.ipcmanager.impl.console;

import rina.ipcmanager.impl.IPCManagerImpl;

/**
 * The command to create a new IPC Process
 * @author eduardgrasa
 *
 */
public class DeallocateFlowCommand extends ConsoleCommand{

	public static final String ID = "deallocateflow";
	private static final String USAGE = "deallocateflow sourceipcprocessname portId";
	
	/**
	 * Required parameter
	 */
	private String sourceIPCProcessName = null;
	
	/**
	 * Required parameter
	 */
	private int portId = 0;
	
	public DeallocateFlowCommand(IPCManagerImpl ipcManagerImpl){
		super(ID, ipcManagerImpl);
	}
	
	@Override
	public String execute(String[] splittedCommand) {
		if (splittedCommand.length != 3){
			return "Wrong number of parameters. Usage: "+USAGE;
		}
		
		sourceIPCProcessName = splittedCommand[1];
		portId = Integer.parseInt(splittedCommand[2]);
		
		try{
			this.getIPCManagerImpl().deallocateFlow(sourceIPCProcessName, portId);
			return "Deallocate Flow process started successfully";
		}catch(Exception ex){
			return "Problems starting the deallocate flow process " +ex.getMessage();
		}
	}

}
