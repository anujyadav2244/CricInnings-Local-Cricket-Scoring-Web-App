package com.cricbook.cricbook.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cricbook.cricbook.model.Match;
import com.cricbook.cricbook.repository.MatchRepository;

import lombok.Data;

@Data
@Service
public class MatchService {

    private final MatchRepository matchRepository;

   

    // Create match
    public Match createMatch(Match match) {

    // Team validation
    if(match.getTeam1().equalsIgnoreCase(match.getTeam2())) {
        throw new IllegalArgumentException("Team1 and Team2 cannot be the same");
    }

    // Check if team1 or team2 already has a match in the same league on the same date
    List<Match> conflictingMatches = matchRepository
        .findByLeagueIdAndMatchDateAndTeam1OrTeam2(
            match.getLeagueId(), 
            match.getMatchDate(), 
            match.getTeam1(), 
            match.getTeam2()
        );

    if(!conflictingMatches.isEmpty()) {
        throw new IllegalArgumentException("One of the teams already has a match scheduled on this date in the same league");
    }

    return matchRepository.save(match);
}


    // Get all matches
    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    // Get match by ID
    public Optional<Match> getMatchById(String id) {
        return matchRepository.findById(id);
    }

    // Update basic match info
    public Match updateMatchInfo(String id, String venue, String result, String tossWinner, String tossDecision) {
        Match match = matchRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Match not found"));

        match.setVenue(venue);
        match.setResult(result);
        match.setTossWinner(tossWinner);
        match.setTossDecision(tossDecision);

        return matchRepository.save(match);
    }

    // Update team scores, wickets, overs
    public Match updateScore(String id, String team1Score, String team2Score,
                             int team1Wickets, int team2Wickets, int team1Overs, int team2Overs) {
        Match match = matchRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Match not found"));

        match.setTeam1Score(team1Score);
        match.setTeam2Score(team2Score);
        match.setTeam1Wickets(team1Wickets);
        match.setTeam2Wickets(team2Wickets);
        match.setTeam1Overs(team1Overs);
        match.setTeam2Overs(team2Overs);

        return matchRepository.save(match);
    }

    // Set match winner and player of the match
    public Match setMatchResult(String id, String matchWinner, String playerOfTheMatch) {
        Match match = matchRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Match not found"));

        match.setMatchWinner(matchWinner);
        match.setPlayerOfTheMatch(playerOfTheMatch);

        return matchRepository.save(match);
    }

    // Update match date
    public Match updateMatchDate(String id, java.time.LocalDate matchDate) {
        Match match = matchRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Match not found"));

        match.setMatchDate(matchDate);
        return matchRepository.save(match);
    }

    // Delete match
    public void deleteMatch(String id) {
        matchRepository.deleteById(id);
    }
}
