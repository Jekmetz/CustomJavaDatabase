package core;

import adt.Response;
import java.util.List;
import java.util.Scanner;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/** 
 * This class implements an interactive console
 * for a database server.
 * 
 * Finish implementing the required features
 * but do not modify the protocols.
 */
public class Console {
	/**
	 * This is the entry point for running the
	 * console as a stand-alone program.
	 */
	public static void main(String[] args) {
		prompt(new Server(), System.in, System.out);
	}
	
	public static void prompt (Server server, InputStream in_stream, OutputStream out_stream) {
		//Initialize Varibles
			//Print Stream
		final Scanner in = new Scanner(in_stream);
		final PrintStream out = new PrintStream(out_stream);
			//REP Loop Vars
		boolean stay = true;
		String command = null;
			//Functional
		List<Response> responses = null;
		
		while(stay)
		{
			out.print(">> ");		//prompt for command
			command = in.nextLine();	//read command

			if(command.toLowerCase().equals("exit"))		//if they want to exit...
				stay = false;				//kill it
			else
			{
				responses = server.interpret(command);	//Otherwise, interpret command
			
			
				for(Response response : responses)	//for each response...
				{
					//Print it out
					out.println("Success: " + response.get("success"));
					out.println("Message: " + response.get("message"));
					out.println("Table:   " + response.get("table"));
					if(response.get("schema") != null) out.println("Schema:  " + response.get("schema"));
					out.println();
				}
			}

			
			// TODO: Support tabular view, in a later module.
		}

		in.close();
	}
}
