package rina.shimipcprocess.ip.ribdaemon;

import java.util.ArrayList;
import java.util.List;

import rina.cdap.api.CDAPMessageHandler;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.NotificationPolicy;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.SimpleRIBObject;
import rina.ribdaemon.api.UpdateStrategy;
import rina.shimipcprocess.ip.ShimIPCProcessForIPLayers;
import rina.shimipcprocess.ip.flowallocator.FlowAllocatorImpl;

public class RIBDaemonImpl extends BaseRIBDaemon{
	
	private ShimIPCProcessForIPLayers ipcProcess = null;
	private FlowAllocatorImpl flowAllocator = null;
	
	public RIBDaemonImpl(ShimIPCProcessForIPLayers ipcProcess, FlowAllocatorImpl flowAllocator){
		super();
		this.ipcProcess = ipcProcess;
		this.flowAllocator = flowAllocator;
	}
	
	@Override
	public List<RIBObject> getRIBObjects() {
		List<RIBObject> result = new ArrayList<RIBObject>();
		
		RIBObject ribObject = new SimpleRIBObject(ipcProcess, "address", "/daf/management/naming/address", ipcProcess.getHostname());
		result.add(ribObject);
		ribObject = new SimpleRIBObject(ipcProcess, "applicationprocessname", "/daf/management/naming/applicationprocessname", ipcProcess.getApplicationProcessNamingInfo());
		result.add(ribObject);
		ribObject = new SimpleRIBObject(ipcProcess, "allowed local apps set", "/dif/management/flowallocator/allowedLocalApplications", "set");
		result.add(ribObject);
		//TODO add allowed local apps
		
		ribObject = new SimpleRIBObject(ipcProcess, "directoryforwardingtableentry set", "/dif/management/flowallocator/directoryforwardingtableentries", "set");
		result.add(ribObject);
		//TODO add directory entries
		
		ribObject = new SimpleRIBObject(ipcProcess, "qos cube set", "/dif/management/flowallocator/qoscubes", "set");
		result.add(ribObject);
		ribObject = new SimpleRIBObject(ipcProcess, "qos cube", "/dif/management/flowallocator/qoscubes/reliable", flowAllocator.getQoSCubes().get(0));
		result.add(ribObject);
		ribObject = new SimpleRIBObject(ipcProcess, "qos cube", "/dif/management/flowallocator/qoscubes/unreliable", flowAllocator.getQoSCubes().get(1));
		result.add(ribObject);
		ribObject = new SimpleRIBObject(ipcProcess, "flow set", "/dif/resourceallocation/flowallocator/flows", "set");
		result.add(ribObject);
		//TODO add flows
		
		return result;
	}

	@Override
	public void addRIBObject(RIBObject arg0) throws RIBDaemonException {
		//Won't implement
	}

	@Override
	public void cdapMessageDelivered(byte[] arg0, int arg1) {
		//Won't implement
	}

	@Override
	public void processOperation(CDAPMessage arg0, CDAPSessionDescriptor arg1) throws RIBDaemonException {
		//Won't implement
	}

	@Override
	public void removeRIBObject(RIBObject arg0) throws RIBDaemonException {
		//Won't implement
		
	}

	@Override
	public void removeRIBObject(String arg0) throws RIBDaemonException {
		//Won't implement
		
	}

	@Override
	public void sendMessage(CDAPMessage arg0, int arg1, CDAPMessageHandler arg2) throws RIBDaemonException {
		//Won't implement
	}

	@Override
	public void sendMessages(CDAPMessage[] arg0, UpdateStrategy arg1) {
		// Won't implement
	}

	@Override
	public void create(String arg0, long arg1, String arg2, Object arg3,
			NotificationPolicy arg4) throws RIBDaemonException {
		// Won't implement
	}

	@Override
	public void delete(String arg0, long arg1, String arg2, Object arg3,
			NotificationPolicy arg4) throws RIBDaemonException {
		// Won't implement
	}

	@Override
	public RIBObject read(String arg0, long arg1, String arg2)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void start(String arg0, long arg1, String arg2, Object arg3)
			throws RIBDaemonException {
		//Won't implement
	}

	@Override
	public void stop(String arg0, long arg1, String arg2, Object arg3)
			throws RIBDaemonException {
		//Won't implement
	}

	@Override
	public void write(String arg0, long arg1, String arg2, Object arg3,
			NotificationPolicy arg4) throws RIBDaemonException {
		// Won't implement
	}

}
