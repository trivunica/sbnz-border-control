package com.ftn.sbnz.model.models;

import com.ftn.sbnz.model.enums.ValidityStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermitStatus {
    private ValidityStatus status;
}
