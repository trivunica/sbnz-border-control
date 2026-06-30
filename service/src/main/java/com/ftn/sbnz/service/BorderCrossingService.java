package com.ftn.sbnz.service;

import com.ftn.sbnz.model.enums.CepAlarmType;
import com.ftn.sbnz.model.enums.ViolationType;
import com.ftn.sbnz.model.models.*;
import com.ftn.sbnz.service.persistence.entity.ViolationHistoryEntity;
import com.ftn.sbnz.service.persistence.repository.ViolationHistoryRepository;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.event.rule.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class BorderCrossingService {
    private static final int HISTORY_DAYS = 365;
    private static final Logger log = LoggerFactory.getLogger(BorderCrossingService.class);

    @Autowired
    private KieContainer kieContainer;

    @Autowired
    private ViolationHistoryRepository violationHistoryRepository;

    @Autowired
    private CepService cepService;


    public BorderCrossingResult evaluate(BorderCrossingRequest request) {
        KieSession kieSession = kieContainer.newKieSession("forwardKSession");

        kieSession.addEventListener(new DefaultAgendaEventListener() {
            @Override
            public void afterMatchFired(AfterMatchFiredEvent event) {
                log.info("  [RULE FIRED] {}", event.getMatch().getRule().getName());
            }
        });

        try {
            kieSession.setGlobal("today", LocalDate.now());
            kieSession.setGlobal("ninetyDaysFromNow", LocalDate.now().plusDays(90));

            insertStandardFacts(kieSession, request);
            insertBcFacts(kieSession, request);

            kieSession.fireAllRules();

            log.info("=== BC after fireAllRules ===");
            log.info("  RiskAssessment u sesiji : {}",
                    kieSession.getObjects(o -> o instanceof RiskAssessment).size());
            kieSession.getObjects(o -> o instanceof RiskAssessment)
                    .forEach(o -> log.info("  RiskAssessment: {}", o));

            BorderCrossingResult result = extractResult(kieSession);
            persistViolationsForHistory(request, result);

            return result;
        } finally {
            kieSession.dispose();
        }
    }


    private void insertStandardFacts(KieSession session, BorderCrossingRequest request) {
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
        if (request.getDriverCertificates() != null)
            request.getDriverCertificates().forEach(session::insert);
    }


    private void insertBcFacts(KieSession session, BorderCrossingRequest request) {
        String licenceNumber  = licenceNumber(request);
        String plate      = plate(request);
        String company    = company(request);
        LocalDate since   = LocalDate.now().minusDays(HISTORY_DAYS);

        if (plate != null) {
            violationHistoryRepository
                    .findByPlateNumberAndViolationDateAfter(plate, since)
                    .forEach(e -> session.insert(toPastViolation(e)));
        }

        if (licenceNumber != null) {
            violationHistoryRepository
                    .findByDriverLicenceNumberAndViolationDateAfter(licenceNumber, since)
                    .forEach(e -> session.insert(toPastViolation(e)));
        }

        if (licenceNumber != null && plate != null) {
            session.insert(new VehicleAssignment(licenceNumber, plate));
        }

        if (licenceNumber != null) {
            violationHistoryRepository
                    .findDistinctPlatesByDriver(licenceNumber)
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(p -> !p.equals(plate))
                    .forEach(p -> session.insert(new VehicleAssignment(licenceNumber, p)));
        }

        if (company != null && licenceNumber != null) {
            session.insert(new DriverEmployment(company, licenceNumber));
        }

        if (company != null) {
            violationHistoryRepository
                    .findDistinctDriversByCompany(company)
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(l -> !l.equals(licenceNumber))
                    .forEach(l -> session.insert(new DriverEmployment(company, l)));
        }

        if (plate != null) {
            List<CepAlarm> alarms = cepService.getAllAlarmsForVehicle(plate);
            if (!alarms.isEmpty()) {
                boolean hasCoordinated = alarms.stream()
                        .anyMatch(a -> a.getType() == CepAlarmType.COORDINATED_GROUP);
                session.insert(new VehicleRiskyFlag(plate, alarms.size(), hasCoordinated));
            }
        }


        log.info("=== BC insertBcFacts ===");
        log.info("  licenceNumber={}, plate={}, company={}", licenceNumber, plate, company);
        log.info("  PastViolationRecord u sesiji : {}",
                session.getObjects(o -> o instanceof PastViolationRecord).size());
        log.info("  VehicleAssignment u sesiji   : {}",
                session.getObjects(o -> o instanceof VehicleAssignment).size());
        log.info("  DriverEmployment u sesiji    : {}",
                session.getObjects(o -> o instanceof DriverEmployment).size());
        log.info("  VehicleRiskyFlag u sesiji    : {}",
                session.getObjects(o -> o instanceof VehicleRiskyFlag).size());
        log.info("  CMRDocument u sesiji         : {}",
                session.getObjects(o -> o instanceof CMRDocument).size());
    }


    private void persistViolationsForHistory(BorderCrossingRequest request,
                                             BorderCrossingResult result) {
        if (result.getViolations() == null || result.getViolations().isEmpty()) return;

        String plate     = plate(request);
        String licenceNumber = licenceNumber(request);
        String company   = company(request);

        result.getViolations().forEach(v -> {
            if (v.getType() == ViolationType.HIGH_RISK_ENTITY
                    || v.getType() == ViolationType.MEDIUM_RISK_ENTITY) return;

            violationHistoryRepository.save(
                    ViolationHistoryEntity.builder()
                            .plateNumber(plate)
                            .driverLicenceNumber(licenceNumber)
                            .companyName(company)
                            .violationType(v.getType().name())
                            .violationDate(LocalDate.now())
                            .blocking(!v.getCanContinue())
                            .build()
            );
        });
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
        session.getObjects(obj -> obj instanceof RiskAssessment)
                .forEach(obj -> result.setRiskAssessment((RiskAssessment) obj));

        return result;
    }

    private String licenceNumber(BorderCrossingRequest r) {
        return r.getDrivingLicence() != null ? r.getDrivingLicence().getLicenceNumber() : null;
    }

    private String plate(BorderCrossingRequest r) {
        return r.getVehicleRegistration() != null
                ? r.getVehicleRegistration().getRegistrationNumber() : null;
    }

    private String company(BorderCrossingRequest r) {
        return r.getCmrDocument() != null ? r.getCmrDocument().getSenderIdentity() : null;
    }

    private PastViolationRecord toPastViolation(ViolationHistoryEntity e) {
        PastViolationRecord r = new PastViolationRecord();
        r.setPlateNumber(e.getPlateNumber());
        r.setDriverLicenceNumber(e.getDriverLicenceNumber());
        r.setCompanyName(e.getCompanyName());
        r.setViolationType(ViolationType.valueOf(e.getViolationType()));
        r.setViolationDate(e.getViolationDate());
        r.setBlocking(e.isBlocking());
        return r;
    }
}