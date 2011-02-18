package rina.delimiting.api;

import java.util.List;

/**
 * Delimits and undelimits SDUs, to allow multiple SDUs to be concatenated in the same PDU
 * @author eduardgrasa
 *
 */
public interface Delimiter {
	
	/**
	 * Takes a list of raw sdus and produces a single delimited byte array, consisting in 
	 * the concatenation of the sdus followed by their encoded length: [length][sdu][length][sdu] ...
	 * @param rawSdus
	 * @return
	 */
	public byte[] getDilimitedSdus(List<byte[]> rawSdus);
	
	/**
	 * Takes a delimited byte array ([length][sdu][length][sdu] ..) and extracts the sdus
	 * @param delimitedSdus
	 * @return
	 */
	public List<byte[]> getRawSdus(byte[] delimitedSdus);
}
