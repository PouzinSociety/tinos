package rina.flowallocator.impl.policies;

/**
 * This policy determines when the requesting application is given an Allocate_Response primitive.  
 * In general, the choices are once the request is determined to be well-formed and a create_flow 
 * request has been sent, or withheld until a create_flow response has been received and retries 
 * have been exhausted.
 * @author eduardgrasa
 *
 */
public interface AllocateNotifyPolicy {

}
