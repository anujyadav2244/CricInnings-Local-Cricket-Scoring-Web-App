package com.cricbook.cricbook.match;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "match_scoring")
public class MatchScore {

    @Id
    private String id;
    private String matchId;  // Reference to MatchSchedule

    private String tossWinner;
    private String tossDecision;
    private String matchStatus; // e.g., "In Progress", "Completed" 
    private String matchWinner;
    private String result;
    private String playerOfTheMatch;

    private int team1Runs;
    private int team2Runs;
    private int team1Wickets;
    private int team2Wickets;
    private float team1Overs;
    private float team2Overs;

    private List<String> team1PlayingXI; // Player IDs
    private List<String> team2PlayingXI; // Player IDs
}
