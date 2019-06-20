package com.clms.typhonapi.utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.clms.typhonapi.kafka.QueueConsumer;
import com.clms.typhonapi.kafka.ConsumerHandler;
import com.clms.typhonapi.kafka.QueueProducer;
import com.clms.typhonapi.models.Model;
import com.clms.typhonapi.models.Service;
import com.clms.typhonapi.models.ServiceType;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import ac.uk.york.typhon.analytics.commons.datatypes.events.Event;
import ac.uk.york.typhon.analytics.commons.datatypes.events.PreEvent;

@Component
public class QueryRunner implements ConsumerHandler {

	private QueueProducer preProducer;
	private static String PRE_TOPIC = "PRE";
	private static String AUTH_TOPIC = "AUTH";
	private Map<Integer, PreEvent> receivedQueries = new HashMap<Integer, PreEvent>();
	private String kafkaConnection = "";
	private Map<String, Object> dbConnections = new HashMap<String, Object>();
	private boolean isReady;
	
	@Autowired
	private ServiceRegistry serviceRegistry;
		
	public void init(Model mlModel) {
		isReady = false;
		if (mlModel == null) {
			return;
		}
		
		if (kafkaConnection == null) {
			Service analyticsQueue = serviceRegistry.getService(ServiceType.Queue);
			if (analyticsQueue != null) {
				receivedQueries.clear();
				kafkaConnection = analyticsQueue.getHost() + ":" + analyticsQueue.getPort();
				preProducer = new QueueProducer(kafkaConnection);
				subscribeToAuthorization();
			} else {
				System.out.println("[~~~~~~~WARNING~~~~~~~] No analytics service found in dl...");
			}
		}
		
		try {
			updateDbConnections();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//TODO: initialize query engine with xmi and dbConnections
		
		isReady = true;
	}
	
	public boolean isReady() {
		return isReady;
	}
	
	public void initDatabases() {
		
	}
	
	public String run(String user, String query) {
		if (kafkaConnection == null) {
			callQueryEngine(query);
			return "Run without kafka";
		}
		
		//Create pre event
		PreEvent event = new PreEvent();
		event.setId(UUID.randomUUID().toString());
		event.setQuery(query);
		event.setUser(user);
		
		//Post pre event to PRE topic
		this.preProducer.produce(PRE_TOPIC, event);
		
		//wait for response on AUTH topic
		long startedOn = System.currentTimeMillis();
		int timeout = 10 * 1000;
		boolean timedOut = false;
		int eventHash = event.getId().hashCode();
		
	    while (true) {
	    	if (receivedQueries.containsKey(eventHash)) {
	    		event = receivedQueries.get(eventHash);
	    		receivedQueries.remove(eventHash);
	    		break;
	    	}
	    	
	        if (System.currentTimeMillis() - startedOn > timeout) {
	        	timedOut = true;
	            break;
	        }
	        
	        try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    }
	    
	    if (timedOut) {
	    	return "Query timeout";
	    } else {
	    	if (event.isAuthenticated() == false) {
	    		return "Not authorized";
	    	}
	    	
	    	startedOn = System.currentTimeMillis();
	    	callQueryEngine(query);
	    	long executionTime = System.currentTimeMillis() - startedOn;
	    	//TODO: run query and publish to POST topic
	    	return "Query response: " + event.getId();
	    }
	}
	
	private void callQueryEngine(String query) {
		
	}
	
	@Override
	public void onNewMesaage(Event event) {
		receivedQueries.put(event.getId().hashCode(), (PreEvent)event);
	}
	
	private void subscribeToAuthorization() {
		Thread subscribeTask = new Thread(new QueueConsumer(kafkaConnection, AUTH_TOPIC, this));
		subscribeTask.start();
	}
	
	private void updateDbConnections() throws Exception {
		closeDbConnections();
		dbConnections.clear();
		
		ArrayList<Service> dbs = serviceRegistry.getDatabases();
		for (Service db : dbs) {
			switch (db.getDbType()) {
			 case MariaDb:
				 dbConnections.put(db.getName(), connectToMariaDBConnection(db));
				 break;
			 case MongoDb:
				 dbConnections.put(db.getName(), connectToMongoDBConnection(db));
				 break;
			 default:
				 throw new Exception("Unhandled database type: " + db.getDbType());
			}
		}
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
	
	private Connection connectToMariaDBConnection(Service db) {
		Connection conn = null;
		
		try {
			Class.forName("org.mariadb.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		
		try {
			String connectionString = String.format("jdbc:mariadb://%s:%d", db.getHost(), db.getPort());
			conn = DriverManager.getConnection(connectionString, db.getUsername(), db.getPassword());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return conn;
	}
	
	private MongoClient connectToMongoDBConnection(Service db) {
		String connectionString = String.format("mongodb://%s:%s@%s:%d", db.getUsername(), db.getPassword(), db.getHost(), db.getPort());
		MongoClientURI uri = new MongoClientURI(connectionString);
		MongoClient mongoClient = new MongoClient(uri);
				
		return mongoClient;
	}
}