package com.cricbook.cricbook.model;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor

@Document(collection = "matchScore")
public class MatchScore {
    private int runs;
    private int wickets;
    private int matches;
    private int strikeRate;
    private int economy;
}
