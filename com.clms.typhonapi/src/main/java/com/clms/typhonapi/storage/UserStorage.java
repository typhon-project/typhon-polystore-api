package com.clms.typhonapi.storage;

import com.clms.typhonapi.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface UserStorage extends MongoRepository<User, String> {

	public void addUser(User u);
	public User updateUser(String userName, User u);
	public User getUser(String userName) ;
	public void deleteUser(String userName);
}
