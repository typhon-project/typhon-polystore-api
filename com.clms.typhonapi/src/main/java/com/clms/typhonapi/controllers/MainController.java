package com.clms.typhonapi.controllers;

import com.clms.typhonapi.models.Model;
import com.clms.typhonapi.models.User;
import com.clms.typhonapi.storage.UserStorage;
import com.clms.typhonapi.utils.DbUtils;
import com.clms.typhonapi.utils.EvolutionHelper;
import com.clms.typhonapi.utils.ModelHelper;
import com.clms.typhonapi.utils.QueryRunner;
import com.clms.typhonapi.utils.ServiceRegistry;
import com.clms.typhonapi.utils.UserHelper;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartRequest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@Api(value="Polystore Services")
public class MainController {
	private static boolean status = true;

	@Autowired
    private UserHelper userHelper;
    @Autowired
    private UserStorage userRepository;
    @Autowired
    private ServiceRegistry serviceRegistry;
    @Autowired
    private QueryRunner queryRunner;
    @Autowired
    private ModelHelper modelHelper;
    @Autowired
    private EvolutionHelper evolutionHelper;
    @Autowired
    private DbUtils dbUtils;

    public MainController() {

    }

    @PostConstruct
    public void init() {
    	userHelper.createInitialUser();
    	serviceRegistry.load(modelHelper.getDlModel());
    	try {
            queryRunner.init(modelHelper.getMlModel());
        }
    	catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @RequestMapping(path = "/api/users/authenticate", method = RequestMethod.POST)
    public boolean login(@RequestBody Map<String,String> json) {
    	User user = userHelper.get(json.get("username"), json.get("password"));
    	return user != null;
    }

    @RequestMapping(value = "/api/status",method = RequestMethod.GET)
    public boolean getStatus() {
    	return status;
    }

    @RequestMapping(value = "/api/down", method = RequestMethod.GET)
    public boolean down() {
    	try {
			Thread.sleep(4000);
			queryRunner.turnOff();
			try {
				dbUtils.bringDatabasesDown();
			} catch (Exception e) {
				e.printStackTrace();
			}
			status = false;
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}
    	return status;
    }

    @RequestMapping(value = "/api/up", method = RequestMethod.GET)
    public boolean up() {
    	queryRunner.init(modelHelper.getMlModel());
		status = true;
    	return status;
    }

    @ApiOperation(value = "Register new user")
    @RequestMapping(path = "/api/user/register", method = RequestMethod.POST)
    public void add(@RequestBody User u) {
        userRepository.save(u);
    }

    @ApiOperation(value= "List all users",response = List.class)
    @RequestMapping(path = "/api/users", method = RequestMethod.GET)
    public List<User> all() {
    	return userRepository.findAll();
    }

    @ApiOperation(value= "Get user by username")
    @RequestMapping(path = "/api/user/{userName}", method = RequestMethod.GET)
    public ResponseEntity get(@PathVariable String userName) {
        Optional<User> user = userRepository.findById(userName);
        if(user.get()!=null){
            return ResponseEntity.status(200).body(user.get());
        }
        else{
            return ResponseEntity.status(404).body(null);
        }
    }

    @ApiOperation(value="Update user by username")
    @RequestMapping(path = "/api/user/{userName}", method = RequestMethod.POST)
    public ResponseEntity update(@PathVariable String userName, @RequestBody User u) {
    	User user = userRepository.findById(userName).get();
        if(user != null){
        	user.setUsername(u.getUsername());
        	user.setPassword(u.getPassword());
            userRepository.save(user);
            return ResponseEntity.status(200).body(u);
        }
        else {
            return ResponseEntity.status(404).body(null);
        }
    }

    @RequestMapping(path = "/api/user/{userName}", method = RequestMethod.DELETE)
    public ResponseEntity delete(@PathVariable String userName) {
        User user = userRepository.findById(userName).get();
        if(user != null){
            userRepository.delete(user);
            return ResponseEntity.status(200).body(user);
        }
        else {
            return ResponseEntity.status(404).body(null);
        }
    }

    @RequestMapping(path = "/api/model/dl", method = RequestMethod.GET)
    public ArrayList<Model> getDlModels() {
    	return modelHelper.getDlModels();
    }

    @RequestMapping(path = "/api/model/ml", method = RequestMethod.GET)
    public ArrayList<Model> getMlModels() {
    	return modelHelper.getMlModels();
    }


    @ApiImplicitParams({
        @ApiImplicitParam(name = "name", value = "name of model", required = true, dataType = "String", paramType = "body"),
        @ApiImplicitParam(name = "contents", value = "contents of model", required = true, dataType = "String", paramType = "body")
    })
    @RequestMapping(path = "/api/model/dl", method = RequestMethod.POST)
    public void setTyphonDLModel(@RequestBody Map<String, String> json) throws Exception {
    	boolean flagReconnect=modelHelper.addDlModel(json.get("name"), json.get("contents"));
    	serviceRegistry.load(modelHelper.getDlModel());
    	if(flagReconnect){
    	    queryRunner.init(modelHelper.getMlModel());
        }
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "name", value = "name of model", required = true, dataType = "String", paramType = "body"),
        @ApiImplicitParam(name = "contents", value = "contents of model", required = true, dataType = "String", paramType = "body")
    })
    @RequestMapping(path = "/api/model/ml", method = RequestMethod.POST)
    public void setTyphonMlModel(@RequestBody Map<String, String> json) throws Exception {
    	modelHelper.addMlModel(json.get("name"), json.get("contents"));
    	evolutionHelper.evolve(modelHelper.getMlModel());
    }

    @RequestMapping(path = "/api/model/{type}/{version}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, method = RequestMethod.GET   )
    public @ResponseBody ResponseEntity<byte[]> getTyphonModel(@PathVariable String type, @PathVariable int version) {
    	HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-disposition", "attachment; filename=model.xmi");

        Model m = modelHelper.getModel(type, version);

        return ResponseEntity.ok()
          .headers(responseHeaders)
          .body((m == null ? "" : m.getContents()).getBytes());
    }

    @ApiOperation(value= "Get databases",httpMethod = "GET")
    @RequestMapping(path = "/api/databases", method = RequestMethod.GET)
    public ResponseEntity getDatabases() {
    	dbUtils.updateDbStatus();
        return ResponseEntity.status(200).body(serviceRegistry.getDatabases());
    }
    @ApiOperation(value= "Get databases")
    @RequestMapping(path = "/api/services", method = RequestMethod.GET)
    public ResponseEntity getServices() {
       // dbUtils.updateDbStatus();
        return ResponseEntity.status(200).body(serviceRegistry.getServices());
    }

    @RequestMapping(path = "/api/resetdatabases", method = RequestMethod.GET)
    public ResponseEntity resetDatabases() {
        dbUtils.updateDbStatus();
        return ResponseEntity.status(200).body(queryRunner.resetDatabases());
    }


    @RequestMapping(path = "/api/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    @Async
    public Future<ResponseEntity<String>> executeQuery(HttpEntity<String> httpEntity){
        try {
            return new AsyncResult<ResponseEntity<String>>(queryRunner.run("nemo", httpEntity,false));
        } catch (UnsupportedEncodingException | URISyntaxException e) {
            e.printStackTrace();
            return new AsyncResult<ResponseEntity<String>>(new ResponseEntity<String>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @RequestMapping(path = "/api/update", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    @Async
    public Future<ResponseEntity<String>> executeUpdate(HttpEntity<String> httpEntity){
        try {
            return new AsyncResult<ResponseEntity<String>>(queryRunner.run("nemo", httpEntity, true));
        } catch (UnsupportedEncodingException | URISyntaxException e) {
            return new AsyncResult<ResponseEntity<String>>(new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @RequestMapping(path = "/api/ddl", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    @Async
    public Future<ResponseEntity<String>> executeDLL(HttpEntity<String> httpEntity){
        try {
            return new AsyncResult<ResponseEntity<String>>(queryRunner.executeDLL(httpEntity.getBody()));
        } catch (URISyntaxException e) {
            return new AsyncResult<ResponseEntity<String>>(new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }
    /*
    @ApiImplicitParams({
            @ApiImplicitParam(name = "command", value = "query", required = true, dataType = "String", paramType = "parameter"),
            @ApiImplicitParam(name = "parameterNames", value = "names of parameters", required = true, dataType = "Array[String]", paramType = "parameter"),
            @ApiImplicitParam(name = "boundRows", value = "values of parameters", required = true, dataType = "Array[String]", paramType = "parameter")
    })
    @RequestMapping(path = "/api/preparedupdate", method = RequestMethod.POST)
    @Async
    public Future<ResponseEntity<String>> executepreparedUpdate(HttpEntity<String> httpEntity){
        return new AsyncResult<ResponseEntity<String>>(queryRunner.preparedUpdate("nemo",httpEntity));
    }
     */

    @RequestMapping(path = "/api/evolve", method = RequestMethod.POST)
    public ResponseEntity Evolve(@RequestBody String json){
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
                File filename  = dbUtils.mariaBackupProcess(json.get("host"), json.get("port"), json.get("username"), json.get("password"), json.get("db_name"), json.get("backup_name"));
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
                String status= dbUtils.mariaRestore(json.get("host"),json.get("port"),json.get("username"),json.get("password"),json.get("db_name"),json.get("backup_name"));
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

    //create a new instance of an entity of ML
    @RequestMapping(
        path = "/crud/{entity}",
        method = RequestMethod.POST
    )
    public ResponseEntity cEntity(@PathVariable String entity, @RequestBody Map<String, Object> jsonBody) {
        return queryRunner.postEntity(entity, jsonBody);
    }

    //read, update and delete an instance of an entity of ML
    @RequestMapping(
        path = "/crud/{entity}/{id}",
        method = RequestMethod.GET
    )
    public ResponseEntity rEntity(@PathVariable String entity, @PathVariable String id) {
        return queryRunner.getEntity(entity, id);
    }

    @RequestMapping(
        path = "/crud/{entity}/{id}",
        method = RequestMethod.PATCH
    )
    public ResponseEntity uEntity(@PathVariable String entity, @PathVariable String id, @RequestBody Map<String, Object> jsonBody) {
        return queryRunner.patchEntity(entity, id, jsonBody);
    }

    @RequestMapping(
        path = "/crud/{entity}/{id}",
        method = RequestMethod.DELETE
    )
    public ResponseEntity dEntity(@PathVariable String entity, @PathVariable String id) {
        return queryRunner.deleteEntity(entity, id);
    }

    @RequestMapping(path = "/api/noAnalytics/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    @Async
    public Future<ResponseEntity<String>> executeQueryWOAnalytics(HttpEntity<String> httpEntity){
        try {
            return new AsyncResult<ResponseEntity<String>>(queryRunner.executeQuery(httpEntity.getBody()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new AsyncResult<ResponseEntity<String>>(new ResponseEntity<String>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @RequestMapping(path = "/api/noAnalytics/update", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    @Async
    public Future<ResponseEntity<String>> executeUpdateWOAnalytics(HttpEntity<String> httpEntity){
        try {
            return new AsyncResult<ResponseEntity<String>>(queryRunner.executeUpdate(httpEntity.getBody()));
        } catch (URISyntaxException e) {
            return new AsyncResult<ResponseEntity<String>>(new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

}