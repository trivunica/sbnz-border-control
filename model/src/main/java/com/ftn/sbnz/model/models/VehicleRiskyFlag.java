package com.ftn.sbnz.model.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRiskyFlag {
    private String plateNumber;
    private int alarmCount;
    private boolean hasCoordinatedAlarm;
}
