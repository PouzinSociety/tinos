package rina.ribdaemon.impl.ribobjects;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.ribdaemon.api.RIBDaemon;

/**
 * Sends a CDAP message to check that the CDAP connection 
 * is still working ok
 * @author eduardgrasa
 *
 */
public class WatchdogTimerTask extends TimerTask{
	
	private static final Log log = LogFactory.getLog(WatchdogTimerTask.class);
	
	private CDAPSessionManager cdapSessionManager = null;
	private RIBDaemon ribDaemon = null;
	private WatchdogRIBObject watchdogRIBObject = null;
	private List<Integer> checkedFlows = null;
	
	public WatchdogTimerTask(CDAPSessionManager cdapSessionManager, WatchdogRIBObject watchdogRIBObject){
		this.cdapSessionManager = cdapSessionManager;
		this.watchdogRIBObject = watchdogRIBObject;
		this.ribDaemon = this.watchdogRIBObject.getRIBDaemon();
		this.checkedFlows = new ArrayList<Integer>();
	}
	
	/**
	 * Called by the WatchdogRIBObject when it exchanges a READ/READ_R message 
	 * pair over a certain flow, locally identified by portId.
	 * @param portId
	 */
	public synchronized void checkedFlow(int portId){
		this.checkedFlows.add(new Integer(portId));
	}
	
	/**
	 * Finds out if a certain flow has been checked since the last 
	 * time the task executed
	 * @param portId
	 * @return
	 */
	private synchronized boolean hasBeenChecked(int portId){
		for(int i=0; i<this.checkedFlows.size(); i++){
			if (this.checkedFlows.get(i).intValue() == portId){
				return true;
			}
		}
		
		return false;
	}
	
	private synchronized void emptyCheckedFlows(){
		this.checkedFlows.removeAll(this.checkedFlows);
	}

	@Override
	public void run() {
		int[] sessionIds = this.cdapSessionManager.getAllCDAPSessionIds();
		if (sessionIds == null){
			return;
		}
		
		CDAPMessage cdapMessage = null;
		for(int i=0; i<sessionIds.length; i++){
			if (!hasBeenChecked(sessionIds[i])){
				try{
					cdapMessage = cdapSessionManager.getReadObjectRequestMessage(
							sessionIds[i], null, null, WatchdogRIBObject.WATCHDOG_OBJECT_CLASS, 
							0, WatchdogRIBObject.WATCHDOG_OBJECT_NAME, 0, true);
					this.ribDaemon.sendMessage(cdapMessage, sessionIds[i], watchdogRIBObject);
				}catch(Exception ex){
					log.error(ex);
				}
			}
		}
		
		emptyCheckedFlows();
	}
}
