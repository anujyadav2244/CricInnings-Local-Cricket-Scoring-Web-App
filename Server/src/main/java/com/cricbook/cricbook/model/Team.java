package com.cricbook.cricbook.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "team")
public class Team {
    @Id
    private String id;
    private String name;
    private String leagueId;
    private String coach;
    private List<Player> squad = new ArrayList<>();
    private Player captain;
    private Player viceCaptain;

}
