package rina.shimipcprocess.ip.flowallocator;

import java.net.DatagramSocket;
import java.net.Socket;

import rina.ipcservice.api.APService;
import rina.ipcservice.api.FlowService;
import rina.shimipcprocess.ip.BlockinqQueueReader;

/**
 * Captures the state of a flow
 * @author eduardgrasa
 *
 */
public class FlowState {

	public enum State {NULL, ALLOCATION_REQUESTED, ALLOCATED, DEALLOCATION_REQUESTED};

	private FlowService flowService = null;
	private APService applicationCallback = null;
	private int portId = 0;
	private State state = State.NULL;
	private Socket socket = null;
	private DatagramSocket datagramSocket = null;
	private String blockingQueueId = null;
	private BlockinqQueueReader blockingQueueReader = null;

	public FlowService getFlowService() {
		return flowService;
	}

	public void setFlowService(FlowService flowService) {
		this.flowService = flowService;
	}

	public APService getApplicationCallback() {
		return applicationCallback;
	}

	public void setApplicationCallback(APService applicationCallback) {
		this.applicationCallback = applicationCallback;
	}

	public int getPortId() {
		return portId;
	}

	public void setPortId(int portId) {
		this.portId = portId;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public DatagramSocket getDatagramSocket() {
		return datagramSocket;
	}

	public void setDatagramSocket(DatagramSocket datagramSocket) {
		this.datagramSocket = datagramSocket;
	}

	public String getBlockingQueueId() {
		return blockingQueueId;
	}

	public void setBlockingQueueId(String blockingQueueId) {
		this.blockingQueueId = blockingQueueId;
	}

	public BlockinqQueueReader getBlockingQueueReader() {
		return blockingQueueReader;
	}

	public void setBlockingQueueReader(BlockinqQueueReader blockingQueueReader) {
		this.blockingQueueReader = blockingQueueReader;
	}
}