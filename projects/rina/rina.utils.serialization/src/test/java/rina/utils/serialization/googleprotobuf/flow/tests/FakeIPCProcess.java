package rina.utils.serialization.googleprotobuf.flow.tests;

import java.util.List;

import rina.cdap.api.CDAPSessionFactory;
import rina.delimiting.api.Delimiter;
import rina.efcp.api.DataTransferAE;
import rina.flowallocator.api.FlowAllocator;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.RIBDaemon;
import rina.rmt.api.RMT;
import rina.serialization.api.Serializer;

public class FakeIPCProcess implements IPCProcess{

	private DataTransferAE fakeDataTransferAE = new FakeDataTransferAE();
	
	public void deliverDeallocateRequestToApplicationProcess(int arg0) {
		// TODO Auto-generated method stub
		
	}

	public void deliverSDUsToApplicationProcess(List<byte[]> arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	public CDAPSessionFactory getCDAPSessionFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	public DataTransferAE getDataTransferAE() {
		return fakeDataTransferAE;
	}

	public Delimiter getDelimiter() {
		// TODO Auto-generated method stub
		return null;
	}

	public FlowAllocator getFlowAllocator() {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getIPCProcessAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	public ApplicationProcessNamingInfo getIPCProcessNamingInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public RIBDaemon getRibDaemon() {
		// TODO Auto-generated method stub
		return null;
	}

	public RMT getRmt() {
		// TODO Auto-generated method stub
		return null;
	}

	public Serializer getSerializer() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setCDAPSessionFactory(CDAPSessionFactory arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setDataTransferAE(DataTransferAE arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setDelimiter(Delimiter arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setFlowAllocator(FlowAllocator arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setIPCProcessNamingInfo(ApplicationProcessNamingInfo arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setRibDaemon(RIBDaemon arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setRmt(RMT arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setSerializer(Serializer arg0) {
		// TODO Auto-generated method stub
		
	}

}
