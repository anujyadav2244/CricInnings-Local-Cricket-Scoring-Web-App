package com.cricbook.cricbook.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cricbook.cricbook.model.League;
import com.cricbook.cricbook.repository.LeagueRepository;

@RestController
@RequestMapping("/api/leagues")
@CrossOrigin(origins = "${app.allowed.origins:http://localhost:5173}", allowCredentials = "true")
public class LeagueController {

    @Autowired
    private LeagueRepository leagueRepository;

    // ✅ Create League
    @PostMapping("/create")
    public ResponseEntity<?> createLeague(@RequestBody League league) {
        try {
            // Check for duplicate league name
            if (leagueRepository.findByName(league.getName()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "League name already exists!"));
            }

            // Check number of teams
            if (league.getNoOfTeams() != league.getTeams().size()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "noOfTeams must match the size of teams list!"));
            }

            League savedLeague = leagueRepository.save(league);
            return ResponseEntity.ok(savedLeague);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error while creating league."));
        }
    }

    // Get League by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getLeagueById(@PathVariable String id) {
        return leagueRepository.findById(id)
                .map(league -> ResponseEntity.ok().body(league))
                .orElse(ResponseEntity.notFound().build());
    }

    // Get League by Name
    @GetMapping("/name/{name}")
    public ResponseEntity<?> getLeagueByName(@PathVariable String name) {
        return leagueRepository.findByName(name)
                .map(league -> ResponseEntity.ok().body(league))
                .orElse(ResponseEntity.notFound().build());
    }

    // Get All Leagues
    @GetMapping
    public ResponseEntity<List<League>> getAllLeagues() {
        return ResponseEntity.ok(leagueRepository.findAll());
    }

    // Delete League
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLeagueById(@PathVariable String id) {
        leagueRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "League deleted successfully!"));
    }

    // Delete League
    @DeleteMapping("/name/{name}")
    public ResponseEntity<?> deleteLeagueByName(@PathVariable String name) {
        leagueRepository.findByName(name).ifPresent(league -> leagueRepository.delete(league));
        return ResponseEntity.ok(Map.of("message", "League deleted successfully!"));
    }
}
