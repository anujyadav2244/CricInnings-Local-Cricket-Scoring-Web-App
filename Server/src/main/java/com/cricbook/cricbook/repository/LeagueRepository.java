package com.cricbook.cricbook.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.cricbook.cricbook.model.League;
public interface LeagueRepository extends MongoRepository<League, String>{
    Optional<League> findByName(String name);
    List<League> findByAdminId(String adminId);
    boolean existsByName(String name);
}
