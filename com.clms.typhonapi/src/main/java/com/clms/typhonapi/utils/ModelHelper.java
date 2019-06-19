package com.clms.typhonapi.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.clms.typhonapi.models.Model;
import com.clms.typhonapi.storage.ModelStorage;

@Component
public class ModelHelper {

	@Autowired
    private ModelStorage repo;
	
	public Model getDlModel(int version) {
		return getModel("DL", version);
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

	public void addDlModel(String name, String contents) {
		addModel("DL", name, contents);
	}

	public void addMlModel(String name, String contents) {
		addModel("ML", name, contents);
	}
	
	private void addModel(String type, String name, String contents) {
		Model latest = getModel(type);
		
		Model m = new Model();
		m.setId(UUID.randomUUID().toString());
		m.setDateReceived(new Date());
		m.setType(type);
		m.setContents(contents);
		if (latest == null) {
			m.setVersion(1);
		} else {
			//todo: temp code until model migration is implemented
			m.setVersion(latest.getVersion() + 1);
		}
		
		repo.insert(m);
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
		
		if (model.isEmpty()) {
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
}
