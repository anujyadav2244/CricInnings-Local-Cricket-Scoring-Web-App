package com.cricbook.cricbook.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cricbook.cricbook.model.League;
import com.cricbook.cricbook.model.Team;
import com.cricbook.cricbook.repository.LeagueRepository;
import com.cricbook.cricbook.repository.TeamRepository;

@Service
public class LeagueService {

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private TeamRepository teamRepository;

    // ================== HELPER ==================
    private String getLoggedInAdminId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null || auth.getPrincipal().toString().equals("anonymousUser")) {
            throw new RuntimeException("Unauthorized! Please log in.");
        }
        return auth.getPrincipal().toString();
    }

    // ================= CREATE =================
    public League createLeague(League league) {
        String adminId = getLoggedInAdminId();

        // Check duplicate league
        if (leagueRepository.findByName(league.getName()).isPresent()) {
            throw new RuntimeException("League name already exists!");
        }

        league.setAdminId(adminId);

        if (league.getTeams() == null || league.getTeams().isEmpty()) {
            throw new RuntimeException("Teams cannot be null or empty!");
        }
        if (league.getNoOfTeams() != league.getTeams().size()) {
            throw new RuntimeException("noOfTeams must match the size of teams list!");
        }

        // Create teams if not exists and store as "Name:Id"
        List<String> teamInfo = new ArrayList<>();
        for (String teamName : league.getTeams()) {
            Team team = teamRepository.findByName(teamName);
            if (team == null) {
                team = new Team();
                team.setName(teamName);
                teamRepository.save(team);
            }
            teamInfo.add(team.getName() + ":" + team.getId());
        }
        league.setTeams(teamInfo);

        return leagueRepository.save(league);
    }

    // ================= UPDATE =================
    public League updateLeague(String leagueId, League updatedLeague) {
        String adminId = getLoggedInAdminId();

        return leagueRepository.findById(leagueId)
                .map(existingLeague -> {
                    if (!existingLeague.getAdminId().equals(adminId)) {
                        throw new RuntimeException("This league does not belong to you!");
                    }

                    if (updatedLeague.getName() != null && !updatedLeague.getName().isEmpty()) {
                        leagueRepository.findByName(updatedLeague.getName())
                                .filter(l -> !l.getId().equals(leagueId))
                                .ifPresent(l -> {
                                    throw new RuntimeException("Another league with this name already exists!");
                                });
                        existingLeague.setName(updatedLeague.getName());
                    }

                    if (updatedLeague.getNoOfTeams() > 0)
                        existingLeague.setNoOfTeams(updatedLeague.getNoOfTeams());

                    if (updatedLeague.getTeams() != null && !updatedLeague.getTeams().isEmpty()) {
                        List<String> teamInfo = new ArrayList<>();
                        for (String teamName : updatedLeague.getTeams()) {
                            Team team = teamRepository.findByName(teamName);
                            if (team == null) {
                                team = new Team();
                                team.setName(teamName);
                                teamRepository.save(team);
                            }
                            teamInfo.add(team.getName() + ":" + team.getId());
                        }
                        existingLeague.setTeams(teamInfo);
                    }

                    if (updatedLeague.getNoOfMatches() > 0)
                        existingLeague.setNoOfMatches(updatedLeague.getNoOfMatches());
                    if (updatedLeague.getNoOfOvers() > 0)
                        existingLeague.setNoOfOvers(updatedLeague.getNoOfOvers());
                    if (updatedLeague.getStartDate() != null)
                        existingLeague.setStartDate(updatedLeague.getStartDate());
                    if (updatedLeague.getEndDate() != null)
                        existingLeague.setEndDate(updatedLeague.getEndDate());
                    if (updatedLeague.getVenue() != null && !updatedLeague.getVenue().isEmpty())
                        existingLeague.setVenue(updatedLeague.getVenue());

                    return leagueRepository.save(existingLeague);
                })
                .orElseThrow(() -> new RuntimeException("League not found with ID: " + leagueId));
    }

    // ================= DELETE =================
    public void deleteLeague(String leagueId) {
        String adminId = getLoggedInAdminId();

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new RuntimeException("League not found with ID: " + leagueId));

        if (!league.getAdminId().equals(adminId)) {
            throw new RuntimeException("This league does not belong to you!");
        }

        leagueRepository.delete(league);
    }

    // ================= READ =================
    public Optional<League> getLeagueById(String leagueId) {
        getLoggedInAdminId(); // enforce login
        return leagueRepository.findById(leagueId);
    }

    public List<League> getAllLeagues() {
        getLoggedInAdminId(); // enforce login
        return leagueRepository.findAll();
    }

    public Optional<League> getLeagueByName(String name) {
        getLoggedInAdminId(); // enforce login
        return leagueRepository.findByName(name);
    }

    public List<League> getLeaguesByAdmin() {
        String adminId = getLoggedInAdminId();
        return leagueRepository.findAll().stream()
                .filter(league -> league.getAdminId().equals(adminId))
                .toList();
    }
}
