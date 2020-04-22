package com.clms.typhonapi.utils;

import com.mongodb.client.MongoIterable;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.clms.typhonapi.models.Service;
import com.clms.typhonapi.models.ServiceStatus;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
public class DbUtils {

	@Autowired
	private ServiceRegistry serviceRegistry;
	
	private Map<String, Object> dbConnections = new HashMap<String, Object>();
	
    public File mariaBackupProcess(String host, String port, String user, String password, String database_name, String backup_name){
        ProcessBuilder pb = new ProcessBuilder();
        File backupFile;
        File f = new File("/backups/");
        if(f.exists() && f.isDirectory()) {
             backupFile = new File("/backups/"+backup_name+"maria_"+database_name+"_"+DateTime.now().toString("ddMMyyyy")+".sql");
        }
        else{
            boolean direcot = new File("/backups/").mkdir();
            backupFile=new File("/backups/"+backup_name+"maria_"+database_name+"_"+DateTime.now().toString("ddMMyyyy")+".sql");
        }


        pb.command("mysqldump","--host="+host,"-p "+port,"--user="+user,"--password="+password,database_name);
        pb.directory(new File(System.getProperty("user.home")));
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.redirectOutput(ProcessBuilder.Redirect.to(backupFile));

        Process process;
        try {
             process = pb.start();
             process.waitFor();
        } catch (IOException e) {
            backupFile.delete();
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            backupFile.delete();
            e.printStackTrace();
        }
        
        return backupFile;
    }

    public String mariaRestore(String host, String port, String user, String password, String database_name, String backup_name){
        ProcessBuilder pb = new ProcessBuilder();
        File f = new File("/backups/"+backup_name);
        if(!f.exists() || f.isDirectory()) {
            return "Backup does not exist! ";
        }
        pb.command("mysql","--host="+host,"--port="+port,"--user="+user,"--password="+password, database_name,"--execute=source "+"/backups/"+backup_name);
        System.out.println("" + pb.command());
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process process;
        try {
            process = pb.start();
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
            return "nope";
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "OK";
    }
    
    public void updateDbStatus() {
    	ArrayList<Service> dbs = serviceRegistry.getDatabases();
    	for (Service db : dbs) {
    		if (!dbConnections.containsKey(db.getName())) {
    			db.setStatus(ServiceStatus.OFFLINE);
    			continue;
    		}
    		
    		db.setStatus(ServiceStatus.ONLINE);
    	}
    }
    
    public Map<String, Object> getDbConnections() {
    	return dbConnections;
    }
    
    public void bringDatabasesDown() throws Exception {
    	closeDbConnections();
		dbConnections.clear();
		updateDbStatus();
    }
    
    public void updateDbConnections() throws Exception {
		closeDbConnections();
		dbConnections.clear();
		
		ArrayList<Service> dbs = serviceRegistry.getDatabases();
		for (Service db : dbs) {
			Object con = null;
			System.out.println("DB: " + db);
			
			switch (db.getDbType()) {
			 case MariaDb:
				 con = getMariaDBConnection(db);
				 break;
			 case MongoDb:
				 con = getMongoDBConnection(db);
				 break;
			 case MysqlDb:
				 con = getMysqlDBConnection(db);
				 break;
			 default:
				 throw new Exception("Unhandled database type: " + db.getDbType());
			}
			
			if (con != null) {
				 dbConnections.put(db.getName(), con);
			}
		}
		
		updateDbStatus();
	}
	
	private void closeDbConnections() throws Exception {
		ArrayList<Service> dbs = serviceRegistry.getDatabases();
		
		for (Service db : dbs) {
			if (!dbConnections.containsKey(db.getName())) {
				continue;
			}
			
			Object connection = dbConnections.get(db.getName());
			
			switch (db.getDbType()) {
			 case MariaDb:
				case MysqlDb:
				 Connection sqlCon = (Connection)connection;
				 try {
					if (!sqlCon.isClosed()) {
						sqlCon.close();
					 }
				} catch (SQLException e) {
					e.printStackTrace();
				}
				 break;
			 case MongoDb:
				 MongoClient mongoClient = (MongoClient)connection;
				 mongoClient.close();
				 break;
			 default:
				 throw new Exception("Close db connections: Unhandled database type: " + db.getDbType());
			}			
		}
	}
	
	private Connection getMariaDBConnection(Service db) {
		Connection conn = null;
		
		System.out.println("Trying to connect to: " + db);
		
		try {
			String connectionString = String.format("jdbc:mariadb://%s:%d", db.getInternalHost(), db.getInternalPort());
			conn = DriverManager.getConnection(connectionString, db.getUsername(), db.getPassword());
			if (!conn.isValid(5000)) {
				conn = null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		
		return conn;
	}

	private Connection getMysqlDBConnection(Service db) {
		Connection conn = null;

		System.out.println("Trying to connect to: " + db);

		try {
			Class.forName("org.mariadb.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}

		try {
			String connectionString = String.format("jdbc:mysql://%s:%d", db.getInternalHost(), db.getInternalPort());
			conn = DriverManager.getConnection(connectionString, db.getUsername(), db.getPassword());
			if (!conn.isValid(5000)) {
				conn = null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		return conn;
	}
	
	private MongoClient getMongoDBConnection(Service db) {
		String connectionString = String.format("mongodb://%s:%s@%s:%d", db.getUsername(), db.getPassword(), db.getInternalHost(), db.getInternalPort());
		MongoClientURI uri = new MongoClientURI(connectionString);
		MongoClient mongoClient = new MongoClient(uri);
		try{
			MongoIterable<String> test=mongoClient.listDatabaseNames();
			if(test==null){
				return null;
			}
		}
		catch (Exception x){
			return null;
		}
		return mongoClient;
	}
}
