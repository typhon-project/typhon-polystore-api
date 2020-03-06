package com.clms.typhonapi.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import ac.york.typhon.analytics.commons.datatypes.events.PostEvent;
import com.clms.typhonapi.models.*;

import com.clms.typhonapi.storage.ModelStorage;

import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.util.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.stereotype.Component;


import com.clms.typhonapi.kafka.QueueConsumer;
import com.clms.typhonapi.kafka.ConsumerHandler;
import com.clms.typhonapi.kafka.QueueProducer;

import ac.york.typhon.analytics.commons.datatypes.events.Event;
import ac.york.typhon.analytics.commons.datatypes.events.PreEvent;
import org.springframework.web.client.RestTemplate;
import scala.util.parsing.json.JSONObject;


@Component
public class QueryRunner implements ConsumerHandler {

	private QueueProducer preProducer;
	private QueueProducer postProducer;
	private static String POST_TOPIC = "POST";
	private static String PRE_TOPIC = "PRE";
	private static String AUTH_TOPIC = "AUTH";
	private Map<Integer, PreEvent> receivedQueries = new HashMap<Integer, PreEvent>();
	private String kafkaConnection = "";
	private boolean isReady;
	
	@Autowired
	private ServiceRegistry serviceRegistry;
	@Autowired
	private DbUtils dbHelper;
	@Autowired
	private ModelStorage repo;
	
