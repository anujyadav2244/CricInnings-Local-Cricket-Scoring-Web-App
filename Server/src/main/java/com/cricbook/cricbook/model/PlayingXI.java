package com.cricbook.cricbook.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
@Data

@Document(collection = "playingXI")
public class PlayingXI {
    private String teamName;
    private List<Player> players;
    private Player captain;
    private Player viceCaptain;
    
}
