package com.ftn.sbnz.model.models;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BorderCrossingResult {
    private FinalDecision finalDecision;
    private List<Violation> violations;
    private DriverValidity driverValidity;
    private VehicleValidity vehicleValidity;
    private PermitStatus permitStatus;
    private RiskAssessment riskAssessment;
}