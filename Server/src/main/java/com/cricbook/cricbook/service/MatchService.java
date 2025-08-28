package com.cricbook.cricbook.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cricbook.cricbook.model.Match;
import com.cricbook.cricbook.repository.MatchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;

    // Create match
    public Match createMatch(Match match) {
        if (match.getTeam1().equalsIgnoreCase(match.getTeam2())) {
            throw new IllegalArgumentException("Team1 and Team2 cannot be the same");
        }

        // Check scheduling conflict
        List<Match> conflicts = matchRepository
                .findByLeagueIdAndMatchDateAndTeam1OrTeam2(
                        match.getLeagueId(), 
                        match.getMatchDate(), 
                        match.getTeam1(), 
                        match.getTeam2());

        if(!conflicts.isEmpty()) {
            throw new IllegalArgumentException("One of the teams already has a match on this date in this league");
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

    // Update match
    public Match updateMatch(String id, Match matchDetails) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        // Update fields
        match.setTeam1(matchDetails.getTeam1());
        match.setTeam2(matchDetails.getTeam2());
        match.setLeagueId(matchDetails.getLeagueId());
        match.setMatchDate(matchDetails.getMatchDate());
        match.setVenue(matchDetails.getVenue());
        match.setResult(matchDetails.getResult());
        match.setTossWinner(matchDetails.getTossWinner());
        match.setTossDecision(matchDetails.getTossDecision());
        match.setMatchWinner(matchDetails.getMatchWinner());
        match.setPlayerOfTheMatch(matchDetails.getPlayerOfTheMatch());
        match.setTeam1Runs(matchDetails.getTeam1Runs());
        match.setTeam2Runs(matchDetails.getTeam2Runs());
        match.setTeam1Wickets(matchDetails.getTeam1Wickets());
        match.setTeam2Wickets(matchDetails.getTeam2Wickets());
        match.setTeam1Overs(matchDetails.getTeam1Overs());
        match.setTeam2Overs(matchDetails.getTeam2Overs());
        match.setStatus(matchDetails.getStatus());
        match.setMatchType(matchDetails.getMatchType());

        return matchRepository.save(match);
    }

    // Delete match
    public void deleteMatch(String id) {
        matchRepository.deleteById(id);
    }
}
