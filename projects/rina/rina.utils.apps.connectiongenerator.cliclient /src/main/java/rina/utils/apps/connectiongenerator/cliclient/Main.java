package rina.utils.apps.connectiongenerator.cliclient;

import java.util.Scanner;

public class Main {

	public static final String SOURCE_APPLICATION = "rina/utils/apps/connectiongenerator/cliclient";
	public static final String DEFAULT_APPLICATION = "rina/utils/apps/connectiongenerator/server";
	public static final int DEFAULT_DURATION_IN_SECONDS = 10;
	public static final int DEFAULT_FLOW_GENERATION_RATE_IN_FLOWS_PER_SECOND = 10;
	public static final int DEFAULT_MEAN_FLOW_DURATION_IN_SECONDS = 10;

	public static void main(String[] args){
		String aux = null;
		String destinationApplication = null;
		int durationInSeconds = 0;
		int flowGenerationDistribution = 0;
		int flowGenerationRateInFlowsPerSecond = 0;
		int flowDurationDistribution = 0;
		int meanFlowDurationInSeconds = 0;
		Scanner scanner = new Scanner(System.in);

		//Welcome
		System.out.print("\f");
		System.out.println("Welcome to the RINA Connection Generator Program.");
		System.out.println();

		//Get destination application process name
		System.out.println("To what application you want to connect? " +
				"Typing enter will connect you to the default application " +DEFAULT_APPLICATION);
		destinationApplication = scanner.nextLine();
		if (destinationApplication == null || destinationApplication.equals("")){
			destinationApplication = DEFAULT_APPLICATION;
		}

		//Get duration of the test
		System.out.println("How many seconds you want the test to last? " +
				"Typing enter will select the default duration of  " 
				+DEFAULT_DURATION_IN_SECONDS+ " seconds.");
		aux = scanner.nextLine();
		if (aux == null || aux.equals("")){
			durationInSeconds = DEFAULT_DURATION_IN_SECONDS;
		}else{
			try{
				durationInSeconds = Integer.parseInt(aux);
			}catch(Exception ex){
				System.out.println("There was an error parsing the duration value you entered. The default " +
						"duration of " + DEFAULT_DURATION_IN_SECONDS+ " seconds will be used");
				durationInSeconds = DEFAULT_DURATION_IN_SECONDS;
			}
		}

		//Get flow request distribution
		System.out.println("Choose the flow requests generation distribution: ");
		System.out.println("1. Uniform");
		aux = scanner.nextLine();
		try{
			flowGenerationDistribution = Integer.parseInt(aux);
			if (flowGenerationDistribution != 1){
				throw new Exception();
			}
		}catch(Exception ex){
			System.out.println("There was an error parsing the value you entered. The default " +
			" Uniform distribution will be used");
			flowGenerationDistribution = 1;
		}

		//Get flow rate
		System.out.println("What is the flow request rate in flows per second? " +
				"Typing enter will select the default flow request rate of  " 
				+DEFAULT_FLOW_GENERATION_RATE_IN_FLOWS_PER_SECOND+ " flows per second.");
		aux = scanner.nextLine();
		if (aux == null || aux.equals("")){
			flowGenerationRateInFlowsPerSecond = DEFAULT_FLOW_GENERATION_RATE_IN_FLOWS_PER_SECOND;
		}else{
			try{
				flowGenerationRateInFlowsPerSecond = Integer.parseInt(aux);
			}catch(Exception ex){
				System.out.println("There was an error parsing the flow request value you entered. The default " +
						"flow request rate of " + DEFAULT_FLOW_GENERATION_RATE_IN_FLOWS_PER_SECOND+ " flows per second will be used");
				flowGenerationRateInFlowsPerSecond = DEFAULT_FLOW_GENERATION_RATE_IN_FLOWS_PER_SECOND;
			}
		}
		
		//Get flow duration distribution
		System.out.println("Choose the flow duration distribution: ");
		System.out.println("1. Uniform");
		aux = scanner.nextLine();
		try{
			flowDurationDistribution = Integer.parseInt(aux);
			if (flowDurationDistribution != 1){
				throw new Exception();
			}
		}catch(Exception ex){
			System.out.println("There was an error parsing the value you entered. The default " +
			" Uniform distribution will be used");
			flowDurationDistribution = 1;
		}

		//Get mean flow duration
		System.out.println("What is the mean flow duration in seconds? " +
				"Typing enter will select the default mean flow duration of  " 
				+DEFAULT_MEAN_FLOW_DURATION_IN_SECONDS+ " seconds.");
		aux = scanner.nextLine();
		if (aux == null || aux.equals("")){
			meanFlowDurationInSeconds = DEFAULT_MEAN_FLOW_DURATION_IN_SECONDS;
		}else{
			try{
				meanFlowDurationInSeconds = Integer.parseInt(aux);
			}catch(Exception ex){
				System.out.println("There was an error parsing the flow request value you entered. The default " +
						"mean flow duration of " + DEFAULT_MEAN_FLOW_DURATION_IN_SECONDS+ " seconds will be used");
				meanFlowDurationInSeconds = DEFAULT_MEAN_FLOW_DURATION_IN_SECONDS;
			}
		}
		
		System.out.println("Executing the test with the following parameters:");
		System.out.println("Destination application process name "+destinationApplication);
		System.out.println("Test duration: "+durationInSeconds+ " seconds");
		System.out.println("Distribution of flow requests: "+flowGenerationDistribution);
		System.out.println("Flow requests rate: "+flowGenerationRateInFlowsPerSecond+ " flows per second");
		System.out.println("Flow duration distribution: "+flowDurationDistribution);
		System.out.println("Mean flow duration: "+meanFlowDurationInSeconds+ " seconds");
		
		FlowGenerator flowGenerator = new FlowGenerator(destinationApplication, durationInSeconds, 
				flowGenerationDistribution, flowGenerationRateInFlowsPerSecond, flowDurationDistribution, 
				meanFlowDurationInSeconds);
		flowGenerator.runTest();
	}

}
