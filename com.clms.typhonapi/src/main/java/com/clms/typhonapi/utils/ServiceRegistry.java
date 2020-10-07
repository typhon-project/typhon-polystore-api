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
	private NodeList list;
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

		// parse XMI
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			ByteArrayInputStream input = new ByteArrayInputStream(xmi.getBytes("UTF-8"));
			Document doc = builder.parse(input);

			NodeList nList = doc.getElementsByTagName("elements");
			list =nList;
			for (int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);

				if (nNode.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}

				Service service = null;

		    Element db = (Element) nNode;
 		    if (db.getAttribute("xsi:type").equals("typhonDL:Software") && db.getAttribute("name").equals("zookeeper")) {
					Element kafkaEl = querySelector(doc, ".//elements[@name='Kafka']");
					if (kafkaEl!=null) {
						Service kafka = new Service();
						kafka.setName("kafka");
						kafka.setServiceType(ServiceType.Queue);
						kafka.setExternalHost(querySelector(kafkaEl,".//parameters[@name='KAFKA_ADVERTISED_HOST_NAME']").getAttribute("value"));
						// Element portsEl = querySelector(kafkaEl,".//ports");
						// Element target = querySelector(portsEl, ".//key_values[@name='target']");
						Element portsEl = querySelector(kafkaEl,".//parameters[@name='KAFKA_LISTENERS']");
						String ports = portsEl.getAttribute("value");
						String internalPort = ports.split(",")[1].split(":")[2];
						String externalPort = ports.split(",")[0].split(":")[2];
						kafka.setInternalPort(Integer.parseInt(internalPort));
						kafka.setExternalPort(Integer.parseInt(externalPort));
						// int internalPort = Integer.parseInt(targetport);
						// Element published = querySelector(portsEl, ".//key_values[@name='published']");
						/*if(published!=null){
							String publishport = published.getAttribute("value");
							int externalPort = Integer.parseInt(publishport);
							kafka.setExternalPort(externalPort);

						} */
						//kafka.setInternalPort(internalPort);
						kafka.setInternalHost("kafka");
						_services.add(kafka);
						continue;
					}
				}

				if (db.getAttribute("xsi:type").equals("typhonDL:Software")
							&& !db.getAttribute("name").equals("zookeeper")
							&& !db.getAttribute("name").equals("Kafka")
							&& !db.getAttribute("name").equals("authAll")
							&& !db.getAttribute("name").equals("nlae")) {
					service = new Service();
					service.setName(db.getAttribute("name"));
					service.setServiceType(ServiceType.Software);
					service.setStatus(ServiceStatus.ONLINE);
					fillContainerInfo(doc, service,i);
					_services.add(service);
					continue;
		    }

		    if (db.getAttribute("xsi:type").equals("typhonDL:Software")
						&& db.getAttribute("name").equals("nlae")) {
					service = new Service();
					service.setName(db.getAttribute("name"));
					service.setServiceType(ServiceType.Database);
					service.setDbType(DatabaseType.NLAE);

					if (db.getAttribute("external")!=null && !db.getAttribute("external").isEmpty()) {
						service.setStatus(ServiceStatus.ONLINE);
						service.setExternal(true);
						Element uri = (Element) db.getElementsByTagName("uri").item(0);
						String url = uri.getAttribute("value");
						String host;
						String port;
						String[] urlParts = url.split(":");

						if (urlParts[0].equals("localhost")){
							host = urlParts[0];
							port = urlParts[1];
						} else {
							host = urlParts[1].replace("/","");
							port = urlParts[2];
						}

						service.setInternalHost(host);
						service.setExternalHost(host);
						service.setInternalPort(Integer.parseInt(port));
						service.setExternalPort(Integer.parseInt(port));
					} else {
						service.setExternal(false);
						fillContainerInfo(doc, service,i);
					}
					_services.add(service);
					continue;
				}

		    if (db.getAttribute("xsi:type").equals("typhonDL:DB") && db != null) {
		     	service = parseDbElement(db, list);

					if (db.getAttribute("external")!=null && !db.getAttribute("external").isEmpty()){
						service.setExternal(true);
						Element uri = (Element) db.getElementsByTagName("uri").item(0);
						String url = uri.getAttribute("value");
						String host = url.split(":")[1].replace("/","");
						String port = url.split(":")[2];
						service.setInternalHost(host);
						service.setExternalHost(host);
						service.setInternalPort(Integer.parseInt(port));
						service.setExternalPort(Integer.parseInt(port));
					}
					else{
						service.setExternal(false);
					}
		    }

				if (service != null && !service.getExternal() && service.getServiceType() != ServiceType.Software) {
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
			//dbHelper.updateDbConnections();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Service parseDbElement (Element eElement,NodeList list) {
		Service db = new Service();
		db.setServiceType(ServiceType.Database);
		Element dbElement = eElement;
		String typeReference = dbElement.getAttribute("type");
		int typeElement = Integer.parseInt(typeReference.split("\\.")[1]);
		//int refDbType = Integer.parseInt(typeReference.substring(typeReference.length()-1,typeReference.length()))+1;
		Element dbTypeElement = (Element) list.item(typeElement);//"(.//elements[@type='typhonDL:DBType'])["+Integer.toString(typeElement)+"]");
		String dbType="";
		if(dbTypeElement!=null) {
			dbType = dbTypeElement.getAttribute("name");
			if (dbType == null){
				dbType = dbTypeElement.getAttribute("name").toLowerCase();
			}
		}

		NodeList creds = dbElement.getElementsByTagName("credentials");
		if(creds!=null && creds.getLength()!=0){
			Element credsElement = (Element) creds.item(0);
			db.setUsername(credsElement.getAttribute("username"));
			db.setPassword(credsElement.getAttribute("password"));
		}
		else{
			db.setUsername("admin");
			db.setPassword("password");
		}
		switch (dbType.toLowerCase()) {
			case "mongo":
				db.setDbType(DatabaseType.MongoDb);
				db.setEngineType(EngineType.Document);
				break;
			case "mariadb":
				db.setDbType(DatabaseType.MariaDb);
				db.setEngineType(EngineType.Relational);
				break;
			case "mysql":
				db.setDbType(DatabaseType.MysqlDb);
				db.setEngineType(EngineType.Relational);
				break;
			case "neo4j":
				db.setDbType(DatabaseType.neo4j);
				db.setEngineType(EngineType.Graph);
				break;
			case "cassandra":
				db.setDbType(DatabaseType.cassandra);
				db.setEngineType(EngineType.KeyValue);
				break;
			default:
				System.out.println("Database not supported: "+dbType);
				break;
		}

		db.setName(dbElement.getAttribute("name"));
		return db;
	}

	private void fillContainerInfo(Document doc, Service service,int i) {
		Element containerEl;
		//containerEl = querySelector(doc, "//containers[@name='" + service.getName() + "']");

		containerEl = querySelector(doc, "//containers//deploys[@reference=\"//@elements."+i+"\"]/..");
		if(containerEl==null)
			return;
			Element uiEl;
			uiEl = querySelector(doc, "//elements[@name='" + "polystore_ui" + "']");
			Element parametersEl = querySelector(uiEl, ".//parameters");
			//String externalhost = querySelector(parametersEl, "//parameters[@name='API_HOST']").getAttribute("value");
			//service.setExternalHost(externalhost);
		//new implementation

		Element uri = (Element) containerEl.getElementsByTagName("uri").item(0);
		if(uri==null){
			service.setExternal(false);
			return;
		}
		String url = uri.getAttribute("value");
		String host = url.split(":")[0].replace("/","");
		String port = url.split(":")[1];
		service.setInternalHost(host);
		service.setExternalHost(host);
		service.setInternalPort(Integer.parseInt(port));
		service.setExternalPort(Integer.parseInt(port));
		Element portEl = querySelector(containerEl, ".//ports");
		if (portEl != null) {
			String portsValue = "";
			//service.setExternalHost(externalhost);
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
		service.setStatus(ServiceStatus.ONLINE);
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
