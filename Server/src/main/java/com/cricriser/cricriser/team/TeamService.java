package com.cricriser.cricriser.team;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cricriser.cricriser.cloudinary.CloudinaryService;
import com.cricriser.cricriser.league.League;
import com.cricriser.cricriser.league.LeagueRepository;
import com.cricriser.cricriser.model.Player;
import com.cricriser.cricriser.security.JwtBlacklistService;
import com.cricriser.cricriser.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final LeagueRepository leagueRepository;
    private final JwtUtil jwtUtil;
    private final JwtBlacklistService blacklistService;
    private final CloudinaryService cloudinaryService;
    private final ObjectMapper objectMapper;

    // ======= CREATE TEAM =======
    public Team createTeam(String token, String teamJson, MultipartFile logoFile) throws Exception {
        String adminId = validateToken(token);

        Team team = objectMapper.readValue(teamJson, Team.class);

        var leagueOpt = leagueRepository.findById(team.getLeagueId());
        if (leagueOpt.isEmpty())
            throw new Exception("League not found!");
        if (!leagueOpt.get().getAdminId().equals(adminId))
            throw new Exception("You are not authorized to add team to this league!");

        assignPlayerIds(team);
        validateTeam(team, null);

        // Upload team logo if provided
        if (logoFile != null && !logoFile.isEmpty()) {
            // Check if this logo is already used
            List<Team> allTeams = teamRepository.findAll();
            for (Team t : allTeams) {
                if (t.getLogoUrl() != null && t.getLogoUrl().contains(logoFile.getOriginalFilename())) {
                    throw new Exception("This logo is already assigned to another team!");
                }
            }

            String logoUrl = cloudinaryService.uploadFile(logoFile, "team_logos");
            team.setLogoUrl(logoUrl);
        }

        return teamRepository.save(team);
    }

    // ======= UPDATE TEAM =======
    public Team updateTeam(String token, String id, String teamJson, MultipartFile logoFile) throws Exception {
        String adminId = validateToken(token);

        Team existingTeam = teamRepository.findById(id)
                .orElseThrow(() -> new Exception("Team not found"));

        var leagueOpt = leagueRepository.findById(existingTeam.getLeagueId());
        if (leagueOpt.isEmpty())
            throw new Exception("League not found!");
        if (!leagueOpt.get().getAdminId().equals(adminId))
            throw new Exception("You are not authorized to update this team!");

        Team team = objectMapper.readValue(teamJson, Team.class);

        assignPlayerIds(team);
        validateTeam(team, existingTeam);

        existingTeam.setName(team.getName());
        existingTeam.setCoach(team.getCoach());
        existingTeam.setSquad(team.getSquad());
        existingTeam.setCaptain(team.getCaptain());
        existingTeam.setViceCaptain(team.getViceCaptain());

        // Update logo if new one provided
        if (logoFile != null && !logoFile.isEmpty()) {
            // Check if this logo is already used by another team
            List<Team> allTeams = teamRepository.findAll();
            for (Team t : allTeams) {
                if (!t.getId().equals(existingTeam.getId()) &&
                        t.getLogoUrl() != null &&
                        t.getLogoUrl().contains(logoFile.getOriginalFilename())) {
                    throw new Exception("This logo is already assigned to another team!");
                }
            }

            // Delete old logo if exists
            if (existingTeam.getLogoUrl() != null && !existingTeam.getLogoUrl().isEmpty()) {
                cloudinaryService.deleteFile(existingTeam.getLogoUrl());
            }

            String logoUrl = cloudinaryService.uploadFile(logoFile, "team_logos");
            existingTeam.setLogoUrl(logoUrl);
        }

        return teamRepository.save(existingTeam);
    }

    // ======= DELETE TEAM BY ID =======
    public void deleteTeamById(String token, String id) throws Exception {
        String adminId = validateToken(token);

        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new Exception("Team not found"));

        League league = leagueRepository.findById(team.getLeagueId())
                .orElseThrow(() -> new Exception("League not found!"));

        if (!league.getAdminId().equals(adminId))
            throw new Exception("You are not authorized to delete this team!");

        // Delete logo from Cloudinary if exists
        if (team.getLogoUrl() != null && !team.getLogoUrl().isEmpty()) {
            cloudinaryService.deleteFile(team.getLogoUrl());
        }

        // Remove team from league's teams array safely
        List<String> leagueTeams = league.getTeams();
        if (leagueTeams != null) {
            leagueTeams.removeIf(t -> {
                String[] parts = t.split(":");
                return parts.length > 1 && parts[1].equals(team.getId());
            });
            league.setTeams(leagueTeams);
            leagueRepository.save(league);
        }

        // Delete team from repository
        teamRepository.delete(team);
    }

    // ======= DELETE ALL TEAMS FOR ADMIN =======
    public void deleteAllTeamsByAdmin(String token) throws Exception {
        String adminId = validateToken(token);

        List<League> adminLeagues = leagueRepository.findByAdminId(adminId);

        for (League league : adminLeagues) {
            List<Team> teams = teamRepository.findByLeagueId(league.getId());

            // Delete each team's logo safely
            for (Team team : teams) {
                if (team.getLogoUrl() != null && !team.getLogoUrl().isEmpty()) {
                    cloudinaryService.deleteFile(team.getLogoUrl());
                }
            }

            // Clear league's teams array safely
            if (league.getTeams() != null) {
                league.setTeams(List.of());
                leagueRepository.save(league);
            }

            // Delete all teams from repository
            if (teams != null && !teams.isEmpty()) {
                teamRepository.deleteAll(teams);
            }
        }
    }

    // ======= GET TEAM =======
    public Team getTeamById(String id) throws Exception {
        return teamRepository.findById(id)
                .orElseThrow(() -> new Exception("Team not found"));
    }

    public Team getTeamByName(String name) throws Exception {
        Team team = teamRepository.findByName(name);
        if (team == null)
            throw new Exception("Team not found");
        return team;
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    // ======= VALIDATION LOGIC =======
    private void validateTeam(Team team, Team existingTeam) throws Exception {
        Team duplicate = teamRepository.findByName(team.getName());
        if (duplicate != null && (existingTeam == null || !duplicate.getId().equals(existingTeam.getId())))
            throw new Exception("Another team with this name already exists!");

        if (team.getLeagueId() == null || leagueRepository.findById(team.getLeagueId()).isEmpty())
            throw new Exception("Team must belong to a valid league!");

        if (team.getSquad() == null || team.getSquad().size() < 15)
            throw new Exception("Squad must have at least 15 players!");

        boolean coachInSquad = team.getSquad().stream()
                .anyMatch(p -> p.getName().trim().equalsIgnoreCase(team.getCoach().trim()));
        if (coachInSquad)
            throw new Exception("Coach cannot be part of the squad!");

        if (team.getCaptain() == null || team.getViceCaptain() == null)
            throw new Exception("Captain and Vice-Captain must be assigned!");

        boolean captainInSquad = team.getSquad().stream()
                .anyMatch(p -> p.getName().trim().equalsIgnoreCase(team.getCaptain().trim()));
        boolean viceCaptainInSquad = team.getSquad().stream()
                .anyMatch(p -> p.getName().trim().equalsIgnoreCase(team.getViceCaptain().trim()));

        if (!captainInSquad)
            throw new Exception("Captain must be part of the squad!");
        if (!viceCaptainInSquad)
            throw new Exception("Vice-Captain must be part of the squad!");
        if (team.getCaptain().equalsIgnoreCase(team.getViceCaptain()))
            throw new Exception("Captain and Vice-Captain must be different!");
    }

    // ======= ASSIGN PLAYER IDS =======
    private void assignPlayerIds(Team team) {
        for (Player p : team.getSquad()) {
            if (p.getId() == null || p.getId().isEmpty()) {
                p.setId(UUID.randomUUID().toString());
            }
        }
    }

    // ======= VALIDATE TOKEN =======
    private String validateToken(String token) throws Exception {
        if (token == null || !token.startsWith("Bearer "))
            throw new Exception("Authorization header missing or invalid!");

        String jwt = token.substring(7);
        if (blacklistService.isBlacklisted(jwt))
            throw new Exception("Token is invalid or logged out. Please login again!");

        return jwtUtil.extractEmail(jwt); // adminId/email
    }
}
