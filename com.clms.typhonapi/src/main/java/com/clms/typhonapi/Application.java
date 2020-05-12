package com.clms.typhonapi;

import org.apache.log4j.BasicConfigurator;
import org.apache.logging.log4j.spi.LoggerContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Application {
	
    public static void main(String[] args) {
        BasicConfigurator.configure();
    	SpringApplication.run(Application.class, args);

    }    
    
}