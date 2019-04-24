package com.clms.typhonapi.controllers;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clms.typhonapi.models.*;
import com.clms.typhonapi.storage.ModelStorage;

@RestController
public class UserController {

    @RequestMapping(path = "/user/register", method = RequestMethod.POST)
    public User get(@RequestBody User u) {
        return u;
    }
    
    @RequestMapping(path = "/api/model/dl", method = RequestMethod.POST)
    public void setTyphoneDLModel(@RequestBody Map<String, String> json) {
    	ModelStorage.addDlModel(json.get("name"), json.get("contents"));
    }
    
    @RequestMapping(path = "/api/model/ml", method = RequestMethod.POST)
    public void setTyphoneMlModel(@RequestBody Map<String, String> json) {
    	ModelStorage.addMlModel(json.get("name"), json.get("contents"));
    }
    
    @RequestMapping("/api/model/dl")
    public String getTyphonDLModel() {
    	return ModelStorage.getDlModel();
    }
    
    @RequestMapping("/api/model/ml")
    public String getTyphonMLModel() {
    	return ModelStorage.getMlModel();
    }
}