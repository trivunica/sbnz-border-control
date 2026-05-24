package com.ftn.sbnz.model.models;

import com.ftn.sbnz.model.enums.ViolationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Violation {
    private ViolationType type;
    private String legalBasis;
    private Double fineAmount;
    private Boolean canContinue;
    private String explanation;


    public boolean isArrestType() {
        return type == ViolationType.INTERPOL_WARRANT || type == ViolationType.DOMESTIC_WARRANT;
    }


    public boolean isDriverForbiddenType() {
        return type == ViolationType.EXPIRED_PASSPORT
            || type == ViolationType.EXPIRED_ID_CARD
            || type == ViolationType.PASSPORT_SHORT_VALIDITY
            || type == ViolationType.SUSPECTED_FORGERY
            || type == ViolationType.STOLEN_LOST_DOCUMENT
            || type == ViolationType.MISSING_VISA_SUPPLEMENT
            || type == ViolationType.INSUFFICIENT_FUNDS
            || type == ViolationType.INVALID_DRIVING_CATEGORY;
    }


    public boolean isVehicleForbiddenType() {
        return type == ViolationType.EXPIRED_REGISTRATION || type == ViolationType.PENDING_WEIGHT_CHECK;
    }


    public boolean isVehicleProblemType() {
        return type == ViolationType.TRAILER_OVERLOAD || type == ViolationType.TOTAL_WEIGHT_OVERLOAD;
    }
}
