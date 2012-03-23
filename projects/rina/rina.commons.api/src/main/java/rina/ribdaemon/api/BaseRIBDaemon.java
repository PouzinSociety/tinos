package rina.ribdaemon.api;

import rina.ipcprocess.api.BaseIPCProcessComponent;

/**
 * Provides the component name for the RIB Daemon
 * @author eduardgrasa
 *
 */
public abstract class BaseRIBDaemon extends BaseIPCProcessComponent implements RIBDaemon {

	public static final String getComponentName(){
		return RIBDaemon.class.getName();
	}
	
	public String getName() {
		return BaseRIBDaemon.getComponentName();
	}

	/**
	 * Create or update an object in the RIB
	 * @param objectClass the class of the object
	 * @param objectName the name of the object
	 * @param objectInstance the instance of the object
	 * @param objectValue the value of the object
	 * @param notify if not null notify some of the neighbors about the change
	 * @throws RIBDaemonException
	 */
	public abstract void create(String objectClass, long objectInstance, String objectName, Object objectValue, 
			NotificationPolicy notificationPolicy) throws RIBDaemonException;
	
	public void create(String objectClass, String objectName, Object objectValue, 
			NotificationPolicy notificationPolicy) throws RIBDaemonException{
		this.create(objectClass, 0L, objectName, objectValue, notificationPolicy);
	}

	public void create(long objectInstance, Object objectValue, NotificationPolicy notificationPolicy) throws RIBDaemonException{
		this.create(null, objectInstance, null, objectValue, notificationPolicy);
	}
	
	public void create(String objectClass, String objectName, Object objectValue) throws RIBDaemonException{
		this.create(objectClass, 0L, objectName, objectValue, null);
	}
	
	public void create(long objectInstance, Object objectValue) throws RIBDaemonException{
		this.create(null, objectInstance, null, objectValue, null);
	}
	
	/**
	 * Delete an object from the RIB
	 * @param objectClass the class of the object
	 * @param objectName the name of the object
	 * @param objectInstance the instance of the object
	 * @param object the value of the object
	 * @param notify if not null notify some of the neighbors about the change
	 * @throws RIBDaemonException 
	 */
	public abstract void delete(String objectClass, long objectInstance, String objectName, Object objectValue, 
			NotificationPolicy notificationPolicy) throws RIBDaemonException;
	
	public void delete(String objectClass, String objectName, Object objectValue, 
			NotificationPolicy notificationPolicy) throws RIBDaemonException{
		this.delete(objectClass, 0L, objectName, objectValue, notificationPolicy);
	}

	public void delete(long objectInstance, Object objectValue, NotificationPolicy notificationPolicy) throws RIBDaemonException{
		this.delete(null, objectInstance, null, objectValue, notificationPolicy);
	}
	
	public void delete(String objectClass, String objectName, Object objectValue) throws RIBDaemonException{
		this.delete(objectClass, 0L, objectName, objectValue, null);
	}
	
	public void delete(long objectInstance, Object objectValue) throws RIBDaemonException{
		this.delete(null, objectInstance, null, objectValue, null);
	}
	
	public void delete(String objectClass, long objectInstance, String objectName, NotificationPolicy notificationPolicy) throws RIBDaemonException{
		this.delete(objectClass, objectInstance, objectName, null, notificationPolicy);
	}
	
	public void delete(String objectClass, long objectInstance, String objectName) throws RIBDaemonException{
		this.delete(objectClass, objectInstance, objectName, null, null);
	}
	
	public void delete(String objectClass, String objectName) throws RIBDaemonException{
		this.delete(objectClass, 0L, objectName, null, null);
	}
	
	public void delete(long objectInstance) throws RIBDaemonException{
		this.delete(null, objectInstance, null, null, null);
	}
	
	/**
	 * Read an object from the RIB
	 * @param objectClass the class of the object
	 * @param objectName the name of the object
	 * @param objectInstance the instance of the object
	 * @return
	 * @throws RIBDaemonException
	 */
	public abstract RIBObject read(String objectClass, long objectInstance, String objectName) throws RIBDaemonException;
	
	public RIBObject read(String objectClass, String objectName) throws RIBDaemonException{
		return this.read(objectClass, 0L, objectName);
	}
	
	public RIBObject read(long objectInstance) throws RIBDaemonException{
		return this.read(null, objectInstance, null);
	}
	
	/**
	 * Update the value of an object in the RIB
	 * @param objectClass the class of the object
	 * @param objectName the name of the object
	 * @param objectInstance the instance of the object
	 * @param objectValue the new value of the object
	 * @param notify if not null notify some of the neighbors about the change
	 * @throws RIBDaemonException
	 */
	public abstract void write(String objectClass, long objectInstance, String objectName, Object objectValue, 
			NotificationPolicy notificationPolicy) throws RIBDaemonException;
	
	public void write(String objectClass, String objectName, Object objectValue, 
			NotificationPolicy notificationPolicy) throws RIBDaemonException{
		this.write(objectClass, 0L, objectName, objectValue, notificationPolicy);
	}

	public void write(long objectInstance, Object objectValue, NotificationPolicy notificationPolicy) throws RIBDaemonException{
		this.write(null, objectInstance, null, objectValue, notificationPolicy);
	}
	
	public void write(String objectClass, String objectName, Object objectValue) throws RIBDaemonException{
		this.write(objectClass, 0L, objectName, objectValue, null);
	}
	
	public void write(long objectInstance, Object objectValue) throws RIBDaemonException{
		this.write(null, objectInstance, null, objectValue, null);
	}
	
	/**
	 * Start an object from the RIB
	 * @param objectClass the class of the object
	 * @param objectName the name of the object
	 * @param objectInstance the instance of the object
	 * @param objectValue the new value of the object
	 * @throws RIBDaemonException
	 */
	public abstract void start(String objectClass, long objectInstance, String objectName, Object objectValue) throws RIBDaemonException;
	
	public void start(String objectClass, String objectName, Object objectValue) throws RIBDaemonException{
		this.start(objectClass, 0L, objectName, objectValue);
	}
	
	public void start(long objectInstance, Object objectValue) throws RIBDaemonException{
		this.start(null, objectInstance, null, objectValue);
	}
	
	public void start(String objectClass, String objectName) throws RIBDaemonException{
		this.start(objectClass, 0L, objectName, null);
	}
	
	public void start(long objectInstance) throws RIBDaemonException{
		this.start(null, objectInstance, null, null);
	}
	
	/**
	 * Stop an object from the RIB
	 * @param objectClass the class of the object
	 * @param objectName the name of the object
	 * @param objectInstance the instance of the object
	 * @param objectValue the new value of the object
	 * @throws RIBDaemonException
	 */
	public abstract void stop(String objectClass, long objectInstance, String objectName, Object objectValue) throws RIBDaemonException;
	
	public void stop(String objectClass, String objectName, Object objectValue) throws RIBDaemonException{
		this.stop(objectClass, 0L, objectName, objectValue);
	}
	
	public void stop(long objectInstance, Object objectValue) throws RIBDaemonException{
		this.stop(null, objectInstance, null, objectValue);
	}
}
