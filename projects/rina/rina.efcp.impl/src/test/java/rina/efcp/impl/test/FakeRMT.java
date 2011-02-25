package rina.efcp.impl.test;

import rina.efcp.api.EFCPConstants;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.IPCException;
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
	
	public void sendEFCPPDU(byte[] pdu) {
		System.out.println("Send the following PDU, of "+pdu.length+" bytes:");
		this.pdu = pdu;
		String toPrint = "";
		int index = 0;
		toPrint = toPrint + "(Version) " + String.format("0x%02X", pdu[index]) + "| ";
		index = index +1;
		
		toPrint = toPrint + "(Source Address)";
		for(int i=index; i<index + EFCPConstants.addressLength; i++){
			toPrint = toPrint + " " + String.format("0x%02X", pdu[i]);
		}
		toPrint = toPrint + "| ";
		index = index + EFCPConstants.addressLength;
		
		toPrint = toPrint + "(Destination Address)";
		for(int i=index; i<index + EFCPConstants.addressLength; i++){
			toPrint = toPrint + " " + String.format("0x%02X", pdu[i]);
		}
		toPrint = toPrint + "| ";
		index = index + EFCPConstants.addressLength;
		
		toPrint = toPrint + "(Qos Id)";
		for(int i=index; i<index + EFCPConstants.QoSidLength; i++){
			toPrint = toPrint + " " + String.format("0x%02X", pdu[i]);
		}
		toPrint = toPrint + "| ";
		index = index + EFCPConstants.QoSidLength;
		
		toPrint = toPrint + "(Source CEP Id)";
		for(int i=index; i<index + EFCPConstants.CEPIdLength; i++){
			toPrint = toPrint + " " + String.format("0x%02X", pdu[i]);
		}
		toPrint = toPrint + "| ";
		index = index + EFCPConstants.CEPIdLength;
		
		toPrint = toPrint + "(Destination CEP Id)";
		for(int i=index; i<index + EFCPConstants.CEPIdLength; i++){
			toPrint = toPrint + " " + String.format("0x%02X", pdu[i]);
		}
		toPrint = toPrint + "| ";
		index = index + EFCPConstants.CEPIdLength;
		
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
		for(int i=index; i<index + EFCPConstants.lengthLength; i++){
			toPrint = toPrint + " " + String.format("0x%02X", pdu[i]);
		}
		toPrint = toPrint + "| ";
		index = index + EFCPConstants.lengthLength;
		
		toPrint = toPrint + "(Seq number)";
		for(int i=index; i<index + EFCPConstants.SequenceNumberLength; i++){
			toPrint = toPrint + " " + String.format("0x%02X", pdu[i]);
		}
		toPrint = toPrint + "| ";
		index = index + EFCPConstants.SequenceNumberLength;
		
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

	public void sendCDAPMessage(byte[] arg0, byte[] arg1) throws IPCException {
		// TODO Auto-generated method stub
		
	}
}