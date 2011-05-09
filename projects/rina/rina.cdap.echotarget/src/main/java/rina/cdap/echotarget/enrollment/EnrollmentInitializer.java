package rina.cdap.echotarget.enrollment;

import java.io.IOException;

import rina.applicationprocess.api.ApplicationProcessNameSynonym;
import rina.cdap.api.CDAPException;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.Flags;
import rina.cdap.api.message.ObjectValue;

/**
 * Sends all the M_READ_R messages required to initialize an IPC process that wants to join a DIF
 * @author eduardgrasa
 *
 */
public class EnrollmentInitializer implements Runnable{
	
	private enum State {ADDRESS, WHATEVERCAST_NAMES, DATA_TRANSFER_CONSTANTS, QOS_CUBES, 
		EFCP_POLICIES, FLOW_ALLOCATOR_POLICIES, DONE};
	
	private CDAPEnrollmentWorker cdapEnrollmentWorker = null;
	
	private boolean cancelread = false;
		
	private State state = State.ADDRESS;
	
	private int invokeId = 0;
	
	private int counter = 0;

	public EnrollmentInitializer(CDAPEnrollmentWorker cdapEnrollmentWorker, int invokeId){
		this.cdapEnrollmentWorker = cdapEnrollmentWorker;
		this.invokeId = invokeId;
	}
	
	public void cancelread(){
		this.cancelread = true;
	}
	
	public boolean isRunning(){
		return !cancelread;
	}
	
	public void run() {
		while (!cancelread){
			try{
				switch(state){
				case ADDRESS:
					sendAddress();
					break;
				case WHATEVERCAST_NAMES:
					sendWhatevercastNames();
					break;
				case DATA_TRANSFER_CONSTANTS:
					break;
				case QOS_CUBES:
					break;
				case EFCP_POLICIES:
					break;
				case FLOW_ALLOCATOR_POLICIES:
					break;
				case DONE:
					cdapEnrollmentWorker.processCDAPMessage(null);
					break;
				default:
					break;
				}
			}catch(CDAPException ex){
				ex.printStackTrace();
			}catch(IOException ex){
				ex.printStackTrace();
			}
		}
	}
	
	private void sendAddress() throws CDAPException, IOException{
		byte[] serializedAddress = null;
		ObjectValue objectValue = null;
		
		ApplicationProcessNameSynonym address = new ApplicationProcessNameSynonym();
		address.setApplicationProcessName("B");
		address.setSynonym(new byte[]{0x02});
		try{
			serializedAddress = cdapEnrollmentWorker.getSerializer().serialize(address);
			objectValue = new ObjectValue();
			objectValue.setByteval(serializedAddress);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		CDAPMessage cdapMessage = CDAPMessage.getReadObjectResponseMessage(Flags.F_RD_INCOMPLETE, invokeId, 
				"rina.messages.ApplicationProcessNameSynonym", 1, "daf.management.currentSynonym", objectValue, 0, null);
		
		cdapEnrollmentWorker.sendCDAPMessage(cdapMessage);
		state = State.WHATEVERCAST_NAMES;
	}
	
	private void sendWhatevercastNames() throws CDAPException, IOException{
		
	}

}
