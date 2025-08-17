package com.cricbook.cricbook.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data

@Document(collection = "league")
public class League {
    @Id
    private String id;
    private String name;
    private int noOfTeams;
    private int noOfMatches;
    private int noOfOvers;
    private Date startDate;
    private Date endDate;
    private String venue;


}
