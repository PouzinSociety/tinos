package rina.cdap.echotarget;

import java.net.Socket;

import rina.cdap.api.CDAPSessionManager;
import rina.cdap.echotarget.enrollment.CDAPEnrollmentWorker;
import rina.delimiting.api.Delimiter;
import rina.serialization.api.Serializer;

public class CDAPWorkerFactory {
	
	public static final String ECHO_WORKER = "EchoWorker";
	public static final String ENROLLMENT_WORKER = "EnrollmentWorker";
	
	public static final CDAPWorker createCDAPWorker(String type, Socket socket, CDAPSessionManager cdapSessionManager, Delimiter delimiter, Serializer serializer){
		if (type.equals(ECHO_WORKER)){
			return new CDAPEchoWorker(socket, cdapSessionManager, delimiter, serializer);
		}else if (type.equals(ENROLLMENT_WORKER)){
			return new CDAPEnrollmentWorker(socket, cdapSessionManager, delimiter, serializer);
		}
		
		return null;
	}

}
