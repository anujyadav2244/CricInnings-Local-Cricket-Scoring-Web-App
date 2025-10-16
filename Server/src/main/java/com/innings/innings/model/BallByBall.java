package com.innings.innings.model;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor

@Document(collection = "ballByBall")
public class BallByBall {
    
    private int over;
    private int ball;
    private String batsman;
    private String bowler;
    private String event; // 
    private String wicketType;
}
