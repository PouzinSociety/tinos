package rina.utils.serialization.googleprotobuf.flow.tests;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import rina.efcp.api.EFCPConstants;
import rina.flowallocator.api.ConnectionId;
import rina.flowallocator.api.message.Flow;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.utils.serialization.googleprotobuf.flow.FlowSerializer;
import rina.utils.types.Unsigned;

/**
 * Test if the serialization/deserialization mechanisms for the Flow object work
 * @author eduardgrasa
 *
 */
public class FlowSerializerTest {
	
	private Flow flow = null;
	private FlowSerializer flowSerializer = null;
	
	@Before
	public void setup(){
		flowSerializer = new FlowSerializer();
		flow = new Flow();
		flow.setAccessControl(new byte[]{0x01, 0x02, 0x03, 0x04});
		flow.setCreateFlowRetries(2);
		flow.setCurrentFlowId(0);
		flow.setDestinationAddress(new byte[]{0x01, 0x00});
		flow.setDestinationNamingInfo(new ApplicationProcessNamingInfo("b", null, null, null));
		flow.setDestinationPortId(new Unsigned(EFCPConstants.PortIdLength, 8));
		flow.setHopCount(3);
		List<ConnectionId> flowIds = new ArrayList<ConnectionId>();
		ConnectionId connectionId = new ConnectionId();
		connectionId.setDestinationCEPId(new Unsigned(EFCPConstants.CEPIdLength, 43));
		connectionId.setSourceCEPId(new Unsigned(EFCPConstants.CEPIdLength, 55));
		connectionId.setQosId(new Unsigned(EFCPConstants.QoSidLength, 1));
		flowIds.add(connectionId);
		flow.setFlowIds(flowIds);
		flow.setSourceAddress(new byte[]{0x00, 0x01});
		flow.setSourceNamingInfo(new ApplicationProcessNamingInfo("a", null, null, null));
		flow.setSourcePortId(new Unsigned(EFCPConstants.PortIdLength, 5));
	}
	
	@Test
	public void testSerilalization() throws Exception{
		byte[] serializedFlow = flowSerializer.serialize(flow);
		for(int i=0; i<serializedFlow.length; i++){
			System.out.print(serializedFlow[i] + " ");
		}
		
		Flow recoveredFlow = (Flow) flowSerializer.deserialize(serializedFlow, Flow.class.toString());
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
