package rina.cdap.impl.googleprotobuf;

import com.google.protobuf.ByteString;

import rina.cdap.api.CDAPException;
import rina.cdap.api.message.AuthValue;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.cdap.api.message.CDAPMessage.AuthTypes;
import rina.cdap.api.message.CDAPMessage.Flags;
import rina.cdap.api.message.CDAPMessage.Opcode;
import rina.cdap.impl.WireMessageProvider;
import rina.cdap.impl.googleprotobuf.CDAP.authTypes_t;
import rina.cdap.impl.googleprotobuf.CDAP.authValue_t;
import rina.cdap.impl.googleprotobuf.CDAP.flagValues_t;
import rina.cdap.impl.googleprotobuf.CDAP.objVal_t;
import rina.cdap.impl.googleprotobuf.CDAP.opCode_t;

public class GoogleProtocolBufWireMessageProvider implements WireMessageProvider{

	public CDAPMessage deserializeMessage(byte[] message) throws CDAPException {
		try{
			CDAP.CDAPMessage cdapMessage = CDAP.CDAPMessage.parseFrom(message);
			AuthTypes authMech = getAuthMech(cdapMessage);
			AuthValue authValue = getAuthValue(cdapMessage);
			Flags flags = getFlags(cdapMessage);
			ObjectValue objValue = getObjValue(cdapMessage);
			Opcode opCode = getOpcode(cdapMessage);

			CDAPMessage response = new CDAPMessage();
			response.setAbsSyntax(cdapMessage.getAbsSyntax());
			response.setAuthMech(authMech);
			response.setAuthValue(authValue);
			response.setDestAEInst(cdapMessage.getDestAEInst());
			response.setDestAEName(cdapMessage.getDestAEName());
			response.setDestApInst(cdapMessage.getDestApInst());
			response.setDestApName(cdapMessage.getDestApName());
			response.setFilter(cdapMessage.getFilter().toByteArray());
			response.setFlags(flags);
			response.setInvokeID(cdapMessage.getInvokeID());
			response.setObjClass(cdapMessage.getObjClass());
			response.setObjInst(cdapMessage.getObjInst());
			response.setObjName(cdapMessage.getObjName());
			response.setObjValue(objValue);
			response.setOpCode(opCode);
			response.setResult(cdapMessage.getResult());
			response.setResultReason(cdapMessage.getResultReason());
			response.setScope(cdapMessage.getScope());
			response.setSrcAEInst(cdapMessage.getSrcAEInst());
			response.setSrcAEName(cdapMessage.getSrcAEName());
			response.setSrcApInst(cdapMessage.getSrcApInst());
			response.setSrcApName(cdapMessage.getSrcApName());
			response.setVersion(cdapMessage.getVersion());

			return response;
		}catch(Exception ex){
			throw new CDAPException(ex);
		}
	}
	
	private AuthTypes getAuthMech(CDAP.CDAPMessage cdapMessage){
		AuthTypes authMech = null;
		
		if (cdapMessage.getAuthMech().equals(null)){
			return null;
		}else if (cdapMessage.getAuthMech().equals(authTypes_t.AUTH_NONE)){
			authMech = AuthTypes.AUTH_NONE;
		}else if (cdapMessage.getAuthMech().equals(authTypes_t.AUTH_PASSWD)){
			authMech = AuthTypes.AUTH_PASSWD;
		}else if (cdapMessage.getAuthMech().equals(authTypes_t.AUTH_SSHDSA)){
			authMech = AuthTypes.AUTH_SSHDSA;
		}else if (cdapMessage.getAuthMech().equals(authTypes_t.AUTH_SSHRSA)){
			authMech = AuthTypes.AUTH_SSHRSA;
		}
		
		return authMech;
	}
	
	private AuthValue getAuthValue(CDAP.CDAPMessage cdapMessage){
		AuthValue authValue = null;
		
		if (cdapMessage.getAuthValue() == null){
			return null;
		}else{
			authValue = new AuthValue();
			authValue.setAuthName(cdapMessage.getAuthValue().getAuthName());
			if (cdapMessage.getAuthValue().getAuthOther() != null){
				authValue.setAuthOther(cdapMessage.getAuthValue().getAuthOther().toByteArray());
			}
			authValue.setAuthPassword(cdapMessage.getAuthValue().getAuthPassword());
		}
		
		return authValue;
	}
	
