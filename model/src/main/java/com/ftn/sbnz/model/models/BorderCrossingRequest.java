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
public class BorderCrossingRequest {
    private Driver driver;
    private DrivingLicence drivingLicence;
    private IdentificationDocument identificationDocument;
    private VehicleRegistration vehicleRegistration;
    private Vehicle vehicle;
    private LiveWeightMeasurement liveWeightMeasurement;
    private CMRDocument cmrDocument;
    private List<TransportPermit> transportPermits;
}