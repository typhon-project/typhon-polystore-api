package com.clms.typhonapi.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ModelStorage {

	private static final String PATH = "models";
			
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
		
		File f = new File(filePath);
		if (f.exists()) {
			f.delete();
		}
		
		try (PrintWriter out = new PrintWriter(filePath)) {
		    out.println(contents);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	private static String getLatestPath() {
		Path p = Paths.get(PATH, "latest");
		
		ensureFolderExists(p.toString());
		
		return p.toString();
	}
	
	private static void ensureFolderExists(String path) {
		File f = new File(path);		
		if (f.exists() && f.isDirectory()) {
			return;
		}
		
		f.mkdirs();
	}
	
	private static String getFileExtention(File f) {
		String extension = "";
		String fileName = f.getName();
		
		int i = fileName.lastIndexOf('.');
		if (i > 0) {
		    extension = fileName.substring(i+1);
		}
		
		return extension;
	}
	
	private static String readFile(File f) 
	{
		byte[] encoded;
		try {
			encoded = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
			return new String(encoded, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  
		return "";
	}
}
