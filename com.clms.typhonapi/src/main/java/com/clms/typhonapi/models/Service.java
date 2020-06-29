package com.clms.typhonapi.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.catalina.Engine;


@JsonSerialize
public class Service {

    private String name;
    private ServiceStatus status;
    private String internalHost;
    private String externalHost;
    private int internalPort;
    private int externalPort;
    private String username;
    private String password;
    private DatabaseType dbType;
    private ServiceType serviceType;
    private EngineType engineType;
    private Boolean isExternal;

    public Boolean getExternal() {
        return isExternal;
    }

    public void setExternal(Boolean external) {
        isExternal = external;
    }



    public String getInternalHost() {
        return internalHost;
    }

    public void setInternalHost(String internalHost) {
        this.internalHost = internalHost;
    }

    public String getExternalHost() {
        return externalHost;
    }

    public void setExternalHost(String externalHost) {
        this.externalHost = externalHost;
    }

    public int getInternalPort() {
        return internalPort;
    }

    public void setInternalPort(int internalPort) {
        this.internalPort = internalPort;
    }

    public int getExternalPort() {
        return externalPort;
    }

    public void setExternalPort(int externalPort) {
        this.externalPort = externalPort;
    }


    
    public Service() {
    	
    }
    
    public Service(String name, ServiceStatus status) {
        this.name = name;
        this.status = status;
    }
    
    public Service(ServiceType serviceType, String name, String internalHost, int internalPort,String externalHost,int externalPort) {
    	this.name = name;
        this.serviceType = serviceType;
        this.internalHost = internalHost;
        this.externalHost = externalHost;
        this.internalPort = internalPort;
        this.externalPort = externalPort;
    }

    public Service(ServiceType serviceType, String name, ServiceStatus status, String internalHost,String externalHost,int internalPort,
                   int externalPort, String username, String password, DatabaseType dbType, EngineType engineType) {
    	this(serviceType, name, internalHost, internalPort,externalHost,externalPort);
        this.status = status;
        this.username = username;
        this.password = password;
        this.dbType = dbType;
        this.engineType = engineType;
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

    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }


    public EngineType getEngineType() {
        return engineType;
    }

    public void setEngineType(EngineType engineType) {
        this.engineType = engineType;
    }
	@Override
    public String toString() {
		return String.format("Host: %s, port: %d, user: %s, pass: %s", internalHost, internalPort, username, password);
    }
}
