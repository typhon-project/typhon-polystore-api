package com.clms.typhonapi.storage;

import com.clms.typhonapi.models.DbAction;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DbActionStorage extends MongoRepository<DbAction, String> {

}
