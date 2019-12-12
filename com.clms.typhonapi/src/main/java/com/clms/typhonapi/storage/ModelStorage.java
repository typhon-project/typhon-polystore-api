package com.clms.typhonapi.storage;

import com.clms.typhonapi.models.Model;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;


public interface ModelStorage extends MongoRepository<Model, String> {

    @Query("{ 'type' : ?0,},{$max:'version'}")
    Model findLatestByType(String type);
}