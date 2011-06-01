package rina.cdap.echotarget;

import java.net.Socket;

import rina.cdap.api.CDAPSession;
import rina.cdap.echotarget.enrollment.CDAPEnrollmentWorker;
import rina.delimiting.api.Delimiter;
import rina.serialization.api.Serializer;

public class CDAPWorkerFactory {
	
	public static final String ECHO_WORKER = "EchoWorker";
	public static final String ENROLLMENT_WORKER = "EnrollmentWorker";
	
	public static final CDAPWorker createCDAPWorker(String type, Socket socket, CDAPSession cdapSession, Delimiter delimiter, Serializer serializer){
		if (type.equals(ECHO_WORKER)){
			return new CDAPEchoWorker(socket, cdapSession, delimiter, serializer);
		}else if (type.equals(ENROLLMENT_WORKER)){
			return new CDAPEnrollmentWorker(socket, cdapSession, delimiter, serializer);
		}
		
		return null;
	}

}
