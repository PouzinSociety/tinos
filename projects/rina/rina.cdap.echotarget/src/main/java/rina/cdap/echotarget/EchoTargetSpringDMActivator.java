package rina.cdap.echotarget;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * When the bundle is deployed in an OSGi container (such as the TINOS environment), this bundle 
 * will load and start a CDAP Echo Server
 * @author eduardgrasa
 *
 */
public class EchoTargetSpringDMActivator {
	
	/**
	 * The thread pool implementation
	 */
	private ExecutorService executorService = null;
	
	/**
	 * Runs a CDAP echo server in a separate thread
	 */
	public EchoTargetSpringDMActivator(){
		this.executorService = Executors.newFixedThreadPool(2);
		CDAPServer cdapEchoServer = CDAPServer.getNewInstance(CDAPServer.DEFAULT_ECHO_PORT, CDAPWorkerFactory.ECHO_WORKER);
		CDAPServerRunner cdapEchoServerRunner = new CDAPServerRunner(cdapEchoServer);
		executorService.execute(cdapEchoServerRunner);
	}
	
	/**
	 * Runs a CDAP echo Server as part of a standalone Java execution
	 * @param args
	 */
	public static void main(String args[]){
		CDAPServer cdapEchoServer = CDAPServer.getNewInstance(CDAPServer.DEFAULT_ECHO_PORT, CDAPWorkerFactory.ECHO_WORKER);
		cdapEchoServer.run();
	}

}
