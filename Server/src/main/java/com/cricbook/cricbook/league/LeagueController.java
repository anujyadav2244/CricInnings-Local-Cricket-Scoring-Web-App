package com.cricbook.cricbook.league;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cricbook.cricbook.match.MatchSchedule;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/leagues")
@CrossOrigin(origins = "${app.allowed.origins:http://localhost:5173}", allowCredentials = "true")
public class LeagueController {

    @Autowired
    private LeagueService leagueService;

    @PostMapping(value = "/create", consumes = { "multipart/form-data" })
    public ResponseEntity<?> createLeague(
            @RequestPart("league") String leagueJson,
            @RequestPart(value = "logo", required = false) MultipartFile logoFile,
            @RequestParam(defaultValue = "false") boolean includeEliminator,
            @RequestParam(defaultValue = "true") boolean includeKnockouts) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
            League league = objectMapper.readValue(leagueJson, League.class);

            List<MatchSchedule> matches = leagueService.createLeagueAndScheduleMatches(
                    league, logoFile, includeEliminator, includeKnockouts);

            return ResponseEntity.ok(Map.of(
                    "message", "League created successfully",
                    "leagueId", league.getId(),
                    "matches", matches));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateLeague(
            @PathVariable String id,
            @RequestPart("league") League league,
            @RequestPart(value = "logo", required = false) MultipartFile logoFile) {
        try {
            return ResponseEntity.ok(leagueService.updateLeague(id, league, logoFile));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteLeague(@PathVariable String id) {
        try {
            leagueService.deleteLeague(id);
            return ResponseEntity.ok(Map.of("message", "League and all related matches deleted successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllLeagues() {
        try {
            leagueService.deleteAllLeagues();
            return ResponseEntity
                    .ok(Map.of("message", "All leagues and all related matches deleted successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/id/{id}")
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
