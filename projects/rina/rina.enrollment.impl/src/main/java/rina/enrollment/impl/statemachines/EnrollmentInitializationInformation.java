package rina.enrollment.impl.statemachines;

import java.util.ArrayList;
import java.util.List;

import rina.applicationprocess.api.WhatevercastName;
import rina.efcp.api.DataTransferConstants;
import rina.enrollment.api.Neighbor;
import rina.flowallocator.api.DirectoryForwardingTableEntry;
import rina.flowallocator.api.QoSCube;

/**
 * Contains enrollment initialization information
 * @author eduardgrasa
 *
 */
public class EnrollmentInitializationInformation {
	
	private Long synonym = null;
	private List<QoSCube> qosCubes = null;
	private List<Neighbor> neighbors = null;
	private List<WhatevercastName> whatevercastNames = null;
	private List<DirectoryForwardingTableEntry> directoryEntries = null;
	private DataTransferConstants dataTransferConstants = null;
	
	public EnrollmentInitializationInformation(){
		qosCubes = new ArrayList<QoSCube>();
		neighbors = new ArrayList<Neighbor>();
		whatevercastNames = new ArrayList<WhatevercastName>();
		directoryEntries = new ArrayList<DirectoryForwardingTableEntry>();
	}

	public Long getSynonym() {
		return synonym;
	}

	public void setSynonym(Long synonym) {
		this.synonym = synonym;
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
	
	public void addQoSCube(QoSCube qosCube){
		this.qosCubes.add(qosCube);
	}

	public List<Neighbor> getNeighbors() {
		return neighbors;
	}
	
	public void addNeighbor(Neighbor neighbor){
		this.neighbors.add(neighbor);
	}

	public List<WhatevercastName> getWhatevercastNames() {
		return whatevercastNames;
	}
	
	public void addWhatevercastName(WhatevercastName whatevercastName){
		this.whatevercastNames.add(whatevercastName);
	}
	
	public void addDirectoryEntry(DirectoryForwardingTableEntry entry){
		this.directoryEntries.add(entry);
	}
	
	public List<DirectoryForwardingTableEntry> getDirectoryEntries() {
		return directoryEntries;
	}
}
