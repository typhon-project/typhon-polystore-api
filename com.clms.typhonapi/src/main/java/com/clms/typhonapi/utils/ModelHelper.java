package com.clms.typhonapi.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.clms.typhonapi.models.Model;
import com.clms.typhonapi.storage.ModelStorage;

public class ModelHelper {

	public static Model getDlModel(ModelStorage repo, int version) {
		return ModelHelper.getModel(repo, "DL", version);
	}
	
	public static Model getMlModel(ModelStorage repo, int version) {
		return ModelHelper.getModel(repo, "ML", version);
	}
	
	public static ArrayList<Model> getDlModels(ModelStorage repo) {
		return ModelHelper.getModels(repo, "DL");
	}
	
	public static ArrayList<Model> getMlModels(ModelStorage repo) {
		return ModelHelper.getModels(repo, "ML");
	}

	public static void addDlModel(ModelStorage repo, String name, String contents) {
		ModelHelper.addModel(repo, "DL", name, contents);
	}

	public static void addMlModel(ModelStorage repo, String name, String contents) {
		ModelHelper.addModel(repo, "ML", name, contents);
	}
	
	private static void addModel(ModelStorage repo, String type, String name, String contents) {
		Model latest =ModelHelper.getModel(repo, type);
		
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
	
	public static Model getModel(ModelStorage repo, String type) {
		return ModelHelper.getModel(repo, type, -1);
	}
	
	public static Model getModel(ModelStorage repo, String type, int version) {
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
	
	private static ArrayList<Model> getModels(ModelStorage repo, String type) {
		return new ArrayList<Model>(repo.findAll()
				.stream()
				.filter(m -> m.getType().equalsIgnoreCase(type))
				.sorted(Comparator.comparing(Model::getVersion).reversed())
				.collect(Collectors.toList()));
	}
}
