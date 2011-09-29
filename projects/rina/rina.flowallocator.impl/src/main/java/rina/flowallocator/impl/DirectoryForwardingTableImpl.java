package rina.flowallocator.impl;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.flowallocator.api.DirectoryForwardingTable;
import rina.flowallocator.api.message.DirectoryEntry;
import rina.ipcservice.api.ApplicationProcessNamingInfo;

/**
 * The DirectoryForwardingTable. Tells what is the next place (IPC Process Address) where 
 * an Application Process Name should be searched
 * @author eduardgrasa
 *
 */
public class DirectoryForwardingTableImpl implements DirectoryForwardingTable{
	
	private static final String CONFIG_FILE_NAME = "config/rina/directoryforwardingtable.rina";
	
	private static final Log log = LogFactory.getLog(DirectoryForwardingTableImpl.class);
	
	/**
	 * Look for expired entries every 5 minutes
	 */
	private static long DIRECTORY_TIMER_TASK_FREQUENCY = 5*60*1000;
	
	/**
	 * Set the lifetime of a directory entry to 1 day
	 */
	private static long DIRECTORY_ENTRY_LIFETIME = 24*60*60*1000;
	
	/**
	 * The actual directory data structure
	 */
	private List<DirectoryEntry> directory = null;
	
	/**
	 * The default IPC process where the requests will be forwarded 
	 * in case that the ApplicationProcessName looked up is reachable 
	 * through this IPC process
	 */
	private long defaultIPCProcessAddress = 0;
	
	public DirectoryForwardingTableImpl(){
		directory = new ArrayList<DirectoryEntry>();
		populateDirectory();
		
		//TODO uncomment this when we want timers to remove expired entries
		/*Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask(){
			public void run(){
				List<DirectoryEntry> entriesToRemove = new ArrayList<DirectoryEntry>();
				
				for(int i=0; i<directory.size(); i++){
					if (directory.get(i).getAge() < Calendar.getInstance().getTimeInMillis()){
						entriesToRemove.add(directory.get(i));
					}
				}
				
				for(int i=0; i<entriesToRemove.size(); i++){
					directory.remove(entriesToRemove.get(i));
				}
			}
		}, Calendar.getInstance().getTime(), DIRECTORY_TIMER_TASK_FREQUENCY);*/
	}
	
	private void populateDirectory(){
		try{
			FileInputStream fstream = new FileInputStream(CONFIG_FILE_NAME);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = null;
			String[] tokens = null;
			ApplicationProcessNamingInfo apNamingInfo = null;
			
			log.debug("Reading configuration file to populate the directory");
			while ((strLine = br.readLine()) != null)   {
				if (strLine.startsWith("#")){
					continue;
				}
				tokens = strLine.split(" ");
				if (tokens.length == 2 && tokens[0].equals("default")){
					try{
						this.setDefaultIPCProcess(Long.parseLong(tokens[1]));
						log.debug("Default Entry: remote IPC process address: "+tokens[1]);
					}catch(NumberFormatException e){
						log.error("Error parsing address, it is not a valid long: "+tokens[1]);
						continue;
					}
				}else if (tokens.length == 3 && tokens[1].equals("address")){
					try{
						apNamingInfo = new ApplicationProcessNamingInfo(tokens[0], null);
						this.addEntry(apNamingInfo, Long.parseLong(tokens[2]));
						log.debug("Application process with AP name "+tokens[0]+ " reachable at IPC process "+tokens[2]);
					}catch(NumberFormatException e){
						log.error("Error parsing address, it is not a valid long: "+tokens[2]);
						continue;
					}
				}else if (tokens.length == 4 && tokens[2].equals("address")){
					try{
						apNamingInfo = new ApplicationProcessNamingInfo(tokens[0], tokens[1]);
						this.addEntry(apNamingInfo, Long.parseLong(tokens[3]));
						log.debug("Application process with AP name "+tokens[0]+ " and AP instance "+tokens[1]+" reachable at IPC process "+tokens[3]);
					}catch(NumberFormatException e){
						log.error("Error parsing address, it is not a valid long: "+tokens[3]);
						continue;
					}
				}else{
					log.error("Ignoring line "+strLine+" because it hasn't got enough arguments");
					continue;
				}
			}
			in.close();
		}catch (Exception e){
			log.error("Error populating directory forwarding table: " + e.getMessage());
		}
	}
	
