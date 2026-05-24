package com.ftn.sbnz.model.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRegistration {
    private String registrationNumber;
    private LocalDate expiryDate;
    private Double maxAllowedWeight;
    private Double trailerCapacity;
    private Double truckWeight;
}
