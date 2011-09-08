package rina.ipcmanager.impl.console;

import java.util.List;

import rina.ipcmanager.impl.IPCManagerImpl;

/**
 * The command to create a new IPC Process
 * @author eduardgrasa
 *
 */
public class ListIPCProcessesCommand extends ConsoleCommand{

	public static final String ID = "listipcprocesses";
	private static final String USAGE = "listipcprocesses";
	
	public ListIPCProcessesCommand(IPCManagerImpl ipcManagerImpl){
		super(ID, ipcManagerImpl);
	}
	
	@Override
	public String execute(String[] splittedCommand) {
		if (splittedCommand.length > 1){
			return "Wrong number of parameters. Usage: "+USAGE;
		}

		List<String> processes = this.getIPCManagerImpl().listIPCProcessesInformation();
		String modifier = "are";
		if (processes.size() == 1){
			modifier = "is";
		}
		String response = "There "+modifier+" currently "+processes.size()+" IPC proceses running in your system.\n";
		for(int i=0; i<processes.size(); i++){
			response = response + processes.get(i);
		}

		return response;
	}

}
