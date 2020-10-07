package com.clms.typhonapi.config;

import com.mongodb.MongoClientOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.messaging.MessageListenerContainer;
import org.springframework.data.mongodb.core.messaging.DefaultMessageListenerContainer;

import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories(basePackages = "com.clms.typhonapi.storage")
@Configuration
public class MongoDbSettings {

    @Bean
    public MongoClientOptions mongoOptions() {
        MongoClientOptions options = MongoClientOptions
                                        .builder()
                                        .connectTimeout(20000)
                                        .socketTimeout(20000)
                                        .build();
        return  options;
    }

    @Bean
	public MessageListenerContainer messageListenerContainer(MongoTemplate template) {
		return new DefaultMessageListenerContainer(template);
	}
}