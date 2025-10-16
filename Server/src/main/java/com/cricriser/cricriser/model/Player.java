package com.cricriser.cricriser.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Document(collection = "player")
public class Player {
    
    @Id
    private String id;
    private String name;
    private String role; 
    private String battingStyle; // Left Hander / Right Hander
    private String bowlingType;  // Fast/Spin
    private String bowlingStyle; // eg. Right Arm Off Spin, Left Arm Orthodox etc.
    // 
}