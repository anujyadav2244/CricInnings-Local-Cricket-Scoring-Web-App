package com.cricbook.cricbook.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.cricbook.cricbook.model.Match;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MatchRepository extends MongoRepository<Match, String> {
    
    // Find matches for a team in a league on a specific date
    List<Match> findByLeagueIdAndMatchDateAndTeam1OrTeam2(String leagueId, LocalDate matchDate, String team1, String team2);
}
