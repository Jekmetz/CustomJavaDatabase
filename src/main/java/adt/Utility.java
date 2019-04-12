package adt;

import java.io.File;

public class Utility {
	public static String stripFL(String data) { return data.substring(1,data.length()-1); }
	
	public static File ensureDirExists(String canDirPath)
	{
		File file = new File(System.getProperty("user.dir") + canDirPath);
		file.mkdir();
		return file;
	}
	
	public static File getRootDirectory(String dirName)
	{
		return new File(System.getProperty("user.dir") + "\\" + dirName);
	}
}
