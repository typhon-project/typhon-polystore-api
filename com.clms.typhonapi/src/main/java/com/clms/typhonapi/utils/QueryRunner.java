package com.clms.typhonapi.utils;

import java.io.IOException;
import java.util.*;

import com.clms.typhonapi.models.DatabaseType;

import nl.cwi.swat.typhonql.MariaDB;
import nl.cwi.swat.typhonql.MongoDB;
import nl.cwi.swat.typhonql.MySQL;
import nl.cwi.swat.typhonql.client.DatabaseInfo;
import nl.cwi.swat.typhonql.workingset.WorkingSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.clms.typhonapi.kafka.QueueConsumer;
import com.clms.typhonapi.kafka.ConsumerHandler;
import com.clms.typhonapi.kafka.QueueProducer;
import com.clms.typhonapi.models.Model;
import com.clms.typhonapi.models.Service;
import com.clms.typhonapi.models.ServiceType;

import ac.york.typhon.analytics.commons.datatypes.events.Event;
import ac.york.typhon.analytics.commons.datatypes.events.PreEvent;
import nl.cwi.swat.typhonql.client.XMIPolystoreConnection;
import nl.cwi.swat.typhonql.DBType;


@Component
public class QueryRunner implements ConsumerHandler {

	private QueueProducer preProducer;
	private static String PRE_TOPIC = "PRE";
	private static String AUTH_TOPIC = "AUTH";
	private Map<Integer, PreEvent> receivedQueries = new HashMap<Integer, PreEvent>();
	private String kafkaConnection = "";
	private boolean isReady;
	private XMIPolystoreConnection connection;
	
	@Autowired
	private ServiceRegistry serviceRegistry;
	@Autowired
	private DbUtils dbHelper;
	
	public void init(Model mlModel) {
		isReady = false;
		
		try {
			dbHelper.updateDbConnections();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (mlModel == null) {
			return;
		}
		
		if (!isAnalyticsAvailiable()) {
			Service analyticsService = serviceRegistry.getService(ServiceType.Analytics);
			Service analyticsQueue = serviceRegistry.getService(ServiceType.Queue);
			if (analyticsService != null && analyticsQueue != null) {
				receivedQueries.clear();
				kafkaConnection = analyticsQueue.getHost() + ":" + analyticsQueue.getPort();
				preProducer = new QueueProducer(kafkaConnection);
				subscribeToAuthorization();
			} else {
				System.out.println("[~~~~~~~WARNING~~~~~~~] No analytics service found in dl...");
			}
		}
		List<DatabaseInfo> infos = new ArrayList<DatabaseInfo>();
		for (Service service: serviceRegistry.getDatabases()){
			DBType swattype;
			DatabaseType type = service.getDbType();
			String dbms;
			if(type==DatabaseType.MongoDb){
				swattype=DBType.documentdb;
				dbms = new MongoDB().getName();
			}
			else if(type==DatabaseType.MysqlDb){
				swattype=DBType.relationaldb;
				dbms = new MySQL().getName();
			}
			else {
				swattype = DBType.relationaldb;
				dbms = new MariaDB().getName();
			}
			infos.add(new DatabaseInfo(service.getHost(),service.getPort(),service.getName(),swattype,dbms,service.getUsername(),service.getPassword()));
		}
		//TODO: initialize query engine with xmi and dbConnections
		try {
			connection = new XMIPolystoreConnection(mlModel.getContents(), infos);
		} catch (IOException e) {
			e.printStackTrace();
		}

		isReady = true;
	}
	
	public void turnOff() {
		isReady = false;
	}
	
	public boolean isReady() {
		return isReady;
	}
	
	public boolean isAnalyticsAvailiable() {
		return preProducer != null;
	}
	
	public void initDatabases() {
		
	}
	
	public String run(String user, String query) {
		if (!isReady()) {
			return "Query engine is not initialized";
		}
		
		if (!isAnalyticsAvailiable()) {
			callQueryEngine(query);
			return "Run without analytics";
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
	    	WorkingSet set = callQueryEngine(query);
	    	long executionTime = System.currentTimeMillis() - startedOn;
	    	//TODO: run query and publish to POST topic
	    	return "Query response: " + event.getId();
	    }
	}
	
	private WorkingSet callQueryEngine(String query) {
		return connection.executeQuery(query);
	}
	
	@Override
	public void onNewMesaage(Event event) {
		receivedQueries.put(event.getId().hashCode(), (PreEvent)event);
	}
	
	private void subscribeToAuthorization() {
		Thread subscribeTask = new Thread(new QueueConsumer(kafkaConnection, AUTH_TOPIC, this));
		subscribeTask.start();
	}
	
}