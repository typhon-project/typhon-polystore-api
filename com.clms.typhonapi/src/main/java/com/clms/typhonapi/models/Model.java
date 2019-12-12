package com.clms.typhonapi.models;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "models")
public class Model {

	public Model () {}
	
	@Id
	private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
	private int version;

    public boolean isInitializedDatabases() {
        return initializedDatabases;
    }

    public void setInitializedDatabases(boolean initializedDatabases) {
        this.initializedDatabases = initializedDatabases;
    }

    private boolean initializedDatabases;

    public boolean isInitializedConnections() {
        return initializedConnections;
    }

    public void setInitializedConnections(boolean initializedConnections) {
        this.initializedConnections = initializedConnections;
    }

    private boolean initializedConnections;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
    
    private String contents;

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
    
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    private Date dateReceived;

    public Date getDateReceived() {
        return dateReceived;
    }

    public void setDateReceived(Date dateReceived) {
        this.dateReceived = dateReceived;
    }



}
