package com.clms.typhonapi.storage;

import com.clms.typhonapi.models.Model;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ModelStorage extends MongoRepository<Model, String> {


    Model findTopModelByTypeOrderByVersionDesc(String type);
}