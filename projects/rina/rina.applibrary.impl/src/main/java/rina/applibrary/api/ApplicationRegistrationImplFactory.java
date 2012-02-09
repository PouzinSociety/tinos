package rina.applibrary.api;

/**
 * Users can provide an instance of this class to customize the 
 * behavior of the ApplicationRegistration class
 * @author eduardgrasa
 *
 */
public interface ApplicationRegistrationImplFactory {

	/**
	 * Creates an instance of the ApplicationRegistrationImpl interface
	 * @return
	 */
	public ApplicationRegistrationImpl createApplicationRegistration();
}
