package rina.ipcmanager.impl.console;

import rina.ipcmanager.impl.IPCManagerImpl;

/**
 * The command to create a new IPC Process
 * @author eduardgrasa
 *
 */
public class CreateIPCProcessCommand extends ConsoleCommand{
	
	public static final String ID = "createipcprocess";
	private static final String USAGE = "createipcprocess applicationprocessname applicationProcessInstance [difname]";
	
	/**
	 * Optional parameter
	 */
	private String difName = null;
	
	/**
	 * Required parameter
	 */
	private String applicationProcessName = null;
	
	/**
	 * Required parameter
	 */
	private String applicationProcessInstance = null;
	
	public CreateIPCProcessCommand(IPCManagerImpl ipcManagerImpl){
		super(ID, ipcManagerImpl);
	}
	
	@Override
	public String execute(String[] splittedCommand) {
		if (splittedCommand.length > 4 || splittedCommand.length<3){
			return "Wrong number of parameters. Usage: "+USAGE;
		}
		
		applicationProcessName = splittedCommand[1];
		applicationProcessInstance = splittedCommand[2];
		if (splittedCommand.length == 4){
			difName = splittedCommand[3];
		}
		
		try{
			this.getIPCManagerImpl().createIPCProcess(applicationProcessName, applicationProcessInstance, difName, null);
			return "IPC Process created successfully";
		}catch(Exception ex){
			ex.printStackTrace();
			return "Problems creating IPC Process. " +ex.getMessage();
		}
	}

}
