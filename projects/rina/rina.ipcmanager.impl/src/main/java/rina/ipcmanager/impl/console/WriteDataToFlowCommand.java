package rina.ipcmanager.impl.console;

import rina.ipcmanager.impl.IPCManagerImpl;

/**
 * The command to create a new IPC Process
 * @author eduardgrasa
 *
 */
public class WriteDataToFlowCommand extends ConsoleCommand {

	public static final String ID = "writedata";
	private static final String USAGE = "writedata ipcprocessname ipcprocessinstance portId data <data> <data> ...";
	
	/**
	 * Required parameter
	 */
	private String sourceIPCProcessName = null;
	
	/**
	 * Required parameter
	 */
	private String sourceIPCProcessInstance = null;
	
	/**
	 * Required parameter
	 */
	private int portId = 0;
	
	/**
	 * The data, required parameter
	 */
	private String data = null;
	
	public WriteDataToFlowCommand(IPCManagerImpl ipcManagerImpl){
		super(ID, ipcManagerImpl);
	}
	
	@Override
	public String execute(String[] splittedCommand) {
		if (splittedCommand.length < 5){
			return "Wrong number of parameters. Usage: "+USAGE;
		}
		
		sourceIPCProcessName = splittedCommand[1];
		sourceIPCProcessInstance = splittedCommand[2];
		portId = Integer.parseInt(splittedCommand[3]);
		
		data = "";
		for(int i=4; i<splittedCommand.length; i++){
			data = data + " " + splittedCommand[i];
		}
		
		try{
			((IPCManagerImpl)this.getIPCManagerImpl()).writeDataToFlow(
					sourceIPCProcessName, sourceIPCProcessInstance, portId, data);
			return "Data successfully written to flow "+portId;
		}catch(Exception ex){
			return "Problems writing data to flow" +portId;
		}
	}

}
