package com.clms.typhonapi.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public class Service {

    public Service(String name, String status) {
        this.name = name;
        this.status = status;
    }
    
    public Service(ServiceType serviceType, String name, String host, String port) {
    	this.name = name;
        this.serviceType = serviceType;
        this.host = host;
        this.port = port;
    }

    public Service(ServiceType serviceType, String name, String status, String host, String port, String username, String password, String type,String db_name) {
    	this(serviceType, name, host, port);
        this.status = status;
        this.username = username;
        this.password = password;
        this.type = type;
        this.db_name = db_name;
    }

    private ServiceType serviceType;
    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }
    
    private String name;
    private String status;
    private String host;

    public String getDb_name() {
        return db_name;
    }

    public void setDb_name(String db_name) {
        this.db_name = db_name;
    }

    private String db_name;
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
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

    private String port;
    private String username;
    private String password;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String type;

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
