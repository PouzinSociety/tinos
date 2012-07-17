package rina.examples.apps.cliclient;

import java.util.Scanner;

public class Main {

	public static void main(String[] args){
		try{
			System.out.println("Welcome to the RINA Client Program. Which application would you like to connect to?");
			System.out.println();
			System.out.println("Available applications: ");
			System.out.println("1. Echo Server");
			System.out.println();
			System.out.println("In order to connect to an application, type its number followed by the 'Enter' key.");
			System.out.println();
			System.out.print("Selected application: ");

			Scanner scanner = new Scanner(System.in);
			int option = scanner.nextInt();
			switch (option){
			case 1:
				System.out.println("Available QoS ids to connect to the server: ");
				System.out.println("1. Unreliable");
				System.out.println("2. Reliable");
				System.out.println();
				System.out.println("Type '1' or '2' followed by the 'Enter' key.");
				System.out.println();
				System.out.print("Selected qosId: ");
				option = scanner.nextInt();
				if (option <1 && option >2){
					System.out.println("Invalid option");
					break;
				}
				EchoClient echoClient = new EchoClient(option);
				echoClient.run();
				break;
			default:
				System.out.println("Application "+option+" is not available.");
			}

			System.exit(0);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

}
