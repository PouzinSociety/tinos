package rina.ipcmanager.impl.console;

import rina.ipcmanager.impl.IPCManagerImpl;

/**
 * The command to create a new IPC Process
 * @author eduardgrasa
 *
 */
public class CreateIPCProcessCommand extends ConsoleCommand{
	
	public static final String ID = "createipcprocess";
	private static final String USAGE = "createipcprocess applicationprocessname [difname]";
	
	/**
	 * Required parameter
	 */
	private String difName = null;
	
	/**
	 * Required parameter
	 */
	private String applicationProcessName = null;
	
	public CreateIPCProcessCommand(IPCManagerImpl ipcManagerImpl){
		super(ID, ipcManagerImpl);
	}
	
	@Override
	public String execute(String[] splittedCommand) {
		if (splittedCommand.length > 3 || splittedCommand.length<2){
			return "Wrong number of parameters. Usage: "+USAGE;
		}
		
		applicationProcessName = splittedCommand[1];
		if (splittedCommand.length == 3){
			difName = splittedCommand[2];
		}
		
		try{
			this.getIPCManagerImpl().createIPCProcess(applicationProcessName, difName);
			return "IPC Process created successfully";
		}catch(Exception ex){
			ex.printStackTrace();
			return "Problems creating IPC Process. " +ex.getMessage();
		}
	}

}
