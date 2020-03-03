package com.clms.typhonapi.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.clms.typhonapi.models.Model;
import com.clms.typhonapi.storage.ModelStorage;

@Component
public class ModelHelper {

	@Autowired
    private ModelStorage repo;
	
	public Model getDlModel() {
		return getModel("DL", -1);
	}
	
	public Model getDlModel(int version) {
		return getModel("DL", version);
	}
	
	public Model getMlModel() {
		return getModel("ML", -1);
	}
	
	public Model getMlModel(int version) {
		return getModel("ML", version);
	}
	
	public ArrayList<Model> getDlModels() {
		return getModels("DL");
	}
	
	public ArrayList<Model> getMlModels() {
		return getModels("ML");
	}

	public boolean addDlModel(String name, String contents) throws Exception {
		return addModel("DL", name, contents);
	}

	public void addMlModel(String name, String contents) throws Exception {
		addModel("ML", name, contents);
	}
	
	private boolean addModel(String type, String name, String contents) throws Exception {
		if (!isValid(contents)) {
			throw new Exception("Not valid model");
		}
		Model latest = getModel(type);
		
		Model m = new Model();
		m.setId(UUID.randomUUID().toString());
		m.setDateReceived(new Date());
		m.setType(type);
		m.setContents(contents);
		m.setInitializedConnections(false);
		if (latest == null) {
			m.setVersion(1);
			m.setInitializedDatabases(false);
		} else {
			//todo: temp code until model migration is implemented

			m.setVersion(latest.getVersion() + 1);
		}

		repo.insert(m);
		if(m.getType()=="DL"){
			return true;
		}
		else{
			return false;
		}
	}
	
	public Model getModel(String type) {
		return getModel(type, -1);
	}
	
	public Model getModel(String type, int version) {
		Optional<Model> model = null;
		
		if (version < 0) {
			model = repo.findAll()
				.stream()
				.filter(m -> m.getType().equalsIgnoreCase(type))
				.sorted(Comparator.comparing(Model::getVersion).reversed())
				.findFirst();
		} else {
			model = repo.findAll()
					.stream()
					.filter(m -> m.getType().equalsIgnoreCase(type) && m.getVersion() == version)
					.findFirst();
		}
		
		if (!model.isPresent()) {
			return null;
		}
		
		return model.get();
	}
	
	private ArrayList<Model> getModels(String type) {
		return new ArrayList<Model>(repo.findAll()
				.stream()
				.filter(m -> m.getType().equalsIgnoreCase(type))
				.sorted(Comparator.comparing(Model::getVersion).reversed())
				.collect(Collectors.toList()));
	}
	
	private boolean isValid(String xmi) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			ByteArrayInputStream input = new ByteArrayInputStream(xmi.getBytes("UTF-8"));
			builder.parse(input);
						
			return true;
		} catch (ParserConfigurationException e) {
		} catch (UnsupportedEncodingException e) {
		} catch (SAXException e) {
		} catch (IOException e) {
		}
		
		return false;
	}
}
