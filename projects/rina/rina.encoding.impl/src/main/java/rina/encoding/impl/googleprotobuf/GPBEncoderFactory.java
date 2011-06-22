package rina.encoding.impl.googleprotobuf;

import rina.applicationprocess.api.ApplicationProcessNameSynonym;
import rina.applicationprocess.api.WhatevercastName;
import rina.efcp.api.DataTransferConstants;
import rina.encoding.api.Encoder;
import rina.encoding.api.EncoderFactory;
import rina.encoding.impl.EncoderImpl;
import rina.encoding.impl.googleprotobuf.apnamesynonim.ApplicationProcessNameSynonymEncoder;
import rina.encoding.impl.googleprotobuf.datatransferconstants.DataTransferConstantsEncoder;
import rina.encoding.impl.googleprotobuf.flow.FlowEncoder;
import rina.encoding.impl.googleprotobuf.qoscube.QoSCubeEncoder;
import rina.encoding.impl.googleprotobuf.whatevercast.WhatevercastNameEncoder;
import rina.flowallocator.api.QoSCube;
import rina.flowallocator.api.message.Flow;

/**
 * Creates instances of encoders that encode/decode objects using the Google protocol buffers 
 * encoding/decoding format
 * @author eduardgrasa
 *
 */
public class GPBEncoderFactory implements EncoderFactory{

	public Encoder createEncoderInstance() {
		EncoderImpl encoder = new EncoderImpl();
		
		encoder.addEncoder(ApplicationProcessNameSynonym.class.toString(), new ApplicationProcessNameSynonymEncoder());
		encoder.addEncoder(DataTransferConstants.class.toString(), new DataTransferConstantsEncoder());
		encoder.addEncoder(Flow.class.toString(), new FlowEncoder());
		encoder.addEncoder(QoSCube.class.toString(), new QoSCubeEncoder());
		encoder.addEncoder(WhatevercastName.class.toString(), new WhatevercastNameEncoder());
		
		return encoder;
	}

}
