package rina.flowallocator.impl.policies;

/**
 * This policy is used when the destination has refused the create_flow request, 
 * and the FAI can overcome the cause for refusal and try again. This policy 
 * should re-formulate the request. This policy should formulate the contents of the reply.
 * @author eduardgrasa
 *
 */
public interface AllocateRetryPolicy {

}
