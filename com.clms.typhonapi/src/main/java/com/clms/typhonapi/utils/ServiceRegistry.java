package com.clms.typhonapi.utils;

import java.util.ArrayList;
import java.util.stream.Collectors;

import com.clms.typhonapi.models.*;
import com.clms.typhonapi.storage.ModelStorage;
import jdk.nashorn.internal.runtime.logging.DebugLogger;
import org.apache.catalina.Engine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

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
		if (dlModel == null) {
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

	public Service getService(ServiceType type) {
		return _services
				.stream()
				.filter(s -> s.getServiceType() == type)
				.findFirst()
				.orElse(null);
	}
	
	private void load(String xmi) {
		/*_services = new ArrayList<Service>() {
			{
				add(new Service(ServiceType.Database, "mariadbtest", "ACTIVE", "localhost", 3306, "root", "admin", DatabaseType.MariaDb));
				add(new Service(ServiceType.Database, "polystoredb","ACTIVE","localhost", 27017,"admin", "admin", DatabaseType.MongoDb));
				add(new Service(ServiceType.Queue, "kafka for analytics", "localhost", 9092));
			}
		};*/
        
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
		        if(!db.getAttribute("xsi:type").equals("typhonDL:DB")){
					System.out.println(db.getAttribute("xsi:type"));

					continue;
		        }
		        if (db != null) {
		        	service = parseDbElement(db,nList);
		        }
		        
		        if (service != null) {
		        	service.setStatus(ServiceStatus.OFFLINE);
					fillContainerInfo(doc, service);
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
			dbType = dbTypeElement.getAttribute("name").toLowerCase();
		}
		switch (dbType) {
			case "mongo":
				Element mongoUser = querySelector(parameters, ".//key_Values[@name='MONGO_INITDB_ROOT_USERNAME']");
				Element mongoPass = querySelector(parameters, ".//key_Values[@name='MONGO_INITDB_ROOT_PASSWORD']");
				db.setUsername(mongoUser == null ? "admin" : mongoUser.getAttribute("value"));
				db.setPassword(mongoPass == null ? "admin" : mongoPass.getAttribute("value"));
				db.setDbType(DatabaseType.MongoDb);
				db.setEngineType(EngineType.Document);
				break;
			case "mariadb":
				Element mariaUser = querySelector(parameters, ".//key_Values[@name='MYSQL_ROOT_USERNAME']");
				Element mariaPass = querySelector(parameters, ".//key_Values[@name='MYSQL_ROOT_PASSWORD']");
				db.setUsername(mariaUser == null ? "root" : mariaUser.getAttribute("value"));
				db.setPassword(mariaPass == null ? "admin" : mariaPass.getAttribute("value"));
				db.setDbType(DatabaseType.MariaDb);
				db.setEngineType(EngineType.Relational);
				break;
			case "mysql":
				Element mysqlUser = querySelector(parameters, ".//key_Values[@name='MYSQL_ROOT_USERNAME']");
				Element mysqlPass = querySelector(parameters, ".//key_Values[@name='MYSQL_ROOT_PASSWORD']");
				db.setUsername(mysqlUser == null ? "root" : mysqlUser.getAttribute("value"));
				db.setPassword(mysqlPass == null ? "admin" : mysqlPass.getAttribute("value"));
				db.setDbType(DatabaseType.MysqlDb);
				db.setEngineType(EngineType.Relational);
				break;
			default:
				System.out.println("Database not supported: "+dbType);
				break;
		}
		
		db.setName(dbElement.getAttribute("name"));
		
		return db;
	}
	
	private void fillContainerInfo(Document doc, Service service) {
		Element containerEl;
		if(service.getName().equals("polystore_db")){
			containerEl = querySelector(doc, "//containers[@name='" + "polystore-mongo" + "']");
		}
		else {
			containerEl = querySelector(doc, "//containers[@name='" + service.getName() + "']");
		}
		if (containerEl == null) {
			return;
		}
		
		//find port
		/*	Element portEl = querySelector(containerEl, ".//properties[@name='ports']");
		if (portEl != null) {
			String portsValue = "";
			if (portEl.hasAttribute("value")) {
				portsValue = portEl.getAttribute("value");
			} else {
				Element valueEl = querySelector(portEl, ".//values");
				if (valueEl != null) {
					portsValue = valueEl.getTextContent();
				}
			}

			if (portsValue != null && portsValue.contains(":")) {
				int port = Integer.parseInt(portsValue.split(":")[0]);
				service.setPort(port);
			}
		}

		//find hostname
		Element hostEl = querySelector(containerEl, ".//properties[@name='hostname']");
		service.setHost(hostEl == null ? "localhost" : hostEl.getAttribute("value")); */

		//new implementation
		Element portEl = querySelector(containerEl, ".//ports");
		if (portEl != null) {
			String portsValue = "";

				Element target = querySelector(portEl, ".//key_values[@name='target']");
				Element published = querySelector(portEl, ".//key_values[@name='published']");
				String targetport = target.getAttribute("value");
				String publishport = published.getAttribute("value");
				portsValue = publishport +":"+targetport;

			/*} else {
				Element valueEl = querySelector(portEl, ".//values");
				if (valueEl != null) {
					portsValue = valueEl.getTextContent();
				}
			} */

			if (portsValue != null) {
				int port = Integer.parseInt(portsValue.split(":")[0]);
				service.setPort(port);
			}
		}
		
		//find hostname
		Element hostEl = querySelector(containerEl, ".//properties[@name='hostname']");
		service.setHost(hostEl == null ? "localhost" : hostEl.getAttribute("value"));
		
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
