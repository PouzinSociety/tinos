package rina.ipcmanager.impl.console;

import rina.ipcmanager.impl.IPCManagerImpl;

/**
 * The command to create a new IPC Process
 * @author eduardgrasa
 *
 */
public class DestroyIPCProcessCommand extends ConsoleCommand{

	public static final String ID = "destroyipcprocess";
	private static final String USAGE = "destroyipcprocess applicationprocessname applicationprocessInstace";
	
	/**
	 * Required parameter
	 */
	private String applicationProcessName = null;
	
	/**
	 * Optional parameter, can be 
	 */
	private String applicationProcessInstance = null;
	
	public DestroyIPCProcessCommand(IPCManagerImpl ipcManagerImpl){
		super(ID, ipcManagerImpl);
	}
	
	@Override
	public String execute(String[] splittedCommand) {
		if (splittedCommand.length != 3){
			return "Wrong number of parameters. Usage: "+USAGE;
		}
		
		applicationProcessName = splittedCommand[1];
		applicationProcessInstance = splittedCommand[2];
		
		try{
			this.getIPCManagerImpl().destroyIPCProcesses(applicationProcessName, applicationProcessInstance);
			return "IPC Process destroyed successfully";
		}catch(Exception ex){
			return "Problems destroying IPC Process. " +ex.getMessage();
		}
	}

}
