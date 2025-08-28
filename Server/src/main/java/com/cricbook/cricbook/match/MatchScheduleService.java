package com.cricbook.cricbook.match;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cricbook.cricbook.league.League;
import com.cricbook.cricbook.league.LeagueRepository;
import com.cricbook.cricbook.security.JwtBlacklistService;
import com.cricbook.cricbook.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchScheduleService {

    private final MatchScheduleRepository repo;
    private final LeagueRepository leagueRepository;
    private final JwtUtil jwtUtil;
    private final JwtBlacklistService blacklistService;

    // ======= CREATE MATCH =======
    public MatchSchedule createMatch(String token, MatchSchedule match) throws Exception {
        String adminId = validateToken(token);

        // Validate league exists
        League league = leagueRepository.findById(match.getLeagueId())
                .orElseThrow(() -> new Exception("League not found"));

        // Validate logged-in user is league admin
        if (!league.getAdminId().equals(adminId))
            throw new Exception("You are not authorized to schedule matches for this league");

        // Validate teams exist in league (league stores "Name:ID")
        boolean team1Exists = league.getTeams().stream()
                .anyMatch(t -> t.startsWith(match.getTeam1() + ":"));
        boolean team2Exists = league.getTeams().stream()
                .anyMatch(t -> t.startsWith(match.getTeam2() + ":"));

        if (!team1Exists) throw new Exception(match.getTeam1() + " is not part of this league!");
        if (!team2Exists) throw new Exception(match.getTeam2() + " is not part of this league!");
        if (match.getTeam1().equals(match.getTeam2()))
            throw new Exception("Team1 and Team2 cannot be the same!");

        // Default status
        if (match.getStatus() == null || match.getStatus().isEmpty())
            match.setStatus("Scheduled");

        return repo.save(match);
    }

    // ======= GET ALL MATCHES =======
    public List<MatchSchedule> getAllMatches() {
        return repo.findAll();
    }

    // ======= GET MATCH BY ID =======
    public MatchSchedule getMatchById(String id) throws Exception {
        return repo.findById(id).orElseThrow(() -> new Exception("Match not found"));
    }

    // ======= UPDATE MATCH =======
    public MatchSchedule updateMatch(String token, String id, MatchSchedule updatedMatch) throws Exception {
        String adminId = validateToken(token);

        MatchSchedule existing = repo.findById(id)
                .orElseThrow(() -> new Exception("Match not found"));

        League league = leagueRepository.findById(existing.getLeagueId())
                .orElseThrow(() -> new Exception("League not found"));

        // Admin verification
        if (!league.getAdminId().equals(adminId))
            throw new Exception("You are not authorized to update matches for this league");

        // Validate teams
        boolean team1Exists = league.getTeams().stream()
                .anyMatch(t -> t.startsWith(updatedMatch.getTeam1() + ":"));
        boolean team2Exists = league.getTeams().stream()
                .anyMatch(t -> t.startsWith(updatedMatch.getTeam2() + ":"));

        if (!team1Exists) throw new Exception(updatedMatch.getTeam1() + " is not part of this league!");
        if (!team2Exists) throw new Exception(updatedMatch.getTeam2() + " is not part of this league!");
        if (updatedMatch.getTeam1().equals(updatedMatch.getTeam2()))
            throw new Exception("Team1 and Team2 cannot be the same!");

        // Update fields safely
        existing.setTeam1(updatedMatch.getTeam1());
        existing.setTeam2(updatedMatch.getTeam2());
        existing.setScheduledDate(updatedMatch.getScheduledDate() != null ? updatedMatch.getScheduledDate() : existing.getScheduledDate());
        existing.setVenue(updatedMatch.getVenue() != null ? updatedMatch.getVenue() : existing.getVenue());
        existing.setStatus(updatedMatch.getStatus() != null ? updatedMatch.getStatus() : existing.getStatus());
        existing.setMatchOvers(updatedMatch.getMatchOvers() != null ? updatedMatch.getMatchOvers() : existing.getMatchOvers());

        return repo.save(existing);
    }

    // ======= DELETE MATCH =======
    public void deleteMatch(String token, String id) throws Exception {
        String adminId = validateToken(token);

        MatchSchedule match = repo.findById(id).orElseThrow(() -> new Exception("Match not found"));
        League league = leagueRepository.findById(match.getLeagueId())
                .orElseThrow(() -> new Exception("League not found"));

        if (!league.getAdminId().equals(adminId))
            throw new Exception("You are not authorized to delete matches for this league");

        repo.delete(match);
    }

    // ======= TOKEN VALIDATION =======
    private String validateToken(String token) throws Exception {
        if (token == null || !token.startsWith("Bearer "))
            throw new Exception("Authorization header missing or invalid");

        String jwt = token.substring(7);

        if (blacklistService.isBlacklisted(jwt))
            throw new Exception("Token is invalid or logged out. Please login again");

        return jwtUtil.extractEmail(jwt); // returns adminId/email
    }
}
