package rina.encoding.impl.googleprotobuf.flowservice.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import rina.encoding.impl.googleprotobuf.flowservice.FlowServiceEncoder;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.FlowService;

/**
 * Test if the serialization/deserialization mechanisms for the FlowService object work
 * @author eduardgrasa
 *
 */
public class FlowServiceEncoderTest {
	
	private FlowService flowService = null;
	private FlowService flowService2 = null;
	private FlowServiceEncoder flowServiceEncoder = null;
	
	@Before
	public void setup(){
		flowServiceEncoder = new FlowServiceEncoder();
		
		flowService = new FlowService();
		flowService.setSourceAPNamingInfo(new ApplicationProcessNamingInfo("a", "1"));
		flowService.setDestinationAPNamingInfo(new ApplicationProcessNamingInfo("b", "1"));
		flowService.setPortId(3327);
		
		flowService2 = new FlowService();
		flowService2.setDestinationAPNamingInfo(new ApplicationProcessNamingInfo("d", "1"));
		flowService2.setPortId(33223);
	}
	
	@Test
	public void testSerilalization() throws Exception{
		byte[] encodedRequest = flowServiceEncoder.encode(flowService);
		for(int i=0; i<encodedRequest.length; i++){
			System.out.print(encodedRequest[i] + " ");
		}
		System.out.println("");
		
		FlowService recoveredRequest= (FlowService) flowServiceEncoder.decode(encodedRequest, FlowService.class.toString());
		Assert.assertEquals(flowService.getPortId(), recoveredRequest.getPortId());
		Assert.assertEquals(flowService.getSourceAPNamingInfo(), recoveredRequest.getSourceAPNamingInfo());
		Assert.assertEquals(flowService.getDestinationAPNamingInfo(), recoveredRequest.getDestinationAPNamingInfo());
		
		encodedRequest = flowServiceEncoder.encode(flowService2);
		for(int i=0; i<encodedRequest.length; i++){
			System.out.print(encodedRequest[i] + " ");
		}
		System.out.println("");
		
		recoveredRequest= (FlowService) flowServiceEncoder.decode(encodedRequest, FlowService.class.toString());
		Assert.assertEquals(flowService2.getPortId(), recoveredRequest.getPortId());
		Assert.assertEquals(flowService2.getDestinationAPNamingInfo(), recoveredRequest.getDestinationAPNamingInfo());
	}

}