	public void init(Model mlModel) {
		isReady = false;
		
		try {
			dbHelper.updateDbConnections();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (mlModel == null) {
			return;
		}
		
		if (!isAnalyticsAvailiable()) {
			Service analyticsService = serviceRegistry.getService(ServiceType.Analytics);
			Service analyticsQueue = serviceRegistry.getService(ServiceType.Queue);
			if (analyticsService != null && analyticsQueue != null) {
				receivedQueries.clear();
				kafkaConnection = analyticsQueue.getInternalHost() + ":" + analyticsQueue.getInternalPort();
				preProducer = new QueueProducer(kafkaConnection);
				postProducer = new QueueProducer(kafkaConnection);
				subscribeToAuthorization();
			} else {
				System.out.println("[~~~~~~~WARNING~~~~~~~] No analytics service found in dl...");
			}
		}
		List<DatabaseInfo> infos = new ArrayList<DatabaseInfo>();
		for (Service service: serviceRegistry.getDatabases()){
			DatabaseType type = service.getDbType();
			String dbms;
			String swattype;
			if(type==DatabaseType.MongoDb){
				swattype="documentdb";
				dbms = "MongoDb";
			}
			else if(type==DatabaseType.MysqlDb){
				swattype="relationaldb";
				dbms = "MySQL";
			}
			else {
				swattype = "relationaldb";
				dbms = "MariaDB";
			}
			infos.add(new DatabaseInfo(service.getInternalHost(),service.getInternalPort(),service.getName(),swattype,dbms,service.getUsername(),service.getPassword()));
		}
		//TODO: initialize query engine with xmi and dbConnections
		try {
			String uri = "http://typhonql-server:8080/initialize";
			Map<String, Object> vars = new HashMap<String, Object>();
			vars.put("xmi", mlModel.getContents());
			vars.put("databaseInfo",infos);


			RestTemplate restTemplate = new RestTemplate();
			Gson gson = new GsonBuilder().disableHtmlEscaping().create();
			System.out.println(gson.toJson(vars));
			ResponseEntity<String> result = restTemplate.postForEntity(uri, gson.toJson(vars), String.class);
			//connection = new XMIPolystoreConnection(mlModel.getContents(), infos);
			System.out.println(result.getBody());
			if (result.getStatusCode() == HttpStatus.OK) {
				isReady = true;
				System.out.println("completed initialization");
			} else {
				System.out.println("Could not establish connections to the polystore databases, reupload DL model and try again!");
				isReady=false;

			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Could not establish connections to the polystore databases, reupload DL model and try again!");
			isReady=false;
		}

	}
	
	public void turnOff() {
		isReady = false;
	}
	
	public boolean isReady() {
		return isReady;
	}
	
	public boolean isAnalyticsAvailiable() {
		return preProducer != null;
	}

	public boolean resetDatabases(){
		try {
			String uri = "http://typhonql-server:8080/reset";

			RestTemplate restTemplate = new RestTemplate();


			ResponseEntity<String> result = restTemplate.postForEntity(uri,null,String.class);
			if(result.getStatusCode()== HttpStatus.OK) {
				System.out.println(result);
				//PageRequest request = new PageRequest(0, 1, new Sort(Sort.Direction.DESC, "version"));
				Model mlModel = repo.findTopModelByTypeOrderByVersionDesc("ML");
				mlModel.setInitializedDatabases(true);
				repo.save(mlModel);
				return true;
			}
			else{
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void initDatabases() {
		
	}
	
	public String run(String user, String query,boolean isUpdate) {
		if (!isReady()) {
			return "Query engine is not initialized";
		}

		PreEvent event = new PreEvent();;
		if (isAnalyticsAvailiable()) {

			event.setId(UUID.randomUUID().toString());
			event.setQuery(query);
			event.setUser(user);
			this.preProducer.produce(PRE_TOPIC, event);
			long startedOn = System.currentTimeMillis();
			int timeout = 10 * 1000;
			boolean timedOut = false;
			int eventHash = event.getId().hashCode();
			while (true) {
				if (receivedQueries.containsKey(eventHash)) {
					event = receivedQueries.get(eventHash);
					receivedQueries.remove(eventHash);
					break;
				}

				if (System.currentTimeMillis() - startedOn > timeout) {
					timedOut = true;
					break;
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (timedOut) {
					return "Query timeout";
				}
				if (event.isAuthenticated() == false) {
					return "Not authorized";
				}
			}
		}
			if(isUpdate){
				String uri = "http://typhonql-server:8080/update";
				Map<String, Object> vars = new HashMap<String, Object>();
				vars.put("command", query);
				RestTemplate restTemplate = new RestTemplate();
				Gson gson = new GsonBuilder().disableHtmlEscaping().create();
				System.out.println(gson.toJson(vars));
				ResponseEntity<String> result = restTemplate.postForEntity(uri, gson.toJson(vars), String.class);
				//connection = new XMIPolystoreConnection(mlModel.getContents(), infos);
				System.out.println(result.getBody());
				if (result.getStatusCode() == HttpStatus.OK) {
					isReady = true;
					System.out.println("update query executed successfully");

				} else {
					System.out.println("error in query");
					isReady=false;

				}
				if(isAnalyticsAvailiable()) {
					sendPostEvent(event,query,result);
				}
				return result.getBody();
			}
			else {
				String uri = "http://typhonql-server:8080/query?q="+query;

				RestTemplate restTemplate = new RestTemplate();

				ResponseEntity<String> result = restTemplate.getForEntity(uri,  String.class);
				//connection = new XMIPolystoreConnection(mlModel.getContents(), infos);
				System.out.println(result.getBody());
				if (result.getStatusCode() == HttpStatus.OK) {
					isReady = true;
					System.out.println("update query executed successfully");

				} else {
					System.out.println("error in query");
					isReady=false;

				}
				if(isAnalyticsAvailiable()) {
					sendPostEvent(event,query,result);
				}
				return result.getBody();

			}
    }

	@Override
	public void onNewMesaage(Event event) {
		receivedQueries.put(event.getId().hashCode(), (PreEvent)event);
	}
	
	private void subscribeToAuthorization() {
		Thread subscribeTask = new Thread(new QueueConsumer(kafkaConnection, AUTH_TOPIC, this));
		subscribeTask.start();
	}

	private void sendPostEvent(PreEvent event,String query,ResponseEntity<String> result){
		PostEvent post = new PostEvent();
		post.setId(UUID.randomUUID().toString());
		post.setQuery(query);
		post.setPreEvent(event);
		post.setSuccess(true);
		this.postProducer.produce(POST_TOPIC, post);
	}
	
}