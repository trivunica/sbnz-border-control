package com.ftn.sbnz.model.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessment {
    private int riskScore;
    private String riskLevel;
    private List<String> riskFactors;
    private DecisionNode explanationTree;
    private String entityId;   // plate number or licence number
}
