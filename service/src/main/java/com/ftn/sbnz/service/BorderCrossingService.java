package com.ftn.sbnz.service;

import com.ftn.sbnz.model.models.*;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class BorderCrossingService {

    @Autowired
    private KieContainer kieContainer;

    public BorderCrossingResult evaluate(BorderCrossingRequest request) {
        KieSession kieSession = kieContainer.newKieSession("forwardKSession");

        try {
            insertFacts(kieSession, request);
            kieSession.setGlobal("today", LocalDate.now());
            kieSession.setGlobal("ninetyDaysFromNow", LocalDate.now().plusDays(90));
            kieSession.fireAllRules();
            return extractResult(kieSession);
        } finally {
            kieSession.dispose();
        }
    }

    private void insertFacts(KieSession session, BorderCrossingRequest request) {
        if (request.getDriver() != null)
            session.insert(request.getDriver());
        if (request.getDrivingLicence() != null)
            session.insert(request.getDrivingLicence());
        if (request.getIdentificationDocument() != null)
            session.insert(request.getIdentificationDocument());
        if (request.getVehicleRegistration() != null)
            session.insert(request.getVehicleRegistration());
        if (request.getCmrDocument() != null)
            session.insert(request.getCmrDocument());
        if (request.getLiveWeightMeasurement() != null)
            session.insert(request.getLiveWeightMeasurement());
        if (request.getTransportPermits() != null)
            request.getTransportPermits().forEach(session::insert);
    }

    private BorderCrossingResult extractResult(KieSession session) {
        BorderCrossingResult result = new BorderCrossingResult();

        List<Violation> violations = new ArrayList<>();
        session.getObjects(obj -> obj instanceof Violation)
                .forEach(obj -> violations.add((Violation) obj));
        result.setViolations(violations);

        session.getObjects(obj -> obj instanceof FinalDecision)
                .forEach(obj -> result.setFinalDecision((FinalDecision) obj));
        session.getObjects(obj -> obj instanceof DriverValidity)
                .forEach(obj -> result.setDriverValidity((DriverValidity) obj));
        session.getObjects(obj -> obj instanceof VehicleValidity)
                .forEach(obj -> result.setVehicleValidity((VehicleValidity) obj));
        session.getObjects(obj -> obj instanceof PermitStatus)
                .forEach(obj -> result.setPermitStatus((PermitStatus) obj));

        return result;
    }
}