package com.ftn.sbnz.model.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Driver {
    private String name;
    private String surname;
    private String dob;
    private String citizenship;
    private Boolean foreignCitizen;
    private Boolean domesticWarrant;
    private Boolean interpolWarrant;
    private Boolean photoMatches;
    private Boolean documentReportedStolen;
    private Boolean hasVisa;
    private Boolean hasSupplementaryDocument;
    private Double financialFunds;
    private Integer plannedStayDays;

}
