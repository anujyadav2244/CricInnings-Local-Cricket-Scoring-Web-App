package com.cricbook.cricbook.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
@Data
@Document(collection = "team")
public class Team {
    @Id
    private String id;
    private String name;
    private String leagueId;
    private String coach;
    public List<Player> squad;
    public Player captain;
    public Player viceCaptain;

    
    
}
