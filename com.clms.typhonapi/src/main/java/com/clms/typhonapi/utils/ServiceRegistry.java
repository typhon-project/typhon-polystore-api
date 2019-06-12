package com.clms.typhonapi.utils;

import java.util.ArrayList;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;

import com.clms.typhonapi.models.Service;;

public class ServiceRegistry {

	private ArrayList<Service> _services;
	
	public ServiceRegistry() {
		_services = new ArrayList<>();
	}
	
	public void load(String xmi) {
		//TODO: temporary code, read from XMI
		_services = new ArrayList<>();
        Service db1 = new Service("mariadbtest","ACTIVE","test.mariadb","3306","root","admin","mariadb","testdb");
        Service db2 = new Service("polystoredb","ACTIVE","mongodb","27017","admin","admin","mongodb","admin");
        _services.add(db1);
        _services.add(db2);
        
		if (xmi == null || xmi.isBlank()) {
			return;
		}
		
		//parse XMI
		DocumentBuilderFactory factory =
		DocumentBuilderFactory.newInstance();
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
				
				Element eElement = (Element) nNode;
				String type = eElement.getAttribute("xsi:type");
				if (type.equals("typhonDL:DBType")) {
					String dbName = eElement.getAttribute("name");
					//TODO: continue with parsing once the XMI is completed
				}
			}
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ArrayList<Service> getDatabases() {
        //TODO: return only databases
        return _services;
	}
}
