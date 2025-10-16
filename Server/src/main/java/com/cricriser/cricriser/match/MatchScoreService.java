package com.cricriser.cricriser.match;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class MatchScoreService {

    private final MatchScoreRepository repo;

    public MatchScoreService(MatchScoreRepository repo) {
        this.repo = repo;
    }

    public MatchScore createScore(MatchScore score) {
        return repo.save(score);
    }

    public List<MatchScore> getAllScores() {
        return repo.findAll();
    }

    public MatchScore getScoreByMatchId(String matchId) {
        return repo.findById(matchId).orElse(null);
    }

    public MatchScore updateScore(String id, MatchScore updatedScore) {
        if (repo.existsById(id)) {
            updatedScore.setId(id);
            return repo.save(updatedScore);
        }
        return null;
    }

    public void deleteScore(String id) {
        repo.deleteById(id);
    }
}
