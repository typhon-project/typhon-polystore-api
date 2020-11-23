package com.clms.typhonapi.config;

import com.mongodb.MongoClientOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories(basePackages = "com.clms.typhonapi.storage")
@Configuration
public class MongoDbSettings {

    @Bean
    public MongoClientOptions mongoOptions() {
        return MongoClientOptions.builder().connectTimeout(20000).socketTimeout(20000).build();
    }

}