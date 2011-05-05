package rina.cdap.echotarget;

/**
 * Runs a CDAP Echo Server
 * @author eduardgrasa
 *
 */
public class CDAPServerRunner implements Runnable{

	private CDAPServer cdapServer = null;
	
	public CDAPServerRunner(CDAPServer cdapEchoServer){
		this.cdapServer = cdapEchoServer;
	}
	
	public void run(){
		cdapServer.run();
	}
}
