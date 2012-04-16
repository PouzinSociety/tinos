package rina.utils.apps.rinaband.server;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rina.applibrary.api.ApplicationRegistration;
import rina.applibrary.api.Flow;
import rina.applibrary.api.FlowAcceptor;
import rina.applibrary.api.FlowListener;
import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.impl.CDAPSessionManagerImpl;
import rina.cdap.impl.googleprotobuf.GoogleProtocolBufWireMessageProviderFactory;

/**
 * Implements the behavior of a RINABand Server
 * @author eduardgrasa
 *
 */
public class RINABandServer implements FlowListener, FlowAcceptor{
	
	public static final int MAX_CONCURRENT_TESTS = 10;
	public static final int MAX_NUMBER_OF_FLOWS = 100;
	public static final int MAX_SDU_SIZE_IN_BYTES = 10000;
	public static final int MAX_SDUS_PER_FLOW = 1000000;
	
	/**
	 * The APNamingInfo associated to the control AE of the RINABand application
	 */
	private ApplicationProcessNamingInfo controlApNamingInfo = null;
	
	/**
	 * The APNamingInfo associated to the data AE of the RINABand application
	 */
	private ApplicationProcessNamingInfo dataApNamingInfo = null;
	
	/**
	 * The control flows from RINABand clients
	 */
	private Map<Integer, TestController> ongoingTests = null;
	
	/**
	 * Manages the CDAP sessions to the control AE
	 */
	private CDAPSessionManager cdapSessionManager = null;
	
	private static ExecutorService executorService = Executors.newCachedThreadPool();
	
	public RINABandServer(ApplicationProcessNamingInfo controlApNamingInfo, 
			ApplicationProcessNamingInfo dataApNamingInfo){
		this.controlApNamingInfo = controlApNamingInfo;
		this.dataApNamingInfo = dataApNamingInfo;
		this.ongoingTests = new Hashtable<Integer, TestController>();
		this.cdapSessionManager = new CDAPSessionManagerImpl(new GoogleProtocolBufWireMessageProviderFactory());
	}
	
	public synchronized static void executeRunnable(Runnable runnable){
		executorService.execute(runnable);
	}
	
	public void execute(){
		//Register the control AE and wait for new RINABand clients to come
		try{
			new ApplicationRegistration(controlApNamingInfo, null, null, this, this);
		}catch(Exception ex){
			ex.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Called when a new RINABand client allocates a flow to the control AE 
	 * of the RINABand Server, in order to negotiate the new test parameters
	 */
	public synchronized void flowAllocated(Flow flow) {
		TestController testController = new TestController(dataApNamingInfo, flow, 
				this.cdapSessionManager);
		flow.setSDUListener(testController);
		ongoingTests.put(new Integer(flow.getPortId()), testController);
		System.out.println("New flow to the control AE allocated, with port id "+flow.getPortId());
	}

	/**
	 * Called when the control flow with the RINABand client is deallocated
	 */
	public synchronized void flowDeallocated(Flow flow) {
		TestController testController = ongoingTests.remove(new Integer(flow.getPortId()));
		testController.abort();
		System.out.println("Control flow with port id "+flow.getPortId()+" deallocated");
	}

	/**
	 * Decide when a flow to the control AE can be accepted
	 */
	public String acceptFlow(ApplicationProcessNamingInfo sourceApplication,
			ApplicationProcessNamingInfo destinationApplication) {
		if (!destinationApplication.equals(controlApNamingInfo)){
			return "The requested destination application does not match this application naming information";
		}
		
		if (this.ongoingTests.size() >= MAX_CONCURRENT_TESTS){
			return "Cannot execute more concurrent tests now. Try later";
		}
		
		return null;
	}
}
