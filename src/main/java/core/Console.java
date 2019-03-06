package core;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import adt.Response;
import adt.Table;
import adt.Row;

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
					out.println("Table:\n" + formatResponse((Table)response.get("table")));
					if(response.get("schema") != null) out.println("Schema:  " + response.get("schema"));
					out.println();
				}
			}	
		}

		in.close();
	}
	
	private static String formatResponse(Table table)
	{
		String output = "";
		if (table == null)
		{
			output = "No table!";
		}else
		{
			//Init tableVars
			ArrayList<String> colNames = new ArrayList<String>(table.getSchema().getStringList("column_names"));
			List<String> colTypes = table.getSchema().getStringList("column_types");
			Integer primaryInt = table.getSchema().getInteger("primary_index");
			String tabName = table.getSchema().getString("table_name");
			//Init method vars
			int numCols = colNames.size();
			int colWidth = 15;
			int tabWidth = numCols*colWidth + 1 + numCols;
			
			if(tabName == null)	//If there is not a given table name...
			{
				output += "+" + repeatStr("-",tabWidth - 2) + "+\n"; //Add a tab to the front
			} else //If there is a given table name...
			{
				output += "    " + repeatStr("_",tabName.length(),tabWidth) + "\n";
				output += "   /" + tabName + "\\\n";
				output += "+" + repeatStr("-",tabWidth - 2) + "+\n";
			}
			
			colNames.set(primaryInt, "*" + colNames.get(primaryInt));
			
			output += "|";
			for(String colName : colNames)
			{
				output += String.format("%-" + colWidth + "s|",repeatStr(colName,1,colWidth));
			}
			
			output += "\n|" + repeatStr("=",tabWidth-2) + "|\n";
			
			
			/*At this point we've added the table name and
			 *the column names 
			 */
			
			if(table.size() > 0)	//If there are rows...
			{
				for(Row row : table.values())			//for every row in the table...
				{
					output += "|";
					for(int i = 0; i < colNames.size(); i++)
					{
						if(row.get(i) == null) //If the object is null...
						{
							output += repeatStr(" ",colWidth);
						} else if(colTypes.get(i).equals("integer"))
						{
							output += String.format("%" + colWidth + "s",repeatStr(row.get(i).toString(), 1, colWidth));
						} else if(colTypes.get(i).equals("boolean"))
						{
							output += String.format("%-" + colWidth + "s",repeatStr(row.get(i).toString(), 1, colWidth));
						} else if(colTypes.get(i).equals("string"))
						{
							output += String.format("%-" + colWidth + "s","\"" + repeatStr(row.get(i).toString(), 1, colWidth - 2) + "\"");
						}
						output += "|";
					}
					output += "\n";
				}
			} else //If there are not rows...
			{
				output += "|" + repeatStr(" ",tabWidth - 2) + "|\n";
			}
			
			/*Add the bottom*/
			
			output += "+" + repeatStr("-",tabWidth - 2) + "+";
		}	
		return output;
	}
	
	private static String repeatStr(String str, int rep, int... trunc)
	{
		String output = "";
		boolean addElipses = false;
		
		if( (trunc.length >= 1) && (trunc[0] < str.length()*rep) )
			addElipses = true;
		
		for(int i = 0; i < rep; i++)
			output += str;
		
		if(addElipses)
		{
			output = str.substring(0,trunc[0] - 3) + "...";
		}
		
		return output;
	}
}
