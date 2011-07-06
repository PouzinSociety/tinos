package rina.applicationprocess.api;

import java.util.ArrayList;
import java.util.List;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.Opcode;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.RIBDaemonException;

/**
 * Base implementation of the ApplicationProcess class. Deals with all the naming related 
 * information and operations.
 * @author eduardgrasa
 *
 */
public class BaseApplicationProcess implements ApplicationProcess{
	
	private String applicationProcessName = null;
	private String applicationProcessInstance = null;
	private List<byte[]> synonyms = null;
	private byte[] currentSynonym = null;
	private List<WhatevercastName> whatevercastNames = null;
	
	public BaseApplicationProcess(){
		synonyms = new ArrayList<byte[]>();
		whatevercastNames = new ArrayList<WhatevercastName>();
	}

	public String getApplicationProcessName() {
		return applicationProcessName;
	}

	public void setApplicationProcessName(String applicationProcessName) {
		this.applicationProcessName = applicationProcessName;
	}

	public String getApplicationProcessInstance() {
		return applicationProcessInstance;
	}

	public void setApplicationProcessInstance(String applicationProcessInstance) {
		this.applicationProcessInstance = applicationProcessInstance;
	}
	
	public ApplicationProcessNamingInfo getApplicationProcessNamingInfo(){
		ApplicationProcessNamingInfo result = new ApplicationProcessNamingInfo(this.applicationProcessName, this.applicationProcessInstance, null, null);
		return result;
	}

	public void setApplicationProcessNamingInfo(ApplicationProcessNamingInfo applicationProcessNamingInfo){
		this.applicationProcessName = applicationProcessNamingInfo.getApplicationProcessName();
		this.applicationProcessInstance = applicationProcessNamingInfo.getApplicationProcessInstance();
	}

	public List<byte[]> getApplicationProcessNameSynonyms() {
		return synonyms;
	}

	public void removeApplicationProcessNameSynonym(ApplicationProcessNameSynonym synonym) throws ApplicationProcessException {
		if (synonym == null || synonym.getApplicationProcessName() == null || synonym.getSynonym() == null){
			throw new ApplicationProcessException(ApplicationProcessException.NULL_OR_MALFORMED_SYNONYM);
		}
		
		if (!synonym.getApplicationProcessName().equals(this.getApplicationProcessName())){
			throw new ApplicationProcessException(ApplicationProcessException.WRONG_APPLICATION_PROCES_NAME);
		}
		
		if (!synonyms.contains(synonym.getSynonym())){
			throw new ApplicationProcessException(ApplicationProcessException.UNEXISTING_SYNOYM);
		}
		
		synonyms.remove(synonym.getSynonym());
	}

	public void addApplicationProcessNameSynonym(ApplicationProcessNameSynonym synonym) throws ApplicationProcessException {
		if (synonym == null || synonym.getApplicationProcessName() == null || synonym.getSynonym() == null){
			throw new ApplicationProcessException(ApplicationProcessException.NULL_OR_MALFORMED_SYNONYM);
		}
		
		if (!synonym.getApplicationProcessName().equals(this.getApplicationProcessName())){
			throw new ApplicationProcessException(ApplicationProcessException.WRONG_APPLICATION_PROCES_NAME);
		}
		
		if (synonyms.contains(synonym.getSynonym())){
			throw new ApplicationProcessException(ApplicationProcessException.ALREADY_EXISTING_SYNOYM);
		}
		
		synonyms.add(synonym.getSynonym());
	}

	public byte[] getCurrentSynonym() {
		return currentSynonym;
	}

	public void setCurrentSynonym(byte[] synonym) throws ApplicationProcessException {
		if (synonym == null){
			throw new ApplicationProcessException(ApplicationProcessException.NULL_OR_MALFORMED_SYNONYM);
		}
		
		if (!synonyms.contains(synonym)){
			throw new ApplicationProcessException(ApplicationProcessException.UNEXISTING_SYNOYM);
		}
		
		this.currentSynonym = synonym;
	}

	public List<WhatevercastName> getWhatevercastNames() {
		return whatevercastNames;
	}

	public void addWhatevercastName(WhatevercastName name) throws ApplicationProcessException {
		if (name == null || name.getName() == null || name.getRule() == null || name.getSetMembers() == null){
			throw new ApplicationProcessException(ApplicationProcessException.NULL_OR_MALFORMED_WHATEVERCAST_NAME);
		}
		
		if (whatevercastNames.contains(name)){
			throw new ApplicationProcessException(ApplicationProcessException.ALREADY_EXISTING_WHATEVERCAST_NAME);
		}
		
		whatevercastNames.add(name);
	}

	public void removeWhatevercastName(WhatevercastName name) throws ApplicationProcessException {
		if (name == null || name.getName() == null){
			throw new ApplicationProcessException(ApplicationProcessException.NULL_OR_MALFORMED_WHATEVERCAST_NAME);
		}
		
		if (!whatevercastNames.contains(name)){
			throw new ApplicationProcessException(ApplicationProcessException.UNEXISTING_WHATEVERCAST_NAME);
		}
		
		whatevercastNames.remove(name);
	}
	
	/**
	 * True if the name matches one of the whatevercast names of the application process, false
	 * otherwise
	 * @param name
	 * @return
	 */
	public boolean containsWhatevercastName(String name){
		for(int i=0; i<whatevercastNames.size(); i++){
			if (whatevercastNames.get(i).getName().equals(name)){
				return true;
			}
		}
		
		return false;
	}

	public void processOperation(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		//TODO
		
	}

	public Object processOperation(Opcode opcode, String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		// TODO Auto-generated method stub
		return null;
	}
}
