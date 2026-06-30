package com.ftn.sbnz.model.models;

import com.ftn.sbnz.model.enums.CepAlarmType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CepAlarm {
    private CepAlarmType type;
    private String plateNumber;
    private String message;
    private long timestamp;
    private String crossedAt;
    private String borderPair;


    public CepAlarm(CepAlarmType type, String plateNumber, String message, long timestamp) {
        this.type = type;
        this.plateNumber = plateNumber;
        this.message = message;
        this.timestamp = timestamp;
    }


    public CepAlarm(CepAlarmType type, String plateNumber, String message,
                    long timestamp, String borderPair) {
        this.type = type;
        this.plateNumber = plateNumber;
        this.message = message;
        this.timestamp = timestamp;
        this.borderPair = borderPair;
    }
}