	private Flags getFlags(CDAP.CDAPMessage cdapMessage){
		Flags flags = null;
		
		if (cdapMessage.getFlags() == null){
			return null;
		}else if (cdapMessage.getFlags().equals(flagValues_t.F_RD_INCOMPLETE)){
			flags = Flags.F_RD_INCOMPLETE;
		}else if (cdapMessage.getFlags().equals(flagValues_t.F_SYNC)){
			flags = Flags.F_SYNC;
		}
		
		return flags;
	}
	
	private ObjectValue getObjValue(CDAP.CDAPMessage cdapMessage){
		ObjectValue objectValue = null;
		
		if (cdapMessage.getObjValue() != null){
			objectValue = new ObjectValue();
			if (cdapMessage.getObjValue().getByteval() != null){
				objectValue.setByteval(cdapMessage.getObjValue().getByteval().toByteArray());
			}
			objectValue.setDoubleval(cdapMessage.getObjValue().getDoubleval());
			objectValue.setFloatval(cdapMessage.getObjValue().getFloatval());
			objectValue.setInt64val(cdapMessage.getObjValue().getInt64Val());
			objectValue.setIntval(cdapMessage.getObjValue().getIntval());
			objectValue.setSint64val(cdapMessage.getObjValue().getSint64Val());
			objectValue.setSintval(cdapMessage.getObjValue().getSintval());
			objectValue.setStrval(cdapMessage.getObjValue().getStrval());
		}
		
		return objectValue;
	}
	
	private Opcode getOpcode(CDAP.CDAPMessage cdapMessage){
		Opcode opcode = null;
		
		if (cdapMessage.getOpCode() == null){
			return null;
		}else if (cdapMessage.getOpCode().equals(opCode_t.M_CANCELREAD)){
			opcode = Opcode.M_CANCELREAD;
		}else if (cdapMessage.getOpCode().equals(opCode_t.M_CANCELREAD_R)){
			opcode = Opcode.M_CANCELREAD_R;
		}else if (cdapMessage.getOpCode().equals(opCode_t.M_CONNECT)){
			opcode = Opcode.M_CONNECT;
		}else if (cdapMessage.getOpCode().equals(opCode_t.M_CONNECT_R)){
			opcode = Opcode.M_CONNECT_R;
		}else if (cdapMessage.getOpCode().equals(opCode_t.M_CREATE)){
			opcode = Opcode.M_CREATE;
		}else if (cdapMessage.getOpCode().equals(opCode_t.M_CREATE_R)){
			opcode = Opcode.M_CREATE_R;
		}else if (cdapMessage.getOpCode().equals(opCode_t.M_DELETE)){
			opcode = Opcode.M_DELETE;
		}else if (cdapMessage.getOpCode().equals(opCode_t.M_DELETE_R)){
			opcode = Opcode.M_DELETE_R;
		}else if (cdapMessage.getOpCode().equals(opCode_t.M_READ)){
			opcode = Opcode.M_READ;
		}else if (cdapMessage.getOpCode().equals(opCode_t.M_READ_R)){
			opcode = Opcode.M_READ_R;
		}else if (cdapMessage.getOpCode().equals(opCode_t.M_RELEASE)){
			opcode = Opcode.M_RELEASE;
		}else if (cdapMessage.getOpCode().equals(opCode_t.M_RELEASE_R)){
			opcode = Opcode.M_RELEASE_R;
		}else if (cdapMessage.getOpCode().equals(opCode_t.M_START)){
			opcode = Opcode.M_START;
		}else if (cdapMessage.getOpCode().equals(opCode_t.M_START_R)){
			opcode = Opcode.M_START_R;
		}else if (cdapMessage.getOpCode().equals(opCode_t.M_STOP)){
			opcode = Opcode.M_STOP;
		}else if (cdapMessage.getOpCode().equals(opCode_t.M_STOP_R)){
			opcode = Opcode.M_STOP_R;
		}else if (cdapMessage.getOpCode().equals(opCode_t.M_WRITE)){
			opcode = Opcode.M_WRITE;
		}else if (cdapMessage.getOpCode().equals(opCode_t.M_WRITE_R)){
			opcode = Opcode.M_WRITE_R;
		}
		
		return opcode;
	}

