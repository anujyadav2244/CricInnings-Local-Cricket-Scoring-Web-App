package com.cricriser.cricriser.model;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor

@Document(collection = "extras")
public class Extras {
    private int wideRuns;
    private int noBallRuns;
    private int legByes;
    private int byes;

}   