	/**
	 * Returns the address of the IPC process where the application process is, or 
	 * null otherwise
	 * @param apNamingInfo
	 * @return
	 */
	public synchronized long getAddress(ApplicationProcessNamingInfo apNamingInfo){
		DirectoryEntry currentEntry = null;
		for(int i=0; i<directory.size(); i++){
			currentEntry = directory.get(i);
			if (currentEntry.getApNamingInfo().equals(apNamingInfo)){
				return currentEntry.getAddress();
			}
		}
		
		return defaultIPCProcessAddress;
	}
	
	/**
	 * Add a new entry to the directory (AP name to IPC process address mapping). If this AP Name was 
	 * mapped to another IPC process address in another entry, drop it. If it was mapped to the same 
	 * IPC process address, extend the lifetime of the entry.
	 * @param apNamingInfo
	 * @param address
	 */
	public synchronized void addEntry(ApplicationProcessNamingInfo apNamingInfo, long address){
		DirectoryEntry currentEntry = null;
		//TODO Check disabled, fix it and reenable it again
		/*List<DirectoryEntry> entriesToCheck = new ArrayList<DirectoryEntry>();
		
		for(int i=0; i<directory.size(); i++){
			currentEntry = directory.get(i);
			if (currentEntry.getApNamingInfo().equals(apNamingInfo)){
				entriesToCheck.add(currentEntry);
			}
		}

		for(int i=0; i<entriesToCheck.size(); i++){
			currentEntry = entriesToCheck.get(i);
			if (currentEntry.getAddress() == address){
				currentEntry.setAge(Calendar.getInstance().getTimeInMillis() + DIRECTORY_ENTRY_LIFETIME);
			}else{
				directory.remove(currentEntry);
			}
		}*/
		
		currentEntry = new DirectoryEntry();
		currentEntry.setAddress(address);
		currentEntry.setApNamingInfo(apNamingInfo);
		currentEntry.setAge(Calendar.getInstance().getTimeInMillis() + DIRECTORY_ENTRY_LIFETIME);
		directory.add(currentEntry);
	}
	
	/**
	 * Remove the entries associated to this AP name
	 * @param apNamingInfo
	 */
	public synchronized void removeEntry(ApplicationProcessNamingInfo apNamingInfo){
		DirectoryEntry currentEntry = null;
		List<DirectoryEntry> entriesToRemove = new ArrayList<DirectoryEntry>();
		
		for(int i=0; i<directory.size(); i++){
			currentEntry = directory.get(i);
			if (currentEntry.getApNamingInfo().equals(apNamingInfo)){
				entriesToRemove.add(currentEntry);
			}
		}
		
		for(int i=0; i<entriesToRemove.size(); i++){
			directory.remove(entriesToRemove.get(i));
		}
	}
	
	/**
	 * Remove the entries associated to this address
	 * @param address
	 */
	public synchronized void removeEntry(long address){
		DirectoryEntry currentEntry = null;
		List<DirectoryEntry> entriesToRemove = new ArrayList<DirectoryEntry>();
		
		for(int i=0; i<directory.size(); i++){
			currentEntry = directory.get(i);
			if (currentEntry.getAddress() == address){
				entriesToRemove.add(currentEntry);
			}
		}
		
		for(int i=0; i<entriesToRemove.size(); i++){
			directory.remove(entriesToRemove.get(i));
		}
	}
	
	/**
	 * Set the default IPC process where the requests will be forwarded 
	 * in case that the ApplicationProcessName looked up is not in the 
	 * table
	 * @param address
	 */
	public void setDefaultIPCProcess(long address){
		this.defaultIPCProcessAddress = address;
	}
	
	@Override
	public String toString(){
		String result = "";
		for(int i=0; i<directory.size(); i++){
			result = result + "Destination AP Name: "+ directory.get(i).getApNamingInfo().getApplicationProcessName() + 
						", destination AP Instance: " + directory.get(i).getApNamingInfo().getApplicationProcessInstance() + 
						"; forward to IPC process address: " + directory.get(i).getAddress() + "\n";
		}
		result = result + "Default, forward to IPC process address "+this.defaultIPCProcessAddress + "\n";
		
		return result;
	}

}
