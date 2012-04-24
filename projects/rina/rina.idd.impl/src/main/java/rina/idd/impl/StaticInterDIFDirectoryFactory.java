package rina.idd.impl;

import rina.idd.api.InterDIFDirectory;
import rina.idd.api.InterDIFDirectoryFactory;
import rina.ipcmanager.api.IPCManager;

public class StaticInterDIFDirectoryFactory implements InterDIFDirectoryFactory{

	public InterDIFDirectory createIDD(IPCManager ipcManager) {
		return new StaticInterDIFDirectory();
	}

}
