package com.clms.typhonapi.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clms.typhonapi.models.*;

@RestController
public class UserController {

    @RequestMapping("/user/add")
    public User get(@RequestParam(value="name", defaultValue="test") String name) {
        User u = new User();
        u.setUsername(name);
        return u;
    }
}