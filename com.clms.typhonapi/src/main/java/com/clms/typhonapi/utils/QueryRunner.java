package com.clms.typhonapi.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import com.clms.typhonapi.kafka.ConsumerCreator;
import com.clms.typhonapi.kafka.IKafkaConstants;
import com.clms.typhonapi.kafka.ProducerCreator;

import ac.uk.york.typhon.analytics.commons.datatypes.events.PreEvent;

public class QueryRunner {

	private ProducerCreator preProducer;
	private static String PRE_TOPIC = "PRE";
	private Map<Integer, String> receivedQueries = new HashMap<Integer, String>();
	
	public QueryRunner() {
		preProducer = new ProducerCreator();
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
	    if (success) {
	    	//run query...
	    	return "Query response !!";
	    }
	    
		return null;
	}
	
	void queryReceived(String message) {
		receivedQueries.put(message.hashCode(), message);
	}
	
	private void subscribeToAuthorization() {
		Thread subscribeTask = new Thread(new Notifier(this));
		subscribeTask.start();
	}
}

class Notifier implements Runnable {

	private QueryRunner queryRunner;
	
    public Notifier(QueryRunner queryRunner) {
    	this.queryRunner = queryRunner;
    }

    @Override
    public void run() {
		Consumer<Long, String> consumer = ConsumerCreator.createConsumer();
        int noMessageFound = 0;
        while (true) {
          ConsumerRecords<Long, String> consumerRecords = consumer.poll(1000);
          // 1000 is the time in milliseconds consumer will wait if no record is found at broker.
          if (consumerRecords.count() == 0) {
              noMessageFound++;
              if (noMessageFound > IKafkaConstants.MAX_NO_MESSAGE_FOUND_COUNT)
                // If no message found count is reached to threshold exit loop.  
                break;
              else
                  continue;
          }
          //print each record. 
          consumerRecords.forEach(record -> {
              System.out.println("Got result from AUTH!!! " + record.value());
              this.queryRunner.queryReceived(record.value());
           });
          // commits the offset of record to broker. 
           consumer.commitAsync();
        }
        consumer.close();
    }

}