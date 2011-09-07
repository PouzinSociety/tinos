package rina.ipcmanager.impl.console;

import rina.ipcmanager.impl.IPCManagerImpl;

/**
 * The command to create a new IPC Process
 * @author eduardgrasa
 *
 */
public class CreateIPCProcessCommand extends ConsoleCommand{

	public static final String ID = "createipcprocess";
	private static final String USAGE = "createipcprocess difName applicationprocessname [applicationprocessInstace]";
	
	/**
	 * Required parameter
	 */
	private String difName = null;
	
	/**
	 * Required parameter
	 */
	private String applicationProcessName = null;
	
	/**
	 * Optional parameter, can be 
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
		
		difName = splittedCommand[1];
		applicationProcessName = splittedCommand[2];
		if (splittedCommand.length == 4){
			applicationProcessInstance = splittedCommand[3];
		}else{
			applicationProcessInstance = "1";
		}
		
		try{
			this.getIPCManagerImpl().createIPCProcess(difName, applicationProcessName, applicationProcessInstance);
			return "IPC Process created successfully";
		}catch(Exception ex){
			return "Problems creating IPC Process. " +ex.getMessage();
		}
	}

}
