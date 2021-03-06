package core;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import adt.Database;
import adt.Response;
import adt.Table;
import adt.Utility;
import driver.DAlterDrop;
import driver.DAlterInsert;
import driver.DAlterRenameColumn;
import driver.DAlterRenameTable;
import driver.DCreate;
import driver.DDrop;
import driver.DDump;
import driver.DEcho;
import driver.DExport;
import driver.DGetTypes;
import driver.DImport;
import driver.DInsert;
import driver.DSelect;
import driver.DShow;
import driver.DSquares;
import driver.Driver;

/**
 * This class implements a server with an active connection to a backing
 * database.
 * 
 * Finish implementing the required features but do not modify the protocols.
 */
public class Server implements Closeable{
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
		drivers.add( new DEcho()             );
		drivers.add( new DCreate()           );
		drivers.add( new DDrop()             );
		drivers.add( new DShow()             );
		drivers.add( new DSquares()          );
		drivers.add( new DDump()             );
		drivers.add( new DInsert()           );
		drivers.add( new DGetTypes()         );
		drivers.add( new DAlterInsert()      );
		drivers.add( new DAlterRenameColumn());
		drivers.add( new DAlterRenameTable() );
		drivers.add( new DAlterDrop()        );
		drivers.add( new DSelect()           );
		drivers.add( new DExport()           );
		drivers.add( new DImport()           );
		// drivers.add(new DRange());
		// drivers.add(new DTable());
		
		deserialize(database);
	}

	public Database database() {
		return database;
	}

	public List<Response> interpret(String script) {

		String[] queries = script.split(";\\n*"); // Separate queries into an array
	
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
						
						/**********CREATING PERSISTENT LOG FILE**************/
						if(driver.isMutation())
						{
							try {
								File f = new File(Utility.getRootDirectory("serialize").getAbsolutePath() + "\\logFile.txt");
								f.createNewFile();
								FileWriter file = new FileWriter(f,true);
								file.write(query + ";");
								file.close();
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
							catch (IOException e) {
								e.printStackTrace();
							}
						}
						/**************************/ 
					}
				}
				if (!found) // if not found...
					responses.add(new Response(false, "Unrecognized query: " + query, null)); // show that.

			}
		}
		return responses; // Give the people what they want
	}

	@Override
	public void close() throws IOException {
		Set<String> tabNames = database.keySet();
		
		try {
			File dir = Utility.getRootDirectory("serialize");
			dir.mkdir();
			
			for(String tabName : tabNames)
				serializeTable(tabName,dir);
			
			//Delete log file
			File logFile = new File(dir.getAbsolutePath() + "\\logFile.txt");
			FileWriter fw = new FileWriter(logFile);
			fw.write("");
			fw.close();
			
		} catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	private void serializeTable(String tabName, File dir) throws FileNotFoundException,IOException
	{
		FileOutputStream file = new FileOutputStream(new File(dir.getAbsolutePath() + "\\" + tabName + ".ser"));
		ObjectOutputStream oos = new ObjectOutputStream(file);
		
		oos.writeObject(database.get(tabName));
		
		oos.close();
		file.close();
	}
	
	private void deserialize(Database database)
	{
		File dir = Utility.getRootDirectory("serialize");
		
		/*****************DELETE Dropped TABLES*************/
		ArrayList<String> droppedTableNames = new ArrayList<>();
		File droppedTables = new File(Utility.getRootDirectory("serialize").getAbsolutePath() + "/droppedTables.txt");
		
		if(droppedTables.exists()) 	//If we even have a dropped tables file...
		{
			try {
				/*get a list of the dropped tables*/
				FileReader fr = new FileReader(droppedTables);
				BufferedReader br = new BufferedReader(fr);
				String line = null;
				
				while((line = br.readLine()) != null)
					if (!line.equals(""))
						droppedTableNames.add(line);
				
				br.close();
				fr.close();
				
				/*Delete all of those files before startup*/
				for(String str : droppedTableNames)
				{
					File del = new File(Utility.getRootDirectory("serialize").getAbsolutePath() + "/" + str + ".ser");
					del.delete();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
			
		droppedTables.delete();
		/**************/
		
		/*********AFTER CRASH***********/
		File logFile = new File(dir.getAbsolutePath() + "\\logFile.txt");
		try {
			FileReader fr = new FileReader(logFile);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			boolean badCrash = false;
			if(line != null)
			{
				badCrash = true;
				this.interpret(line);
			}
			br.close();
			fr.close();
			
			if(badCrash)
			{
				Set<String> tabNames = database.keySet();
				
				try {					
					for(String tabName : tabNames)
						serializeTable(tabName,dir);
				} catch(FileNotFoundException e)
				{
					System.out.println("File not found! Check serialization!");
				}
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		/******************/
		
		/**************EXPECTED SHUTDOWN****************/
		File [] files = dir.listFiles(file ->  file.getName().matches("[a-zA-Z][a-zA-Z0-9_]*.ser"));
		
		FileInputStream file = null;
		ObjectInputStream ois = null;
		
		for(File f : files)
		{
			try 
			{
				file = new FileInputStream(f);
				ois = new ObjectInputStream(file);
				
				Table table = (Table) ois.readObject();
				database.put(table.getSchema().getString("table_name"), table);
				
				ois.close();
				file.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		/************************/
		
		/*********DELETE LOG FILE*************/
		try {
			FileWriter fw = new FileWriter(logFile);
			fw.write("");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		/***********************/
	}
}
