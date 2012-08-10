package rina.ribdaemon.impl;

/**
 * This class captures the information of an incoming management SDU delivered to 
 * this IPC Process, as well as the portId of the underlying N-1 flow throw which 
 * it was delivered
 * @author eduardgrasa
 *
 */
public class IncomingManagementSDU {

	private byte[] managementSDU = null;

	private int portId = 0;

	public byte[] getManagementSDU() {
		return managementSDU;
	}

	public void setManagementSDU(byte[] managementSDU) {
		this.managementSDU = managementSDU;
	}

	public int getPortId() {
		return portId;
	}

	public void setPortId(int portId) {
		this.portId = portId;
	}
}