	public byte[] serializeMessage(CDAPMessage cdapMessage) throws CDAPException {
		try{
			authTypes_t authMech = getAuthMech(cdapMessage);
			authValue_t authValue = getAuthValue(cdapMessage);
			flagValues_t flags = getFlags(cdapMessage);
			objVal_t objValue = getObjValue(cdapMessage);
			opCode_t opCode = getOpCode(cdapMessage);
			ByteString filter = null;
			if (cdapMessage.getFilter() != null){
				filter = ByteString.copyFrom(cdapMessage.getFilter());
			}
			
			CDAP.CDAPMessage response = CDAP.CDAPMessage.newBuilder().
											setAbsSyntax(cdapMessage.getAbsSyntax()).
											setAuthMech(authMech).
											setAuthValue(authValue).
											setDestAEInst(cdapMessage.getDestAEInst()).
											setDestAEName(cdapMessage.getDestAEName()).
											setDestApInst(cdapMessage.getDestApInst()).
											setDestApName(cdapMessage.getDestApName()).
											setFilter(filter).
											setFlags(flags).
											setInvokeID(cdapMessage.getInvokeID()).
											setObjClass(cdapMessage.getObjClass()).
											setObjInst(cdapMessage.getObjInst()).
											setObjName(cdapMessage.getObjName()).
											setObjValue(objValue).setOpCode(opCode).
											setResult(cdapMessage.getResult()).
											setResultReason(cdapMessage.getResultReason()).
											setScope(cdapMessage.getScope()).
											setSrcAEInst(cdapMessage.getSrcAEInst()).
											setSrcAEName(cdapMessage.getSrcAEName()).
											setSrcApInst(cdapMessage.getSrcApInst()).
											setSrcApName(cdapMessage.getSrcApName()).
											setVersion(cdapMessage.getVersion()).
											build();
			
			return response.toByteArray();
		}catch(Exception ex){
			throw new CDAPException(ex);
		}
	}
	
	private authTypes_t getAuthMech(CDAPMessage cdapMessage){
		authTypes_t authMech = null;
		
		if (cdapMessage.getAuthMech() == null){
			return null;
		}else if (cdapMessage.getAuthMech().equals(AuthTypes.AUTH_NONE)){
			authMech = authTypes_t.AUTH_NONE;
		}else if (cdapMessage.getAuthMech().equals(AuthTypes.AUTH_PASSWD)){
			authMech = authTypes_t.AUTH_PASSWD;
		}else if (cdapMessage.getAuthMech().equals(AuthTypes.AUTH_SSHDSA)){
			authMech = authTypes_t.AUTH_SSHDSA;
		}else if (cdapMessage.getAuthMech().equals(AuthTypes.AUTH_SSHRSA)){
			authMech = authTypes_t.AUTH_SSHRSA;
		}
		
		return authMech;
	}
	
	private authValue_t getAuthValue(CDAPMessage cdapMessage){
		authValue_t authValue = null;
		ByteString authOther = null;
		
		if (cdapMessage.getAuthValue() == null){
			return null;
		}
		
		if (cdapMessage.getAuthValue().getAuthOther() != null){
			authOther = ByteString.copyFrom(cdapMessage.getAuthValue().getAuthOther());
		}
		authValue = CDAP.authValue_t.newBuilder().
										setAuthName(cdapMessage.getAuthValue().getAuthName()).
										setAuthPassword(cdapMessage.getAuthValue().getAuthName()).
										setAuthOther(authOther).
										build();
		return authValue;
	}
	
