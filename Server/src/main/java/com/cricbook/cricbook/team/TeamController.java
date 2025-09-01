package com.cricbook.cricbook.team;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "${app.allowed.origins:http://localhost:5173}", allowCredentials = "true")
public class TeamController {

    @Autowired
    private TeamService teamService;

    // ======= CREATE TEAM =======
    @PostMapping("/create")
    public ResponseEntity<?> createTeam(
            @RequestHeader("Authorization") String token,
            @RequestBody Team team) {
        try {
            Team savedTeam = teamService.createTeam(token, team);
            return ResponseEntity.ok(savedTeam);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ======= UPDATE TEAM =======
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateTeam(
            @RequestHeader("Authorization") String token,
            @PathVariable String id,
            @RequestBody Team team) {
        try {
            Team updatedTeam = teamService.updateTeam(token, id, team);
            return ResponseEntity.ok(updatedTeam);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ======= DELETE TEAM =======
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteTeam(
            @RequestHeader("Authorization") String token,
            @PathVariable String id) {
        try {
            teamService.deleteTeamById(token, id);
            return ResponseEntity.ok(Map.of("message", "Team deleted successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ======= DELETE ALL TEAMS =======
    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllTeams(@RequestHeader("Authorization") String token) {
        try {
            teamService.deleteAllTeamsByAdmin(token);
            return ResponseEntity.ok(Map.of("message", "All teams of your leagues deleted successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ======= GET TEAM BY ID =======
    @GetMapping("/{id}")
    public ResponseEntity<?> getTeamById(@PathVariable String id) {
        try {
            Team team = teamService.getTeamById(id);
            return ResponseEntity.ok(team);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ======= GET TEAM BY NAME =======
    @GetMapping("/name/{name}")
    public ResponseEntity<?> getTeamByName(@PathVariable String name) {
        try {
            Team team = teamService.getTeamByName(name);
            return ResponseEntity.ok(team);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ======= GET ALL TEAMS =======
    @GetMapping("/get-all")
    public ResponseEntity<List<Team>> getAllTeams() {
        return ResponseEntity.ok(teamService.getAllTeams());
    }
}
