package com.clms.typhonapi.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public class Service {

    private String name;
    private String status;
    private String host;
    private int port;
    private String username;
    private String password;
    private DatabaseType dbType;
    private ServiceType serviceType;
    
    public Service(String name, String status) {
        this.name = name;
        this.status = status;
    }
    
    public Service(ServiceType serviceType, String name, String host, int port) {
    	this.name = name;
        this.serviceType = serviceType;
        this.host = host;
        this.port = port;
    }

    public Service(ServiceType serviceType, String name, String status, String host, 
    		int port, String username, String password, DatabaseType dbType) {
    	this(serviceType, name, host, port);
        this.status = status;
        this.username = username;
        this.password = password;
        this.dbType = dbType;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }
    
    public DatabaseType getDbType() {
        return dbType;
    }

    public void setDbType(DatabaseType dbType) {
        this.dbType = dbType;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
