package com.ftn.sbnz.model.models;

import com.ftn.sbnz.model.enums.PermitType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransportPermit {
    private PermitType type;
    private LocalDate expiryDate;
    private String coveredRoutes;
}

