package com.ftn.sbnz.service;

import com.ftn.sbnz.model.models.CepAlarm;
import com.ftn.sbnz.model.models.CrossingEvent;
import com.ftn.sbnz.service.persistence.entity.CepAlarmEntity;
import com.ftn.sbnz.service.persistence.entity.CrossingEventEntity;
import com.ftn.sbnz.service.persistence.repository.CepAlarmRepository;
import com.ftn.sbnz.service.persistence.repository.CrossingEventRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CepService {
    @Autowired
    private KieContainer kieContainer;

    @Autowired
    private CrossingEventRepository eventRepository;

    @Autowired
    private CepAlarmRepository cepAlarmRepository;

    private KieSession cepSession;


    @PostConstruct
    public void init() {
        cepSession = kieContainer.newKieSession("cepKSession");

        eventRepository.findAll().forEach(e -> cepSession.insert(toCrossingModel(e)));
        cepSession.fireAllRules();
    }


    public synchronized void registerCrossing(CrossingEvent event) {
        eventRepository.save(toCrossingEntity(event));

        retractExpiredAlarms();

        cepSession.insert(event);
        List<CepAlarm> pre = getActiveAlarms();
        cepSession.fireAllRules();
        List<CepAlarm> post = getActiveAlarms();
        post.stream()
                .filter(a -> pre.stream().noneMatch(p -> p.getType().equals(a.getType())
                        && p.getPlateNumber().equals(a.getPlateNumber())))
                .forEach(a -> cepAlarmRepository.save(toAlarmEntity(a)));
    }


    private void retractExpiredAlarms() {
        long now = System.currentTimeMillis();

        cepSession.getObjects(obj -> obj instanceof CepAlarm)
                .forEach(obj -> {
                    CepAlarm a = (CepAlarm) obj;
                    long windowMs = switch (a.getType()) {
                        case FREQUENT_CROSSINGS -> 6  * 60 * 60 * 1000L;
                        case COORDINATED_GROUP  -> 30 * 60 * 1000L;
                        case BORDER_AVOIDANCE   -> 24 * 60 * 60 * 1000L;
                    };
                    if (now - a.getTimestamp() > windowMs) {
                        cepSession.delete(cepSession.getFactHandle(obj));
                    }
                });
    }


    public synchronized List<CepAlarm> getActiveAlarms() {
        long now = System.currentTimeMillis();
        List<CepAlarm> alarms = new ArrayList<>();

        cepSession.getObjects(obj -> obj instanceof CepAlarm)
                .forEach(obj -> {
                    CepAlarm a = (CepAlarm) obj;

                    long windowMs = switch (a.getType()) {
                        case FREQUENT_CROSSINGS  -> 6  * 60 * 60 * 1000L;  // 6h
                        case COORDINATED_GROUP   -> 30 * 60 * 1000L;        // 30min
                        case BORDER_AVOIDANCE    -> 24 * 60 * 60 * 1000L;  // 24h
                    };

                    if (now - a.getTimestamp() <= windowMs) {
                        if (a.getCrossedAt() == null) {
                            a.setCrossedAt(
                                    Instant.ofEpochMilli(a.getTimestamp())
                                            .atZone(ZoneId.systemDefault())
                                            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            );
                        }
                        alarms.add(a);
                    }
                });
        return alarms;
    }


    public synchronized List<CepAlarm> getAlarmsForVehicle(String plateNumber) {
        long now = System.currentTimeMillis();
        List<CepAlarm> alarms = new ArrayList<>();

        cepSession.getObjects(obj -> obj instanceof CepAlarm
                        && ((CepAlarm) obj).getPlateNumber().equals(plateNumber))
                .forEach(obj -> {
                    CepAlarm a = (CepAlarm) obj;

                    long windowMs = switch (a.getType()) {
                        case FREQUENT_CROSSINGS -> 6  * 60 * 60 * 1000L;   // 6h
                        case COORDINATED_GROUP  -> 30 * 60 * 1000L;          // 30min
                        case BORDER_AVOIDANCE   -> 24 * 60 * 60 * 1000L;   // 24h
                    };

                    if (now - a.getTimestamp() <= windowMs) {
                        alarms.add(a);
                    }
                });

        return alarms;
    }


    public synchronized List<CepAlarm> getAllAlarmsForVehicle(String plateNumber) {
        List<CepAlarm> alarms = new ArrayList<>();
        cepSession.getObjects(obj -> obj instanceof CepAlarm
                        && ((CepAlarm) obj).getPlateNumber().equals(plateNumber))
                .forEach(obj -> alarms.add((CepAlarm) obj));
        return alarms;
    }


    private CrossingEventEntity toCrossingEntity(CrossingEvent e) {
        LocalDateTime crossedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(e.getTimestamp()), ZoneId.systemDefault());

        return new CrossingEventEntity(
                null,
                e.getPlateNumber(),
                e.getDriverDocumentNumber(),
                e.getBorderCrossingId(),
                e.getCompanyName(),
                e.getDestinationCountry(),
                crossedAt);
    }


    private CrossingEvent toCrossingModel(CrossingEventEntity e) {
        long timestamp = e.getCrossedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        return new CrossingEvent(
                e.getPlateNumber(),
                e.getDriverDocumentNumber(),
                e.getBorderCrossingId(),
                e.getCompanyName(),
                e.getDestinationCountry(),
                timestamp);
    }


    private CepAlarmEntity toAlarmEntity(CepAlarm a) {
        LocalDateTime crossedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(a.getTimestamp()), ZoneId.systemDefault());

        return new CepAlarmEntity(
                null,
                a.getType().name(),
                a.getPlateNumber(),
                a.getMessage(),
                crossedAt);
    }


    @PreDestroy
    public void shutdown() {
        if (cepSession != null) {
            cepSession.dispose();
        }
    }

}
