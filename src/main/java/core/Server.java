package core;

import adt.Table;

import driver.*;
import adt.Response;
import adt.Database;

import java.util.List;
import java.util.LinkedList;

/**
 * This class implements a server with an active connection to a backing
 * database.
 * 
 * Finish implementing the required features but do not modify the protocols.
 */
public class Server {
	@SuppressWarnings("unused")
	private static final String STUDENT_NAME = "Jay Kmetz", STUDENT_IDNUM = "800249366",
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
		drivers.add(new DTable());
		drivers.add(new DSquares());
		// drivers.add(new DRange());
	}

	public Database database() {
		return database;
	}

	public List<Response> interpret(String script) {

		String[] queries = script.split(";"); // Separate queries into an array
		/*
		 * TODO: This only checks the first driver for a response to the first query.
		 * Instead, iterate over all drivers until one of them gives a response for the
		 * first query. Default to a failure response only if no driver gave a response
		 * for a query. Then iterate again for the next query. Don't forget to pass a
		 * reference to the actual database.
		 */
		List<Response> responses = new LinkedList<Response>(); // Initialize responses

		{
			Response response; 				// batton the hatches
			boolean found; 					// ready the port bow
			for (String query : queries) 	// for every query in queries...
			{
				found = false; 					// init found to be false
				for (Driver driver : drivers) 	// for every driver in drivers...
				{
					response = driver.execute(database, query); // execute that query through the driver
					if (response != null) 					// if response...
					{
						responses.add(response); 	// add it
						
						if(response.get("table") != null)
						{
							database.put(((Table)response.get("table")).getSchema().getString("table_name"),(Table)response.get("table"));
						}
						
						found = true; 				// ITS BEEN FOUND... HUZZAH!
					}

				}
				if (!found) // if not found...
					responses.add(new Response(false, "Unrecognized query: " + query, null)); // show that.
			}
		}

		return responses; // Give the people what they want
	}
}
