package com.cricbook.cricbook.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.cricbook.cricbook.model.Team;

public interface TeamRepository extends MongoRepository<Team, String> {
    Team findByName(String name);
    List<Team> findByLeagueId(String leagueId);
}
