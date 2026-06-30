package com.ftn.sbnz.model.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RiskFactor {
    private String entityId;      // licenceNumber, plateNumber or companyName
    private int score;
    private String description;
    private String proofStep;
}
