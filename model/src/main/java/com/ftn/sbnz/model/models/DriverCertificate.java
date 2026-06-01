package com.ftn.sbnz.model.models;

import com.ftn.sbnz.model.enums.GoodsCertificate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DriverCertificate {
    private GoodsCertificate goodsCertificate;
    private int yearsExperience;
    private LocalDate expiryDate;
}
