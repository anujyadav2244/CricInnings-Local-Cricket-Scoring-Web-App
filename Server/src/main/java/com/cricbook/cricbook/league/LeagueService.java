package com.cricbook.cricbook.league;

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

import com.cricbook.cricbook.match.MatchSchedule;
import com.cricbook.cricbook.match.MatchScheduleRepository;
import com.cricbook.cricbook.team.Team;
import com.cricbook.cricbook.team.TeamRepository;

@Service
public class LeagueService {

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchScheduleRepository matchRepository;

    // ---------------- HELPER ----------------
    private String getLoggedInAdminId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null || auth.getPrincipal().toString().equals("anonymousUser")) {
            throw new RuntimeException("Unauthorized! Please log in.");
        }
        return auth.getPrincipal().toString();
    }

    private List<String> prepareTeams(List<String> teamNames) {
        List<String> teamInfo = new ArrayList<>();
        for (String teamName : teamNames) {
            Team team = teamRepository.findByName(teamName);
            if (team == null) {
                team = new Team();
                team.setName(teamName);
                teamRepository.save(team);
            }
            teamInfo.add(team.getName() + ":" + team.getId());
        }
        return teamInfo;
    }

    // ---------------- CREATE LEAGUE & MATCHES ----------------
    public List<MatchSchedule> createLeagueAndScheduleMatches(League league, boolean includeEliminator,
            boolean includeKnockouts) {

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
        league.setTeams(prepareTeams(league.getTeams()));
        League savedLeague = leagueRepository.save(league);

        // Generate league stage matches
        List<MatchSchedule> matches = switch (league.getLeagueFormatType()) {
            case "SINGLE_ROUND_ROBIN" -> generateRoundRobinMatches(savedLeague, false);
            case "DOUBLE_ROUND_ROBIN" -> generateRoundRobinMatches(savedLeague, true);
            case "GROUP" -> generateGroupFormatMatches(savedLeague);
            default -> throw new RuntimeException(
                    "Invalid leagueFormatType! Choose SINGLE_ROUND_ROBIN, DOUBLE_ROUND_ROBIN, or GROUP.");
        };

        // Determine last match number to start knockout matches
        int lastMatchNo = matches.isEmpty() ? 1 : matches.get(matches.size() - 1).getMatchNo() + 1;

        // Add knockout stage matches if requested
        if (includeKnockouts) {
            matches.addAll(generateKnockoutMatches(savedLeague, includeEliminator, lastMatchNo));
        }

        // Assign dates and times
        assignMatchDates(savedLeague, matches);

        // Assign match numbers and types
        assignMatchNumbersAndTypes(matches, savedLeague.getLeagueFormatType(), includeEliminator, includeKnockouts);

        // Save all matches
        matchRepository.saveAll(matches);

        savedLeague.setNoOfMatches(matches.size());
        leagueRepository.save(savedLeague);

        return matches;
    }

    private void assignMatchNumbersAndTypes(List<MatchSchedule> matches,
                                        String leagueFormatType,
                                        boolean includeEliminator,
                                        boolean includeKnockouts) {
    int matchNo = 1;

    // ---------- Assign League Matches ----------
    for (MatchSchedule match : matches) {
        if (match.getMatchType() == null || match.getMatchType().equals("LEAGUE")) {
            match.setMatchNo(matchNo++);
            match.setMatchType("LEAGUE");
        }
    }

    if (!includeKnockouts) return; // nothing more to assign

    // ---------- Assign Knockout Matches ----------
    int knockoutIndex = 1;
    for (MatchSchedule match : matches) {
        if (!match.getMatchType().equals("LEAGUE")) {
            match.setMatchNo(matchNo++);

            // Determine type based on placeholders
            if (match.getTeam1().startsWith("Loser") || match.getTeam2().startsWith("Loser")) {
                match.setMatchType("ELIMINATOR");
            } else if (match.getTeam1().startsWith("Winner") && match.getTeam2().startsWith("Winner")) {
                // Semi-finals or final
                switch (knockoutIndex) {
                    case 1 -> match.setMatchType("SEMI FINAL 1");
                    case 2 -> match.setMatchType("SEMI FINAL 2");
                    default -> match.setMatchType("FINAL");
                }
                knockoutIndex++;
            }
        }
    }
}


    // ---------------- ROUND-ROBIN ----------------
    private List<MatchSchedule> generateRoundRobinMatches(League league, boolean doubleRound) {
        List<String> teamNames = new ArrayList<>();
        for (String t : league.getTeams())
            teamNames.add(t.split(":")[0]);

        List<MatchSchedule> matches = new ArrayList<>();
        int matchCounter = 1; // Initialize match number

        for (int i = 0; i < teamNames.size(); i++) {
            for (int j = i + 1; j < teamNames.size(); j++) {
                // First match
                MatchSchedule match1 = new MatchSchedule();
                match1.setLeagueId(league.getId());
                match1.setTeam1(teamNames.get(i));
                match1.setTeam2(teamNames.get(j));
                match1.setStatus("Scheduled");
                match1.setMatchType("LEAGUE");
                match1.setMatchNo(matchCounter++);
                matches.add(match1);

                // Double round-robin (reverse match)
                if (doubleRound) {
                    MatchSchedule match2 = new MatchSchedule();
                    match2.setLeagueId(league.getId());
                    match2.setTeam1(teamNames.get(j));
                    match2.setTeam2(teamNames.get(i));
                    match2.setStatus("Scheduled");
                    match2.setMatchType("LEAGUE");
                    match2.setMatchNo(matchCounter++);
                    matches.add(match2);
                }
            }
        }

        return matches;
    }

    // ---------------- GROUP FORMAT ----------------
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

    // ---------------- KNOCKOUT ----------------
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
        semi1.setMatchType("SEMI_FINAL_1");
        semi1.setMatchNo(matchCounter++);
        knockouts.add(semi1);

        MatchSchedule semi2 = new MatchSchedule();
        semi2.setLeagueId(league.getId());
        semi2.setTeam1("Winner2");
        semi2.setTeam2("Winner3");
        semi2.setStatus("Scheduled");
        semi2.setVenue(league.getVenue());
        semi2.setMatchType("SEMI_FINAL_2");
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

    // ---------------- ASSIGN DATES ----------------
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

    // ---------------- UPDATE LEAGUE ----------------
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
                        existingLeague.setTeams(prepareTeams(updatedLeague.getTeams()));
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

                    return leagueRepository.save(existingLeague);
                })
                .orElseThrow(() -> new RuntimeException("League not found with ID: " + leagueId));
    }

    // ---------------- DELETE LEAGUE ----------------
    public void deleteLeague(String leagueId) {
        String adminId = getLoggedInAdminId();

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new RuntimeException("League not found with ID: " + leagueId));

        if (!league.getAdminId().equals(adminId)) {
            throw new RuntimeException("This league does not belong to you!");
        }

        // Delete all teams belonging to this league
        List<Team> teams = teamRepository.findByLeagueId(leagueId);
        if (!teams.isEmpty()) {
            teamRepository.deleteAll(teams);
        }

        // Delete all matches belonging to this league
        List<MatchSchedule> matches = matchRepository.findByLeagueId(leagueId);
        if (!matches.isEmpty()) {
            matchRepository.deleteAll(matches);
        }

        // Finally, delete the league
        leagueRepository.delete(league);
    }

    // ---------------- DELETE ALL LEAGUES ----------------
    public void deleteAllLeagues() {
        String adminId = getLoggedInAdminId();

        List<League> leagues = leagueRepository.findByAdminId(adminId);
        if (leagues.isEmpty()) {
            throw new RuntimeException("No leagues found to delete!");
        }

        for (League league : leagues) {
            // Delete teams
            List<Team> teams = teamRepository.findByLeagueId(league.getId());
            if (!teams.isEmpty())
                teamRepository.deleteAll(teams);

            // Delete matches
            List<MatchSchedule> matches = matchRepository.findByLeagueId(league.getId());
            if (!matches.isEmpty())
                matchRepository.deleteAll(matches);
        }

        // Delete leagues
        leagueRepository.deleteAll(leagues);
    }

    // ---------------- READ ----------------
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
