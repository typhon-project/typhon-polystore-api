package com.clms.typhonapi.utils;

import java.util.HashMap;
import java.util.Map;

import com.clms.typhonapi.kafka.QueueConsumer;
import com.clms.typhonapi.kafka.ConsumerHandler;
import com.clms.typhonapi.kafka.QueueProducer;

import ac.uk.york.typhon.analytics.commons.datatypes.events.PreEvent;

public class QueryRunner implements ConsumerHandler {

	private QueueProducer preProducer;
	private static String PRE_TOPIC = "PRE";
	private static String AUTH_TOPIC = "AUTH";
	private Map<Integer, String> receivedQueries = new HashMap<Integer, String>();
	private String kafkaConnection = "192.168.2.28:9092";
	
	public QueryRunner() {
		preProducer = new QueueProducer(kafkaConnection);
		//TODO: initialize query engine
		subscribeToAuthorization();
	}
	
	public String run(String user, String query) {
		//Create pre event
		PreEvent event = new PreEvent();
		event.setQuery(query);
		event.setUser(user);
		
		//Post pre event to PRE queue
		this.preProducer.produce(PRE_TOPIC, query);
		
		long startedOn = System.currentTimeMillis();
		int timeout = 10 * 1000;
		
		boolean success = false;
		
	    while (true) {
	    	if (receivedQueries.containsKey(query.hashCode())) {
	    		success = true;
	    		receivedQueries.remove(query.hashCode());
	    		break;
	    	}
	    	
	        if (System.currentTimeMillis() - startedOn > timeout) {
	            break;
	        }
	        
	        try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    }
	    
	    if (success) {
	    	//run query...
	    	return "Query response !!";
	    }
	    
		return null;
	}
	
	@Override
	public void onNewMesaage(String message) {
		receivedQueries.put(message.hashCode(), message);
	}
	
	private void subscribeToAuthorization() {
		Thread subscribeTask = new Thread(new QueueConsumer(kafkaConnection, AUTH_TOPIC, this));
		subscribeTask.start();
	}
}