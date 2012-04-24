package java.net;

import java.io.IOException;
import java.io.OutputStream;

import rina.applibrary.api.FlowImpl;
import rina.ipcservice.api.IPCException;

public class FlowOutputStream extends OutputStream{

	private FlowImpl flowImpl = null;
	private byte[] aux = null;
	
	public FlowOutputStream(FlowImpl flowImpl){
		this.flowImpl = flowImpl;
	}
	
	@Override
	public void write(int data) throws IOException {
		try{
			aux = new byte[]{(byte) data};
			flowImpl.write(aux);
		}catch(IPCException ex){
			throw new IOException(ex);
		}
	}
	
	@Override
	public void write(byte data[]) throws IOException {
		if (data == null) {
            throw new NullPointerException();
		}
		
		try{
			flowImpl.write(data);
		}catch(IPCException ex){
			throw new IOException(ex);
		}
    }
	
	@Override
	public void write(byte data[], int off, int len) throws IOException {
		if (data == null) {
			throw new NullPointerException();
		} else if ((off < 0) || (off > data.length) || (len < 0) ||
				((off + len) > data.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}
		
		aux = new byte[len];
		for (int i = 0 ; i < len ; i++) {
            aux[i] = data[off + i];
        }
		
		try{
			flowImpl.write(aux);
		}catch(IPCException ex){
			throw new IOException(ex);
		}
	}

}
