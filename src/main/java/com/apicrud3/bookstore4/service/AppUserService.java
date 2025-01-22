package com.apicrud3.bookstore4.service;

import com.apicrud3.bookstore4.model.AppUser;
import com.apicrud3.bookstore4.repository.AppRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserService implements UserDetailsService {

    @Autowired
    private AppRepository repo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = repo.findByUsername(username);
        if(user != null){
           var springUser = User.withUsername(user.getUsername())
                   .password(user.getPassword())
                   .authorities(user.getRole())
                   .build();
            return springUser;
        }

        return null;
    }
}
