package rina.protection.api;

/**
 * A protection module that provides no protection
 * @author eduardgrasa
 *
 */
public class NullSDUProtectionModule implements SDUProtectionModule{

	public String getType() {
		return SDUProtectionModule.NULL;
	}

	public byte[] protectSDU(byte[] sdu) {
		return sdu;
	}

	public byte[] unprotectSDU(byte[] sdu) {
		return sdu;
	}
	
	public String toString(){
		return "Protection type: "+this.getType();
	}

}
