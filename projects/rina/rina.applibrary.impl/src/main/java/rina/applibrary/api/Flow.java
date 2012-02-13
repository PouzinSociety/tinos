package rina.applibrary.api;

import java.util.List;

import rina.applibrary.api.FlowImpl.State;
import rina.applibrary.impl.DefaultFlowImpl;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.QualityOfServiceSpecification;

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
	 * The class that does the work
	 */
	private FlowImpl flowImplementation = null;
	
	/**
	 * The flow implementation factory. It can be used 
	 * to change the behavior of the FlowImpl.
	 */
	private FlowImplFactory flowImplementationFactory = null;
	
	public Flow(FlowImpl flowImplementation){
		this.flowImplementation = flowImplementation;
		this.flowImplementation.setFlow(this);
	}
	
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
	public Flow(ApplicationProcessNamingInfo sourceApplication, ApplicationProcessNamingInfo destinationApplication, 
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
	public Flow(ApplicationProcessNamingInfo sourceApplication, ApplicationProcessNamingInfo destinationApplication, 
			QualityOfServiceSpecification qosSpec, SDUListener sduListener, FlowImplFactory flowImplFactory) throws IPCException{
		if (sourceApplication == null){
			IPCException flowException = new IPCException(IPCException.SOURCE_APPLICATION_NOT_SPECIFIED);
			flowException.setErrorCode(IPCException.SOURCE_APPLICATION_NOT_SPECIFIED_CODE);
			throw flowException;
		}
		
		if (destinationApplication == null){
			IPCException flowException = new IPCException(IPCException.DESTINATION_APPLICATION_NOT_SPECIFIED);
			flowException.setErrorCode(IPCException.DESTINATION_APPLICATION_NOT_SPECIFIED_CODE);
			throw flowException;
		}
		
		if (sduListener == null){
			IPCException flowException = new IPCException(IPCException.SDU_LISTENER_NOT_SPECIFIED);
			flowException.setErrorCode(IPCException.SDU_LISTENER_NOT_SPECIFIED_CODE);
			throw flowException;
		}
		
		this.flowImplementationFactory = flowImplFactory;
		
		this.createFlowImpl();
		this.flowImplementation.setSourceApplication(sourceApplication);
		this.flowImplementation.setDestinationApplication(destinationApplication);
		this.flowImplementation.setQosSpec(qosSpec);
		this.flowImplementation.setSduListener(sduListener);
		
		this.flowImplementation.allocate();
	}
	
	/**
	 * Creates the flow implementation
	 */
	private void createFlowImpl(){
		if (this.flowImplementationFactory != null){
			this.flowImplementation = this.flowImplementationFactory.createFlowImpl();
		}else{
			this.flowImplementation = new DefaultFlowImpl();
			this.flowImplementation.setFlow(this);
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
			throw new IPCException("List of SDUs cannot be null");
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
	
	public void setSDUListener(SDUListener sduListener){
		flowImplementation.setSduListener(sduListener);
	}
	
	public void setFlowListener(FlowListener flowListener){
		flowImplementation.setFlowListener(flowListener);
	}
	
	public void setSourceApplication(ApplicationProcessNamingInfo sourceApplication) throws IPCException{
		if (sourceApplication == null){
			IPCException flowException = new IPCException(IPCException.SOURCE_APPLICATION_NOT_SPECIFIED);
			flowException.setErrorCode(IPCException.SOURCE_APPLICATION_NOT_SPECIFIED_CODE);
			throw flowException;
		}
		flowImplementation.setSourceApplication(sourceApplication);
	}
	
	public ApplicationProcessNamingInfo getSourceApplication(){
		return flowImplementation.getSourceApplication();
	}
	
	public void setDestinationApplication(ApplicationProcessNamingInfo destinationApplication) throws IPCException{
		if (destinationApplication == null){
			IPCException flowException = new IPCException(IPCException.DESTINATION_APPLICATION_NOT_SPECIFIED);
			flowException.setErrorCode(IPCException.DESTINATION_APPLICATION_NOT_SPECIFIED_CODE);
			throw flowException;
		}
		flowImplementation.setDestinationApplication(destinationApplication);
	}
	
	public ApplicationProcessNamingInfo getDestinationApplication(){
		return flowImplementation.getDestinationApplication();
	}
	
	public void setQoSSpec(QualityOfServiceSpecification qosSpec){
		flowImplementation.setQosSpec(qosSpec);
	}
	
	public QualityOfServiceSpecification getQoSSpec(){
		return flowImplementation.getQosSpec();
	}
	
	public boolean isAllocated(){
		return flowImplementation.getState() == State.ALLOCATED;
	}
	
	public boolean isDeallocated(){
		return flowImplementation.getState() == State.DEALLOCATED;
	}
	
	/**
	 * Return the portId associated to this flow
	 * @return
	 */
	public int getPortId(){
		return flowImplementation.getPortId();
	}

}
