package com.clms.typhonapi.kafka;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;

import ac.york.typhon.analytics.commons.datatypes.events.Event;
import ac.york.typhon.analytics.commons.datatypes.events.PreEvent;
import ac.york.typhon.analytics.commons.serialization.EventSchema;

public class QueueConsumer implements Runnable {
	
	private ConsumerHandler handler;
	private Consumer<Long, Event> consumer;
	
    public QueueConsumer(String connectionString, String topic, ConsumerHandler handler) {
    	this.handler = handler;
    	
    	Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, connectionString);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, IKafkaConstants.GROUP_ID_CONFIG);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        
        consumer = new KafkaConsumer<>(props, new LongDeserializer(), new EventSchema(PreEvent.class));
        consumer.subscribe(Collections.singletonList(topic));
    }

    @Override
    public void run() {
        while (true) {
          ConsumerRecords<Long, Event> consumerRecords = consumer.poll(Duration.ofMillis(1000));

          consumerRecords.forEach(record -> {
              System.out.println("Got result from AUTH!!! " + record.value().getId());
              handler.onNewMesaage(record.value());
           });

           consumer.commitAsync();
        }
        //consumer.close();
    }

}