package com.clms.typhonapi.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.clms.typhonapi.kafka.QueueConsumer;
import com.clms.typhonapi.kafka.ConsumerHandler;
import com.clms.typhonapi.kafka.QueueProducer;
import com.clms.typhonapi.models.Service;
import com.clms.typhonapi.models.ServiceType;

import ac.uk.york.typhon.analytics.commons.datatypes.events.Event;
import ac.uk.york.typhon.analytics.commons.datatypes.events.PreEvent;

public class QueryRunner implements ConsumerHandler {

	private QueueProducer preProducer;
	private static String PRE_TOPIC = "PRE";
	private static String AUTH_TOPIC = "AUTH";
	private Map<Integer, PreEvent> receivedQueries = new HashMap<Integer, PreEvent>();
	private String kafkaConnection = "";
	
	public QueryRunner(ServiceRegistry serviceRegistry) {
		init(serviceRegistry);
	}
	
	public void init(ServiceRegistry serviceRegistry) {
		Service analyticsService = serviceRegistry.getService(ServiceType.Analytics);
		if (analyticsService == null) {
			System.out.println("[~~~~~~~WARNING~~~~~~~] No analytics service found in dl...");
			return;
		}
		
		receivedQueries.clear();
		kafkaConnection = analyticsService.getHost() + ":" + analyticsService.getPort();
		preProducer = new QueueProducer(kafkaConnection);
		//TODO: initialize query engine
		
		//TODO: kill previous consumption task (if any)
		subscribeToAuthorization();
	}
	
	public String run(String user, String query) {
		//Create pre event
		PreEvent event = new PreEvent();
		event.setId(UUID.randomUUID().toString());
		event.setQuery(query);
		event.setUser(user);
		
		//Post pre event to PRE queue
		this.preProducer.produce(PRE_TOPIC, event);
		
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
	    	return "Quert timeout";
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
}