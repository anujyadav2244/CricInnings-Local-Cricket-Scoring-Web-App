package com.cricbook.cricbook.league;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cricbook.cricbook.match.MatchSchedule;

@RestController
@RequestMapping("/api/leagues")
@CrossOrigin(origins = "${app.allowed.origins:http://localhost:5173}", allowCredentials = "true")
public class LeagueController {

    @Autowired
    private LeagueService leagueService;

    // -------- Create League (with league stage matches + knockout stage: semifinals/final/eliminator) --------
    @PostMapping("/create")
    public ResponseEntity<?> createLeague(
            @RequestBody League league,
            @RequestParam(defaultValue = "false") boolean includeEliminator,
            @RequestParam(defaultValue = "true") boolean includeKnockouts) {
        try {
            List<MatchSchedule> matches = leagueService.createLeagueAndScheduleMatches(
                    league, includeEliminator, includeKnockouts);
            return ResponseEntity.ok(matches);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // -------- Update League --------
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateLeague(@PathVariable String id, @RequestBody League league) {
        try {
            return ResponseEntity.ok(leagueService.updateLeague(id, league));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // -------- Delete Single League (with teams & matches) --------
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteLeague(@PathVariable String id) {
        try {
            leagueService.deleteLeague(id);
            return ResponseEntity.ok(Map.of("message", "League and all related teams & matches deleted successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // -------- Delete All Leagues of Admin (with all teams & matches) --------
    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllLeagues() {
        try {
            leagueService.deleteAllLeagues();
            return ResponseEntity.ok(Map.of("message", "All leagues and all related teams & matches deleted successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // -------- Get League by ID --------
    @GetMapping("/{id}")
    public ResponseEntity<?> getLeagueById(@PathVariable String id) {
        return leagueService.getLeagueById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "League not found")));
    }

    // -------- Get League by Name --------
    @GetMapping("/name/{name}")
    public ResponseEntity<?> getLeagueByName(@PathVariable String name) {
        return leagueService.getLeagueByName(name)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "League not found")));
    }

    // -------- Get My Leagues (Admin) --------
    @GetMapping("/my-leagues")
    public ResponseEntity<?> getMyLeagues() {
        return ResponseEntity.ok(leagueService.getLeaguesByAdmin());
    }

    // -------- Get All Leagues --------
    @GetMapping
    public ResponseEntity<?> getAllLeagues() {
        return ResponseEntity.ok(leagueService.getAllLeagues());
    }
}
