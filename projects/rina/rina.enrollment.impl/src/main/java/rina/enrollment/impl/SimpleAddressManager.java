package rina.enrollment.impl;

import java.util.List;

import rina.applicationprocess.api.DAFMember;
import rina.efcp.api.DataTransferConstants;
import rina.enrollment.api.AddressManager;
import rina.enrollment.api.EnrollmentTask;
import rina.ipcservice.api.IPCException;

/**
 * Allocates addresses by simple enumeration
 * @author eduardgrasa
 *
 */
public class SimpleAddressManager implements AddressManager{
	
	private EnrollmentTask enrollmentTask = null;
	
	/**
	 * The next address that might be available
	 */
	private long nextAvailableAddress = 1;
	
	/**
	 * The maximum address possible on the DIF
	 */
	private long maxAddress = -1;
	
	public SimpleAddressManager(EnrollmentTask enrollmentTask){
		this.enrollmentTask = enrollmentTask;
		initializeMaxAddress();
	}
	
	private void initializeMaxAddress(){
		DataTransferConstants dataTransferConstants = enrollmentTask.getIPCProcess().getDataTransferConstants();
		if (dataTransferConstants != null){
			int addressLengthInBits = 8*dataTransferConstants.getAddressLength();
			maxAddress = new Double(Math.pow(2, new Long(addressLengthInBits).doubleValue()) - 1).longValue();
		}
	}

	/**
	 * Returns an available address for a remote process that is joining the DIF.
	 * @return
	 */
	public synchronized long getAvailableAddress() throws IPCException{
		Long myAddress = this.enrollmentTask.getIPCProcess().getAddress();
		List<DAFMember> dafMembers = enrollmentTask.getIPCProcess().getDAFMembers();
		if (dafMembers.size() + 1 == maxAddress){
			throw new IPCException(IPCException.NO_AVAILABLE_ADDRESSES_CODE, IPCException.NO_AVAILABLE_ADDRESSES);
		}
		
		if (maxAddress == -1){
			initializeMaxAddress();
		}
		
		incrementNextAvailableAddress();
		
		while (addressInUse(nextAvailableAddress, dafMembers, myAddress)){
			incrementNextAvailableAddress();
		}
		
		return nextAvailableAddress;
	}
	
	private void incrementNextAvailableAddress(){
		if (nextAvailableAddress<maxAddress){
			nextAvailableAddress++;
		}else{
			nextAvailableAddress = 1;
		}
	}
	
	/**
	 * True if an address is in use, false otherwise
	 * @param address
	 * @return
	 */
	public synchronized boolean addressInUse(long address) {
		return addressInUse(address, 
				enrollmentTask.getIPCProcess().getDAFMembers(), 
				this.enrollmentTask.getIPCProcess().getAddress());
	}
	
	private boolean addressInUse(long address, List<DAFMember> dafMembers, Long myAddress){
		if (myAddress != null && (myAddress.longValue() == address)){
			return true;
		}
		
		for(int i=0; i<dafMembers.size(); i++){
			if (dafMembers.get(i).getSynonym() == address){
				return true;
			}
		}
		
		return false;
	}
}
