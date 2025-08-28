package com.cricbook.cricbook.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.cricbook.cricbook.model.Player;
import com.cricbook.cricbook.model.Team;
import com.cricbook.cricbook.repository.LeagueRepository;
import com.cricbook.cricbook.repository.TeamRepository;
import com.cricbook.cricbook.security.JwtBlacklistService;
import com.cricbook.cricbook.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final LeagueRepository leagueRepository;
    private final JwtUtil jwtUtil;
    private final JwtBlacklistService blacklistService;

    // ======= CREATE TEAM =======
    public Team createTeam(String token, Team team) throws Exception {
        String adminId = validateToken(token);

        var leagueOpt = leagueRepository.findById(team.getLeagueId());
        if (leagueOpt.isEmpty()) throw new Exception("League not found!");
        if (!leagueOpt.get().getAdminId().equals(adminId))
            throw new Exception("You are not authorized to add team to this league!");

        assignPlayerIds(team);
        validateTeam(team, null);

        return teamRepository.save(team);
    }

    // ======= UPDATE TEAM =======
    public Team updateTeam(String token, String id, Team team) throws Exception {
        String adminId = validateToken(token);

        Team existingTeam = teamRepository.findById(id)
                .orElseThrow(() -> new Exception("Team not found"));

        var leagueOpt = leagueRepository.findById(existingTeam.getLeagueId());
        if (leagueOpt.isEmpty()) throw new Exception("League not found!");
        if (!leagueOpt.get().getAdminId().equals(adminId))
            throw new Exception("You are not authorized to update this team!");

        assignPlayerIds(team); // assign IDs to new players
        validateTeam(team, existingTeam);

        existingTeam.setName(team.getName());
        existingTeam.setCoach(team.getCoach());
        existingTeam.setSquad(team.getSquad());
        existingTeam.setCaptain(team.getCaptain());
        existingTeam.setViceCaptain(team.getViceCaptain());

        return teamRepository.save(existingTeam);
    }

    // ======= DELETE TEAM =======
    public void deleteTeamById(String token, String id) throws Exception {
        String adminId = validateToken(token);

        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new Exception("Team not found"));

        var leagueOpt = leagueRepository.findById(team.getLeagueId());
        if (leagueOpt.isEmpty()) throw new Exception("League not found!");
        if (!leagueOpt.get().getAdminId().equals(adminId))
            throw new Exception("You are not authorized to delete this team!");

        teamRepository.delete(team);
    }

    // ======= GET TEAM =======
    public Team getTeamById(String id) throws Exception {
        return teamRepository.findById(id)
                .orElseThrow(() -> new Exception("Team not found"));
    }

    public Team getTeamByName(String name) throws Exception {
        Team team = teamRepository.findByName(name);
        if (team == null) throw new Exception("Team not found");
        return team;
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    // ======= VALIDATION LOGIC =======
    private void validateTeam(Team team, Team existingTeam) throws Exception {
        // Duplicate name check
        Team duplicate = teamRepository.findByName(team.getName());
        if (duplicate != null && (existingTeam == null || !duplicate.getId().equals(existingTeam.getId())))
            throw new Exception("Another team with this name already exists!");

        // League must exist
        if (team.getLeagueId() == null || leagueRepository.findById(team.getLeagueId()).isEmpty())
            throw new Exception("Team must belong to a valid league!");

        // Squad validations
        if (team.getSquad() == null || team.getSquad().size() < 15)
            throw new Exception("Squad must have at least 15 players!");

        // Coach cannot be in squad
        boolean coachInSquad = team.getSquad().stream()
                .anyMatch(p -> p.getName().trim().equalsIgnoreCase(team.getCoach().trim()));
        if (coachInSquad) throw new Exception("Coach cannot be part of the squad!");

        // Captain & Vice-Captain validations
        if (team.getCaptain() == null || team.getViceCaptain() == null)
            throw new Exception("Captain and Vice-Captain must be assigned!");

        boolean captainInSquad = team.getSquad().stream()
                .anyMatch(p -> p.getId().equals(team.getCaptain().getId()));
        boolean viceCaptainInSquad = team.getSquad().stream()
                .anyMatch(p -> p.getId().equals(team.getViceCaptain().getId()));

        if (!captainInSquad) throw new Exception("Captain must be in the squad!");
        if (!viceCaptainInSquad) throw new Exception("Vice-Captain must be in the squad!");
        if (team.getCaptain().getId().equals(team.getViceCaptain().getId()))
            throw new Exception("Captain and Vice-Captain must be different!");
    }

    // ======= ASSIGN PLAYER IDS =======
    private void assignPlayerIds(Team team) throws Exception {
        // Assign ID to each player if not present
        for (Player p : team.getSquad()) {
            if (p.getId() == null || p.getId().isEmpty()) {
                p.setId(UUID.randomUUID().toString());
            }
        }

        // Map captain and vice-captain from squad by name
        boolean captainFound = false;
        boolean viceCaptainFound = false;

        for (Player p : team.getSquad()) {
            if (p.getName().trim().equalsIgnoreCase(team.getCaptain().getName().trim())) {
                team.setCaptain(p);
                captainFound = true;
            }
            if (p.getName().trim().equalsIgnoreCase(team.getViceCaptain().getName().trim())) {
                team.setViceCaptain(p);
                viceCaptainFound = true;
            }
        }

        if (!captainFound) throw new Exception("Captain must be part of the squad!");
        if (!viceCaptainFound) throw new Exception("Vice-Captain must be part of the squad!");
    }

    // ======= VALIDATE TOKEN =======
    
}
