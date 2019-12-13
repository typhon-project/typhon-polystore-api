package com.clms.typhonapi.storage;

import com.clms.typhonapi.models.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;


public interface ModelStorage extends MongoRepository<Model, String> {


    Model findTopModelByTypeOrderByVersionDesc(String type);
}