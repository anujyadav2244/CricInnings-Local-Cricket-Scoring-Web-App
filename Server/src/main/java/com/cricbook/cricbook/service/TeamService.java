package com.cricbook.cricbook.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cricbook.cricbook.model.Team;
import com.cricbook.cricbook.repository.TeamRepository;
import com.cricbook.cricbook.repository.LeagueRepository;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final LeagueRepository leagueRepository;

    public TeamService(TeamRepository teamRepository, LeagueRepository leagueRepository) {
        this.teamRepository = teamRepository;
        this.leagueRepository = leagueRepository;
    }

    // Create a new team
    public Team createTeam(Team team) throws Exception {
        if (teamRepository.findByName(team.getName()) != null) {
            throw new Exception("Team name already exists!");
        }

        // Check league
        if (team.getLeagueId() == null || leagueRepository.findById(team.getLeagueId()).isEmpty()) {
            throw new Exception("Team must belong to a valid league!");
        }

        // Squad size
        if (team.getSquad() == null || team.getSquad().size() < 15) {
            throw new Exception("Squad must have at least 15 players!");
        }

        // Coach cannot be in squad
        boolean coachInSquad = team.getSquad().stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(team.getCoach()));
        if (coachInSquad) throw new Exception("Coach cannot be part of the squad!");

        // Captain & Vice-Captain in squad
        boolean captainExists = team.getSquad().stream()
                .anyMatch(p -> p.getName().equals(team.getCaptain().getName()));
        if (!captainExists) throw new Exception("Captain must be part of the squad!");

        boolean viceCaptainExists = team.getSquad().stream()
                .anyMatch(p -> p.getName().equals(team.getViceCaptain().getName()));
        if (!viceCaptainExists) throw new Exception("Vice-Captain must be part of the squad!");

        // Captain and Vice-Captain must be different
        if (team.getCaptain().getName().equals(team.getViceCaptain().getName())) {
            throw new Exception("Captain and Vice-Captain must be different players!");
        }

        return teamRepository.save(team);
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team getTeamById(String id) {
        return teamRepository.findById(id).orElse(null);
    }

    public Team getTeamByName(String name) {
        return teamRepository.findByName(name);
    }

    // Update by ID
    public String updateTeamById(String id, Team team) throws Exception {
        Team existingTeam = teamRepository.findById(id)
                .orElseThrow(() -> new Exception("Team not found"));

        // Check duplicate name
        Team teamWithSameName = teamRepository.findByName(team.getName());
        if (teamWithSameName != null && !teamWithSameName.getId().equals(id)) {
            throw new Exception("Another team with this name already exists!");
        }

        // Check league
        if (team.getLeagueId() == null || leagueRepository.findById(team.getLeagueId()).isEmpty()) {
            throw new Exception("Team must belong to a valid league!");
        }

        // Squad validations
        if (team.getSquad() == null || team.getSquad().size() < 15) {
            throw new Exception("Squad must have at least 15 players!");
        }

        // Coach cannot be in squad
        boolean coachInSquad = team.getSquad().stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(team.getCoach()));
        if (coachInSquad) throw new Exception("Coach cannot be part of the squad!");

        // Captain & Vice-Captain validations
        if (team.getCaptain() == null || !team.getSquad().contains(team.getCaptain())) {
            throw new Exception("Captain must be part of the squad!");
        }

        if (team.getViceCaptain() == null || !team.getSquad().contains(team.getViceCaptain())) {
            throw new Exception("Vice-Captain must be part of the squad!");
        }

        if (team.getCaptain().getName().equals(team.getViceCaptain().getName())) {
            throw new Exception("Captain and Vice-Captain must be different players!");
        }

        // Check if any field changed
        if (existingTeam.getName().equals(team.getName()) &&
            existingTeam.getCoach().equals(team.getCoach()) &&
            existingTeam.getSquad().equals(team.getSquad()) &&
            existingTeam.getCaptain().equals(team.getCaptain()) &&
            existingTeam.getViceCaptain().equals(team.getViceCaptain()) &&
            existingTeam.getLeagueId().equals(team.getLeagueId())) {
            throw new Exception("Team is already up to date");
        }

        // Update fields
        existingTeam.setName(team.getName());
        existingTeam.setCoach(team.getCoach());
        existingTeam.setSquad(team.getSquad());
        existingTeam.setCaptain(team.getCaptain());
        existingTeam.setViceCaptain(team.getViceCaptain());
        existingTeam.setLeagueId(team.getLeagueId());

        teamRepository.save(existingTeam);
        return "Team updated successfully";
    }

    // Update by Name
    public String updateTeamByName(String name, Team team) throws Exception {
        Team existingTeam = teamRepository.findByName(name);
        if (existingTeam == null) {
            throw new Exception("Team not found");
        }

        Team teamWithSameName = teamRepository.findByName(team.getName());
        if (teamWithSameName != null && !teamWithSameName.getId().equals(existingTeam.getId())) {
            throw new Exception("Another team with this name already exists!");
        }

        // Check league
        if (team.getLeagueId() == null || leagueRepository.findById(team.getLeagueId()).isEmpty()) {
            throw new Exception("Team must belong to a valid league!");
        }

        // Squad validations
        if (team.getSquad() == null || team.getSquad().size() < 15) {
            throw new Exception("Squad must contain at least 15 players!");
        }

        boolean coachInSquad = team.getSquad().stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(team.getCoach()));
        if (coachInSquad) throw new Exception("Coach cannot be part of the squad!");

        if (team.getCaptain() == null || !team.getSquad().contains(team.getCaptain())) {
            throw new Exception("Captain must be part of the squad!");
        }

        if (team.getViceCaptain() == null || !team.getSquad().contains(team.getViceCaptain())) {
            throw new Exception("Vice-Captain must be part of the squad!");
        }

        if (team.getCaptain().getName().equals(team.getViceCaptain().getName())) {
            throw new Exception("Captain and Vice-Captain must be different players!");
        }

        // Check if any field changed
        if (existingTeam.getName().equals(team.getName()) &&
            existingTeam.getCoach().equals(team.getCoach()) &&
            existingTeam.getSquad().equals(team.getSquad()) &&
            existingTeam.getCaptain().equals(team.getCaptain()) &&
            existingTeam.getViceCaptain().equals(team.getViceCaptain()) &&
            existingTeam.getLeagueId().equals(team.getLeagueId())) {
            throw new Exception("Team is already up to date");
        }

        existingTeam.setName(team.getName());
        existingTeam.setCoach(team.getCoach());
        existingTeam.setSquad(team.getSquad());
        existingTeam.setCaptain(team.getCaptain());
        existingTeam.setViceCaptain(team.getViceCaptain());
        existingTeam.setLeagueId(team.getLeagueId());

        teamRepository.save(existingTeam);
        return "Team updated successfully";
    }

    // Delete by ID
    public void deleteTeamById(String id) throws Exception {
        if (!teamRepository.existsById(id)) {
            throw new Exception("Team not found");
        }
        teamRepository.deleteById(id);
    }

    // Delete by Name
    public void deleteTeamByName(String name) throws Exception {
        Team existingTeam = teamRepository.findByName(name);
        if (existingTeam == null) {
            throw new Exception("Team not found");
        }
        teamRepository.delete(existingTeam);
    }
}
