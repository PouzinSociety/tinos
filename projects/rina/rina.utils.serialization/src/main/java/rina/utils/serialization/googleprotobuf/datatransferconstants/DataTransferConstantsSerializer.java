package rina.utils.serialization.googleprotobuf.datatransferconstants;

import rina.efcp.api.DataTransferConstants;
import rina.ipcprocess.api.IPCProcess;
import rina.serialization.api.Serializer;

public class DataTransferConstantsSerializer implements Serializer{
	
	public void setIPCProcess(IPCProcess ipcProcess) {
	}

	public Object deserialize(byte[] serializedObject, String objectClass) throws Exception {
		if (objectClass == null || !(objectClass.equals(DataTransferConstants.class.toString()))){
			throw new Exception("This is not the serializer for objects of type "+objectClass);
		}
		
		DataTransferConstantsMessage.dataTransferConstants_t gpbDataTransferConstants = 
			DataTransferConstantsMessage.dataTransferConstants_t.parseFrom(serializedObject);
		
		DataTransferConstants dataTransferConstants = new DataTransferConstants();
		dataTransferConstants.setAddressLength(gpbDataTransferConstants.getAddressLength());
		dataTransferConstants.setCepIdLength(gpbDataTransferConstants.getCepIdLength());
		dataTransferConstants.setDIFConcatenation(gpbDataTransferConstants.getDIFConcatenation());
		dataTransferConstants.setDIFFragmentation(gpbDataTransferConstants.getDIFFragmentation());
		dataTransferConstants.setDIFIntegrity(gpbDataTransferConstants.getDIFIntegrity());
		dataTransferConstants.setLengthLength(gpbDataTransferConstants.getLengthLength());
		dataTransferConstants.setMaxPDULifetime(gpbDataTransferConstants.getMaxPDULifetime());
		dataTransferConstants.setMaxPDUSize(gpbDataTransferConstants.getMaxPDUSize());
		dataTransferConstants.setPortIdLength(gpbDataTransferConstants.getPortIdLength());
		dataTransferConstants.setQosIdLength(gpbDataTransferConstants.getQosidLength());
		dataTransferConstants.setSequenceNumberLength(gpbDataTransferConstants.getSequenceNumberLength());
		
		return dataTransferConstants;
	}

	public byte[] serialize(Object object) throws Exception {
		if (object == null || !(object instanceof DataTransferConstants)){
			throw new Exception("This is not the serializer for objects of type " + DataTransferConstants.class.toString());
		}
		
		DataTransferConstants dataTransferConstants = (DataTransferConstants) object;
		
		DataTransferConstantsMessage.dataTransferConstants_t gpbDataTransferConstants = 
			DataTransferConstantsMessage.dataTransferConstants_t.newBuilder().
										setAddressLength(dataTransferConstants.getAddressLength()).
										setCepIdLength(dataTransferConstants.getCepIdLength()).
										setDIFConcatenation(dataTransferConstants.isDIFConcatenation()).
										setDIFFragmentation(dataTransferConstants.isDIFFragmentation()).
										setDIFIntegrity(dataTransferConstants.isDIFIntegrity()).
										setLengthLength(dataTransferConstants.getLengthLength()).
										setMaxPDULifetime(dataTransferConstants.getMaxPDULifetime()).
										setMaxPDUSize(dataTransferConstants.getMaxPDUSize()).
										setPortIdLength(dataTransferConstants.getPortIdLength()).
										setQosidLength(dataTransferConstants.getQosIdLength()).
										setSequenceNumberLength(dataTransferConstants.getSequenceNumberLength()).
										build();
		
		return gpbDataTransferConstants.toByteArray();
	}

}
