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
public class IdentificationDocument {
    private String documentNumber;
    private String type;
    private String issuingCountry;
    private LocalDate expiryDate;


    public boolean expiresAfter(LocalDate date) {
        return expiryDate != null && expiryDate.isAfter(date);
    }

    public boolean expiresBefore(LocalDate date) {
        return expiryDate != null && expiryDate.isBefore(date);
    }
}
