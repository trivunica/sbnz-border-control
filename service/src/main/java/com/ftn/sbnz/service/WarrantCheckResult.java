package com.ftn.sbnz.service;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantCheckResult {
    private String documentNumber;
    private boolean interpolWarrant;
    private boolean domesticWarrant;
    private boolean documentReportedStolen;

    private String interpolReason;
    private String domesticReason;
    private String stolenReason;          // "STOLEN" or "LOST"

    public boolean hasAnyHit() {
        return interpolWarrant || domesticWarrant || documentReportedStolen;
    }

    public boolean requiresImmediateArrest() {
        return interpolWarrant || domesticWarrant;
    }
}