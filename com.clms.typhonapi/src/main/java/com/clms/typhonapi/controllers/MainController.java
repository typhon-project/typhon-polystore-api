package com.clms.typhonapi.controllers;

import com.clms.typhonapi.models.User;
import com.clms.typhonapi.storage.ModelStorage;
import com.clms.typhonapi.storage.UserStorage;
import com.clms.typhonapi.utils.DbUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class MainController {


    @Autowired
    private UserStorage userRepository;

    @RequestMapping(path = "/user/register", method = RequestMethod.POST)
    public void add(@RequestBody User u) {
        userRepository.save(u);
    }
    
    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public List<User> all() {
    	return userRepository.findAll();

    }

    @RequestMapping(path = "/user/{userName}", method = RequestMethod.GET)
    public ResponseEntity get(@PathVariable String userName) {
        Optional<User> user = userRepository.findById(userName);
        if(user.get()!=null){
            return ResponseEntity.status(200).body(user.get());
        }
        else{
            return ResponseEntity.status(404).body(null);
        }
    }

    @RequestMapping(path = "/user/{userName}", method = RequestMethod.POST)
    public ResponseEntity update(@PathVariable String userName, @RequestBody User u) {
    	User user = userRepository.findById(userName).get();
        if(user != null){
        	user.setUsername(u.getUsername());
        	user.setPassword(u.getPassword());
            userRepository.save(user);
            return ResponseEntity.status(200).body(u);
        }
        else{
            return ResponseEntity.status(404).body(null);
        }
    }

    @RequestMapping(path = "/user/{userName}", method = RequestMethod.DELETE)
    public void delete(@PathVariable String userName) {

    }


    @RequestMapping(path = "/api/model/dl", method = RequestMethod.POST)
    public void setTyphoneDLModel(@RequestBody Map<String, String> json) {
    	ModelStorage.addDlModel(json.get("name"), json.get("contents"));
    }
    
    @RequestMapping(path = "/api/model/ml", method = RequestMethod.POST)
    public void setTyphoneMlModel(@RequestBody Map<String, String> json) {
    	ModelStorage.addMlModel(json.get("name"), json.get("contents"));
    }
    
    @RequestMapping(path = "/api/model/dl", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody ResponseEntity<byte[]> getTyphonDLModel() {
    	HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-disposition", "attachment; filename=model.tdl");
     
        return ResponseEntity.ok()
          .headers(responseHeaders)
          .body(ModelStorage.getDlModel().getBytes());
    }
    
    @RequestMapping(path = "/api/model/ml", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody ResponseEntity<byte[]> getTyphonMLModel() {
    	HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-disposition", "attachment; filename=model.tml");
     
        return ResponseEntity.ok()
          .headers(responseHeaders)
          .body(ModelStorage.getMlModel().getBytes());
    }

    @RequestMapping(path = "/api/query", method = RequestMethod.POST)
    public ResponseEntity executeQuery(@RequestBody String query){
        //Run the query on the TQL compiler and pass the result to the body
        ResponseEntity response=ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
        return response;
    }

    @RequestMapping(path = "/api/evolve", method = RequestMethod.POST)
    public ResponseEntity Evolve(@RequestBody Map<String,String> json){
        //Run consume the evolution toolset
        ResponseEntity response=ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
        return response;
    }

    @RequestMapping(path = "/api/backup", method = RequestMethod.POST)
    public ResponseEntity Backup(@RequestBody Map<String,String> json){
        ResponseEntity response;
        if(!json.containsKey("db_name") || !json.containsKey("db_type")){
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            return response;
        }
        else{
            if(json.get("db_type").equals("mariadb")) {
                    String filename=DbUtils.MariaBackupProcess(json.get("host"), json.get("port"), json.get("username"), json.get("password"), json.get("db_name"), json.get("backup_name"));
                    response = ResponseEntity.status(HttpStatus.OK).body("Backup "+filename+" Executed successfully!");
                return response;
            }
            else{
                response = ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(json.get("db_type"));
                return response;
            }
        }

    }


    @RequestMapping(path = "/api/restore", method = RequestMethod.POST)
    public ResponseEntity Restore(@RequestBody Map<String,String> json){
        //Run consume the evolution toolset
        ResponseEntity response;
        if(!json.containsKey("db_name") || !json.containsKey("db_type") || !json.containsKey("backup_name")){
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            return response;
        }
        else{
            if(json.get("db_type").equals("mariadb")){
                String status=DbUtils.MariaRestore(json.get("host"),json.get("port"),json.get("username"),json.get("password"),json.get("db_name"),json.get("backup_name"));
                if(!status.equals("OK")){
                    response=ResponseEntity.status(HttpStatus.BAD_REQUEST).body(status);
                    return response;
                }
                else
                    return ResponseEntity.status(HttpStatus.OK).body(null);
            }
            else{
                response=ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
                return response;
            }
        }

    }

}