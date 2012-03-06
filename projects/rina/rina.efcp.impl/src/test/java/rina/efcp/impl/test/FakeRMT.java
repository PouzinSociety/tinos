package rina.efcp.impl.test;

import rina.efcp.api.DataTransferConstants;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.QualityOfServiceSpecification;
import rina.rmt.api.RMT;

/**
 * A fake implementation of the RMT that prints the PDU it gets 
 * passed and stores it
 * @author eduardgrasa
 *
 */
public class FakeRMT implements RMT{
	
	/**
	 * The latest pdu sent to this RMT
	 */
	private byte[] pdu = null;
	
	private DataTransferConstants dataTransferConstants = new DataTransferConstants();
	
	public void sendEFCPPDU(byte[] pdu) {
		System.out.println("Send the following PDU, of "+pdu.length+" bytes:");
		this.pdu = pdu;
		String toPrint = "";
		int index = 0;
		toPrint = toPrint + "(Version) " + String.format("0x%02X", pdu[index]) + "| ";
		index = index +1;
		
		toPrint = toPrint + "(Source Address)";
		for(int i=index; i<index + dataTransferConstants.getAddressLength(); i++){
			toPrint = toPrint + " " + String.format("0x%02X", pdu[i]);
		}
		toPrint = toPrint + "| ";
		index = index + dataTransferConstants.getAddressLength();
		
		toPrint = toPrint + "(Destination Address)";
		for(int i=index; i<index + dataTransferConstants.getAddressLength(); i++){
			toPrint = toPrint + " " + String.format("0x%02X", pdu[i]);
		}
		toPrint = toPrint + "| ";
		index = index + dataTransferConstants.getAddressLength();
		
		toPrint = toPrint + "(Qos Id)";
		for(int i=index; i<index + dataTransferConstants.getQosIdLength(); i++){
			toPrint = toPrint + " " + String.format("0x%02X", pdu[i]);
		}
		toPrint = toPrint + "| ";
		index = index + dataTransferConstants.getQosIdLength();
		
		toPrint = toPrint + "(Source CEP Id)";
		for(int i=index; i<index + dataTransferConstants.getCepIdLength(); i++){
			toPrint = toPrint + " " + String.format("0x%02X", pdu[i]);
		}
		toPrint = toPrint + "| ";
		index = index + dataTransferConstants.getCepIdLength();
		
		toPrint = toPrint + "(Destination CEP Id)";
		for(int i=index; i<index + dataTransferConstants.getCepIdLength(); i++){
			toPrint = toPrint + " " + String.format("0x%02X", pdu[i]);
		}
		toPrint = toPrint + "| ";
		index = index + dataTransferConstants.getCepIdLength();
		
		toPrint = toPrint + "(PDU Type)";
		for(int i=index; i<index + 1; i++){
			toPrint = toPrint + " " + String.format("0x%02X", pdu[i]);
		}
		toPrint = toPrint + "| ";
		index = index + 1;
		
		toPrint = toPrint + "(Flags)";
		for(int i=index; i<index + 1; i++){
			toPrint = toPrint + " " + String.format("0x%02X", pdu[i]);
		}
		toPrint = toPrint + "| ";
		index = index + 1;
		
		toPrint = toPrint + "(Length)";
		for(int i=index; i<index + dataTransferConstants.getLengthLength(); i++){
			toPrint = toPrint + " " + String.format("0x%02X", pdu[i]);
		}
		toPrint = toPrint + "| ";
		index = index + dataTransferConstants.getLengthLength();
		
		toPrint = toPrint + "(Seq number)";
		for(int i=index; i<index + dataTransferConstants.getSequenceNumberLength(); i++){
			toPrint = toPrint + " " + String.format("0x%02X", pdu[i]);
		}
		toPrint = toPrint + "| ";
		index = index + dataTransferConstants.getSequenceNumberLength();
		
		toPrint = toPrint + "(User data)";
		for(int i=index; i<pdu.length; i++){
			toPrint = toPrint + " " + String.format("0x%02X", pdu[i]);
		}
		toPrint = toPrint + "| ";
		
		System.out.println(toPrint);	
	}

	public byte[] getPdu() {
		return pdu;
	}

	public void setIPCProcess(IPCProcess arg0) {
		// TODO Auto-generated method stub
		
	}

	public void sendCDAPMessage(int arg0, byte[] arg1) {
		// TODO Auto-generated method stub
		
	}

	public int allocateFlow(ApplicationProcessNamingInfo arg0,
			QualityOfServiceSpecification arg1) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getName() {
		return RMT.class.getName();
	}

	public void deallocateFlow(int arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void stop() {
		// TODO Auto-generated method stub
		
	}

	public String getIPAddressFromApplicationNamingInformation(String arg0,
			String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public IPCProcess getIPCProcess() {
		// TODO Auto-generated method stub
		return null;
	}

	public void startListening() {
		// TODO Auto-generated method stub
		
	}
}