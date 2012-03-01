package rina.enrollment.impl.test.addressmanager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import rina.applicationprocess.api.DAFMember;
import rina.enrollment.impl.SimpleAddressManager;
import rina.ipcservice.api.IPCException;

public class SimpleAddressManagerTest {
	
	private static final int MEMBERS = 1000; 
		
	SimpleAddressManager addressManager = null;
	MockEnrollmentTask enrollmentTask = null;
	MockIPCProcess ipcProcess = null;

	@Before
	public void setup(){
		enrollmentTask = new MockEnrollmentTask();
		ipcProcess = new MockIPCProcess();
		enrollmentTask.setIPCProcess(ipcProcess);
		addressManager = new SimpleAddressManager(enrollmentTask);
	}
	
	@Test
	public void testWithNoMembers() throws IPCException{
		long address = addressManager.getAvailableAddress();
		System.out.println("Got address "+address);
		Assert.assertNotSame(1L, address);
	}
	
	@Test
	public void testWith1Member() throws IPCException{
		DAFMember dafMember = new DAFMember();
		dafMember.setApplicationProcessName("test");
		dafMember.setApplicationProcessInstance("1");
		dafMember.setSynonym(2);
		ipcProcess.addDAFMember(dafMember);
		
		long address = addressManager.getAvailableAddress();
		System.out.println("Got address "+address);
		Assert.assertNotSame(1L, address);
		Assert.assertNotSame(2L, address);
	}
	
	@Test
	public void testWithNMembers() throws IPCException{
		DAFMember dafMember = null;
		for(int i=2; i<=MEMBERS; i++){
			dafMember = new DAFMember();
			dafMember.setApplicationProcessName("test");
			dafMember.setApplicationProcessInstance(""+i);
			dafMember.setSynonym(i);
			ipcProcess.addDAFMember(dafMember);
		}
		
		long address = addressManager.getAvailableAddress();
		System.out.println("Got address "+address);
		for(int i=1; i<=MEMBERS; i++){
			Assert.assertNotSame(i, address);
		}
	}
	
	@Test
	public void testNoAvailableAddresses(){
		DAFMember dafMember = null;
		String errorMessage = null;
		int errorCode = 0;
		
		for(int i=2; i<=65535; i++){
			dafMember = new DAFMember();
			dafMember.setApplicationProcessName("test");
			dafMember.setApplicationProcessInstance(""+i);
			dafMember.setSynonym(i);
			ipcProcess.addDAFMember(dafMember);
		}
		
		try{
			addressManager.getAvailableAddress();
		}catch(IPCException ex){
			errorCode = ex.getErrorCode();
			errorMessage = ex.getMessage();
			System.out.println(errorMessage);
			System.out.println(errorCode);
		}
		
		Assert.assertEquals(IPCException.NO_AVAILABLE_ADDRESSES, errorMessage);
		Assert.assertEquals(IPCException.NO_AVAILABLE_ADDRESSES_CODE, errorCode);
	}
}
