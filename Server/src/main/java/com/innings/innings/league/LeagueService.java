package com.innings.innings.league;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.innings.innings.cloudinary.CloudinaryService;
import com.innings.innings.match.MatchSchedule;
import com.innings.innings.match.MatchScheduleRepository;
import com.innings.innings.team.Team;
import com.innings.innings.team.TeamRepository;

@Service
public class LeagueService {

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchScheduleRepository matchRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    private String getLoggedInAdminId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null || auth.getPrincipal().toString().equals("anonymousUser")) {
            throw new RuntimeException("Unauthorized! Please log in.");
        }
        return auth.getPrincipal().toString();
    }

    public List<MatchSchedule> createLeagueAndScheduleMatches(
            League league, MultipartFile logoFile,
            boolean includeEliminator, boolean includeKnockouts) {

        String adminId = getLoggedInAdminId();

        if (leagueRepository.existsByName(league.getName())) {
            throw new RuntimeException("League name already exists!");
        }

        if (league.getTeams() == null || league.getTeams().isEmpty()) {
            throw new RuntimeException("Teams cannot be null or empty!");
        }

        if (league.getNoOfTeams() != league.getTeams().size()) {
            throw new RuntimeException("noOfTeams must match the size of teams list!");
        }

        league.setAdminId(adminId);

        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                String logoUrl = cloudinaryService.uploadFile(logoFile, "leagues");
                league.setLogoUrl(logoUrl);
            } catch (Exception e) {
                throw new RuntimeException("League logo upload failed: " + e.getMessage());
            }
        }

        League savedLeague = leagueRepository.save(league);

        List<MatchSchedule> matches = switch (league.getLeagueFormatType()) {
            case "SINGLE_ROUND_ROBIN" -> generateRoundRobinMatches(savedLeague, false);
            case "DOUBLE_ROUND_ROBIN" -> generateRoundRobinMatches(savedLeague, true);
            case "GROUP" -> generateGroupFormatMatches(savedLeague);
            default -> throw new RuntimeException(
                    "Invalid leagueFormatType! Choose SINGLE ROUND ROBIN, DOUBLE ROUND ROBIN, or GROUP.");
        };

        int lastMatchNo = matches.isEmpty() ? 1 : matches.get(matches.size() - 1).getMatchNo() + 1;

        if (includeKnockouts) {
            matches.addAll(generateKnockoutMatches(savedLeague, includeEliminator, lastMatchNo));
        }

        assignMatchDates(savedLeague, matches);
        assignMatchNumbersAndTypes(matches, league.getLeagueFormatType(), includeEliminator, includeKnockouts);
        matchRepository.saveAll(matches);

        savedLeague.setNoOfMatches(matches.size());
        leagueRepository.save(savedLeague);

        return matches;
    }

    public League updateLeague(String leagueId, League updatedLeague, MultipartFile logoFile) {
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
                        existingLeague.setTeams(updatedLeague.getTeams());
                    }

                    if (updatedLeague.getNoOfMatches() > 0)
                        existingLeague.setNoOfMatches(updatedLeague.getNoOfMatches());

                    if (updatedLeague.getStartDate() != null)
                        existingLeague.setStartDate(updatedLeague.getStartDate());

                    if (updatedLeague.getEndDate() != null)
                        existingLeague.setEndDate(updatedLeague.getEndDate());

                    if (updatedLeague.getVenue() != null && !updatedLeague.getVenue().isEmpty())
                        existingLeague.setVenue(updatedLeague.getVenue());

                    if (updatedLeague.getLeagueFormat() != null && !updatedLeague.getLeagueFormat().isEmpty())
                        existingLeague.setLeagueFormat(updatedLeague.getLeagueFormat());

                    if (updatedLeague.getUmpires() != null && !updatedLeague.getUmpires().isEmpty())
                        existingLeague.setUmpires(updatedLeague.getUmpires());

                    if (logoFile != null && !logoFile.isEmpty()) {
                        try {
                            String logoUrl = cloudinaryService.uploadFile(logoFile, "leagues");
                            existingLeague.setLogoUrl(logoUrl);
                        } catch (Exception e) {
                            throw new RuntimeException("League logo upload failed: " + e.getMessage());
                        }
                    }

                    return leagueRepository.save(existingLeague);
                })
                .orElseThrow(() -> new RuntimeException("League not found with ID: " + leagueId));
    }

    private List<MatchSchedule> generateRoundRobinMatches(League league, boolean doubleRound) {
        List<String> teamNames = new ArrayList<>();
        for (String t : league.getTeams())
            teamNames.add(t.split(":")[0]); // extract only team name

        List<MatchSchedule> matches = new ArrayList<>();
        int n = teamNames.size();

        // If odd, add dummy team "BYE"
        if (n % 2 != 0) {
            teamNames.add("BYE");
            n++;
        }

        int totalRounds = n - 1;
        int matchesPerRound = n / 2;
        int matchCounter = 1;

        for (int round = 0; round < totalRounds; round++) {
            for (int i = 0; i < matchesPerRound; i++) {
                String team1 = teamNames.get(i);
                String team2 = teamNames.get(n - 1 - i);

                if (!team1.equals("BYE") && !team2.equals("BYE")) {
                    MatchSchedule match = new MatchSchedule();
                    match.setLeagueId(league.getId());
                    match.setTeam1(team1);
                    match.setTeam2(team2);
                    match.setStatus("Scheduled");
                    match.setMatchType("LEAGUE");
                    match.setMatchNo(matchCounter++);
                    matches.add(match);

                    // For double round robin, reverse home/away
                    if (doubleRound) {
                        MatchSchedule reverse = new MatchSchedule();
                        reverse.setLeagueId(league.getId());
                        reverse.setTeam1(team2);
                        reverse.setTeam2(team1);
                        reverse.setStatus("Scheduled");
                        reverse.setMatchType("LEAGUE");
                        reverse.setMatchNo(matchCounter++);
                        matches.add(reverse);
                    }
                }
            }

            // Rotate teams except the first one
            List<String> rotated = new ArrayList<>();
            rotated.add(teamNames.get(0)); // keep first fixed
            rotated.add(teamNames.get(n - 1));
            for (int i = 1; i < n - 1; i++) {
                rotated.add(teamNames.get(i));
            }
            teamNames = rotated;
        }

        return matches;
    }

    private List<MatchSchedule> generateGroupFormatMatches(League league) {
        List<String> teamNames = new ArrayList<>();
        for (String t : league.getTeams())
            teamNames.add(t.split(":")[0]);

        List<String> groupA = new ArrayList<>();
        List<String> groupB = new ArrayList<>();
        for (int i = 0; i < teamNames.size(); i++) {
            if (i % 2 == 0)
                groupA.add(teamNames.get(i));
            else
                groupB.add(teamNames.get(i));
        }

        List<MatchSchedule> matches = new ArrayList<>();
        matches.addAll(generateMatchesWithinGroup(league, groupA));
        matches.addAll(generateMatchesWithinGroup(league, groupB));

        return matches;
    }

    private List<MatchSchedule> generateMatchesWithinGroup(League league, List<String> group) {
        List<MatchSchedule> matches = new ArrayList<>();
        for (int i = 0; i < group.size(); i++) {
            for (int j = i + 1; j < group.size(); j++) {
                MatchSchedule match = new MatchSchedule();
                match.setLeagueId(league.getId());
                match.setTeam1(group.get(i));
                match.setTeam2(group.get(j));
                match.setStatus("Scheduled");
                matches.add(match);
            }
        }
        return matches;
    }

    private List<MatchSchedule> generateKnockoutMatches(League league, boolean includeEliminator, int startingMatchNo) {
        List<MatchSchedule> knockouts = new ArrayList<>();
        int matchCounter = startingMatchNo;

        if (includeEliminator) {
            MatchSchedule eliminator = new MatchSchedule();
            eliminator.setLeagueId(league.getId());
            eliminator.setTeam1("Loser3");
            eliminator.setTeam2("Loser4");
            eliminator.setStatus("Scheduled");
            eliminator.setVenue(league.getVenue());
            eliminator.setMatchType("ELIMINATOR");
            eliminator.setMatchNo(matchCounter++);
            knockouts.add(eliminator);
        }

        MatchSchedule semi1 = new MatchSchedule();
        semi1.setLeagueId(league.getId());
        semi1.setTeam1("Winner1");
        semi1.setTeam2("Winner4");
        semi1.setStatus("Scheduled");
        semi1.setVenue(league.getVenue());
        semi1.setMatchType("SEMI FINAL 1");
        semi1.setMatchNo(matchCounter++);
        knockouts.add(semi1);

        MatchSchedule semi2 = new MatchSchedule();
        semi2.setLeagueId(league.getId());
        semi2.setTeam1("Winner2");
        semi2.setTeam2("Winner3");
        semi2.setStatus("Scheduled");
        semi2.setVenue(league.getVenue());
        semi2.setMatchType("SEMI FINAL 2");
        semi2.setMatchNo(matchCounter++);
        knockouts.add(semi2);

        MatchSchedule finalMatch = new MatchSchedule();
        finalMatch.setLeagueId(league.getId());
        finalMatch.setTeam1("WinnerSemi1");
        finalMatch.setTeam2("WinnerSemi2");
        finalMatch.setStatus("Scheduled");
        finalMatch.setVenue(league.getVenue());
        finalMatch.setMatchType("FINAL");
        finalMatch.setMatchNo(matchCounter++);
        knockouts.add(finalMatch);

        return knockouts;
    }

    private void assignMatchDates(League league, List<MatchSchedule> matches) {
        LocalDateTime start = LocalDateTime.ofInstant(league.getStartDate().toInstant(), ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(league.getEndDate().toInstant(), ZoneId.systemDefault());

        long totalDays = ChronoUnit.DAYS.between(start, end) + 1;
        long interval = Math.max(totalDays / matches.size(), 1);

        for (int i = 0; i < matches.size(); i++) {
            LocalDateTime matchDate = start.plusDays(i * interval)
                    .withHour(10)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);

            matches.get(i).setScheduledDate(Date.from(matchDate.atZone(ZoneId.systemDefault()).toInstant()));
        }
    }

    private void assignMatchNumbersAndTypes(List<MatchSchedule> matches, String leagueFormatType,
            boolean includeEliminator, boolean includeKnockouts) {
        int matchNo = 1;

        for (MatchSchedule match : matches) {
            if (match.getMatchType() == null || match.getMatchType().equals("LEAGUE")) {
                match.setMatchNo(matchNo++);
                match.setMatchType("LEAGUE");
            }
        }

        if (!includeKnockouts)
            return;

        int knockoutIndex = 1;
        for (MatchSchedule match : matches) {
            if (!match.getMatchType().equals("LEAGUE")) {
                match.setMatchNo(matchNo++);

                if (match.getTeam1().startsWith("Loser") || match.getTeam2().startsWith("Loser")) {
                    match.setMatchType("ELIMINATOR");
                } else if (match.getTeam1().startsWith("Winner") && match.getTeam2().startsWith("Winner")) {
                    switch (knockoutIndex) {
                        case 1 -> match.setMatchType("SEMI_FINAL_1");
                        case 2 -> match.setMatchType("SEMI_FINAL_2");
                        default -> match.setMatchType("FINAL");
                    }
                    knockoutIndex++;
                }
            }
        }
    }

    public void deleteLeague(String leagueId) {
        String adminId = getLoggedInAdminId();

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new RuntimeException("League not found with ID: " + leagueId));

        if (!league.getAdminId().equals(adminId)) {
            throw new RuntimeException("This league does not belong to you!");
        }

        List<String> teamIds = league.getTeams().stream()
                .map(team -> {
                    String[] parts = team.split(":");
                    return parts.length > 1 ? parts[1] : parts[0]; // if no ":", take the whole string
                })
                .toList();

        List<Team> teams = teamRepository.findAllById(teamIds);
        for (Team team : teams) {
            if (team.getLogoUrl() != null && !team.getLogoUrl().isEmpty()) {
                try {
                    cloudinaryService.deleteFile(team.getLogoUrl());
                } catch (Exception e) {
                    System.err.println("Failed to delete team logo for " + team.getName() + ": " + e.getMessage());
                }
            }
        }
        teamRepository.deleteAll(teams);

        // Delete league logo
        if (league.getLogoUrl() != null && !league.getLogoUrl().isEmpty()) {
            try {
                cloudinaryService.deleteFile(league.getLogoUrl());
            } catch (Exception e) {
                System.err.println("Failed to delete league logo: " + e.getMessage());
            }
        }

        // Delete matches
        List<MatchSchedule> matches = matchRepository.findByLeagueId(leagueId);
        if (!matches.isEmpty()) {
            matchRepository.deleteAll(matches);
        }

        // Delete league
        leagueRepository.delete(league);
    }

    public void deleteAllLeagues() {
        String adminId = getLoggedInAdminId();

        List<League> leagues = leagueRepository.findByAdminId(adminId);
        if (leagues.isEmpty()) {
            throw new RuntimeException("No leagues found to delete!");
        }

        for (League league : leagues) {
            // Delete teams and their logos
            List<String> teamIds = league.getTeams().stream()
                    .map(team -> {
                        String[] parts = team.split(":");
                        return parts.length > 1 ? parts[1] : parts[0]; // if no ":", take the whole string
                    })
                    .toList();

            List<Team> teams = teamRepository.findAllById(teamIds);
            for (Team team : teams) {
                if (team.getLogoUrl() != null && !team.getLogoUrl().isEmpty()) {
                    try {
                        cloudinaryService.deleteFile(team.getLogoUrl());
                    } catch (Exception e) {
                        System.err.println("Failed to delete team logo for " + team.getName() + ": " + e.getMessage());
                    }
                }
            }
            teamRepository.deleteAll(teams);

            // Delete league logo
            if (league.getLogoUrl() != null && !league.getLogoUrl().isEmpty()) {
                try {
                    cloudinaryService.deleteFile(league.getLogoUrl());
                } catch (Exception e) {
                    System.err.println("Failed to delete league logo: " + e.getMessage());
                }
            }

            // Delete matches
            List<MatchSchedule> matches = matchRepository.findByLeagueId(league.getId());
            if (!matches.isEmpty())
                matchRepository.deleteAll(matches);
        }

        // Delete leagues
        leagueRepository.deleteAll(leagues);
    }

    public Optional<League> getLeagueById(String leagueId) {
        getLoggedInAdminId();
        return leagueRepository.findById(leagueId);
    }

    public List<League> getAllLeagues() {
        getLoggedInAdminId();
        return leagueRepository.findAll();
    }

    public Optional<League> getLeagueByName(String name) {
        getLoggedInAdminId();
        return leagueRepository.findByName(name);
    }

    public List<League> getLeaguesByAdmin() {
        String adminId = getLoggedInAdminId();
        return leagueRepository.findByAdminId(adminId);
    }
}