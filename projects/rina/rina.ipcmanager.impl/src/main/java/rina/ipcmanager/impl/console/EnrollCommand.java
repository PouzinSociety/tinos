package rina.ipcmanager.impl.console;

import rina.ipcmanager.impl.IPCManagerImpl;

/**
 * The command to create a new IPC Process
 * @author eduardgrasa
 *
 */
public class EnrollCommand extends ConsoleCommand{

	public static final String ID = "enroll";
	private static final String USAGE = "enroll sourceapplicationprocessname destinationapplicationprocessname";
	
	/**
	 * Required parameter
	 */
	private String sourceApplicationProcessName = null;
	
	/**
	 * Required parameter
	 */
	private String destinationApplicationProcessName = null;
	
	public EnrollCommand(IPCManagerImpl ipcManagerImpl){
		super(ID, ipcManagerImpl);
	}
	
	@Override
	public String execute(String[] splittedCommand) {
		if (splittedCommand.length != 3){
			return "Wrong number of parameters. Usage: "+USAGE;
		}
		
		sourceApplicationProcessName = splittedCommand[1];
		destinationApplicationProcessName = splittedCommand[2];
		
		try{
			this.getIPCManagerImpl().enroll(sourceApplicationProcessName, destinationApplicationProcessName);
			return "Enrollment process started successfully";
		}catch(Exception ex){
			return "Problems starting the enrollment process " +ex.getMessage();
		}
	}

}
