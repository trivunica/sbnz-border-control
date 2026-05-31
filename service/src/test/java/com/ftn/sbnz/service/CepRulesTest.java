package com.ftn.sbnz.service;

import com.ftn.sbnz.model.enums.CepAlarmType;
import com.ftn.sbnz.model.models.CepAlarm;
import com.ftn.sbnz.model.models.CrossingEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CepRulesTest {
    private KieSession kieSession;
    private long now;

    @BeforeEach
    void setUp() {
        KieServices ks = KieServices.Factory.get();
        KieContainer kieContainer = ks.getKieClasspathContainer();
        kieSession = kieContainer.newKieSession("cepKSession");
        now = System.currentTimeMillis();
    }

    @AfterEach
    void tearDown() {
        if (kieSession != null) {
            kieSession.dispose();
        }
    }

    // helper methods
    private void insertAndFire(CrossingEvent... events) {
        for (CrossingEvent event : events) {
            kieSession.insert(event);
        }
        kieSession.fireAllRules();
    }

    private List<CepAlarm> getAlarms() {
        List<CepAlarm> alarms = new ArrayList<>();
        kieSession.getObjects(obj -> obj instanceof CepAlarm)
                .forEach(obj -> alarms.add((CepAlarm) obj));
        return alarms;
    }

    private List<CepAlarm> getAlarmsOfType(CepAlarmType type) {
        return getAlarms().stream()
                .filter(alarm -> alarm.getType() == type)
                .toList();
    }


    // TEST 1 - FREQUENT_CROSSINGS: the same vehicle crosses border 4 times in 6 hours -> alarm
    @Test
    void testFrequentCrossings_fourTimesInSixHours_shouldCreateAlarm() {
        insertAndFire(
                new CrossingEvent("NS-111-AB", "RS-P-001", "SEPAK", "Firma DOO", "BA",
                        now - 5 * 3600 * 1000L),
                new CrossingEvent("NS-111-AB", "RS-P-001", "SEPAK", "Firma DOO", "BA",
                        now - 3 * 3600 * 1000L),
                new CrossingEvent("NS-111-AB", "RS-P-001", "SEPAK", "Firma DOO", "BA",
                        now - 3600 * 1000L),
                new CrossingEvent("NS-111-AB", "RS-P-001", "SEPAK", "Firma DOO", "BA",
                        now)
        );

        List<CepAlarm> alarms = getAlarmsOfType(CepAlarmType.FREQUENT_CROSSINGS);
        assertEquals(1, alarms.size(), "FREQUENT_CROSSINGS alarm should be created.");
        assertEquals("NS-111-AB", alarms.get(0).getPlateNumber());
    }


    // TEST 2 — FREQUENT_CROSSINGS: 3 times in 6 hours → no alarm
    @Test
    void testFrequentCrossings_threeTimesInSixHours_shouldNotCreateAlarm() {
        insertAndFire(
                new CrossingEvent("NS-222-AB", "RS-P-002", "SEPAK", "Firma DOO", "BA",
                        now - 4 * 3600 * 1000L),
                new CrossingEvent("NS-222-AB", "RS-P-002", "SEPAK", "Firma DOO", "BA",
                        now - 2 * 3600 * 1000L),
                new CrossingEvent("NS-222-AB", "RS-P-002", "SEPAK", "Firma DOO", "BA",
                        now)
        );

        List<CepAlarm> alarms = getAlarmsOfType(CepAlarmType.FREQUENT_CROSSINGS);
        assertTrue(alarms.isEmpty(), "3 crossings in 6 hours should not create the FREQUENT_CROSSINGS alarm.");
    }


    // TEST 3 — FREQUENT_CROSSINGS: 4 times but outside the 6-hour-window -> no alarm
    @Test
    void testFrequentCrossings_fourTimesOutsideWindow_shouldNotCreateAlarm() {
        insertAndFire(
                new CrossingEvent("NS-333-AB", "RS-P-003", "SEPAK", "Firma DOO", "BA",
                        now - 10 * 3600 * 1000L),
                new CrossingEvent("NS-333-AB", "RS-P-003", "SEPAK", "Firma DOO", "BA",
                        now - 3 * 3600 * 1000L),
                new CrossingEvent("NS-333-AB", "RS-P-003", "SEPAK", "Firma DOO", "BA",
                        now - 3600 * 1000L),
                new CrossingEvent("NS-333-AB", "RS-P-003", "SEPAK", "Firma DOO", "BA",
                        now)
        );

        List<CepAlarm> alarms = getAlarmsOfType(CepAlarmType.FREQUENT_CROSSINGS);
        assertTrue(alarms.isEmpty(),
                "4 crossings where one is outside the 6-hour-window should not create the FREQUENT_CROSSINGS alarm.");
    }


    // TEST 4 — COORDINATED_GROUP: 4 vehicles of the same company, same border, 30 min → alarm
    @Test
    void testCoordinatedGroup_fourVehiclesSameCompanySameBorder_shouldCreateAlarm() {
        long base = now - 20 * 60 * 1000L;
        insertAndFire(
                new CrossingEvent("NS-001-BA", "BA-P-010", "RACA", "ABC DOO", "DE", base),
                new CrossingEvent("NS-002-BA", "BA-P-011", "RACA", "ABC DOO", "DE",
                        base + 5 * 60 * 1000L),
                new CrossingEvent("NS-003-BA", "BA-P-012", "RACA", "ABC DOO", "DE",
                        base + 10 * 60 * 1000L),
                new CrossingEvent("NS-004-BA", "BA-P-013", "RACA", "ABC DOO", "DE",
                        base + 15 * 60 * 1000L)
        );

        List<CepAlarm> alarms = getAlarmsOfType(CepAlarmType.COORDINATED_GROUP);
        assertFalse(alarms.isEmpty(), "COORDINATED_GROUP alarm should be created.");
    }


    // TEST 5 — COORDINATED_GROUP: 4 vehicles but different companies → no alarm
    @Test
    void testCoordinatedGroup_differentCompanies_shouldNotCreateAlarm() {
        long base = now - 20 * 60 * 1000L;
        insertAndFire(
                new CrossingEvent("NS-001-BA", "BA-P-010", "RACA", "Firma A", "DE", base),
                new CrossingEvent("NS-002-BA", "BA-P-011", "RACA", "Firma B", "DE",
                        base + 5 * 60 * 1000L),
                new CrossingEvent("NS-003-BA", "BA-P-012", "RACA", "Firma C", "DE",
                        base + 10 * 60 * 1000L),
                new CrossingEvent("NS-004-BA", "BA-P-013", "RACA", "Firma D", "DE",
                        base + 15 * 60 * 1000L)
        );

        List<CepAlarm> alarms = getAlarmsOfType(CepAlarmType.COORDINATED_GROUP);
        assertTrue(alarms.isEmpty(), "Different companies should not create the COORDINATED_GROUP alarm.");
    }


    // TEST 6 — COORDINATED_GROUP: 3 vehicles of the same company → no alarm
    @Test
    void testCoordinatedGroup_threeVehicles_shouldNotCreateAlarm() {
        long base = now - 20 * 60 * 1000L;
        insertAndFire(
                new CrossingEvent("NS-001-BA", "BA-P-010", "RACA", "ABC DOO", "DE", base),
                new CrossingEvent("NS-002-BA", "BA-P-011", "RACA", "ABC DOO", "DE",
                        base + 5 * 60 * 1000L),
                new CrossingEvent("NS-003-BA", "BA-P-012", "RACA", "ABC DOO", "DE",
                        base + 10 * 60 * 1000L)
        );

        List<CepAlarm> alarms = getAlarmsOfType(CepAlarmType.COORDINATED_GROUP);
        assertTrue(alarms.isEmpty(), "3 vehicles should not create the COORDINATED_GROUP alarm.");
    }


    // TEST 7 — BORDER_AVOIDANCE: the same vehicle, 3 different borders in 24 hours → alarm
    @Test
    void testBorderAvoidance_differentBordersIn24Hours_shouldCreateAlarm() {
        insertAndFire(
                new CrossingEvent("BG-456-AB", "BA-P-002", "SEPAK",    "Kompanija AD", "HR",
                        now - 20 * 3600 * 1000L),
                new CrossingEvent("BG-456-AB", "BA-P-002", "BRATUNAC", "Kompanija AD", "HR",
                        now - 12 * 3600 * 1000L),
                new CrossingEvent("BG-456-AB", "BA-P-002", "GRADISKA", "Kompanija AD", "HR",
                        now - 2 * 3600 * 1000L)
        );

        List<CepAlarm> alarms = getAlarmsOfType(CepAlarmType.BORDER_AVOIDANCE);
        assertFalse(alarms.isEmpty(), "BORDER_AVOIDANCE alarm should be created for the vehicle changing 3 borders in 24 hours.");
        assertEquals("BG-456-AB", alarms.get(0).getPlateNumber());
    }


    // TEST 8 — BORDER_AVOIDANCE: the same vehicle and border in 24 hours -> no alarm
    @Test
    void testBorderAvoidance_sameBorderIn24Hours_shouldNotCreateAlarm() {
        insertAndFire(
                new CrossingEvent("BG-789-AB", "BA-P-003", "SEPAK", "Kompanija XY", "DE",
                        now - 10 * 3600 * 1000L),
                new CrossingEvent("BG-789-AB", "BA-P-003", "SEPAK", "Kompanija XY", "DE",
                        now - 2 * 3600 * 1000L)
        );

        List<CepAlarm> alarms = getAlarmsOfType(CepAlarmType.BORDER_AVOIDANCE);
        assertTrue(alarms.isEmpty(), "BORDER_AVOIDANCE alarm should not be created fot the same border.");
    }


    // TEST 9 — BORDER_AVOIDANCE: different borders outside the 24-hour-window → no alarm
    @Test
    void testBorderAvoidance_differentBordersOutsideWindow_shouldNotCreateAlarm() {
        insertAndFire(
                new CrossingEvent("BG-999-AB", "BA-P-004", "SEPAK", "Kompanija ZZ", "HR",
                        now - 30 * 3600 * 1000L),
                new CrossingEvent("BG-999-AB", "BA-P-004", "BRATUNAC", "Kompanija ZZ", "HR",
                        now)
        );

        List<CepAlarm> alarms = getAlarmsOfType(CepAlarmType.BORDER_AVOIDANCE);
        assertTrue(alarms.isEmpty(),
                "Different borders outside the 24-hour-window should not create the BORDER_AVOIDANCE alarm");
    }

}
