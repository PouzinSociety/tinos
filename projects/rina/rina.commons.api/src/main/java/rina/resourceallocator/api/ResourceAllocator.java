package rina.resourceallocator.api;

/**
 * The Resource Allocator (RA) is the core of management in the IPC Process. 
 * The degree of decentralization depends on the policies and how it is used. The RA has a set of meters 
 * and dials that it can manipulate. The meters fall in 3 categories:
 * 		Ð Traffic characteristics from the user of the DIF
 * 		Ð Traffic characteristics of incoming and outgoing flows
 * 		Ð Information from other members of the DIF
 * The Dials:
 * 		Ð Creation/Deletion of QoS Classes
 * 		Ð Data Transfer QoS Sets
 *		Ð Modifying Data Transfer Policy Parameters
 * 		Ð Creation/Deletion of RMT Queues
 * 		Ð Modify RMT Queue Servicing
 * 		Ð Creation/Deletion of (N-1)-flows
 * 		Ð Assignment of RMT Queues to (N-1)-flows
 * 		Ð Forwarding Table Generator Output
 * 
 * @author eduardgrasa
 *
 */
public interface ResourceAllocator {

	/**
	 * Returns the N-1 Flow Manager of this IPC Process
	 * @return
	 */
	public NMinus1FlowManager getNMinus1FlowManager();

}
