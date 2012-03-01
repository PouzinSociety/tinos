package rina.utils.apps.connectiongenerator.cliclient;

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
	}
	
	public void runTest(){
		
		//Run the printStatistics timer
		timer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run() {
				printStatistics();
				if (activeFlows == 0){
					timer.cancel();
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
	
	public void flowCompletedSuccessfully(){
		synchronized(successFulFlowsLock){
			successfulFlows++;
		}
		
		synchronized(completedFlowsLock){
			completedFlows++;
		}
		
		synchronized(activeFlowsLock){
			activeFlows--;
		}
	}
	
	public void flowAllocatedSuccessfully(){
		synchronized(activeFlowsLock){
			activeFlows++;
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
}
