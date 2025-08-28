package com.cricbook.cricbook.match;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/match")
@CrossOrigin(origins = "${app.allowed.origins:http://localhost:5173}", allowCredentials = "true")
public class MatchScheduleController {

    private final MatchScheduleService service;

    public MatchScheduleController(MatchScheduleService service) {
        this.service = service;
    }

    // ======= CREATE MATCH =======
    @PostMapping("/create")
    public ResponseEntity<?> createMatch(
            @RequestHeader("Authorization") String token,
            @RequestBody MatchSchedule match) {
        try {
            MatchSchedule saved = service.createMatch(token, match);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ======= GET ALL MATCHES =======
    @GetMapping("/get-all")
    public ResponseEntity<List<MatchSchedule>> getAllMatches() {
        return ResponseEntity.ok(service.getAllMatches());
    }

    // ======= GET MATCH BY ID =======
    @GetMapping("/{id}")
    public ResponseEntity<?> getMatchById(@PathVariable String id) {
        try {
            MatchSchedule match = service.getMatchById(id);
            return ResponseEntity.ok(match);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ======= UPDATE MATCH =======
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateMatch(
            @RequestHeader("Authorization") String token,
            @PathVariable String id,
            @RequestBody MatchSchedule updatedMatch) {
        try {
            MatchSchedule match = service.updateMatch(token, id, updatedMatch);
            return ResponseEntity.ok(match);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ======= DELETE MATCH =======
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMatch(
            @RequestHeader("Authorization") String token,
            @PathVariable String id) {
        try {
            service.deleteMatch(token, id);
            return ResponseEntity.ok(Map.of("message", "Match deleted successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