	private flagValues_t getFlags(CDAPMessage cdapMessage){
		flagValues_t flags = null;
		
		if (cdapMessage.getFlags() == null){
			return null;
		}else if (cdapMessage.getFlags().equals(Flags.F_RD_INCOMPLETE)){
			flags = flagValues_t.F_RD_INCOMPLETE;
		}else if (cdapMessage.getFlags().equals(Flags.F_SYNC)){
			flags = flagValues_t.F_SYNC;
		}
		
		return flags;
	}
	
	private objVal_t getObjValue(CDAPMessage cdapMessage){
		objVal_t objValue = null;
		ByteString byteVal = null;
		
		if (cdapMessage.getObjValue() == null){
			return null;
		}
		
		if (cdapMessage.getObjValue().getByteval() != null){
			byteVal = ByteString.copyFrom(cdapMessage.getObjValue().getByteval());
		}
		
		objValue = CDAP.objVal_t.newBuilder().setByteval(byteVal).
											setDoubleval(cdapMessage.getObjValue().getDoubleval()).
											setFloatval(cdapMessage.getObjValue().getFloatval()).
											setIntval(cdapMessage.getObjValue().getIntval()).
											setInt64Val(cdapMessage.getObjValue().getInt64val()).
											setSint64Val(cdapMessage.getObjValue().getSint64val()).
											setSintval(cdapMessage.getObjValue().getSintval()).
											setStrval(cdapMessage.getObjValue().getStrval()).
											build();
		return objValue;
	}
	
	private opCode_t getOpCode(CDAPMessage cdapMessage){
		opCode_t opCode = null;
		
		if (cdapMessage.getOpCode() == null){
			return null;
		}else if (cdapMessage.getOpCode().equals(Opcode.M_CANCELREAD)){
			opCode = opCode_t.M_CANCELREAD;
		}else if (cdapMessage.getOpCode().equals(Opcode.M_CANCELREAD_R)){
			opCode = opCode_t.M_CANCELREAD_R;
		}else if (cdapMessage.getOpCode().equals(Opcode.M_CONNECT)){
			opCode = opCode_t.M_CONNECT;
		}else if (cdapMessage.getOpCode().equals(Opcode.M_CONNECT_R)){
			opCode = opCode_t.M_CONNECT_R;
		}else if (cdapMessage.getOpCode().equals(Opcode.M_CREATE)){
			opCode = opCode_t.M_CREATE;
		}else if (cdapMessage.getOpCode().equals(Opcode.M_CREATE_R)){
			opCode = opCode_t.M_CREATE_R;
		}else if (cdapMessage.getOpCode().equals(Opcode.M_DELETE)){
			opCode = opCode_t.M_DELETE;
		}else if (cdapMessage.getOpCode().equals(Opcode.M_DELETE_R)){
			opCode = opCode_t.M_DELETE_R;
		}else if (cdapMessage.getOpCode().equals(Opcode.M_READ)){
			opCode = opCode_t.M_READ;
		}else if (cdapMessage.getOpCode().equals(Opcode.M_READ_R)){
			opCode = opCode_t.M_READ_R;
		}else if (cdapMessage.getOpCode().equals(Opcode.M_RELEASE)){
			opCode = opCode_t.M_RELEASE;
		}else if (cdapMessage.getOpCode().equals(Opcode.M_RELEASE_R)){
			opCode = opCode_t.M_RELEASE_R;
		}else if (cdapMessage.getOpCode().equals(Opcode.M_START)){
			opCode = opCode_t.M_START;
		}else if (cdapMessage.getOpCode().equals(Opcode.M_START_R)){
			opCode = opCode_t.M_START_R;
		}else if (cdapMessage.getOpCode().equals(Opcode.M_STOP)){
			opCode = opCode_t.M_STOP;
		}else if (cdapMessage.getOpCode().equals(Opcode.M_STOP_R)){
			opCode = opCode_t.M_STOP_R;
		}else if (cdapMessage.getOpCode().equals(Opcode.M_WRITE)){
			opCode = opCode_t.M_WRITE;
		}else if (cdapMessage.getOpCode().equals(Opcode.M_WRITE_R)){
			opCode = opCode_t.M_WRITE_R;
		}
		
		return opCode;
	}

}
