package rina.enrollment.impl;

import java.util.List;

import rina.configuration.RINAConfiguration;
import rina.enrollment.api.EnrollmentTask;
import rina.enrollment.api.Neighbor;

/**
 * This class periodically looks for known neighbors we're currently not enrolled to, and tries 
 * to enroll with them again.
 * @author eduardgrasa
 *
 */
public class NeighborsEnroller implements Runnable{

	/**
	 * The list of known neighbors
	 */
	private List<Neighbor> knownNeighbors = null;
	
	/**
	 * The enrollment task
	 */
	private EnrollmentTask enrollmentTask = null;
	
	public NeighborsEnroller(EnrollmentTask enrollmentTask){
		this.enrollmentTask = enrollmentTask;
	}
	
	public void run() {
		while(true){
			this.knownNeighbors = enrollmentTask.getIPCProcess().getNeighbors();
			for(int i=0; i<this.knownNeighbors.size(); i++){
				if (enrollmentTask.isEnrolledTo(this.knownNeighbors.get(i).getApplicationProcessName())){
					//We're enrolled to this guy, continue
					continue;
				}
				
				enrollmentTask.initiateEnrollment(this.knownNeighbors.get(i));
			}
			
			try{
				Thread.sleep(RINAConfiguration.getInstance().getLocalConfiguration().getNeighborsEnrollerPeriodInMs());
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}

}
