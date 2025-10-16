package com.innings.innings.match;

import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scores")
public class MatchScoreController {

    private final MatchScoreService service;

    public MatchScoreController(MatchScoreService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public MatchScore createScore(@RequestBody MatchScore score) {
        return service.createScore(score);
    }

    @GetMapping
    public List<MatchScore> getAllScores() {
        return service.getAllScores();
    }

    @GetMapping("/{matchId}")
    public MatchScore getScoreByMatchId(@PathVariable String matchId) {
        return service.getScoreByMatchId(matchId);
    }

    @PutMapping("/{id}")
    public MatchScore updateScore(@PathVariable String id, @RequestBody MatchScore updatedScore) {
        return service.updateScore(id, updatedScore);
    }

    @DeleteMapping("/{id}")
    public void deleteScore(@PathVariable String id) {
        service.deleteScore(id);
    }
}
