package rina.applibrary.api;

import java.util.List;

import rina.applibrary.impl.DefaultFlowImpl;

/**
 * Flows provide the interprocess communication (IPC) service between two or 
 * more application processes, allowing application processes to exchange data 
 * between them. Flows have an associated quality of service, requested by the
 * application process when creating the flow. 
 * @author eduardgrasa
 *
 */
public class Flow {
	
	/**
	 * The name of the source application of this flow (i.e. the application that requested
	 * the establishment of this flow)
	 */
	protected ApplicationProcess sourceApplication = null;
	
	/**
	 * The name of the destination application of this flow (i.e. the application that accepted 
	 * the establishment of this flow)
	 */
	protected ApplicationProcess destinationApplication = null;
	
	/**
	 * The quality of service requested for this flow. This is the service level 
	 * agreement negotiated between the application requesting the flow and the DIF
	 */
	protected QualityOfServiceSpecification qosSpec = null;
	
	/**
	 * The class to be called when a new SDU is received by this flow
	 */
	protected SDUListener sduListener = null;
	
	/**
	 * The class that does the work
	 */
	private FlowImpl flowImplementation = null;
	
	/**
	 * The flow implementation factory. It can be used 
	 * to change the behavior of the FlowImpl.
	 */
	private FlowImplFactory flowImplementationFactory = null;
	
	/**
	 * Will try to allocate a flow to the destination application process with the level of 
	 * service specified by qosSpec. SDUs received from the flow will be passed to the 
	 * sduListener class.
	 * 
	 * @param sourceApplication
	 * @param destinationApplication
	 * @param qosSpec
	 * @param sduListener
	 * @throws IPCException
	 */
	public Flow(ApplicationProcess sourceApplication, ApplicationProcess destinationApplication, 
			QualityOfServiceSpecification qosSpec, SDUListener sduListener) throws IPCException{
		this(sourceApplication, destinationApplication, qosSpec, sduListener, null);
	}
	
	/**
	 * Will try to allocate a flow to the destination application process with the level of 
	 * service specified by qosSpec. SDUs received from the flow will be passed to the 
	 * sduListener class. Clients that want to customize the behavior of the FlowImpl can 
	 * provide their own implementations via the FlowImplFactory.
	 * 
	 * @param sourceApplication
	 * @param destinationAppliation
	 * @param qosSpec
	 * @param sduListener
	 * @param flowImplFactory
	 * @throws IPCException
	 */
	public Flow(ApplicationProcess sourceApplication, ApplicationProcess destinationApplication, 
			QualityOfServiceSpecification qosSpec, SDUListener sduListener, FlowImplFactory flowImplFactory) throws IPCException{
		if (sourceApplication == null){
			IPCException flowException = new IPCException(IPCException.SOURCE_APPLICATION_NOT_SPECIFIED);
			flowException.setErrorCode(IPCException.SOURCE_APPLICATION_NOT_SPECIFIED_CODE);
		}
		sourceApplication.validate();
		this.sourceApplication = sourceApplication;
		
		if (destinationApplication == null){
			IPCException flowException = new IPCException(IPCException.DESTINATION_APPLICATION_NOT_SPECIFIED);
			flowException.setErrorCode(IPCException.DESTINATION_APPLICATION_NOT_SPECIFIED_CODE);
		}
		destinationApplication.validate();
		this.destinationApplication = destinationApplication;
		
		if (this.sduListener == null){
			IPCException flowException = new IPCException(IPCException.SDU_LISTENER_NOT_SPECIFIED);
			flowException.setErrorCode(IPCException.SDU_LISTENER_NOT_SPECIFIED_CODE);
		}
		
		this.qosSpec = qosSpec;
		this.flowImplementationFactory = flowImplFactory;
		
		this.createFlowImpl();
		
		flowImplementation.allocate(sourceApplication, destinationApplication, qosSpec, sduListener);
	}
	
	/**
	 * Creates the flow implementation
	 */
	private void createFlowImpl(){
		if (this.flowImplementationFactory != null){
			this.flowImplementation = this.flowImplementationFactory.createFlowImpl();
		}else{
			this.flowImplementation = new DefaultFlowImpl();
		}
	}
	
	/**
	 * Sends a single SDU through the flow
	 * @param sdu
	 */
	public void write(byte[] sdu) throws IPCException{
		flowImplementation.write(sdu);
	}
	
	/**
	 * Sends an array of SDUs through the flow
	 * @param sdu
	 */
	public void write(List<byte[]> sdus) throws IPCException{
		if (sdus == null){
			throw new IPCException("Null parameter");
		}
		
		for(int i=0; i<sdus.size(); i++){
			this.write(sdus.get(i));
		}
	}
	
	/**
	 * Causes the flow to be terminated. All the resources associated to it will be deallocated 
	 * @throws IPCException
	 */
	public void deallocate() throws IPCException{
		flowImplementation.deallocate();
	}

}
