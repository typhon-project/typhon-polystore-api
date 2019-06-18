package com.clms.typhonapi.utils;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.clms.typhonapi.models.User;
import com.clms.typhonapi.storage.UserStorage;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserHelper userService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String name = authentication.getName();
        String password = authentication.getCredentials().toString();

        User user = userService.get(name, password);

        if (user == null)
        {
        	return null;
        }
        
        ArrayList<GrantedAuthority> authorities = new ArrayList<>();

        return new UsernamePasswordAuthenticationToken(name, password, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
