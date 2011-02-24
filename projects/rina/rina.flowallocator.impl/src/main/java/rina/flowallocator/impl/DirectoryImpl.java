package rina.flowallocator.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import rina.flowallocator.api.Directory;
import rina.flowallocator.api.message.DirectoryEntry;
import rina.ipcservice.api.ApplicationProcessNamingInfo;

/**
 * The directory. Maps application process names to IPC process addresses
 * @author eduardgrasa
 *
 */
public class DirectoryImpl implements Directory{
	
	/**
	 * Look for expired entries every 5 minutes
	 */
	private static long DIRECTORY_TIMER_TASK_FREQUENCY = 50*60*1000;
	
	/**
	 * Set the lifetime of a directory entry to 1 day
	 */
	private static long DIRECTORY_ENTRY_LIFETIME = 24*60*1000;
	
	/**
	 * The actual directory data structure
	 */
	private List<DirectoryEntry> directory = null;
	
	public DirectoryImpl(){
		directory = new ArrayList<DirectoryEntry>();
		
		Timer timer = new Timer();
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
		}, Calendar.getInstance().getTime(), DIRECTORY_TIMER_TASK_FREQUENCY);
	}
	
	/**
	 * Returns the address of the IPC process where the application process is, or 
	 * null otherwise
	 * @param apNamingInfo
	 * @return
	 */
	public synchronized byte[] getAddress(ApplicationProcessNamingInfo apNamingInfo){
		DirectoryEntry currentEntry = null;
		for(int i=0; i<directory.size(); i++){
			currentEntry = directory.get(i);
			if (currentEntry.getApNamingInfo().equals(apNamingInfo)){
				return currentEntry.getAddress();
			}
		}
		
		return null;
	}
	
	/**
	 * Add a new entry to the directory (AP name to IPC process address mapping). If this AP Name was 
	 * mapped to another IPC process address in another entry, drop it. If it was mapped to the same 
	 * IPC process address, extend the lifetime of the entry.
	 * @param apNamingInfo
	 * @param address
	 */
	public synchronized void addEntry(ApplicationProcessNamingInfo apNamingInfo, byte[] address){
		DirectoryEntry currentEntry = null;
		List<DirectoryEntry> entriesToCheck = new ArrayList<DirectoryEntry>();
		
		for(int i=0; i<directory.size(); i++){
			currentEntry = directory.get(i);
			if (currentEntry.getApNamingInfo().equals(apNamingInfo)){
				entriesToCheck.add(currentEntry);
			}
		}

		for(int i=0; i<entriesToCheck.size(); i++){
			currentEntry = entriesToCheck.get(i);
			if (currentEntry.getAddress().equals(address)){
				currentEntry.setAge(Calendar.getInstance().getTimeInMillis() + DIRECTORY_ENTRY_LIFETIME);
			}else{
				directory.remove(currentEntry);
			}
		}
		
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
	public synchronized void removeEntry(byte[] address){
		DirectoryEntry currentEntry = null;
		List<DirectoryEntry> entriesToRemove = new ArrayList<DirectoryEntry>();
		
		for(int i=0; i<directory.size(); i++){
			currentEntry = directory.get(i);
			if (currentEntry.getApNamingInfo().equals(address)){
				entriesToRemove.add(currentEntry);
			}
		}
		
		for(int i=0; i<entriesToRemove.size(); i++){
			directory.remove(entriesToRemove.get(i));
		}
	}

}
