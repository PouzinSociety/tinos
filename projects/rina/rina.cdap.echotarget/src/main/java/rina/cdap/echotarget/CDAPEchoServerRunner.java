package rina.cdap.echotarget;

/**
 * Runs a CDAP Echo Server
 * @author eduardgrasa
 *
 */
public class CDAPEchoServerRunner implements Runnable{

	private CDAPEchoServer cdapEchoServer = null;
	
	public CDAPEchoServerRunner(CDAPEchoServer cdapEchoServer){
		this.cdapEchoServer = cdapEchoServer;
	}
	
	public void run(){
		cdapEchoServer.run();
	}
}
