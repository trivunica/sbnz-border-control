package com.ftn.sbnz.model.models;

import com.ftn.sbnz.model.enums.GoodsCertificate;
import com.ftn.sbnz.model.enums.TransportPermitType;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TransportPermit {
    private TransportPermitType type;
    private GoodsCertificate goodsCertificate;
    private LocalDate expiryDate;
    private String coveredRoutes;
}

