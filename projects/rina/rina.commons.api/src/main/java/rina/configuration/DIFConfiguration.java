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

	public String getDifName() {
		return difName;
	}

	public void setDifName(String difName) {
		this.difName = difName;
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
	
	public static DIFConfiguration getDefaultDIFConfiguration(){
		DIFConfiguration result = new DIFConfiguration();
		result.setDifName("RINA-Demo.DIF");
		result.setDataTransferConstants(DataTransferConstants.getDefaultInstance());
		List<QoSCube> qosCubes = new ArrayList<QoSCube>();
		qosCubes.add(QoSCube.getDefaultReliableQoSCube());
		qosCubes.add(QoSCube.getDefaultUnreliableQoSCube());
		result.setQosCubes(qosCubes);
		
		return result;
	}
}
