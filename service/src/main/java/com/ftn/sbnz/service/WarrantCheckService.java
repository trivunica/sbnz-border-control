package com.ftn.sbnz.service;

import com.ftn.sbnz.service.persistence.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WarrantCheckService {

    @Autowired
    private InterpolWarrantRepository interpolRepo;

    @Autowired
    private DomesticWarrantRepository domesticRepo;

    @Autowired
    private StolenLostDocumentRepository stolenRepo;

    public WarrantCheckResult check(String documentNumber) {
        var builder = WarrantCheckResult.builder()
                .documentNumber(documentNumber);

        interpolRepo.findByDocumentNumberAndActiveTrue(documentNumber)
                .ifPresent(w -> builder
                        .interpolWarrant(true)
                        .interpolReason(w.getReason()));

        domesticRepo.findByDocumentNumberAndActiveTrue(documentNumber)
                .ifPresent(w -> builder
                        .domesticWarrant(true)
                        .domesticReason(w.getReason()));

        stolenRepo.findByDocumentNumberAndActiveTrue(documentNumber)
                .ifPresent(d -> builder
                        .documentReportedStolen(true)
                        .stolenReason(d.getReason()));  // "STOLEN" or "LOST"

        return builder.build();
    }
}