package rina.shimipcprocess.ip.ribdaemon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import rina.cdap.api.CDAPMessageHandler;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.NotificationPolicy;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.SimpleRIBObject;
import rina.ribdaemon.api.UpdateStrategy;
import rina.shimipcprocess.ip.ShimIPCProcessForIPLayers;
import rina.shimipcprocess.ip.flowallocator.DirectoryEntry;
import rina.shimipcprocess.ip.flowallocator.FlowAllocatorImpl;
import rina.shimipcprocess.ip.flowallocator.FlowState;

public class RIBDaemonImpl extends BaseRIBDaemon{

	private ShimIPCProcessForIPLayers ipcProcess = null;
	private FlowAllocatorImpl flowAllocator = null;

	public RIBDaemonImpl(ShimIPCProcessForIPLayers ipcProcess, FlowAllocatorImpl flowAllocator){
		super();
		this.ipcProcess = ipcProcess;
		this.flowAllocator = flowAllocator;
	}

	@Override
	public void setIPCProcess(IPCProcess ipcProcess){
		super.setIPCProcess(ipcProcess);
	}

	public List<RIBObject> getRIBObjects() {
		List<RIBObject> result = new ArrayList<RIBObject>();

		RIBObject ribObject = new SimpleRIBObject(ipcProcess, "address", "/daf/management/naming/address", ipcProcess.getHostname());
		result.add(ribObject);
		ribObject = new SimpleRIBObject(ipcProcess, "applicationprocessname", "/daf/management/naming/applicationprocessname", ipcProcess.getApplicationProcessNamingInfo());
		result.add(ribObject);
		ribObject = new SimpleRIBObject(ipcProcess, "allowed local apps set", "/dif/management/flowallocator/allowedLocalApplications", "set");
		result.add(ribObject);

		Iterator<Entry<String, Integer>>  iterator = this.flowAllocator.getExpectedApplicationRegistrations().entrySet().iterator();
		Entry<String, Integer> currentEntry = null;
		while(iterator.hasNext()){
			currentEntry = iterator.next();
			ribObject = new SimpleRIBObject(ipcProcess, "allowed local app", 
					"/dif/management/flowallocator/allowedLocalApplications/" + 
					currentEntry.getKey(), currentEntry.getKey() + ", port "+currentEntry.getValue());
			result.add(ribObject);
		}

		ribObject = new SimpleRIBObject(ipcProcess, "directoryforwardingtableentry set", "/dif/management/flowallocator/directoryforwardingtableentries", "set");
		result.add(ribObject);

		Iterator<Entry<String, DirectoryEntry>> iterator2 = this.flowAllocator.getDirectory().entrySet().iterator();
		Entry<String, DirectoryEntry> currentEntry2 = null;
		while(iterator2.hasNext()){
			currentEntry2 = iterator2.next();
			ribObject = new SimpleRIBObject(ipcProcess, "directoryforwardingtableentry", 
					"/dif/management/flowallocator/directoryforwardingtableentries/" + currentEntry2.getKey(), 
					currentEntry2.getValue().getApNamingInfo().getEncodedString() + " is available at " +
					currentEntry2.getValue().getHostname() + " port " + currentEntry2.getValue().getPortNumber());
			result.add(ribObject);
		}

		ribObject = new SimpleRIBObject(ipcProcess, "qos cube set", "/dif/management/flowallocator/qoscubes", "set");
		result.add(ribObject);
		ribObject = new SimpleRIBObject(ipcProcess, "qos cube", "/dif/management/flowallocator/qoscubes/reliable", flowAllocator.getQoSCubes().get(0));
		result.add(ribObject);
		ribObject = new SimpleRIBObject(ipcProcess, "qos cube", "/dif/management/flowallocator/qoscubes/unreliable", flowAllocator.getQoSCubes().get(1));
		result.add(ribObject);
		ribObject = new SimpleRIBObject(ipcProcess, "flow set", "/dif/resourceallocation/flowallocator/flows", "set");
		result.add(ribObject);

		Iterator<Entry<Integer, FlowState>> iterator3 = this.flowAllocator.getFlows().entrySet().iterator();
		Entry<Integer, FlowState> currentEntry3 = null;
		boolean reliable = false;
		while(iterator3.hasNext()){
			currentEntry3 = iterator3.next();
			if (currentEntry3.getValue().getSocket() != null){
				reliable = true;
			}
			ribObject = new SimpleRIBObject(ipcProcess, "flow", 
					"/dif/resourceallocation/flowallocator/flows/"+currentEntry3.getKey(), 
					"Source application: "+currentEntry3.getValue().getFlowService().getSourceAPNamingInfo().getEncodedString() + 
					"\nDestination application:  "+currentEntry3.getValue().getFlowService().getDestinationAPNamingInfo().getEncodedString() + 
					"\nPort Id: "+currentEntry3.getValue().getPortId()+ " State: "+currentEntry3.getValue().getState() + " Reliable: "+reliable);
			result.add(ribObject);
		}

		return result;
	}

	public void addRIBObject(RIBObject arg0) throws RIBDaemonException {
		//Won't implement
	}

	public void processOperation(CDAPMessage arg0, CDAPSessionDescriptor arg1) throws RIBDaemonException {
		//Won't implement
	}

	public void removeRIBObject(RIBObject arg0) throws RIBDaemonException {
		//Won't implement
	}

	public void removeRIBObject(String arg0) throws RIBDaemonException {
		//Won't implement
	}

	public void sendMessage(CDAPMessage arg0, int arg1, CDAPMessageHandler arg2) throws RIBDaemonException {
		//Won't implement
	}

	public void sendMessages(CDAPMessage[] arg0, UpdateStrategy arg1) {
		// Won't implement
	}

	public void create(String arg0, long arg1, String arg2, Object arg3,
			NotificationPolicy arg4) throws RIBDaemonException {
		// Won't implement
	}

	public void delete(String arg0, long arg1, String arg2, Object arg3,
			NotificationPolicy arg4) throws RIBDaemonException {
		// Won't implement
	}

	public RIBObject read(String arg0, long arg1, String arg2)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		return null;
	}

	public void start(String arg0, long arg1, String arg2, Object arg3)
			throws RIBDaemonException {
		//Won't implement
	}

	public void stop(String arg0, long arg1, String arg2, Object arg3)
			throws RIBDaemonException {
		//Won't implement
	}

	public void write(String arg0, long arg1, String arg2, Object arg3,
			NotificationPolicy arg4) throws RIBDaemonException {
		// Won't implement
	}

	public void managementSDUDelivered(byte[] arg0, int arg1) {
		//Won't implement
	}

}