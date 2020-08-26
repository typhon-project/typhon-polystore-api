package com.clms.typhonapi.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.nio.charset.StandardCharsets;


import com.google.gson.Gson;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


import com.clms.typhonapi.kafka.QueueConsumer;
import com.clms.typhonapi.kafka.ConsumerHandler;
import com.clms.typhonapi.kafka.QueueProducer;
import com.clms.typhonapi.models.*;
import com.clms.typhonapi.storage.ModelStorage;


import ac.york.typhon.analytics.commons.datatypes.events.Event;
import ac.york.typhon.analytics.commons.datatypes.events.PreEvent;
import ac.york.typhon.analytics.commons.datatypes.events.PostEvent;

@Component
public class QueryRunner implements ConsumerHandler {
    List<DatabaseInfo> infos;
    Model ml;
    private QueueProducer preProducer;
    private QueueProducer postProducer;
    private static String POST_TOPIC = "POST";
    private static String PRE_TOPIC = "PRE";
    private static String AUTH_TOPIC = "AUTH";
    private Map<Integer, PreEvent> receivedQueries = new HashMap<Integer, PreEvent>();
    private String kafkaConnection = "";
    private boolean isReady;
    HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(
            HttpClientBuilder.create().build());

    @Autowired
    private ServiceRegistry serviceRegistry;
    @Autowired
    private DbUtils dbHelper;
    @Autowired
    private ModelStorage repo;

    public void init(Model mlModel) {

        isReady = false;

        try {
            //dbHelper.updateDbConnections();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mlModel == null) {
            return;
        }
        ml = mlModel;
        if (isAnalyticsAvailiable()) {
            Service analyticsQueue = serviceRegistry.getService(ServiceType.Queue);
            if (analyticsQueue != null) {
                receivedQueries.clear();
                kafkaConnection = analyticsQueue.getInternalHost() + ":" + analyticsQueue.getInternalPort();
                System.out.println("Polystore will try to publish on " + kafkaConnection);
                preProducer = new QueueProducer(kafkaConnection);
                postProducer = new QueueProducer(kafkaConnection);
                subscribeToAuthorization();
            } else {
                System.out.println("[~~~~~~~WARNING~~~~~~~] No analytics service found in dl...");
            }
        }
        infos = new ArrayList<DatabaseInfo>();
        for (Service service : serviceRegistry.getDatabases()) {
            DatabaseType type = service.getDbType();
            String dbms;
            String swattype;
            if (type == DatabaseType.MongoDb) {
                swattype = "documentdb";
                dbms = "MongoDb";
            } else if (type == DatabaseType.MysqlDb) {
                swattype = "relationaldb";
                dbms = "MySQL";
            } else if (type == DatabaseType.cassandra) {
                swattype = "KeyValue";
                dbms = "cassandra";
            } else if (type == DatabaseType.neo4j) {
                swattype = "Graph";
                dbms = "neo4j";
            } else {
                swattype = "relationaldb";
                dbms = "MariaDB";
            }
            infos.add(new DatabaseInfo(service.getInternalHost(), service.getInternalPort(), service.getName(), swattype, dbms, service.getUsername(), service.getPassword()));
            isReady = true;
        }
        //TODO: initialize query engine with xmi and dbConnections
	/*	try {
			String uri = "http://typhonql-server:7000/initialize";
			Map<String, Object> vars = new HashMap<String, Object>();
			vars.put("xmi", mlModel.getContents());
			vars.put("databaseInfo",infos);


			RestTemplate restTemplate = RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);;
			Gson gson = new GsonBuilder().disableHtmlEscaping().create();
			System.out.println(gson.toJson(vars));
			ResponseEntity<String> result = restTemplate.postForEntity(uri, gson.toJson(vars), String.class);
			//connection = new XMIPolystoreConnection(mlModel.getContents(), infos);
			System.out.println(result.getBody());
			if (result.getStatusCode() == HttpStatus.OK) {

				System.out.println("completed initialization");
			} else {
				System.out.println("Could not establish connections to the polystore databases, reupload DL model and try again!");
				isReady=false;

			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Could not establish connections to the polystore databases, reupload DL model and try again!");
			isReady=false;
		} */

    }

