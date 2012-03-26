package rina.ipcmanager.impl.console;

import rina.ipcmanager.impl.IPCManagerImpl;

/**
 * The command to create a new IPC Process
 * @author eduardgrasa
 *
 */
public class AllocateFlowCommand extends ConsoleCommand{

	public static final String ID = "allocateflow";
	private static final String USAGE = "allocateflow sourceipcprocessname destinationapplicationprocessname";
	
	/**
	 * Required parameter
	 */
	private String sourceIPCProcessName = null;
	
	/**
	 * Required parameter
	 */
	private String destinationApplicationProcessName = null;
	
	public AllocateFlowCommand(IPCManagerImpl ipcManagerImpl){
		super(ID, ipcManagerImpl);
	}
	
	@Override
	public String execute(String[] splittedCommand) {
		if (splittedCommand.length != 3){
			return "Wrong number of parameters. Usage: "+USAGE;
		}
		
		sourceIPCProcessName = splittedCommand[1];
		destinationApplicationProcessName = splittedCommand[2];
		
		try{
			this.getIPCManagerImpl().allocateFlow(sourceIPCProcessName, destinationApplicationProcessName);
			return "Allocate Flow process started successfully";
		}catch(Exception ex){
			return "Problems starting the allocate flow process " +ex.getMessage();
		}
	}

}
