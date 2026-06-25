package com.ftn.sbnz.model.models;

import com.ftn.sbnz.model.enums.GoodsCertificate;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DriverCertificate {
    private GoodsCertificate goodsCertificate;
    private int yearsExperience;
    private LocalDate expiryDate;
}
