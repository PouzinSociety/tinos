package rina.enrollment.impl.ribobjects;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.enrollment.api.Neighbor;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.ObjectInstanceGenerator;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * TO describe
 * @author eduardgrasa
 *
 */
public class NeighborSetRIBObject extends BaseRIBObject{
	
	private static final Log log = LogFactory.getLog(NeighborSetRIBObject.class);
	
	public NeighborSetRIBObject(IPCProcess ipcProcess){
		super(ipcProcess, Neighbor.NEIGHBOR_SET_RIB_OBJECT_CLASS, 
				ObjectInstanceGenerator.getObjectInstance(), Neighbor.NEIGHBOR_SET_RIB_OBJECT_NAME);
	}
	
	@Override
	public RIBObject read() throws RIBDaemonException{
		return this;
	}
	
	@Override
	public void create(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException{
		try{
			Neighbor[] neighbors = (Neighbor[])
				this.getEncoder().decode(cdapMessage.getObjValue().getByteval(), Neighbor[].class);
			this.getRIBDaemon().create(cdapMessage.getObjClass(), cdapMessage.getObjInst(), 
					cdapMessage.getObjName(), neighbors, null);
		}catch(Exception ex){
			log.error(ex);
			ex.printStackTrace();
		}
	}
	
	@Override
	public void create(String objectClass, long objectInstance, String objectName, Object objectValue) throws RIBDaemonException{
		if (objectValue instanceof Neighbor){
			this.createOrUpdateNeighbor(objectName, (Neighbor) objectValue);
		}else if (objectValue instanceof Neighbor[]){
			Neighbor[] neighbors = (Neighbor[]) objectValue;
			String candidateObjectName = null;
			
			for(int i=0; i<neighbors.length; i++){
				candidateObjectName = this.getObjectName() + RIBObjectNames.SEPARATOR + neighbors[i].getApplicationProcessName();
				this.createOrUpdateNeighbor(candidateObjectName, neighbors[i]);
			}
		}else{
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Object class ("+objectValue.getClass().getName()+") does not match object name "+objectName);
		}
	}
	
	/**
	 * Create or update a child Neighbor RIB Object
	 * @param objectName
	 * @param objectValue
	 */
	private synchronized void createOrUpdateNeighbor(String objectName, Neighbor neighbor) throws RIBDaemonException{
		//Avoid creating myself as a neighbor
		if (neighbor.getApplicationProcessName().equals(this.getRIBDaemon().getIPCProcess().getApplicationProcessName())){
			return;
		}
		
		RIBObject child = this.getChild(objectName);
		if (child == null){
			//Create the new RIBOBject
			child = new NeighborRIBObject(this.getIPCProcess(), objectName, neighbor);
			this.addChild(child);
			child.setParent(this);
			getRIBDaemon().addRIBObject(child);
		}else{
			//Update the existing RIBObject
			child.write(neighbor);
		}
	}

	@Override
	public synchronized void delete(Object objectValue) throws RIBDaemonException {
		String childName = null;
		List<String> childrenNames = new ArrayList<String>();
		
		for(int i=0; i<this.getChildren().size(); i++){
			childName = this.getChildren().get(i).getObjectName();
			childrenNames.add(childName);
			getRIBDaemon().delete(null, childName, null);
		}
		
		for(int i=0; i<childrenNames.size(); i++){
			this.removeChild(childrenNames.get(i));
		}
	}
	
	@Override
	public synchronized Object getObjectValue(){
		Neighbor[] dafMembers = new Neighbor[this.getChildren().size()];
		for(int i=0; i<dafMembers.length; i++){
			dafMembers[i] = (Neighbor) this.getChildren().get(i).getObjectValue();
		}
		
		return dafMembers;
	}
}
