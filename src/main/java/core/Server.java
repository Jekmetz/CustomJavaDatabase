package core;

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
		drivers.add(new DCreate());
		drivers.add(new DDrop());
		drivers.add(new DShow());
		drivers.add(new DSquares());
		drivers.add(new DDump());
		drivers.add(new DInsert());
		drivers.add(new DGetTypes());
		// drivers.add(new DRange());
		// drivers.add(new DTable());
	}

	public Database database() {
		return database;
	}

	public List<Response> interpret(String script) {

		String[] queries = script.split(";"); // Separate queries into an array
	
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
