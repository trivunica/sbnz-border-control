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
}
