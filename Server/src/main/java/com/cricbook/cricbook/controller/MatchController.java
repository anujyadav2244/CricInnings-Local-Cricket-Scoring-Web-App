package com.cricbook.cricbook.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cricbook.cricbook.model.Match;
import com.cricbook.cricbook.service.MatchService;

@RestController
@RequestMapping("/api/match")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    // Create a match
    @PostMapping("/create")
    public ResponseEntity<Match> createMatch(@RequestBody Match match) {
        // Validate league and teams
        if (match.getTeam1().equalsIgnoreCase(match.getTeam2())) {
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok(matchService.createMatch(match));
    }

    // Get all matches
    @GetMapping("/all")
    public ResponseEntity<List<Match>> getAllMatches() {
        return ResponseEntity.ok(matchService.getAllMatches());
    }

    // Get match by ID
    @GetMapping("id/{id}")
    public ResponseEntity<Match> getMatch(@PathVariable String id) {
        return matchService.getMatchById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Match> updateMatch(
            @PathVariable String id,
            @RequestBody Match matchDetails) {
        Match match = matchService.getMatchById(id)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        // Update all fields
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
        match.setTeam1Score(matchDetails.getTeam1Score());
        match.setTeam2Score(matchDetails.getTeam2Score());
        match.setTeam1Wickets(matchDetails.getTeam1Wickets());
        match.setTeam2Wickets(matchDetails.getTeam2Wickets());
        match.setTeam1Overs(matchDetails.getTeam1Overs());
        match.setTeam2Overs(matchDetails.getTeam2Overs());

        Match updatedMatch = matchService.createMatch(match); // Reuse service
        return ResponseEntity.ok(updatedMatch);
    }

    // Delete match
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<String> deleteMatch(@PathVariable String id) {
        matchService.deleteMatch(id);
        return ResponseEntity.ok("Match deleted successfully");
    }
}
