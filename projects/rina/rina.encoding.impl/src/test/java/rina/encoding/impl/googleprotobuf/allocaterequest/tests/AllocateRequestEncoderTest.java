package rina.encoding.impl.googleprotobuf.allocaterequest.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import rina.encoding.impl.googleprotobuf.allocaterequest.AllocateRequestEncoder;
import rina.ipcservice.api.AllocateRequest;
import rina.ipcservice.api.ApplicationProcessNamingInfo;

/**
 * Test if the serialization/deserialization mechanisms for the AllocateRequest object work
 * @author eduardgrasa
 *
 */
public class AllocateRequestEncoderTest {
	
	private AllocateRequest allocateRequest = null;
	private AllocateRequest allocateRequest2 = null;
	private AllocateRequestEncoder allocateRequestEncoder = null;
	
	@Before
	public void setup(){
		allocateRequestEncoder = new AllocateRequestEncoder();
		
		allocateRequest = new AllocateRequest();
		allocateRequest.setSourceAPNamingInfo(new ApplicationProcessNamingInfo("a", "1"));
		allocateRequest.setDestinationAPNamingInfo(new ApplicationProcessNamingInfo("b", "1"));
		allocateRequest.setPortId(3327);
		
		allocateRequest2 = new AllocateRequest();
		allocateRequest2.setDestinationAPNamingInfo(new ApplicationProcessNamingInfo("d", "1"));
		allocateRequest2.setPortId(33223);
	}
	
	@Test
	public void testSerilalization() throws Exception{
		byte[] encodedRequest = allocateRequestEncoder.encode(allocateRequest);
		for(int i=0; i<encodedRequest.length; i++){
			System.out.print(encodedRequest[i] + " ");
		}
		System.out.println("");
		
		AllocateRequest recoveredRequest= (AllocateRequest) allocateRequestEncoder.decode(encodedRequest, AllocateRequest.class.toString());
		Assert.assertEquals(allocateRequest.getPortId(), recoveredRequest.getPortId());
		Assert.assertEquals(allocateRequest.getSourceAPNamingInfo(), recoveredRequest.getSourceAPNamingInfo());
		Assert.assertEquals(allocateRequest.getDestinationAPNamingInfo(), recoveredRequest.getDestinationAPNamingInfo());
		
		encodedRequest = allocateRequestEncoder.encode(allocateRequest2);
		for(int i=0; i<encodedRequest.length; i++){
			System.out.print(encodedRequest[i] + " ");
		}
		System.out.println("");
		
		recoveredRequest= (AllocateRequest) allocateRequestEncoder.decode(encodedRequest, AllocateRequest.class.toString());
		Assert.assertEquals(allocateRequest2.getPortId(), recoveredRequest.getPortId());
		Assert.assertEquals(allocateRequest2.getDestinationAPNamingInfo(), recoveredRequest.getDestinationAPNamingInfo());
	}

}
