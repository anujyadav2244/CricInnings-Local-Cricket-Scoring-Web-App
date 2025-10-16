package com.cricriser.cricriser.league;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "league")
public class League {
    @Id
    private String id;
    private String adminId;
    private String name;
    private int noOfTeams;
    private List<String> teams = new ArrayList<>();
    private int noOfMatches;
    private Date startDate;
    private Date endDate;
    private String venue;
    private String leagueFormat; // ODI, T20, Test
    private List<String> umpires;
    private String leagueFormatType; // SINGLE_ROUND_ROBIN, DOUBLE_ROUND_ROBIN, GROUP
    private String logoUrl;
}
