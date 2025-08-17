package com.cricbook.cricbook.model;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor

@Document(collection = "matche")
public class Matches {

    private String team1;
    private String team2;

    private LocalDate date;
    private LocalTime time;

    private String venue;
    private String result;
    private String tossWinner;
    private String tossDecision;

    private String matchWinner;
    private String playerOfTheMatch;

    private String team1Score;
    private String team2Score;

    private int team1Wickets;
    private int team2Wickets;

    private int team1Overs;
    private int team2Overs;

}
