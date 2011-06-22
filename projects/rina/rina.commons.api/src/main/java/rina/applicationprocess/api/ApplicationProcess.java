package rina.applicationprocess.api;

import java.util.List;

import rina.ipcservice.api.ApplicationProcessNamingInfo;

/**
 * Defines an application process
 * @author eduardgrasa
 *
 */
public interface ApplicationProcess {
	
	/* MANAGE OWN NAMING INFORMATION */
	
	/**
	 * Return the application process name
	 * @return
	 */
	public String getApplicationProcessName();
	
	/**
	 * Set the application process name
	 * @param applicationProcessName
	 */
	public void setApplicationProcessName(String applicationProcessName);
	
	/**
	 * Return the application process instance
	 * @return
	 */
	public String getApplicationProcessInstance();
	
	/**
	 * Set the application process instance
	 * @param applicationProcessInstance
	 */
	public void setApplicationProcessInstance(String applicationProcessInstance);
	
	/**
	 * Return the application process name and instance
	 * @return
	 */
	public ApplicationProcessNamingInfo getApplicationProcessNamingInfo();
	
	/**
	 * Set the application process name and instance
	 * @param applicationProcessNamingInfo
	 */
	public void setApplicationProcessNamingInfo(ApplicationProcessNamingInfo applicationProcessNamingInfo);
	
	/* MANAGE SYNONYMS */
	
	/**
	 * Return all the synonyms of the application process
	 * @return
	 */
	public List<byte[]> getApplicationProcessNameSynonyms();
	
	/**
	 * Remove an application process name synonym from the list
	 * @param synonym
	 * @throws ApplicationProcessException if the synonym to be removed does not exist or if the application process name is wrong
	 */
	public void removeApplicationProcessNameSynonym(ApplicationProcessNameSynonym synonym) throws ApplicationProcessException;
	
	/**
	 * Add an application process name synonym to the list
	 * @param synonym
	 * @throws ApplicationProcessException if the application process name is wrong
	 */
	public void addApplicationProcessNameSynonym(ApplicationProcessNameSynonym synonym) throws ApplicationProcessException;
	
	/**
	 * Return the synonym currently being used by this application process
	 * @return
	 */
	public byte[] getCurrentSynonym();
	
	/**
	 * Set the synonym currently being used by this application process
	 * @param synonym
	 * @throws ApplicationProcessException if the synonym is not contained in the synonyms list
	 */
	public void setCurrentSynonym(byte[] synonym) throws ApplicationProcessException;
	
	/* MANAGE WHATEVERCAST NAMES */
	
	/**
	 * Return the whatevercast names that this application process nows
	 */
	public List<WhatevercastName> getWhatevercastNames();
	
	/**
	 * Add a whatevercast name to the list of known whatevercast name, if the
	 * name is not null or it is not already there yet
	 * @param name
	 * @throws ApplicationProcessException if the name is null or it is already in the list
	 */
	public void addWhatevercastName(WhatevercastName name) throws ApplicationProcessException;
	
	/**
	 * Remove a whatevercast name from the list of known whatevercast names, if the 
	 * name is not null or it is not there 
	 * @param name
	 * @throws ApplicationProcessException if the name is null or it is not there
	 */
	public void removeWhatevercastName(WhatevercastName name) throws ApplicationProcessException;

}
