package com.ftn.sbnz.model.models;

import jdk.jfr.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kie.api.definition.type.Role;

@Role(Role.Type.EVENT)
@Timestamp("timestamp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CrossingEvent {
    private String plateNumber;
    private String driverDocumentNumber;
    private String borderCrossingId;
    private String companyName;
    private String destinationCountry;
    private long timestamp;
}
