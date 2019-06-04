package com.clms.typhonapi.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public class Database {

    public Database(String name, String status) {
        this.name = name;
        this.status = status;
    }

    private String name;
    private String status;

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
