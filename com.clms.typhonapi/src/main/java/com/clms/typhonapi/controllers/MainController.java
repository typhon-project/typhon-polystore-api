package com.clms.typhonapi.controllers;

import com.clms.typhonapi.models.Database;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
//@Api(value="Polystore Services")
public class MainController {


	private static boolean status = true;
	
    @Autowired
    private UserStorage userRepository;

    @RequestMapping("/api/status")
    public boolean getStatus() {
    	return status;
    }
    
    @RequestMapping("/api/down")
    public boolean down() {
    	try {
			Thread.sleep(4000);
			status = false;
		} catch (InterruptedException e) {
			
		}
    	return status;
    }
    
    @RequestMapping("/api/up")
    public boolean up() {
    	try {
			Thread.sleep(4000);
			status = true;
		} catch (InterruptedException e) {
			
		}
    	
    	
    	return status;
    }
    
    //@ApiOperation(value = "Register new user")
    @RequestMapping(path = "/user/register", method = RequestMethod.POST)
    public void add(@RequestBody User u) {
        userRepository.save(u);
    }

    //@ApiOperation(value= "List all users",response = List.class)
    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public List<User> all() {
    	return userRepository.findAll();

    }

    //@ApiOperation(value= "Get user by username")
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
    //@ApiOperation(value="Update user by username")
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

    //@ApiOperation(value= "Get databases")
    @RequestMapping(path = "/api/databases", method = RequestMethod.GET)
    public ResponseEntity getDatabases() {
        ArrayList<Database> dbs = new ArrayList<>();
        Database db1 = new Database("mariadbtest","ACTIVE","test.mariadb","3306","root","admin","mariadb","testdb");
        Database db2 = new Database("mongostore","ACTIVE","mongodb","27017","admin","admin","mongodb","admin");
        dbs.add(db1);
        dbs.add(db2);
        return ResponseEntity.status(200).body(dbs);
    }
    
    @RequestMapping(path = "/api/model/dl", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody ResponseEntity<byte[]> getTyphonDLModel() {
    	HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-disposition", "attachment; filename=model.tml");
     
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
        if(!json.containsKey("db_name") || !json.containsKey("type")){
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            return response;
        }
        else{
            if(json.get("type").equals("mariadb")) {
                File filename  = DbUtils.MariaBackupProcess(json.get("host"), json.get("port"), json.get("username"), json.get("password"), json.get("db_name"), json.get("backup_name"));
                if(filename!=null) {
                    Map<String,String> resp = new HashMap<String, String>();
                    resp.put("filename",filename.getName());
                    response = ResponseEntity.status(HttpStatus.OK).body(resp);
                	return response;
                }
                else{
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                }
            }
            else{
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
            }
        }
    }
    
    @RequestMapping(path = "/api/download/{fileName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> Backup(@PathVariable String fileName) throws IOException{
        ResponseEntity response;
        HttpHeaders responseHeaders = new HttpHeaders();

        File f = new File("backups/" + fileName);
        
        responseHeaders.set("Content-disposition", "attachment; filename="+ f.getName());
        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(Files.readAllBytes(Paths.get(f.getPath())));
    }


    @RequestMapping(path = "/api/restore", method = RequestMethod.POST)
    public ResponseEntity Restore(@RequestBody Map<String,String> json){
        //Run consume the evolution toolset
        ResponseEntity response;
        if(!json.containsKey("db_name") || !json.containsKey("type") || !json.containsKey("backup_name")){
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            return response;
        }
        else{
            if(json.get("type").equals("mariadb")){
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