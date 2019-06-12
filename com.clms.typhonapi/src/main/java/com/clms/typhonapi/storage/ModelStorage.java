package com.clms.typhonapi.storage;

import java.io.File;
import java.nio.file.Paths;

public class ModelStorage extends FileStorage {
			
	public static void addDlModel(String name, String contents) {
		addModel(name, contents);
	}
	
	public static void addMlModel(String name, String contents) {
		addModel(name, contents);
	}
	
	public static String getDlModel() {
		return getModel("tdl");
	}
	
	public static String getMlModel() {
		return getModel("tml");
	}
	
	private static void addModel(String name, String contents) {
		String latestPath = getLatestPath();
		
		String filePath = Paths.get(latestPath, name).toString();
		
		writeFile(filePath, contents);
	}
	
	private static String getModel(String extention) {
		String latestPath = getLatestPath();
		
		for (File f : new File(latestPath).listFiles()) {
	      if (getFileExtention(f).equals(extention)) {
	    	  return readFile(f);
	      }
		}
		
		return "";
	}
	
}
