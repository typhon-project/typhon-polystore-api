package com.clms.typhonapi.models;

import org.springframework.data.annotation.Id;

public class User {

    @Id
    private String username;
    private String first_name;

    public User () {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirst_name() {return first_name;}

    public void setFirst_name(String first_name) {this.first_name = first_name;}
}
