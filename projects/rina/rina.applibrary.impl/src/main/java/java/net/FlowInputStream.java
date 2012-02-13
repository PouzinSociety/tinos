package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FlowInputStream extends InputStream{

	BlockingQueue<byte[]> pendingSDUs = null;
	boolean open = false;
	byte[] currentSDU = null;
	int position = 0;
	
	public FlowInputStream(){
		pendingSDUs = new LinkedBlockingQueue<byte[]>();
		open = true;
	}
	
	public void addSDU(byte[] sdu){
		pendingSDUs.add(sdu);
	}
	
	public boolean isOpen(){
		return open;
	}
	
	public boolean isClosed(){
		return !open;
	}
	
	public void close(){
		open = false;
		//Add an SDU in case there was a thread blocked on the read() call
		pendingSDUs.add(new byte[]{1});
	}
	
	@Override
	public int read() throws IOException {
		if (isClosed()){
			return -1;
		}

		if (currentSDU == null){
			try{
				currentSDU = pendingSDUs.take();
			}catch(Exception ex){
				throw new IOException(ex);
			}
			
			//As the last method blocks, make sure the 
			//flow is still open
			if (isClosed()){
				return -1;
			}
		}
		
		int result = (int) currentSDU[position];
		position = position +1;
		if (position == currentSDU.length){
			resetSDU();
		}
		
		return result;
	}
	
	private void resetSDU(){
		currentSDU = null;
		position = 0;
	}
}
