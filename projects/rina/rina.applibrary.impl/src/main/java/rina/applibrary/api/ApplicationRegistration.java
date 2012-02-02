package rina.applibrary.api;

import java.util.List;

import rina.applibrary.impl.DefaultApplicationRegistrationImpl;
import rina.applibrary.impl.DefaultFlowAcceptor;

/**
 * The object that is used to allow an application to register in one or more DIFs
 * available at this system. By registering to a DIF, an application becomes available
 * through this DIF and will consider accepting flow requests.
 * @author eduardgrasa
 *
 */
public class ApplicationRegistration {
	
	/**
	 * The application that has registered
	 */
	private ApplicationProcess application = null;
	
	/**
	 * The DIF names where this application is registered. If the list 
	 * is null it means the application is registered to all the DIFs 
	 * available in this system.
	 */
	private List<String> difNames = null;
	
	/**
	 * The flowAcceptor object associated to this ApplicationRegistration. Decides 
	 * if new flow requests are accepted or not
	 */
	private FlowAcceptor flowAcceptor = null;
	
	/**
	 * The actual implementation of the behavior of this class
	 */
	private ApplicationRegistrationImpl applicationRegistrationImplementation = null;
	
	/**
	 * Provided by the user in case it wants to use a non-default implementation of 
	 * the behavior of this class
	 */
	private ApplicationRegistrationImplFactory ariFactory = null;
	
	/**
	 * Controls if this object is already unregistered
	 */
	private boolean unregistered = false;
	
	/**
	 * Controls if flows will be notified
	 */
	private FlowListener flowListener = null;
	
	/**
	 * Constructor for blocking servers registering the application in all the DIFs, using the default acceptor and the default 
	 * registration implementation
	 * @param application
	 * @throws IPCException
	 */
	public ApplicationRegistration(ApplicationProcess application) throws IPCException{
		this(application, null, null, null, null);
	}
	
	/**
	 * Constructor for non-blocking servers registering the application in all the DIFs, using the default acceptor and the default 
	 * registration implementation
	 * @param application
	 * @param flowListener
	 * @throws IPCException
	 */
	public ApplicationRegistration(ApplicationProcess application, FlowListener flowListener) throws IPCException{
		this(application, null, null, null, flowListener);
	}
	
	/**
	 * Registers the application to the specified list of DIFs. If the list is null, the application is 
	 * registered in all the DIFs that currently exist in this system.
	 * @param application
	 * @param difNames
	 * @param ariFactory If provided the default behavior of this class will be replaced by the instances created by
	 * this factory
	 * @param flowAcceptor Decides what flows will be accepted and what flows will be rejected. If it is null, all the 
	 * incoming flows are accepted
	 * @param flowListener If provided, every time a new flow is accepted the flowListener will be called (non-blocking
	 * mode). In non-blocking mode calls to "accept" will throw an Exception. If it is null, users of this class will have 
	 * to call the "accept" blocking operation in order to get the accepted flows.
	 * @throws IPCException
	 */
	public ApplicationRegistration(ApplicationProcess application, List<String> difNames, 
			ApplicationRegistrationImplFactory ariFactory, FlowAcceptor flowAcceptor, FlowListener flowListener) throws IPCException{
		if (application == null){
			IPCException flowException = new IPCException(IPCException.APPLICATION_NOT_SPECIFIED);
			flowException.setErrorCode(IPCException.APPLICATION_NOT_SPECIFIED_CODE);
		}
		application.validate();
		this.application = application;
		
		this.difNames = difNames;
		this.ariFactory = ariFactory;
		this.flowListener = flowListener;
		
		this.flowAcceptor = flowAcceptor;
		if (this.flowAcceptor == null){
			this.flowAcceptor = new DefaultFlowAcceptor(application);
		}
		
		this.createApplicationRegistrationImpl();
		
		this.applicationRegistrationImplementation.register(this.application, this.difNames, this.flowAcceptor, this.flowListener);
	}
	
	private void createApplicationRegistrationImpl(){
		if (this.ariFactory != null){
			this.applicationRegistrationImplementation = this.ariFactory.createApplicationRegistration();
		}else{
			this.applicationRegistrationImplementation = new DefaultApplicationRegistrationImpl();
		}
	}
	
	/**
	 * This operation will block until a new incoming flow is accepted. If we are in non-blocking 
	 * mode, this operation will throw an Exception. If the application is already unregistered, this 
	 * operation will throw an exception
	 * @param SDUListener the SDUs received by this flow will be sent to the SDUListener
	 * @return the accepted Flow
	 */
	public Flow accept(SDUListener sduListener) throws IPCException{
		if (isUnregistered()){
			IPCException ipcException = new IPCException(IPCException.APPLICATION_UNREGISTERED);
			ipcException.setErrorCode(IPCException.APPLICATION_UNREGISTERED_CODE);
			throw ipcException;
		}
		
		if (!isBlocking()){
			IPCException ipcException = new IPCException(IPCException.NON_BLOCKING_REGISTRATION);
			ipcException.setErrorCode(IPCException.NON_BLOCKING_REGISTRATION_CODE);
			throw ipcException;
		}
		
		if (sduListener == null){
			IPCException ipcException = new IPCException(IPCException.SDU_LISTENER_NOT_SPECIFIED);
			ipcException.setErrorCode(IPCException.SDU_LISTENER_NOT_SPECIFIED_CODE);
			throw ipcException;
		}
		
		return this.applicationRegistrationImplementation.accept(sduListener);
	}
	
	/**
	 * Cancel the registration
	 * @throws IPCException
	 */
	public void unregister() throws IPCException{
		this.applicationRegistrationImplementation.unregister();
		this.unregistered = true;
	}


	public boolean isUnregistered() {
		return unregistered;
	}
	
	/**
	 * Returns we
	 * @return
	 */
	public boolean isBlocking(){
		return flowListener == null;
	}

}
