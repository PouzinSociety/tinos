package rina.utils.apps.rinaband.generator;

/**
 * Generates SDUs representing 4-bytes encoded integers starting at 0 
 * and incrementing from there (until it reaches Integer.MAX_VALUE, then 
 * it starts again at 0).
 * @author eduardgrasa
 *
 */
public class IncrementSDUGenerator extends BaseSDUGenerator{
	
	private enum State {FIRST_BYTE, SECOND_BYTE, THIRD_BYTE, FOURTH_BYTE};
	
	/**
	 * The next SDU to return
	 */
	private byte[] sdu = null;
	
	/**
	 * The next integer
	 */
	private int counter = 0;
	
	/**
	 * The state of the Generator
	 */
	private State state = State.FIRST_BYTE;
	
	public IncrementSDUGenerator(int sduSize){
		super(SDUGenerator.INCREMENT_PATTERN, sduSize);
		sdu = new byte[sduSize];
	}

	
	/**
	 * Assumes that the SDU will be written to the flow 
	 * before the call to getNextSDU is invoked (because 
	 * the SDU values are overriden every time this 
	 * function is called)
	 */
	public byte[] getNextSDU() {
		for(int i=0; i<sdu.length; i++){
			sdu[i] = this.getNextByte();
		}
		
		return sdu;
	}
	
	private byte getNextByte(){
		byte nextByte = 0x00;
		
		switch(this.state){
		case FIRST_BYTE:
			nextByte = (byte) (counter >>> 24);
			this.state = State.SECOND_BYTE;
			break;
		case SECOND_BYTE:
			nextByte = (byte) (counter >>> 16);
			this.state = State.THIRD_BYTE;
			break;
		case THIRD_BYTE:
			nextByte = (byte) (counter >>> 8);
			this.state = State.FOURTH_BYTE;
			break;
		case FOURTH_BYTE:
			nextByte = (byte) counter;
			this.state = State.FIRST_BYTE;
			if (counter < Integer.MAX_VALUE){
				counter ++;
			}else{
				counter = 0;
			}
			break;
		}
		
		return nextByte;
	}

}
