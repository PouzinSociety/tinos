package rina.applibrary.api;

/**
 * Allows the creation of different intances of classes implementing FlowImpl
 * @author eduardgrasa
 *
 */
public interface FlowImplFactory {

	public FlowImpl createFlowImpl();
}
