package rina.applibrary.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rina.cdap.api.CDAPSessionManager;
import rina.cdap.impl.CDAPSessionManagerFactoryImpl;
import rina.cdap.impl.googleprotobuf.GoogleProtocolBufWireMessageProviderFactory;
import rina.delimiting.api.Delimiter;
import rina.delimiting.api.DelimiterFactory;
import rina.delimiting.impl.DelimiterFactoryImpl;
import rina.encoding.api.Encoder;
import rina.encoding.api.EncoderFactory;
import rina.encoding.impl.googleprotobuf.GPBEncoderFactory;

/**
 * Creates classes that are required to interact with the local RINA software 
 * @author eduardgrasa
 *
 */
public class RINAFactory {
	
	/**
	 * The default port to connect to the local RINA software
	 */
	public static final int DEFAULT_PORT = 32771;
	
	private static CDAPSessionManagerFactoryImpl cdapSessionManagerFactory = null;
	private static DelimiterFactory delimiterFactory = new DelimiterFactoryImpl();
	private static EncoderFactory encoderFactory = new GPBEncoderFactory();
	
	/**
	 * Centralize the executor service for all the classes using the RINA Library in this 
	 * Java Virtual Machine
	 */
	private static ExecutorService executorService = null;
	
	public static synchronized CDAPSessionManager getCDAPSessionManagerInstance(){
		if (cdapSessionManagerFactory == null){
			cdapSessionManagerFactory = new CDAPSessionManagerFactoryImpl();
			cdapSessionManagerFactory.setWireMessageProviderFactory(new GoogleProtocolBufWireMessageProviderFactory());
		}
		
		return cdapSessionManagerFactory.createCDAPSessionManager();
	}
	
	public static synchronized Delimiter getDelimiterInstance(){
		return delimiterFactory.createDelimiter(DelimiterFactory.DIF);
	}
	
	public static synchronized Encoder getEncoderInstance(){
		return encoderFactory.createEncoderInstance();
	}
	
	/**
	 * Execute a runnable. The threads created will be daemon threads, 
	 * so that a program can exit when the main class completes.
	 * @param runnable
	 */
	public static synchronized void execute(Runnable runnable){
		if (executorService == null){
			executorService = Executors.newCachedThreadPool();
		}
		
		executorService.execute(runnable);
	}

}
