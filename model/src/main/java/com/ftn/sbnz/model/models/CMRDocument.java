package com.ftn.sbnz.model.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CMRDocument {
    private String originCountry;
    private String destinationCountry;
    private String goodsDescription;
    private Double goodsWeight;
    private String senderIdentity;
    private String receiverIdentity;
}