    public void turnOff() {
        isReady = false;
    }

    public boolean isReady() {
        return isReady;
    }

    public boolean isAnalyticsAvailiable() {
        if (serviceRegistry.getService(ServiceType.Queue) == null) {
            System.out.println(ServiceType.Analytics + " are turned off...");
            return false;
        } else {
            return true;
        }
    }

    public boolean resetDatabases() {
        try {
            String uri = "http://typhonql-server:7000/reset";
            //String uri = "http://localhost:7000/reset";

            RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
            Map<String, Object> vars = new HashMap<String, Object>();
            vars.put("xmi", ml.getContents());
            vars.put("databaseInfo", infos);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_ENCODING, "gzip");
            headers.add(HttpHeaders.ACCEPT_ENCODING, "gzip");
            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(vars, headers);
            ResponseEntity<String> result = restTemplate.postForEntity(uri, request, String.class);
            if (result.getStatusCode() == HttpStatus.OK) {
                System.out.println(result);
                //PageRequest request = new PageRequest(0, 1, new Sort(Sort.Direction.DESC, "version"));
                Model mlModel = repo.findTopModelByTypeOrderByVersionDesc("ML");
                mlModel.setInitializedDatabases(true);
                repo.save(mlModel);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void initDatabases() {

    }

    public ResponseEntity<String> run(String user, Map<String,Object> query, boolean isUpdate) throws UnsupportedEncodingException {
        ResponseEntity<String> response;
        if (!isReady()) {
            response = new ResponseEntity<String>("Query engine is not initialized", HttpStatus.PRECONDITION_FAILED);
            return response;
        }
        System.out.println("Log 1 of run in QueryRunner...");
        PreEvent event = new PreEvent();
        System.out.println("Analytics available: " + isAnalyticsAvailiable());

        if (isAnalyticsAvailiable()) {
            event.setQueryTime(new Date());
            event.setId(UUID.randomUUID().toString());
            event.setQuery(String.valueOf(query.get("query")));
            event.setDbUser(user);
            //debugging purposes
            //event.setInvertedNeeded(true);
            //event.setResultSetNeeded(true);
            //event.setInvertedQuery("from OBLG_GNL o select o.OBLG_ID");


            preProducer.produce(PRE_TOPIC, event);
            long startedOn = System.currentTimeMillis();
            int timeout = 10 * 1000;
            boolean timedOut = false;
            int eventHash = event.getId().hashCode();
            System.out.println("This is the PreEvent: " + event.toString());
            //PreEvent recevent;
            while (true) {
                if (receivedQueries.containsKey(eventHash)) {
                    PreEvent recevent = receivedQueries.get(eventHash);
                    System.out.println("This is the recevent: " + recevent.toString());
                    receivedQueries.remove(eventHash);
                    if (recevent.isAuthenticated() == false) {
                        response = new ResponseEntity<String>("Not authorized on Analytics Queue", HttpStatus.UNAUTHORIZED);
                        System.out.println("The recevent isn't authenticated...");
                        return response;
                    } else if (recevent.isAuthenticated()) {
                        PostEvent postEvent = new PostEvent();
                        try {
                            if (recevent.isInvertedNeeded()) {
                                if (recevent.getInvertedQuery().isEmpty() || recevent.getInvertedQuery() == null) {
                                    System.out.println("Inverted Query was either empty or null...");
                                }
                                else {
                                    ResponseEntity<String> invertedQueryResponse;
                                    String invertedQueryResponseBody;
                                    Map<String,Object> justTheQuery = new HashMap<>();
                                    justTheQuery.put("query", recevent.getInvertedQuery());
                                    System.out.println("The invertedQuery is: " + justTheQuery.get("query"));
                                    if (isUpdate) {
                                        invertedQueryResponse = executeUpdate(justTheQuery);
                                    } else {
                                        invertedQueryResponse = executeQuery(justTheQuery);
                                    }
                                    invertedQueryResponseBody = invertedQueryResponse.getBody();
                                    System.out.println("The response status of the inverted query is : " + invertedQueryResponse.getStatusCode() + invertedQueryResponse.getStatusCodeValue());
                                    System.out.println("The response of the inverted query is : " + invertedQueryResponse);
                                    System.out.println("The response body of the inverted query is : " + invertedQueryResponseBody);
                                    postEvent.setInvertedQueryResultSet(invertedQueryResponseBody);
                                }
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                        }

                        System.out.println("This is the PreEvent': " + recevent.toString());

                        postEvent.setPreEvent(recevent);
                        postEvent.setStartTime(new Date());
                        ResponseEntity<String> result;
                        if (isUpdate) {
                            result = executeUpdate(query);
                        } else {
                            result = executeQuery(query);
                        }
                        postEvent.setEndTime(new Date());
                        //check bool ResultSetNeeded to decide if we add the result set.
                        if (recevent.isResultSetNeeded()) {
                            System.out.println("The result is : " + result + " " + result.getStatusCode() + " " + result.getStatusCodeValue());
                            System.out.println("The result body is : " + result.getBody());
                            postEvent.setResultSet(result.getBody());
                        }

                        sendPostEvent(postEvent);
                        System.out.println("This is the PostEvent: " + postEvent.toString());
                        return result;
                    }
                }

                if (System.currentTimeMillis() - startedOn > timeout) {
                    timedOut = true;
                    response = new ResponseEntity<String>("Auth queue timeout", HttpStatus.REQUEST_TIMEOUT);
                    return response;
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            ResponseEntity<String> result;
            if (isUpdate) {
                result = executeUpdate(query);

            } else {
                result = executeQuery(query);
            }
            return result;
        }
    }


    public ResponseEntity<String> preparedUpdate(String user, Map<String, Object> json) {
        ResponseEntity<String> response;
        if (!isReady()) {
            response = new ResponseEntity<String>("Query engine is not initialized", HttpStatus.PRECONDITION_FAILED);
            return response;
        }

        PreEvent event = new PreEvent();

        if (isAnalyticsAvailiable()) {
            event.setQueryTime(new Date());
            event.setId(UUID.randomUUID().toString());
            //event.setAuthenticated(true);
            event.setQuery((String) json.get("command"));
            event.setDbUser(user);
            this.preProducer.produce(PRE_TOPIC, event);
            long startedOn = System.currentTimeMillis();
            int timeout = 10 * 1000;
            boolean timedOut = false;
            int eventHash = event.getId().hashCode();
            System.out.println("This is the PreEvent: " + event.toString());
            PreEvent recevent;
            while (true) {
                if (receivedQueries.containsKey(eventHash)) {
                    recevent = receivedQueries.get(eventHash);
                    System.out.println("This is the recevent: " + recevent.toString());
                    receivedQueries.remove(eventHash);
                    if (recevent.isAuthenticated() == false) {
                        response = new ResponseEntity<String>("Not authorized on Analytics Queue", HttpStatus.UNAUTHORIZED);
                        System.out.println("The recevent isn't authenticated...");
                        return response;
                    } else if (recevent.isAuthenticated() == true) {
                        PostEvent postEvent = new PostEvent();
                        try {
                            if (recevent.isInvertedNeeded()) {
                                if (recevent.getInvertedQuery().isEmpty() || recevent.getInvertedQuery() == null) {
                                    System.out.println("Inverted Query was either empty or null...");
                                }
                                else {
                                    Map<String, Object> temp;
                                    temp = json;
                                    temp.put("command", recevent.getInvertedQuery());
                                    postEvent.setInvertedQueryResultSet(executePreparedUpdate(temp).getBody());
                                }
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                        System.out.println("This is the PreEvent': " + recevent.toString());
                        postEvent.setPreEvent(recevent);
                        postEvent.setStartTime(new Date());
                        ResponseEntity<String> result = executePreparedUpdate(json);
                        postEvent.setEndTime(new Date());
                        if (event.isResultSetNeeded()) {
                            postEvent.setResultSet(result.getBody());
                        }
                        sendPostEvent(postEvent);
                        System.out.println("This is the PostEvent: " + postEvent.toString());
                        return result;
                    }
                }

                if (System.currentTimeMillis() - startedOn > timeout) {
                    timedOut = true;
                    response = new ResponseEntity<String>("Auth queue timeout", HttpStatus.REQUEST_TIMEOUT);
                    return response;
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (timedOut) {
                    response = new ResponseEntity<String>("Analytics Query post timeout", HttpStatus.REQUEST_TIMEOUT);
                    return response;
                }

            }
        } else {
            ResponseEntity<String> result = executePreparedUpdate(json);

            //connection = new XMIPolystoreConnection(mlModel.getContents(), infos);
            System.out.println(result.getBody());
            return result;
        }
    }


    @Override
    public void onNewMesaage(Event event) {
        receivedQueries.put(event.getId().hashCode(), (PreEvent) event);
    }

    private void subscribeToAuthorization() {
        Thread subscribeTask = new Thread(new QueueConsumer(kafkaConnection, AUTH_TOPIC, this));
        subscribeTask.start();
    }

    private void sendPostEvent(PostEvent event) {
        this.postProducer.produce(POST_TOPIC, event);
    }

    private ResponseEntity<String> executeQuery(Map<String,Object> query) throws UnsupportedEncodingException {
        String finalQuery = URLEncoder.encode((String) query.get("query"), StandardCharsets.UTF_8.toString());

        String tempuri = "http://typhonql-server:7000/query";
        //String tempuri = "http://localhost:7000/query";
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("xmi", ml.getContents());
        vars.put("databaseInfo", infos);
        vars.put("query", query.get("query"));

        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_ENCODING, "gzip");
        headers.add(HttpHeaders.ACCEPT_ENCODING, "gzip");
        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(vars, headers);
        ResponseEntity<String> result = restTemplate.postForEntity(tempuri, request, String.class);
        //connection = new XMIPolystoreConnection(mlModel.getContents(), infos);
        System.out.println(result.getBody());
        System.out.println(result.getHeaders().get("ql-wall-time-ms"));
        if (result.getStatusCode() == HttpStatus.OK) {
            isReady = true;
            System.out.println("update query executed successfully");

        } else {
            System.out.println("error in query");
            isReady = false;

        }
        return result;
    }

    private ResponseEntity<String> executeUpdate(Map<String,Object> query) {
        String uri = "http://typhonql-server:7000/update";
        //String uri = "http://localhost:7000/update";
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("xmi", ml.getContents());
        vars.put("databaseInfo", infos);
        vars.put("command", query.get("query"));

        if (query.containsKey("blobs")){
            vars.put("blobs", query.get("blobs"));
        }

        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_ENCODING, "gzip");
        headers.add(HttpHeaders.ACCEPT_ENCODING, "gzip");
        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(vars, headers);
        ResponseEntity<String> result = restTemplate.postForEntity(uri, request, String.class);
        //connection = new XMIPolystoreConnection(mlModel.getContents(), infos);
        System.out.println(result.getBody());
        System.out.println(result.getHeaders().get("ql-wall-time-ms"));

        if (result.getStatusCode() == HttpStatus.OK) {
            isReady = true;
            System.out.println("update query executed successfully");

        } else {
            System.out.println("error in query");
            isReady = false;

        }
        return result;
    }

    private ResponseEntity<String> executePreparedUpdate(Map<String, Object> json) {
        String uri = "http://typhonql-server:7000/preparedUpdate";
        //String uri = "http://localhost:7000/preparedUpdate";
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        json.put("xmi", ml.getContents());
        json.put("databaseInfo", infos);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_ENCODING, "gzip");
        headers.add(HttpHeaders.ACCEPT_ENCODING, "gzip");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(json, headers);
        ResponseEntity<String> result = restTemplate.postForEntity(uri, request, String.class);

        if (result.getStatusCode() == HttpStatus.OK) {
            isReady = true;
            System.out.println("prepared update query executed successfully");
        } else {
            System.out.println("error in query");
            isReady = false;
        }
        return result;
    }


    //To POST a new Entity
    public ResponseEntity<String> postEntity(String entity, Map<String, Object> jsonBody) {
        String uri = "http://typhonql-server:7000/crud/" + entity;

        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("xmi", ml.getContents());
        vars.put("databaseInfo", infos);
        Gson gson = new Gson();
        String qlRestArgsValue = gson.toJson(vars);

        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_ENCODING, "gzip");
        headers.add(HttpHeaders.ACCEPT_ENCODING, "gzip");
        headers.add("QL-RestArguments", qlRestArgsValue);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<String> result = restTemplate.postForEntity(uri, request, String.class);

        if (result.getStatusCode() == HttpStatus.OK) {
            System.out.println("POST request was executed successfully");
        } else {
            System.out.println("Error in request");
        }
        return result;
    }

    //To GET an existing Entity
    public ResponseEntity<String> getEntity(String entity, String id) {
        String uri = "http://typhonql-server:7000/crud/" + entity + "/" + id;

        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("xmi", ml.getContents());
        vars.put("databaseInfo", infos);
        Gson gson = new Gson();
        String qlRestArgsValue = gson.toJson(vars);
//		System.out.println("The headerSomething is: " + headersSomething);
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_ENCODING, "gzip");
        headers.add(HttpHeaders.ACCEPT_ENCODING, "gzip");
        headers.add("QL-RestArguments", qlRestArgsValue);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(headers);
        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);

        if (result.getStatusCode() == HttpStatus.OK) {
            System.out.println("GET request was executed successfully");
        } else {
            System.out.println("Error in request");
        }
        return result;
    }

    //To PATCH an existing Entity
    public ResponseEntity<String> patchEntity(String entity, String id, Map<String, Object> jsonBody) {
        String uri = "http://typhonql-server:7000/crud/" + entity + "/" + id;

        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("xmi", ml.getContents());
        vars.put("databaseInfo", infos);
        Gson gson = new Gson();
        String qlRestArgsValue = gson.toJson(vars);

        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_ENCODING, "gzip");
        headers.add(HttpHeaders.ACCEPT_ENCODING, "gzip");
        headers.add("QL-RestArguments", qlRestArgsValue);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.PATCH, request, String.class);

        if (result.getStatusCode() == HttpStatus.OK) {
            System.out.println("PATCH request was executed successfully");
        } else {
            System.out.println("Error in request");
        }
        return result;
    }

    //To DELETE an existing Entity
    public ResponseEntity<String> deleteEntity(String entity, String id) {
        String uri = "http://typhonql-server:7000/crud/" + entity + "/" + id;

        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("xmi", ml.getContents());
        vars.put("databaseInfo", infos);
        Gson gson = new Gson();
        String qlRestArgsValue = gson.toJson(vars);

        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_ENCODING, "gzip");
        headers.add(HttpHeaders.ACCEPT_ENCODING, "gzip");
        headers.add("QL-RestArguments", qlRestArgsValue);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(headers);
        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.DELETE, request, String.class);

        if (result.getStatusCode() == HttpStatus.OK) {
            System.out.println("DELETE request was executed successfully");
        } else {
            System.out.println("Error in request");
        }
        return result;
    }
}
