package rina.ipcmanager.impl;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.ipcmanager.api.InterDIFDirectory;
import rina.ipcservice.api.ApplicationProcessNamingInfo;

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
	
	private Map<String, String> apToDIFMappings = null;
	
	public StaticInterDIFDirectory(){
		this.apToDIFMappings = new Hashtable<String, String>();
		readConfigurationFile();
	}
	
	private void readConfigurationFile(){
		try{
			apToDIFMappings = new Hashtable<String, String>();
			FileInputStream fstream = new FileInputStream(CONFIG_FILE_NAME);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = null;
			String[] tokens = null;
			log.debug("Reading configuration file to learn applications AP naming to DIF mappings");
			while ((strLine = br.readLine()) != null)   {
				if (strLine.startsWith("#")){
					continue;
				}
				tokens = strLine.split(" ");
				if (tokens.length != 3){
					log.error("Ignoring line "+strLine+" because it hasn't got enough arguments");
					continue;
				}
				apToDIFMappings.put(tokens[0]+tokens[1], tokens[2]);
				log.debug("Application " + tokens[0] + " " + tokens[1] + " reachable at DIF "+ tokens[2]);
			}
			in.close();
		}catch (Exception e){
			log.error("Error initializing application process name to host mappings: " + e.getMessage());
		}
	}
	
	public synchronized String mapApplicationProcessNamingInfoToDIFName(ApplicationProcessNamingInfo apNamingInfo){
		String result = apToDIFMappings.get(apNamingInfo.getApplicationProcessName()+apNamingInfo.getApplicationProcessInstance());
		return result;
	}

}
