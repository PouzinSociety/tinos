package rina.encoding.impl.googleprotobuf.flow.tests;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import rina.efcp.api.BaseDataTransferAE;
import rina.efcp.api.DataTransferAE;
import rina.encoding.impl.googleprotobuf.flow.FlowEncoder;
import rina.flowallocator.api.ConnectionId;
import rina.flowallocator.api.message.Flow;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.utils.types.Unsigned;

/**
 * Test if the serialization/deserialization mechanisms for the Flow object work
 * @author eduardgrasa
 *
 */
public class FlowEncoderTest {
	
	private Flow flow = null;
	private FlowEncoder flowSerializer = null;
	private IPCProcess fakeIPCProcess = null;
	
	@Before
	public void setup(){
		fakeIPCProcess = new FakeIPCProcess();
		DataTransferAE dataTransferAE = (DataTransferAE) fakeIPCProcess.getIPCProcessComponent(BaseDataTransferAE.getComponentName());
		flowSerializer = new FlowEncoder();
		flowSerializer.setIPCProcess(fakeIPCProcess);
		flow = new Flow();
		flow.setAccessControl(new byte[]{0x01, 0x02, 0x03, 0x04});
		flow.setCreateFlowRetries(2);
		flow.setCurrentFlowId(0);
		flow.setDestinationAddress(new byte[]{0x01, 0x00});
		flow.setDestinationNamingInfo(new ApplicationProcessNamingInfo("b", null, null, null));
		flow.setDestinationPortId(new Unsigned(dataTransferAE.getDataTransferConstants().getPortIdLength(), 8));
		flow.setHopCount(3);
		List<ConnectionId> flowIds = new ArrayList<ConnectionId>();
		ConnectionId connectionId = new ConnectionId();
		connectionId.setDestinationCEPId(new Unsigned(dataTransferAE.getDataTransferConstants().getCepIdLength(), 43));
		connectionId.setSourceCEPId(new Unsigned(dataTransferAE.getDataTransferConstants().getCepIdLength(), 55));
		connectionId.setQosId(new Unsigned(dataTransferAE.getDataTransferConstants().getQosIdLength(), 1));
		flowIds.add(connectionId);
		flow.setFlowIds(flowIds);
		flow.setSourceAddress(new byte[]{0x00, 0x01});
		flow.setSourceNamingInfo(new ApplicationProcessNamingInfo("a", null, null, null));
		flow.setSourcePortId(new Unsigned(dataTransferAE.getDataTransferConstants().getPortIdLength(), 5));
	}
	
	@Test
	public void testSerilalization() throws Exception{
		byte[] serializedFlow = flowSerializer.encode(flow);
		for(int i=0; i<serializedFlow.length; i++){
			System.out.print(serializedFlow[i] + " ");
		}
		
		Flow recoveredFlow = (Flow) flowSerializer.decode(serializedFlow, Flow.class.toString());
		Assert.assertArrayEquals(flow.getAccessControl(), recoveredFlow.getAccessControl());
		Assert.assertEquals(flow.getCreateFlowRetries(), recoveredFlow.getCreateFlowRetries());
		Assert.assertEquals(flow.getCurrentFlowId(), recoveredFlow.getCurrentFlowId());
		Assert.assertArrayEquals(flow.getDestinationAddress(), recoveredFlow.getDestinationAddress());
		Assert.assertEquals(flow.getDestinationNamingInfo(), recoveredFlow.getDestinationNamingInfo());
		Assert.assertEquals(flow.getDestinationPortId(), recoveredFlow.getDestinationPortId());
		Assert.assertEquals(flow.getHopCount(), recoveredFlow.getHopCount());
		Assert.assertEquals(flow.getFlowIds().get(0).getDestinationCEPId(), recoveredFlow.getFlowIds().get(0).getDestinationCEPId());
		Assert.assertEquals(flow.getFlowIds().get(0).getSourceCEPId(), recoveredFlow.getFlowIds().get(0).getSourceCEPId());
		Assert.assertEquals(flow.getFlowIds().get(0).getQosId(), recoveredFlow.getFlowIds().get(0).getQosId());
		Assert.assertArrayEquals(flow.getSourceAddress(), recoveredFlow.getSourceAddress());
		Assert.assertEquals(flow.getSourceNamingInfo(), recoveredFlow.getSourceNamingInfo());
		Assert.assertEquals(flow.getSourcePortId(), recoveredFlow.getSourcePortId());
	}

}
