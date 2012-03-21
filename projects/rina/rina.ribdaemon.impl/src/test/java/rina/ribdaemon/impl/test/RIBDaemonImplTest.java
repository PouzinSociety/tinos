package rina.ribdaemon.impl.test;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.SimpleRIBObject;
import rina.ribdaemon.api.SimpleSetRIBObject;
import rina.ribdaemon.impl.RIBDaemonImpl;

public class RIBDaemonImplTest {
	
	private static RIBDaemonImpl ribDaemon = null;
	private static IPCProcess ipcProcess = null;
	
	public static final String TEST_RIB_OBJECT_1 = "/test/rib/object/1";
	public static final String TEST_RIB_OBJECT_2 = "/test/rib/object/2";
	public static final String TEST_CLASS = "test object";
	
	@BeforeClass
	public static void setup(){
		ribDaemon = new RIBDaemonImpl();
		ipcProcess = new MockIPCProcess();
		ribDaemon.setIPCProcess(ipcProcess);
		ipcProcess.addIPCProcessComponent(ribDaemon);
	}
	
	@Test
	public void testAddObject() throws RIBDaemonException{
		//Create a simple object
		RIBObject ribObject = new SimpleRIBObject(ipcProcess, TEST_RIB_OBJECT_1, TEST_CLASS, new String("Test RIB Object 1"));
		ribDaemon.addRIBObject(ribObject);
		Assert.assertEquals(1, ribDaemon.getRIBObjects().size());
		
		//Try to create the same object again, the RIB Daemon should throw an exception
		ribObject = new SimpleRIBObject(ipcProcess, TEST_RIB_OBJECT_1, TEST_CLASS, new String("Test RIB Object 1"));
		try{
			ribDaemon.addRIBObject(ribObject);
			Assert.assertFalse(true);
		}catch(Exception ex){
			ex.printStackTrace();
			Assert.assertTrue(true);
		}
		Assert.assertEquals(1, ribDaemon.getRIBObjects().size());
		
		//Add a simple set object
		ribObject = new SimpleSetRIBObject(ipcProcess, TEST_RIB_OBJECT_2, TEST_CLASS, TEST_CLASS){
			@Override
			public Object getObjectValue(){
				return new Object[0];
			}
		};
		ribDaemon.addRIBObject(ribObject);
		
		Assert.assertEquals(2, ribDaemon.getRIBObjects().size());
	}
	
	@Test
	public void testReadCreateObjects() throws RIBDaemonException{
		RIBObject ribObject = ribDaemon.read(TEST_CLASS, TEST_RIB_OBJECT_1, 0L);
		Assert.assertEquals(String.class, ribObject.getObjectValue().getClass());
		Assert.assertEquals("Test RIB Object 1", ribObject.getObjectValue().toString());
		
		ribDaemon.create(TEST_CLASS, TEST_RIB_OBJECT_2+"/child1", 0, new String("Test RIB Object 2 child 1"), null);
		Assert.assertEquals(3, ribDaemon.getRIBObjects().size());
		try{
			ribDaemon.create(TEST_CLASS, TEST_RIB_OBJECT_2+"/child1", 0, new String("Test RIB Object 2 child 1"), null);
			Assert.assertFalse(true);
		}catch(Exception ex){
			ex.printStackTrace();
			Assert.assertTrue(true);
		}
		Assert.assertEquals(3, ribDaemon.getRIBObjects().size());
		
		ribDaemon.create(TEST_CLASS, TEST_RIB_OBJECT_2+"/child2", 0, new String("Test RIB Object 2 child 2"), null);
		Assert.assertEquals(4, ribDaemon.getRIBObjects().size());
		try{
			ribDaemon.create(TEST_CLASS, TEST_RIB_OBJECT_2, 0, new String("Test RIB Object 2 child 2"), null);
			Assert.assertFalse(true);
		}catch(Exception ex){
			ex.printStackTrace();
			Assert.assertTrue(true);
		}
		Assert.assertEquals(4, ribDaemon.getRIBObjects().size());
		
		try{
			ribObject = ribDaemon.read(TEST_CLASS, "test2", 0);
			Assert.assertFalse(true);
		}catch(Exception ex){
			ex.printStackTrace();
			Assert.assertTrue(true);
		}
		
		ribObject = ribDaemon.read(TEST_CLASS, TEST_RIB_OBJECT_2+"/child2", 0);
		Assert.assertEquals(String.class, ribObject.getObjectValue().getClass());
		Assert.assertEquals("Test RIB Object 2 child 2", ribObject.getObjectValue().toString());
	}
	
	@Test
	public void testWriteDeleteObjects() throws RIBDaemonException{
		try{
			ribDaemon.write(TEST_CLASS, TEST_RIB_OBJECT_2, 0, new String("Written object"), null);
			Assert.assertFalse(true);
		}catch(Exception ex){
			ex.printStackTrace();
			Assert.assertTrue(true);
		}
		
		ribDaemon.write(TEST_CLASS, TEST_RIB_OBJECT_1, 0, new String("Written object"), null);
		RIBObject ribObject = ribDaemon.read(TEST_CLASS, TEST_RIB_OBJECT_1, 0L);
		Assert.assertEquals("Written object", ribObject.getObjectValue().toString());
		
		ribDaemon.delete(TEST_CLASS, TEST_RIB_OBJECT_2+"/child2", 0, null, null);
		Assert.assertEquals(3, ribDaemon.getRIBObjects().size());
		
		ribDaemon.delete(TEST_CLASS, TEST_RIB_OBJECT_2+"/child1", 0, null, null);
		Assert.assertEquals(2, ribDaemon.getRIBObjects().size());
		
		try{
			ribDaemon.delete(TEST_CLASS, TEST_RIB_OBJECT_1, 0, null, null);
			Assert.assertFalse(true);
		}catch(Exception ex){
			ex.printStackTrace();
			Assert.assertTrue(true);
		}
	}
	
	@Test
	public void testRemoveRIBObjects() throws RIBDaemonException{
		ribDaemon.removeRIBObject(TEST_RIB_OBJECT_2);
		Assert.assertEquals(1, ribDaemon.getRIBObjects().size());
		
		try{
			ribDaemon.removeRIBObject(TEST_RIB_OBJECT_2);
		}catch(Exception ex){
			ex.printStackTrace();
			Assert.assertTrue(true);
		}
		Assert.assertEquals(1, ribDaemon.getRIBObjects().size());
		
		ribDaemon.removeRIBObject(TEST_RIB_OBJECT_1);
		Assert.assertEquals(0, ribDaemon.getRIBObjects().size());
	}

}
