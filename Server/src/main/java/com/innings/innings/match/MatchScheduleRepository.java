package com.innings.innings.match;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MatchScheduleRepository extends MongoRepository<MatchSchedule, String> {
    List<MatchSchedule> findByLeagueId(String leagueId);

}

