package rina.examples.apps.cliclient;

import java.util.Scanner;

public class Main {
	
	public static void main(String[] args){
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
			EchoClient echoClient = new EchoClient();
			echoClient.run();
			break;
		default:
			System.out.println("Application "+option+" is not available.");
		}
		
		System.exit(0);
	}

}
