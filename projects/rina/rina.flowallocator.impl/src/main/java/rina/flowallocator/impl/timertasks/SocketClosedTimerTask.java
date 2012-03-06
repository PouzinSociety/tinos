package rina.flowallocator.impl.timertasks;

import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.flowallocator.impl.FlowAllocatorInstanceImpl;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;

/**
 * Activates when the socket reader detects that the socket has been closed.
 * Will only run if the Flow Allocator instance is not in the deallocation state.
 * @author eduardgrasa
 *
 */
public class SocketClosedTimerTask extends TimerTask {
	
	public static final int DELAY = 5*1000;
	private static final Log log = LogFactory.getLog(SocketClosedTimerTask.class);
	
	private FlowAllocatorInstanceImpl flowAllocatorInstance = null;
	private RIBDaemon ribDaemon = null;
	private String objectName = null;
	private String objectClass = null;
	
	public SocketClosedTimerTask(FlowAllocatorInstanceImpl flowAllocatorInstance, RIBDaemon ribDaemon, String objectClass, String objectName){
		this.flowAllocatorInstance = flowAllocatorInstance;
		this.ribDaemon = ribDaemon;
		this.objectClass = objectClass;
		this.objectName = objectName;
	}

	@Override
	public void run() {
		log.debug("Looking if flow "+objectName+" is still in the RIB");
		if (!flowAllocatorInstance.isFinished()){
			flowAllocatorInstance.destroyFlowAllocatorInstance();
			try{
				this.ribDaemon.delete(objectClass, objectName, 0);
			}catch(RIBDaemonException ex){
				ex.printStackTrace();
			}
		}

	}

}
