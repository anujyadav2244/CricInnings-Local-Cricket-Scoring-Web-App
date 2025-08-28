package com.cricbook.cricbook.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cricbook.cricbook.model.League;
import com.cricbook.cricbook.service.LeagueService;

@RestController
@RequestMapping("/api/leagues")
@CrossOrigin(origins = "${app.allowed.origins:http://localhost:5173}", allowCredentials = "true")
public class LeagueController {

    @Autowired
    private LeagueService leagueService;

    @PostMapping("/create")
    public ResponseEntity<?> createLeague(@RequestBody League league) {
        try {
            return ResponseEntity.ok(leagueService.createLeague(league));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateLeague(@PathVariable String id, @RequestBody League league) {
        try {
            return ResponseEntity.ok(leagueService.updateLeague(id, league));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLeague(@PathVariable String id) {
        try {
            leagueService.deleteLeague(id);
            return ResponseEntity.ok(Map.of("message", "League deleted successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLeagueById(@PathVariable String id) {
        return leagueService.getLeagueById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "League not found")));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<?> getLeagueByName(@PathVariable String name) {
        return leagueService.getLeagueByName(name)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "League not found")));
    }

    @GetMapping("/my-leagues")
    public ResponseEntity<?> getMyLeagues() {
        return ResponseEntity.ok(leagueService.getLeaguesByAdmin());
    }

    @GetMapping
    public ResponseEntity<?> getAllLeagues() {
        return ResponseEntity.ok(leagueService.getAllLeagues());
    }
}
