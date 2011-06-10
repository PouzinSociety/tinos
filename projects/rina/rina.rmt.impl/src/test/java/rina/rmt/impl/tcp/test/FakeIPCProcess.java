package rina.rmt.impl.tcp.test;

import java.util.List;

import rina.applicationprocess.api.ApplicationProcessException;
import rina.applicationprocess.api.ApplicationProcessNameSynonym;
import rina.applicationprocess.api.WhatevercastName;
import rina.cdap.api.CDAPSessionManager;
import rina.delimiting.api.Delimiter;
import rina.delimiting.api.DelimiterFactory;
import rina.delimiting.impl.DelimiterFactoryImpl;
import rina.efcp.api.DataTransferAE;
import rina.flowallocator.api.FlowAllocator;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.RIBDaemon;
import rina.rmt.api.RMT;
import rina.serialization.api.Serializer;

public class FakeIPCProcess implements IPCProcess {
	
	private RIBDaemon ribdaemon = null;
	private Delimiter delimiter = null;
	private RMT rmt = null;
	
	public FakeIPCProcess(){
		this.ribdaemon = new FakeRIBDaemon();
		this.ribdaemon.setIPCProcess(this);
		DelimiterFactory delimiterFactory = new DelimiterFactoryImpl();
		this.delimiter = delimiterFactory.createDelimiter(DelimiterFactory.DIF);
	}

	public void addApplicationProcessNameSynonym(
			ApplicationProcessNameSynonym arg0)
			throws ApplicationProcessException {
		// TODO Auto-generated method stub
		
	}

	public void addWhatevercastName(WhatevercastName arg0)
			throws ApplicationProcessException {
		// TODO Auto-generated method stub
		
	}

	public String getApplicationProcessInstance() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getApplicationProcessName() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<byte[]> getApplicationProcessNameSynonyms() {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getCurrentSynonym() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<WhatevercastName> getWhatevercastNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeApplicationProcessNameSynonym(
			ApplicationProcessNameSynonym arg0)
			throws ApplicationProcessException {
		// TODO Auto-generated method stub
		
	}

	public void removeWhatevercastName(WhatevercastName arg0)
			throws ApplicationProcessException {
		// TODO Auto-generated method stub
		
	}

	public void setApplicationProcessInstance(String arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setApplicationProcessName(String arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setCurrentSynonym(byte[] arg0)
			throws ApplicationProcessException {
		// TODO Auto-generated method stub
		
	}

	public void deliverDeallocateRequestToApplicationProcess(int arg0) {
		// TODO Auto-generated method stub
		
	}

	public void deliverSDUsToApplicationProcess(List<byte[]> arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	public CDAPSessionManager getCDAPSessionManager() {
		// TODO Auto-generated method stub
		return null;
	}

	public DataTransferAE getDataTransferAE() {
		// TODO Auto-generated method stub
		return null;
	}

	public Delimiter getDelimiter() {
		return delimiter;
	}

	public FlowAllocator getFlowAllocator() {
		// TODO Auto-generated method stub
		return null;
	}

	public RIBDaemon getRibDaemon() {
		return ribdaemon;
	}

	public RMT getRmt() {
		return rmt;
	}

	public Serializer getSerializer() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setCDAPSessionManager(CDAPSessionManager arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setDataTransferAE(DataTransferAE arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setDelimiter(Delimiter delimiter) {
		this.delimiter = delimiter;
	}

	public void setFlowAllocator(FlowAllocator arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setRibDaemon(RIBDaemon ribdaemon) {
		this.ribdaemon = ribdaemon;
	}

	public void setRmt(RMT rmt) {
		this.rmt = rmt;
	}

	public void setSerializer(Serializer arg0) {
		// TODO Auto-generated method stub
		
	}

}
