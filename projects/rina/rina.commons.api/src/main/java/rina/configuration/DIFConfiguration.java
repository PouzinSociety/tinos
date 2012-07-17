package rina.configuration;

import java.util.ArrayList;
import java.util.List;

import rina.efcp.api.DataTransferConstants;
import rina.flowallocator.api.QoSCube;

/**
 * The configuration required to create a DIF
 * @author eduardgrasa
 *
 */
public class DIFConfiguration {

	/**
	 * The DIF Name
	 */
	private String difName = null;
	
	/**
	 * The DIF Data Transfer constants
	 */
	private DataTransferConstants dataTransferConstants = null;

	/**
	 * The QoS cubes available in the DIF
	 */
	private List<QoSCube> qosCubes = null;
	
	/** Only for shim IP DIFs **/
	private List<ExpectedApplicationRegistration> expectedApplicationRegistrations = null;
	private List<DirectoryEntry> directory = null;

	public List<ExpectedApplicationRegistration> getExpectedApplicationRegistrations() {
		return expectedApplicationRegistrations;
	}

	public void setExpectedApplicationRegistrations(
			List<ExpectedApplicationRegistration> expectedApplicationRegistrations) {
		this.expectedApplicationRegistrations = expectedApplicationRegistrations;
	}

	public List<DirectoryEntry> getDirectory() {
		return directory;
	}

	public void setDirectory(List<DirectoryEntry> directory) {
		this.directory = directory;
	}
	
	public DataTransferConstants getDataTransferConstants() {
		return dataTransferConstants;
	}

	public void setDataTransferConstants(DataTransferConstants dataTransferConstants) {
		this.dataTransferConstants = dataTransferConstants;
	}

	public List<QoSCube> getQosCubes() {
		return qosCubes;
	}

	public void setQosCubes(List<QoSCube> qosCubes) {
		this.qosCubes = qosCubes;
	}
	
	public static DIFConfiguration getDefaultNormalDIFConfiguration(){
		DIFConfiguration result = new DIFConfiguration();
		result.setDifName("RINA-Demo.DIF");
		result.setDataTransferConstants(DataTransferConstants.getDefaultInstance());
		List<QoSCube> qosCubes = new ArrayList<QoSCube>();
		qosCubes.add(QoSCube.getDefaultReliableQoSCube());
		qosCubes.add(QoSCube.getDefaultUnreliableQoSCube());
		result.setQosCubes(qosCubes);
		
		return result;
	}

	public String getDifName() {
		return difName;
	}

	public void setDifName(String difName) {
		this.difName = difName;
	}
}
