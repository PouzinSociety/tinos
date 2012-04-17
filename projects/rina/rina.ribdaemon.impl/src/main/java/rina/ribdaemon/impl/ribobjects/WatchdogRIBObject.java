package rina.ribdaemon.impl.ribobjects;

import java.util.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.BaseCDAPSessionManager;
import rina.cdap.api.CDAPMessageHandler;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.configuration.RINAConfiguration;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.ObjectInstanceGenerator;
import rina.ribdaemon.api.RIBDaemonException;

public class WatchdogRIBObject extends BaseRIBObject implements CDAPMessageHandler{
	
	private static final Log log = LogFactory.getLog(WatchdogRIBObject.class);
	
	public static final String WATCHDOG_OBJECT_NAME = "/dif/management/watchdog";
	public static final String WATCHDOG_OBJECT_CLASS = "watchdog timer";
	
	/** Timer for the watchdog timertask **/
	private Timer timer = null;
	
	private CDAPSessionManager cdapSessionManager = null;
	
	private CDAPMessage responseMessage = null;
	
	private WatchdogTimerTask timerTask = null;
	
	/**
	 * Schedule the watchdogtimer_task to run every PERIOD_IN_MS milliseconds
	 * @param ipcProcess
	 */
	public WatchdogRIBObject(IPCProcess ipcProcess){
		super(ipcProcess, WATCHDOG_OBJECT_CLASS, ObjectInstanceGenerator.getObjectInstance(), WATCHDOG_OBJECT_NAME);
		this.cdapSessionManager = (CDAPSessionManager) getIPCProcess().getIPCProcessComponent(BaseCDAPSessionManager.getComponentName());
		this.timer = new Timer();
		timerTask = new WatchdogTimerTask(this.cdapSessionManager, this);
		long periodInMs = RINAConfiguration.getInstance().getLocalConfiguration().getWatchdogPeriodInMs();
		timer.schedule(timerTask, new Double(periodInMs*Math.random()).longValue(), periodInMs);
	}
	
	@Override
	public Object getObjectValue() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Reply to the CDAP message
	 */
	@Override
	public void read(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor)
		throws RIBDaemonException {
		try{
			responseMessage = cdapSessionManager.getReadObjectResponseMessage(cdapSessionDescriptor.getPortId(), null, 
					cdapMessage.getObjClass(), 0L, cdapMessage.getObjName(), null, 0, null, cdapMessage.getInvokeID());
			this.getRIBDaemon().sendMessage(responseMessage, cdapSessionDescriptor.getPortId(), null);
			this.timerTask.checkedFlow(cdapSessionDescriptor.getPortId());
		}catch(Exception ex){
			log.error(ex);
		}
	}
	
	public void readResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor)
		throws RIBDaemonException {
		//Just ignore the responses
	}

	public void cancelReadResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor)
		throws RIBDaemonException {
	}

	public void createResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor)
		throws RIBDaemonException {
	}

	public void deleteResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor)
		throws RIBDaemonException {
	}

	public void startResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor)
		throws RIBDaemonException {
	}

	public void stopResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor)
		throws RIBDaemonException {
	}

	public void writeResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor)
		throws RIBDaemonException {
	}
}
