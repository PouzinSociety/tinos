package rina.shimipcprocess.ip.flowallocator;

import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.APService;
import rina.shimipcprocess.ip.TCPServer;
import rina.shimipcprocess.ip.UDPServer;

public class ApplicationRegistration {
	
	private ApplicationProcessNamingInfo apNamingInfo = null;
	private APService applicationCallback = null;
	private TCPServer tcpServer = null;
	private UDPServer udpServer = null;
	private int portNumber = 0;
	
	public ApplicationProcessNamingInfo getApNamingInfo() {
		return apNamingInfo;
	}
	
	public void setApNamingInfo(ApplicationProcessNamingInfo apNamingInfo) {
		this.apNamingInfo = apNamingInfo;
	}
	
	public APService getApplicationCallback() {
		return applicationCallback;
	}

	public void setApplicationCallback(APService applicationCallback) {
		this.applicationCallback = applicationCallback;
	}

	public TCPServer getTcpServer() {
		return tcpServer;
	}
	
	public void setTcpServer(TCPServer tcpServer) {
		this.tcpServer = tcpServer;
	}
	
	public UDPServer getUdpServer() {
		return udpServer;
	}
	
	public void setUdpServer(UDPServer udpServer) {
		this.udpServer = udpServer;
	}
	
	public int getPortNumber() {
		return portNumber;
	}
	
	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}
	
}
