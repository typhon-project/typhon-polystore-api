package com.clms.typhonapi.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileStorage {
	
	public static String PATH = "";
			
	protected static String getLatestPath() {
		Path p = Paths.get(PATH, "latest");
		
		ensureFolderExists(p.toString());
		
		return p.toString();
	}
	
	protected static void ensureFolderExists(String path) {
		File f = new File(path);		
		if (f.exists() && f.isDirectory()) {
			return;
		}
		
		f.mkdirs();
	}
	
	protected static String getFileExtention(File f) {
		String extension = "";
		String fileName = f.getName();
		
		int i = fileName.lastIndexOf('.');
		if (i > 0) {
		    extension = fileName.substring(i+1);
		}
		
		return extension;
	}
	
	protected static void writeFile(String filePath, String contents) {
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
	
	protected static String readFile(File f) 
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
