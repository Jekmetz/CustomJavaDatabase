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
		final Scanner in = new Scanner(in_stream);
		final PrintStream out = new PrintStream(out_stream);
		
		/*
		 * TODO: Use a REPL to prompt the user for inputs,
		 * and send each input to the server for parsing.
		 * No inputs are to be parsed in the console, except
		 * for the case-insensitive sentinel EXIT, which
		 * terminates the console.
		*/
		out.print(">> ");
		String text = in.nextLine();
		in.close();
		
		List<Response> responses = server.interpret(text);
		
		/*
		 * TODO: This wrongly assumes that there is only
		 * one response from the server. However, there 
		 * may be one or more responses, and each response
		 * should be reported in the order listed.
		 */
		out.println("Success: " + responses.get(0).get("success"));
		out.println("Message: " + responses.get(0).get("message"));
		out.println("Table:   " + responses.get(0).get("table"));

		// TODO: Support tabular view, in a later module.
	}
}
