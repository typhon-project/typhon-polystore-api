package com.clms.typhonapi.utils;

import java.util.ArrayList;
import java.util.stream.Collectors;

import com.clms.typhonapi.models.*;
import com.clms.typhonapi.storage.ModelStorage;
import org.apache.catalina.Engine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import scala.Int;

import javax.xml.parsers.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.*;

@Component
public class ServiceRegistry {

	private ArrayList<Service> _services;
	@Autowired
	private DbUtils dbHelper;
	@Autowired
	private ModelStorage repo;

	public ServiceRegistry() {
		_services = new ArrayList<>();
	}
	
	public void load(Model dlModel) {
		_services.clear();
		if (dlModel == null	) {
			return;
		}
		load(dlModel.getContents());
		dlModel.setInitializedConnections(true);
		repo.save(dlModel);
	}
	
	public ArrayList<Service> getDatabases() {
        return new ArrayList<>(_services.stream()
        		.filter(s -> s.getServiceType() == ServiceType.Database)
        		.collect(Collectors.toList()));
	}

	public ArrayList<Service> getServices() {
		return new ArrayList<>(_services.stream()
				.collect(Collectors.toList()));
	}

	public Service getService(ServiceType type) {
		return _services
				.stream()
				.filter(s -> s.getServiceType() == type)
				.findFirst()
				.orElse(null);
	}
	
