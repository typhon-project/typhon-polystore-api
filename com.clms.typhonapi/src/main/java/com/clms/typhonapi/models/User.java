package com.clms.typhonapi.models;

import org.springframework.data.annotation.Id;

public class User {

    @Id
    private String username;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String password;

    public User () {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
