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
			NeighborRIBObject ribObject = new NeighborRIBObject(this.getIPCProcess(), objectName, (Neighbor) objectValue);
			this.addChild(ribObject);
			getRIBDaemon().addRIBObject(ribObject);
		}else if (objectValue instanceof Neighbor[]){
			Neighbor[] neighbors = (Neighbor[]) objectValue;
			String candidateObjectName = null;
			
			for(int i=0; i<neighbors.length; i++){
				candidateObjectName = this.getObjectName() + RIBObjectNames.SEPARATOR + neighbors[i].getKey();
				if (!this.hasChild(candidateObjectName)){
					NeighborRIBObject ribObject = new NeighborRIBObject(this.getIPCProcess(), candidateObjectName, neighbors[i]);
					this.addChild(ribObject);
					getRIBDaemon().addRIBObject(ribObject);
				}
			}
			
		}else{
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Object class ("+objectValue.getClass().getName()+") does not match object name "+objectName);
		}
	}

	@Override
	public void delete(Object objectValue) throws RIBDaemonException {
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
	public Object getObjectValue(){
		Neighbor[] dafMembers = new Neighbor[this.getChildren().size()];
		for(int i=0; i<dafMembers.length; i++){
			dafMembers[i] = (Neighbor) this.getChildren().get(i).getObjectValue();
		}
		
		return dafMembers;
	}
}