	private void load(String xmi) {
		if (xmi == null || xmi.isEmpty()) {
			return;
		}
		
		//parse XMI
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			ByteArrayInputStream input = new ByteArrayInputStream(xmi.getBytes("UTF-8"));
			Document doc = builder.parse(input);
						
			NodeList nList = doc.getElementsByTagName("elements");
			for (int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);
				if (nNode.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				
				Service service = null;
				
		        Element db = (Element) nNode;
		        if(db.getAttribute("xsi:type").equals("typhonDL:Software") && db.getAttribute("name").equals("zookeeper")){
					Element kafkaEl = querySelector(doc, ".//containers[@name='kafka']");
					if(kafkaEl!=null){
						Service kafka = new Service();
						kafka.setName("kafka");
						kafka.setServiceType(ServiceType.Queue);
						kafka.setExternalHost(querySelector(kafkaEl,".//properties[@name='KAFKA_ADVERTISED_HOST_NAME']").getAttribute("value"));
						//Element portsEl = querySelector(kafkaEl,".//ports");
						//Element target = querySelector(portsEl, ".//key_values[@name='target']");
						Element portsEl = querySelector(kafkaEl,".//properties[@name='KAFKA_LISTENERS']");
						String ports = portsEl.getAttribute("value");
						String internalPort = ports.split(",")[1].split(":")[2];
						String externalPort = ports.split(",")[0].split(":")[2];
						kafka.setInternalPort(Integer.parseInt(internalPort));
						kafka.setExternalPort(Integer.parseInt(externalPort));
						//int internalPort = Integer.parseInt(targetport);
						//Element published = querySelector(portsEl, ".//key_values[@name='published']");
						/*if(published!=null){
							String publishport = published.getAttribute("value");
							int externalPort = Integer.parseInt(publishport);
							kafka.setExternalPort(externalPort);

						} */
					//	kafka.setInternalPort(internalPort);
						kafka.setInternalHost("kafka");
						_services.add(kafka);
					}
				}
		        if(!db.getAttribute("xsi:type").equals("typhonDL:DB")){
					System.out.println(db.getAttribute("xsi:type"));
					continue;
		        }
		        if (db != null) {
		        	service = parseDbElement(db,nList);
		        }
		        
		        if (service != null) {
		        	service.setStatus(ServiceStatus.OFFLINE);
					fillContainerInfo(doc, service,i);
					System.out.println("Parsed: " + service);
		        	_services.add(service);
		        }
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			dbHelper.updateDbConnections();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private Service parseDbElement(Element eElement,NodeList nList) {
		Service db = new Service();
		db.setServiceType(ServiceType.Database);
		Element dbElement = eElement;
		String typeReference = dbElement.getAttribute("type");
		int typeElement = Integer.parseInt(typeReference.substring(typeReference.length()-1,typeReference.length()));
		//int refDbType = Integer.parseInt(typeReference.substring(typeReference.length()-1,typeReference.length()))+1;
		Element dbTypeElement = (Element) nList.item(typeElement);//"(.//elements[@type='typhonDL:DBType'])["+Integer.toString(typeElement)+"]");
		Element parameters = querySelector(dbElement, "./parameters[@type='typhonDL:Key_KeyValueList']");
		String dbType="";
		if(dbTypeElement!=null) {
			dbType = dbTypeElement.getAttribute("name");
			if (dbType == null){
				dbType = dbTypeElement.getAttribute("name").toLowerCase();
			}
		}
		switch (dbType.toLowerCase()) {
			case "mongo":
				Element mongoUser = querySelector(parameters, ".//properties[@name='MONGO_INITDB_ROOT_USERNAME']");
				Element mongoPass = querySelector(parameters, ".//properties[@name='MONGO_INITDB_ROOT_PASSWORD']");
				db.setUsername(mongoUser == null ? "admin" : mongoUser.getAttribute("value"));
				db.setPassword(mongoPass == null ? "admin" : mongoPass.getAttribute("value"));
				db.setDbType(DatabaseType.MongoDb);
				db.setEngineType(EngineType.Document);
				break;
			case "mariadb":
				Element mariaUser = querySelector(parameters, ".//properties[@name='MYSQL_ROOT_USERNAME']");
				Element mariaPass = querySelector(parameters, ".//properties[@name='MYSQL_ROOT_PASSWORD']");
				db.setUsername(mariaUser == null ? "root" : mariaUser.getAttribute("value"));
				db.setPassword(mariaPass == null ? "admin" : mariaPass.getAttribute("value"));
				db.setDbType(DatabaseType.MariaDb);
				db.setEngineType(EngineType.Relational);
				break;
			case "mysql":
				Element mysqlUser = querySelector(parameters, ".//properties[@name='MYSQL_ROOT_USERNAME']");
				Element mysqlPass = querySelector(parameters, ".//properties[@name='MYSQL_ROOT_PASSWORD']");
				db.setUsername(mysqlUser == null ? "root" : mysqlUser.getAttribute("value"));
				db.setPassword(mysqlPass == null ? "admin" : mysqlPass.getAttribute("value"));
				db.setDbType(DatabaseType.MysqlDb);
				db.setEngineType(EngineType.Relational);
				break;
			case "neo4j":
				db.setDbType(DatabaseType.neo4j);
				db.setEngineType(EngineType.Graph);
			case "cassandra":
				db.setDbType(DatabaseType.cassandra);
				db.setEngineType(EngineType.Relational);
			default:
				System.out.println("Database not supported: "+dbType);
				break;
		}
		
		db.setName(dbElement.getAttribute("name"));
		
		return db;
	}
	
	private void fillContainerInfo(Document doc, Service service,int i) {
		Element containerEl;
		containerEl = querySelector(doc, "//containers[@name='" + service.getName() + "']");

		if (containerEl == null) {
			containerEl = querySelector(doc, "//containers//deploys[@reference=\"//@elements."+i+"\"]/..");
			if(containerEl==null)
			return;
		}
		Element uiEl;
		uiEl = querySelector(doc, "//elements[@name='" + "polystore_ui" +"']");
		Element parametersEl = querySelector(uiEl, ".//parameters");
		String externalhost = querySelector(parametersEl,"//properties[@name='API_HOST']").getAttribute("value");
		service.setExternalHost(externalhost);


		//new implementation
		Element portEl = querySelector(containerEl, ".//ports");
		if (portEl != null) {
			String portsValue = "";

				Element target = querySelector(portEl, ".//key_values[@name='target']");
				String targetport = target.getAttribute("value");
				int internalPort = Integer.parseInt(targetport);
				Element published = querySelector(portEl, ".//key_values[@name='published']");
				if(published!=null){
					String publishport = published.getAttribute("value");
					int externalPort = Integer.parseInt(publishport);
					service.setExternalPort(externalPort);

				}
				service.setInternalPort(internalPort);
			}

		
		//find hostname
		Element hostEl = querySelector(containerEl, ".//properties[@name='hostname']");
		service.setInternalHost(hostEl == null ? containerEl.getAttribute("name") : hostEl.getAttribute("value"));

		
	}

	private Element querySelector(Node node, String query) {
		XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        XPathExpression expr = null;
		try {
			expr = xpath.compile(query);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			return null;
		}
        Object result;
		try {
			result = expr.evaluate(node, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			return null;
		}
        return (Element) result;
	}
	
	private NodeList querySelectorAll(Node node, String query) {
		XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        XPathExpression expr = null;
		try {
			expr = xpath.compile(query);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			return null;
		}
        Object result;
		try {
			result = expr.evaluate(node, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			return null;
		}
        return (NodeList) result;
	}
	
}
