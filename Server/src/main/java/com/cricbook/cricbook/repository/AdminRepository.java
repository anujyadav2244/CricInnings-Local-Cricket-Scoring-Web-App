package com.cricbook.cricbook.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.cricbook.cricbook.model.Admin;


public interface AdminRepository extends MongoRepository<Admin, String> {
    Optional<Admin> findByEmail(String email);
}
