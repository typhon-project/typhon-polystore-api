package com.clms.typhonapi;

import com.clms.typhonapi.storage.ModelStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
	
    public static void main(String[] args) {
    	ModelStorage.PATH = "models";
    //	UserStorage.PATH = "users";
    	SpringApplication.run(Application.class, args);
    }    
    
}