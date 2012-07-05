package rina.shimipcprocess.ip;

import java.util.TimerTask;

public class StopBlockingQueueReaderTimerTask extends TimerTask{

	private BlockinqQueueReader blockingQueueReader = null;
	
	public StopBlockingQueueReaderTimerTask(BlockinqQueueReader blockingQueueReader){
		this.blockingQueueReader = blockingQueueReader;
	}
	
	@Override
	public void run() {
		this.blockingQueueReader.stop();
	}
	

}
