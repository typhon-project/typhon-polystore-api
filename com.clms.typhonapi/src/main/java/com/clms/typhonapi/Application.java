package com.clms.typhonapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.clms.typhonapi.storage.ModelStorage;
import com.clms.typhonapi.storage.UserStorage;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
    	ModelStorage.PATH = "models";
    	UserStorage.PATH = "users";
    	SpringApplication.run(Application.class, args);
    }
}