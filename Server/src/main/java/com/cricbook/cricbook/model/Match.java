package com.cricbook.cricbook.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "match")
public class Match {

    @Id
    private String id;
    private String leagueId;
    private String team1;
    private String team2;
    private LocalDate matchDate;
    private String venue;

    private String tossWinner;
    private String tossDecision;

    private String matchWinner;
    private String result;
    private String playerOfTheMatch;

    private int team1Runs;
    private int team2Runs;
    private int team1Wickets;
    private int team2Wickets;
    private float team1Overs;
    private float team2Overs;

    private String status;      // Scheduled, Ongoing, Completed
    private String matchType;   // ODI, T20, Test
}
