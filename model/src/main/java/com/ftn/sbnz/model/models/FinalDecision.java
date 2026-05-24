package com.ftn.sbnz.model.models;

import com.ftn.sbnz.model.enums.ActionRecommendation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinalDecision {
    private ActionRecommendation recommendation;
    private Double totalFine;
    private String entryRefusalCertificate;
    private Boolean requiresDriverReplacement;
    private Boolean requiresCargoOffLoad;
}
