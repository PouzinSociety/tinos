package rina.idd.impl;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.idd.api.InterDIFDirectory;
import rina.applicationprocess.api.ApplicationProcessNamingInfo;

/**
 * Returns the DIF name where an application is to be found. Initially this is just 
 * read from a static configuration file, in the future it will comply with the IDD spec 
 * and search the RIBs of the IPC processes and communicate with remote peer IDDs
 * @author eduardgrasa
 *
 */
public class StaticInterDIFDirectory implements InterDIFDirectory{
	
	private static final Log log = LogFactory.getLog(StaticInterDIFDirectory.class);
	private static final String CONFIG_FILE_NAME = "config/rina/idd.rina";
	
	private Map<String, List<String>> apToDIFMappings = null;
	
	public StaticInterDIFDirectory(){
		this.apToDIFMappings = new Hashtable<String, List<String>>();
		readConfigurationFile();
	}
	
	private void readConfigurationFile(){
		try{
			apToDIFMappings.clear();
			FileInputStream fstream = new FileInputStream(CONFIG_FILE_NAME);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = null;
			String[] tokens = null;
			String[] tokens2 = null;
			ApplicationProcessNamingInfo apNamingInfo = null;
			List<String> value = null;
			
			log.debug("Reading configuration file to learn applications AP naming to DIF mappings");
			while ((strLine = br.readLine()) != null)   {
				if (strLine.startsWith("#")){
					continue;
				}
				tokens = strLine.split(",");
				if (tokens.length != 2){
					log.error("Ignoring line "+strLine+" because it hasn't got enough arguments");
					continue;
				}
				apNamingInfo = new ApplicationProcessNamingInfo();
				tokens2 = tokens[0].split(" ");
				if(tokens2.length == 1){
					apNamingInfo.setApplicationProcessName(tokens2[0]);
				}else if (tokens2.length == 2){
					apNamingInfo.setApplicationProcessName(tokens2[0]);
					apNamingInfo.setApplicationProcessInstance(tokens2[1]);
				}else{
					log.error("Ignoring line "+strLine+" because it hasn't got enough arguments");
					continue;
				}
				
				value = new ArrayList<String>();
				tokens2 = tokens[1].split(" ");
				for(int i=0; i<tokens2.length; i++){
					if (tokens2[i].equals("") || tokens2[i].equals(" ")){
						continue;
					}
					value.add(tokens2[i]);
				}
				
				apToDIFMappings.put(apNamingInfo.getEncodedString(), value);
				log.debug("Application " + apNamingInfo.getEncodedString() + " reachable from DIF(s) "+ printStringList(value));
			}
			in.close();
		}catch (Exception e){
			log.error("Error initializing application process name to host mappings: " + e.getMessage());
		}
	}
	
	public synchronized List<String> mapApplicationProcessNamingInfoToDIFName(ApplicationProcessNamingInfo apNamingInfo){
		log.debug("Looking for the DIFs through which application "+apNamingInfo.getEncodedString()+" is available");
		List<String> result = apToDIFMappings.get(apNamingInfo.getEncodedString());
		if (result == null){
			log.debug("Could not find any DIF through which "+apNamingInfo.getEncodedString()+ " is reachable.");
		}else{
			log.debug(apNamingInfo.getEncodedString()+ " is reachable through DIF(s) " + printStringList(result));
		}
		return result;
	}

	public void addMapping(ApplicationProcessNamingInfo applicationName, String difName) {
		String key = applicationName.getEncodedString();
		List<String> value = apToDIFMappings.get(key);
		if (value == null){
			value = new ArrayList<String>();
			apToDIFMappings.put(key, value);
		}
		
		value.add(difName);
	}

	public void removeMapping(ApplicationProcessNamingInfo applicationName, String difName) {
		String key = applicationName.getEncodedString();
		List<String> value = apToDIFMappings.get(key);
		if (value == null){
			return;
		}
		
		value.remove(difName);
		if (value.size() == 0){
			apToDIFMappings.remove(key);
		}
	}

	public void addMapping(ApplicationProcessNamingInfo applicationName, List<String> difNames) {
		String key = applicationName.getEncodedString();
		List<String> value = apToDIFMappings.get(key);
		if (value == null){
			value = new ArrayList<String>();
			apToDIFMappings.put(key, value);
		}
		
		for(int i=0; i<difNames.size(); i++){
			value.add(difNames.get(i));
		}
	}

	public void removeAllMappings(ApplicationProcessNamingInfo applicationName) {
		String key = applicationName.getEncodedString();
		apToDIFMappings.remove(key);
	}

	public void removeMapping(ApplicationProcessNamingInfo applicationName, List<String> difNames) {
		String key = applicationName.getEncodedString();
		List<String> value = apToDIFMappings.get(key);
		if (value == null){
			return;
		}
		
		for(int i=0; i<difNames.size(); i++){
			value.remove(difNames.get(i));
		}
		
		if (value.size() == 0){
			apToDIFMappings.remove(key);
		}
		
	}
	
	private String printStringList(List<String> list){
		String result = "";
		if (list == null){
			return result;
		}
		
		for(int i=0; i<list.size(); i++){
			result = result + list.get(i);
			if(i+1<list.size()){
				result = result + ", ";
			}
		}
		
		return result;
	}

}
