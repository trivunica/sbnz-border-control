package com.ftn.sbnz.model.models;

import com.ftn.sbnz.model.enums.ViolationType;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PastViolationRecord {
    private String plateNumber;
    private String driverLicenceNumber;
    private String companyName;
    private ViolationType violationType;
    private LocalDate violationDate;
    private boolean blocking;
}
