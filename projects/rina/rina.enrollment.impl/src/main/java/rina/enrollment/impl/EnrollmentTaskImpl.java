package rina.enrollment.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.Opcode;
import rina.enrollment.api.BaseEnrollmentTask;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.MessageSubscriber;
import rina.ribdaemon.api.MessageSubscription;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;

public class EnrollmentTaskImpl extends BaseEnrollmentTask implements MessageSubscriber{
	
	private static final Log log = LogFactory.getLog(EnrollmentTaskImpl.class);

	public EnrollmentTaskImpl(){
		subscribeToRIBDaemon();
	}
	
	/**
	 * Subscribe to all M_CONNECTs, M_CONNECT_R, M_RELEASE and M_RELEASE_R
	 */
	private void subscribeToRIBDaemon(){
		RIBDaemon ribDaemon = (RIBDaemon) getIPCProcess().getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		try{
			MessageSubscription messageSubscription = new MessageSubscription();
			messageSubscription.setOpCode(Opcode.M_CONNECT);
			ribDaemon.subscribeToMessages(messageSubscription, this);
			
			messageSubscription = new MessageSubscription();
			messageSubscription.setOpCode(Opcode.M_CONNECT_R);
			ribDaemon.subscribeToMessages(messageSubscription, this);
			
			messageSubscription = new MessageSubscription();
			messageSubscription.setOpCode(Opcode.M_RELEASE);
			ribDaemon.subscribeToMessages(messageSubscription, this);
			
			messageSubscription = new MessageSubscription();
			messageSubscription.setOpCode(Opcode.M_RELEASE_R);
			ribDaemon.subscribeToMessages(messageSubscription, this);
		}catch(RIBDaemonException ex){
			ex.printStackTrace();
			log.error("Could not subscribe to RIB Daemon:" +ex.getMessage());
		}
	}
	
	/**
	 * Called by the RIB Daemon
	 */
	public void messageReceived(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) {
		log.info("Received cdapMessage from portId "+cdapSessionDescriptor.getPortId()+" and opcode "+cdapMessage.getOpCode());
	}
}
