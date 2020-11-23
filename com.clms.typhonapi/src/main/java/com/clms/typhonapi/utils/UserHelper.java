package com.clms.typhonapi.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.clms.typhonapi.models.User;
import com.clms.typhonapi.storage.UserStorage;

@Component
public class UserHelper {

	@Autowired
    private UserStorage userService;
	
	public void createInitialUser() {
		if (!userService.findAll().isEmpty()) {
			return;
		}
		
		User u = new User();
		u.setUsername("admin");
		u.setPassword("admin1@");
		userService.insert(u);
	}
	
	public User get(String name, String password) {
		return userService.findAll().stream()
			.filter(u -> u.getUsername().equals(name) && u.getPassword().equals(password))
			.findFirst()
			.orElse(null);
	}
}
