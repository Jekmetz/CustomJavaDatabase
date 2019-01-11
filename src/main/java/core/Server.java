package core;

import driver.*;
import adt.Response;
import adt.Database;

import java.util.List;
import java.util.LinkedList;

/** 
 * This class implements a server with an active
 * connection to a backing database.
 * 
 * Finish implementing the required features
 * but do not modify the protocols.
 */
public class Server {
	@SuppressWarnings("unused")
	private static final String
		STUDENT_NAME  = "Jay Kmetz",
		STUDENT_IDNUM = "800249366",
		STUDENT_EMAIL = "jek0025@mix.wvu.edu";
	
	private Database database;
	private List<Driver> drivers;
	
	public Server() {
		this(new Database());
	}
	
	public Server(Database database) {
		this.database = database;
		
		// TODO: Add each new driver as it is implemented.
		drivers = new LinkedList<Driver>();
		drivers.add(new DEcho());
		// drivers.add(new DRange());
	}
	
	public Database database() {
		return database;
	}
	
	public List<Response> interpret(String script) {
		/*
		 * TODO: This wrongly assumes the entire script
		 * is a single query. However, there may be
		 * one or more semicolon-delimited queries in
		 * the script to be split and parsed distinctly.
		 */
		String[] queries = {script};
		
		/* TODO: This only checks the first driver for a
		 * response to the first query. Instead, iterate over
		 * all drivers until one of them gives a response
		 * for the first query. Default to a failure response
		 * only if no driver gave a response for a query.
		 * Then iterate again for the next query. Don't
		 * forget to pass a reference to the actual database.
		 */
		List<Response> responses = new LinkedList<Response>();
		Response response = drivers.get(0).execute(null, queries[0]);
		if (response != null)
			responses.add(response);
		else 
			responses.add(new Response(false, "Unrecognized query", null));
		
		return responses;
	}
}
