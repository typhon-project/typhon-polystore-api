package com.clms.typhonapi.utils;

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
	
	@Autowired
	private ServiceRegistry serviceRegistry;
		
	public void init(Model mlModel) {
		Service analyticsQueue = serviceRegistry.getService(ServiceType.Queue);
		if (analyticsQueue == null) {
			System.out.println("[~~~~~~~WARNING~~~~~~~] No analytics service found in dl...");
			return;
		}
		
		receivedQueries.clear();
		kafkaConnection = analyticsQueue.getHost() + ":" + analyticsQueue.getPort();
		preProducer = new QueueProducer(kafkaConnection);
		//TODO: initialize query engine with xmi and dbConnections
		updateDbConnections();
		
		//TODO: kill previous consumption task (if any)
		subscribeToAuthorization();
	}
	
	public void initDatabases() {
		
	}
	
	public String run(String user, String query) {
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
	    	//TODO: run query and publish to POST topic
	    	return "Query response: " + event.getId();
	    }
	}
	
	@Override
	public void onNewMesaage(Event event) {
		receivedQueries.put(event.getId().hashCode(), (PreEvent)event);
	}
	
	private void subscribeToAuthorization() {
		Thread subscribeTask = new Thread(new QueueConsumer(kafkaConnection, AUTH_TOPIC, this));
		subscribeTask.start();
	}
	
	private void updateDbConnections() {
		//TODO: close open connections
	}
}