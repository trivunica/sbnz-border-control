package com.ftn.sbnz.service;

import com.ftn.sbnz.model.models.CepAlarm;
import com.ftn.sbnz.model.models.CrossingEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CepService {
    @Autowired
    private KieContainer kieContainer;

    private KieSession cepSession;


    @PostConstruct
    public void init() {
        cepSession = kieContainer.newKieSession("cepKSession");
    }


    public synchronized void registerCrossing(CrossingEvent event) {
        cepSession.insert(event);
        cepSession.fireAllRules();
    }


    public synchronized List<CepAlarm> getActiveAlarms() {
        List<CepAlarm> alarms = new ArrayList<>();
        cepSession.getObjects(obj -> obj instanceof CepAlarm)
                .forEach(obj -> alarms.add((CepAlarm) obj));

        return alarms;
    }


    public synchronized List<CepAlarm> getAlarmsForVehicle(String plateNumber) {
        List<CepAlarm> alarms = new ArrayList<>();
        cepSession.getObjects(obj -> obj instanceof CepAlarm
                && ((CepAlarm) obj).getPlateNumber().equals(plateNumber))
                .forEach(obj -> alarms.add((CepAlarm) obj));

        return alarms;
    }


    @PreDestroy
    public void shutdown() {
        if (cepSession != null) {
            cepSession.dispose();
        }
    }

}
