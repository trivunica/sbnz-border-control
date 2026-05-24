package com.ftn.sbnz.model.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CMRDocument {
    private String originCountry;
    private String destinationCountry;
    private String goodsDescription;
    private Double goodsWeight;
    private String senderIdentity;
    private String receiverIdentity;
}
