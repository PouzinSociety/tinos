package rina.enrollment.impl.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.cdap.api.message.CDAPMessage.Flags;
import rina.encoding.api.BaseEncoder;
import rina.encoding.api.Encoder;
import rina.enrollment.impl.EnrollmentTaskImpl;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;

/**
 * Handles all the operations for the "daf.management.enrollment.members" objectname
 * @author eduardgrasa
 *
 */
public class DIFMembersHandler{
	
	private static final Log log = LogFactory.getLog(DIFMembersHandler.class);

	/** The parent enrollment task **/
	private EnrollmentTaskImpl enrollmentTask = null;
	
	public DIFMembersHandler(EnrollmentTaskImpl enrollmentTask){
		this.enrollmentTask = enrollmentTask;
	}
	
	public void read(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		CDAPMessage responseMessage = null;
		RIBDaemon ribDaemon = (RIBDaemon) enrollmentTask.getIPCProcess().getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		Encoder encoder = (Encoder) enrollmentTask.getIPCProcess().getIPCProcessComponent(BaseEncoder.getComponentName());
		byte[] serializedObject = null;
		ObjectValue objectValue = null;
		Flags flags = null;
		
		try{
			for (int i=0; i<enrollmentTask.getMembers().size(); i++){
				serializedObject = encoder.encode(enrollmentTask.getMembers().get(i));
				objectValue = new ObjectValue();
				objectValue.setByteval(serializedObject);
				if (i < enrollmentTask.getMembers().size() -1 ){
					flags = Flags.F_RD_INCOMPLETE;
				}else{
					flags = null;
				}
				responseMessage = CDAPMessage.getReadObjectResponseMessage(flags, 
						cdapMessage.getInvokeID(), cdapMessage.getObjClass(), 0, cdapMessage.getObjName(), objectValue, 0, null);
				ribDaemon.sendMessage(responseMessage, cdapSessionDescriptor.getPortId(), null);
			}
		}catch(Exception ex){
			log.error(ex);
		}
	}
	
	public void create(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		//TODO check that we are not already enrolled to the application
		//TODO initiate the enrollment sequence
	}
	
	public void delete(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		//TODO 
	}
}
