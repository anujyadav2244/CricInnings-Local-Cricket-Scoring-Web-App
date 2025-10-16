package com.innings.innings.model;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
@Data

@Document(collection = "playerStats")
public class PlayerStats {
    private int runs;
    private int wickets;
    private int matches;
    private int strikeRate;
    private int economy;
}
