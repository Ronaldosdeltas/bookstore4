package com.apicrud3.bookstore4.repository;

import com.apicrud3.bookstore4.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppRepository extends JpaRepository<AppUser, Integer> {
    AppUser findByUsername(String username);
    AppUser findByEmail(String email);

}

