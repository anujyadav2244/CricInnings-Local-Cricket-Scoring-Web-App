package com.cricbook.cricbook.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cricbook.cricbook.model.Team;
import com.cricbook.cricbook.service.TeamService;

@RestController
@RequestMapping("/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    // Create team
    @PostMapping("/create")
    public ResponseEntity<?> createTeam(@RequestBody Team team) {
        try {
            Team savedTeam = teamService.createTeam(team);
            return ResponseEntity.ok(savedTeam);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // Get all teams
    @GetMapping("/all")
    public List<Team> getAllTeams() {
        return teamService.getAllTeams();
    }

    // Get team by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getTeamById(@PathVariable String id) {
        Team team = teamService.getTeamById(id);
        if (team != null) {
            return ResponseEntity.ok(team);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Team not found");
        }
    }

    // Get team by name
    @GetMapping("/name/{name}")
    public ResponseEntity<?> getTeamByName(@PathVariable String name) {
        Team team = teamService.getTeamByName(name);
        if (team != null) {
            return ResponseEntity.ok(team);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Team not found");
        }
    }

    // Update team by ID
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateTeamById(@PathVariable String id, @RequestBody Team team) {
        try {
            String message = teamService.updateTeamById(id, team); // Returns only message now
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Update team by name
    @PutMapping("/update/name/{name}")
    public ResponseEntity<?> updateTeamByName(@PathVariable String name, @RequestBody Team team) {
        try {
            String message = teamService.updateTeamByName(name, team); // Returns only message now
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Delete team by ID
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteTeamById(@PathVariable String id) {
        try {
            teamService.deleteTeamById(id);
            return ResponseEntity.ok("Team deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Delete team by name
    @DeleteMapping("/delete/name/{name}")
    public ResponseEntity<?> deleteTeamByName(@PathVariable String name) {
        try {
            teamService.deleteTeamByName(name);
            return ResponseEntity.ok("Team deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
