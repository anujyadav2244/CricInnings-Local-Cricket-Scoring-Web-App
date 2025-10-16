package com.innings.innings.team;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface TeamRepository extends MongoRepository<Team, String> {
    Team findByName(String name);
    List<Team> findByLeagueId(String leagueId);
    
}
