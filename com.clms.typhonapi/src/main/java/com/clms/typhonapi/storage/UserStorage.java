package com.clms.typhonapi.storage;

import com.clms.typhonapi.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface UserStorage extends MongoRepository<User, String> {


}
