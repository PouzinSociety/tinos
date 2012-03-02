package rina.utils.apps.connectiongenerator.cliclient;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlowGenerator {
	private String destinationApplication = null;
	private int durationInSeconds = 0;
	private int flowGenerationDistribution = 0;
	private int flowGenerationRateInFlowsPerSecond = 0;
	private int flowDurationDistribution = 0;
	private int meanFlowDurationInSeconds = 0;
	
	private int generatedFlows = 0;
	private int activeFlows = 0;
	private int completedFlows = 0;
	
	private int successfulFlows = 0;
	private int failedFlows = 0;
	
	private Timer timer = null;
	private boolean end = false;
	private ExecutorService executorService = null;
	
	private List<Long> flowSetupTimes = null;
	private List<Long> flowTearDownTimes = null;
	
	//Locks
	private Object successFulFlowsLock = new Object();
	private Object failedFlowsLock = new Object();
	private Object completedFlowsLock = new Object();
	private Object activeFlowsLock = new Object();
	private Object generatedFlowsLock = new Object();
	
	public FlowGenerator(String destinationApplication, int durationInSeconds, int flowGenerationDistribution, 
			int flowGenerationRateInFlowsPerSecond, int flowDurationDistribution, int meanFlowDurationInSeconds){
		this.destinationApplication = destinationApplication;
		this.durationInSeconds = durationInSeconds;
		this.flowGenerationDistribution = flowGenerationDistribution;
		this.flowGenerationRateInFlowsPerSecond = flowGenerationRateInFlowsPerSecond;
		this.flowDurationDistribution = flowDurationDistribution;
		this.meanFlowDurationInSeconds = meanFlowDurationInSeconds;
		
		timer = new Timer();
		executorService = Executors.newCachedThreadPool();
		flowSetupTimes = new ArrayList<Long>();
		flowTearDownTimes = new ArrayList<Long>();
	}
	
	public void runTest(){
		
		//Run the printStatistics timer
		timer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run() {
				printStatistics();
				if (activeFlows == 0){
					timer.cancel();
					printFinalStatistics();
					System.exit(0);
				}
			}
		}, 1000, 1000);
		
		//Run the finalization timer
		timer.schedule(new TimerTask(){
			@Override
			public void run() {
				end();
			}
		}, durationInSeconds*1000);
		
		int sleepTime = 0;
		while(!end){
			sleepTime = getTimeToNextFlowRequest();
			try{
				Thread.sleep(sleepTime);
				if (end){
					break;
				}else{
					generateFlow();
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	private int getTimeToNextFlowRequest(){
		if (this.flowGenerationDistribution == 1){
			//Uniform distribution
			double sleepTime = Math.random()*2000/flowGenerationRateInFlowsPerSecond;
			return new Double(sleepTime).intValue();
		}else{
			end = true;
			return 0;
		}
	}
	
	public void end(){
		end = true;
	}
	
	public void generateFlow(){
		synchronized(generatedFlowsLock){
			SingleFlowGenerator singleFlowGenerator = 
				new SingleFlowGenerator(generateDuration(), destinationApplication, this);
			executorService.execute(singleFlowGenerator);
			generatedFlows++;
		}
	}
	
	private int generateDuration(){
		if (this.flowGenerationDistribution == 1){
			//Uniform distribution
			double duration = Math.random()*2000*meanFlowDurationInSeconds;
			return new Double(duration).intValue();
		}else{
			end = true;
			return 0;
		}
	}
	
	public void flowCompletedSuccessfully(long tearDownTime){
		synchronized(successFulFlowsLock){
			successfulFlows++;
		}
		
		synchronized(completedFlowsLock){
			completedFlows++;
			flowTearDownTimes.add(new Long(tearDownTime));
		}
		
		synchronized(activeFlowsLock){
			activeFlows--;
		}
	}
	
	public void flowAllocatedSuccessfully(long setupTime){
		synchronized(activeFlowsLock){
			activeFlows++;
			flowSetupTimes.add(new Long(setupTime));
		}
	}
	
	public void flowWithError(){
		synchronized(completedFlowsLock){
			completedFlows++;
		}
	}
	
	public void flowCompletedWithError(){
		synchronized(completedFlowsLock){
			completedFlows++;
		}
		
		synchronized(activeFlowsLock){
			activeFlows--;
		}
	}
	
	public void printStatistics(){
		System.out.println("Generated flows: "+generatedFlows+". Active flows: "+activeFlows+". Completed flows: "+completedFlows);
	}
	
	public void printFinalStatistics(){
		System.out.println("Flow setup time: ");
		System.out.println("Min: "+min(flowSetupTimes)+" ms; Max: "+max(flowSetupTimes)+" ms; Mean: "+ mean(flowSetupTimes)+ " ms");
		System.out.println("Flow teardown time: ");
		System.out.println("Min: "+min(flowTearDownTimes)+" ms; Max: "+max(flowTearDownTimes)+" ms; Mean: "+ mean(flowTearDownTimes)+ " ms");
	}
	
	private long min(List<Long> values){
		Long minimum = Long.MAX_VALUE;
		for(int i=0; i<values.size(); i++){
			if (values.get(i).longValue() < minimum.longValue()){
				minimum = values.get(i);
			}
		}
		
		return minimum.longValue();
	}
	
	private long max(List<Long> values){
		Long maximum = new Long(0);
		for(int i=0; i<values.size(); i++){
			if (values.get(i).longValue() > maximum.longValue()){
				maximum = values.get(i);
			}
		}
		
		return maximum.longValue();
	}
	
	private long mean(List<Long> values){
		long total = 0;
		for(int i=0; i<values.size(); i++){
			total = total + values.get(i).longValue();
		}
		
		return total/values.size();
	}
}
