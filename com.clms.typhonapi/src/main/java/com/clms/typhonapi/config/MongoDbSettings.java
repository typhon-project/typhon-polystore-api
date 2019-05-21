package com.clms.typhonapi.config;

import com.mongodb.MongoClientOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoDbSettings {

    @Bean
    public MongoClientOptions mongoOptions() {
        return MongoClientOptions.builder().socketTimeout(20000).build();
    }

}