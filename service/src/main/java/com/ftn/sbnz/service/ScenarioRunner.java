package com.ftn.sbnz.service;


import com.ftn.sbnz.model.models.*;
import com.ftn.sbnz.model.enums.PermitType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;


@Component
public class ScenarioRunner implements CommandLineRunner {

    @Autowired
    private BorderCrossingService borderCrossingService;

    @Override
    public void run(String... args) {
        printHeader("Ekspertski sistem za kontrolu teretnih vozila na graničnom prelazu");

        scenario1_InterpolArrest();
        scenario2_SpecificationScenario();
        scenario3_EverythingOK();
        scenario4_DocumentForgery();
        scenario5_TrailerOverload();
        scenario6_ExpiredIDValidVehicle();
        scenario7_InsufficientFunds();
    }

    // ************************************************************************************
    // SCENARIO 1 — Driver on Interpol warrant
    // ************************************************************************************
    private void scenario1_InterpolArrest() {
        printScenarioHeader(1, "Vozač na Interpol potrazi");
        System.out.println("  Ulazni podaci:");
        System.out.println("  - Strani vozač, interpolWarrant = true");
        System.out.println("  - Svi dokumenti validni");
        System.out.println("  Očekivano: ARREST");

        Driver driver = new Driver();
        driver.setName("John");
        driver.setSurname("Doe");
        driver.setCitizenship("DE");
        driver.setForeignCitizen(true);
        driver.setInterpolWarrant(true);
        driver.setDomesticWarrant(false);
        driver.setPhotoMatches(true);
        driver.setDocumentReportedStolen(false);
        driver.setHasVisa(true);
        driver.setHasSupplementaryDocument(false);
        driver.setFinancialFunds(500.0);
        driver.setPlannedStayDays(3);

        DrivingLicence dl = new DrivingLicence();
        dl.setLicenceNumber("DE-123456");
        dl.setExpiryDate(LocalDate.now().plusYears(2));
        dl.setCategory("CE");

        IdentificationDocument id = new IdentificationDocument();
        id.setDocumentNumber("P-DE-123");
        id.setType("PASSPORT");
        id.setIssuingCountry("DE");
        id.setExpiryDate(LocalDate.now().plusYears(3));

        BorderCrossingRequest request = new BorderCrossingRequest();
        request.setDriver(driver);
        request.setDrivingLicence(dl);
        request.setIdentificationDocument(id);

        printResult(borderCrossingService.evaluate(request));
    }

    // ************************************************************************************************
    // SCENARIO 2 — Specification scenario
    // Truck from Serbia, goods received in North Macedonia, destination Bosnia and Herzegovina
    // Expired driving licence + trailer overload + bilateral permit doesn't cover a third country
    // ************************************************************************************************
    private void scenario2_SpecificationScenario() {
        printScenarioHeader(2, "Scenario iz specifikacije — Srbija → BiH, roba iz MK");
        System.out.println("  Ulazni podaci:");
        System.out.println("  - Vozač iz Srbije, istekla vozačka (30.11.2024)");
        System.out.println("  - Roba iz Severne Makedonije, odredište BiH");
        System.out.println("  - Masa robe 26500 kg, nosivost prikolice 24000 kg");
        System.out.println("  - Bilateralna dozvola Srbija-BiH (ne pokriva MK)");
        System.out.println("  Očekivano: FORBID_ENTRY, 3 kršenja");

        Driver driver = new Driver();
        driver.setName("Marko");
        driver.setSurname("Marković");
        driver.setCitizenship("RS");
        driver.setForeignCitizen(true);
        driver.setInterpolWarrant(false);
        driver.setDomesticWarrant(false);
        driver.setPhotoMatches(true);
        driver.setDocumentReportedStolen(false);
        driver.setHasVisa(false);
        driver.setHasSupplementaryDocument(true);
        driver.setFinancialFunds(500.0);
        driver.setPlannedStayDays(3);

        DrivingLicence dl = new DrivingLicence();
        dl.setLicenceNumber("RS-789");
        dl.setExpiryDate(LocalDate.of(2024, 11, 30));
        dl.setCategory("CE");

        IdentificationDocument id = new IdentificationDocument();
        id.setDocumentNumber("RS-P-001");
        id.setType("PASSPORT");
        id.setIssuingCountry("RS");
        id.setExpiryDate(LocalDate.now().plusYears(2));

        VehicleRegistration vr = new VehicleRegistration();
        vr.setRegistrationNumber("NS-123-AB");
        vr.setExpiryDate(LocalDate.of(2026, 8, 15));
        vr.setTrailerCapacity(24000.0);
        vr.setTruckWeight(8200.0);

        CMRDocument cmr = new CMRDocument();
        cmr.setOriginCountry("MK");
        cmr.setDestinationCountry("BA");
        cmr.setGoodsWeight(26500.0);
        cmr.setGoodsDescription("Građevinski materijal");

        TransportPermit bilateral = new TransportPermit();
        bilateral.setType(PermitType.BILATERAL);
        bilateral.setExpiryDate(LocalDate.of(2027, 1, 1));
        bilateral.setCoveredRoutes("RS-BA");

        BorderCrossingRequest request = new BorderCrossingRequest();
        request.setDriver(driver);
        request.setDrivingLicence(dl);
        request.setIdentificationDocument(id);
        request.setVehicleRegistration(vr);
        request.setCmrDocument(cmr);
        request.setTransportPermits(List.of(bilateral));

        printResult(borderCrossingService.evaluate(request));
    }

    // ************************************************************************************
    // SCENARIO 3 — Everything is OK
    // CEMT permit, valid documents, no overload
    // ************************************************************************************
    private void scenario3_EverythingOK() {
        printScenarioHeader(3, "Sve u redu");
        System.out.println("  Ulazni podaci:");
        System.out.println("  - Strani vozač, svi dokumenti validni");
        System.out.println("  - Masa robe 20000 kg, nosivost 24000 kg (nema pretovara)");
        System.out.println("  - CEMT dozvola validna");
        System.out.println("  Očekivano: ALLOW, nema kršenja");

        Driver driver = new Driver();
        driver.setName("Hans");
        driver.setSurname("Mueller");
        driver.setCitizenship("DE");
        driver.setForeignCitizen(true);
        driver.setInterpolWarrant(false);
        driver.setDomesticWarrant(false);
        driver.setPhotoMatches(true);
        driver.setDocumentReportedStolen(false);
        driver.setHasVisa(true);
        driver.setHasSupplementaryDocument(false);
        driver.setFinancialFunds(1000.0);
        driver.setPlannedStayDays(5);

        DrivingLicence dl = new DrivingLicence();
        dl.setLicenceNumber("DE-999");
        dl.setExpiryDate(LocalDate.now().plusYears(3));
        dl.setCategory("CE");

        IdentificationDocument id = new IdentificationDocument();
        id.setDocumentNumber("DE-P-999");
        id.setType("PASSPORT");
        id.setIssuingCountry("DE");
        id.setExpiryDate(LocalDate.now().plusYears(5));

        VehicleRegistration vr = new VehicleRegistration();
        vr.setRegistrationNumber("MU-AB-1234");
        vr.setExpiryDate(LocalDate.now().plusYears(1));
        vr.setTrailerCapacity(24000.0);
        vr.setTruckWeight(8000.0);

        CMRDocument cmr = new CMRDocument();
        cmr.setOriginCountry("DE");
        cmr.setDestinationCountry("BA");
        cmr.setGoodsWeight(20000.0);
        cmr.setGoodsDescription("Elektronika");

        TransportPermit cemt = new TransportPermit();
        cemt.setType(PermitType.CEMT);
        cemt.setExpiryDate(LocalDate.now().plusYears(1));
        cemt.setCoveredRoutes("ALL");

        BorderCrossingRequest request = new BorderCrossingRequest();
        request.setDriver(driver);
        request.setDrivingLicence(dl);
        request.setIdentificationDocument(id);
        request.setVehicleRegistration(vr);
        request.setCmrDocument(cmr);
        request.setTransportPermits(List.of(cemt));

        printResult(borderCrossingService.evaluate(request));
    }

    // ************************************************************************************
    // SCENARIO 4 — Document forgery suspicion
    // photoMatches = false
    // ************************************************************************************
    private void scenario4_DocumentForgery() {
        printScenarioHeader(4, "Sumnja na falsifikat dokumenta");
        System.out.println("  Ulazni podaci:");
        System.out.println("  - photoMatches = false");
        System.out.println("  - Ostali dokumenti validni");
        System.out.println("  Očekivano: FORBID_ENTRY, kršenje SUSPECTED_FORGERY");

        Driver driver = new Driver();
        driver.setName("Unknown");
        driver.setSurname("Person");
        driver.setCitizenship("DE");
        driver.setForeignCitizen(true);
        driver.setInterpolWarrant(false);
        driver.setDomesticWarrant(false);
        driver.setPhotoMatches(false);
        driver.setDocumentReportedStolen(false);
        driver.setHasVisa(true);
        driver.setHasSupplementaryDocument(false);
        driver.setFinancialFunds(300.0);
        driver.setPlannedStayDays(2);

        DrivingLicence dl = new DrivingLicence();
        dl.setLicenceNumber("DE-111");
        dl.setExpiryDate(LocalDate.now().plusYears(1));
        dl.setCategory("CE");

        IdentificationDocument id = new IdentificationDocument();
        id.setDocumentNumber("DE-P-111");
        id.setType("PASSPORT");
        id.setIssuingCountry("DE");
        id.setExpiryDate(LocalDate.now().plusYears(2));

        VehicleRegistration vr = new VehicleRegistration();
        vr.setRegistrationNumber("DE-111-AB");
        vr.setExpiryDate(LocalDate.now().plusYears(1));
        vr.setTrailerCapacity(24000.0);
        vr.setTruckWeight(8000.0);

        CMRDocument cmr = new CMRDocument();
        cmr.setOriginCountry("DE");
        cmr.setDestinationCountry("BA");
        cmr.setGoodsWeight(20000.0);
        cmr.setGoodsDescription("Opis robe");

        TransportPermit cemt = new TransportPermit();
        cemt.setType(PermitType.CEMT);
        cemt.setExpiryDate(LocalDate.now().plusYears(1));
        cemt.setCoveredRoutes("ALL");

        BorderCrossingRequest request = new BorderCrossingRequest();
        request.setDriver(driver);
        request.setDrivingLicence(dl);
        request.setIdentificationDocument(id);
        request.setVehicleRegistration(vr);
        request.setCmrDocument(cmr);
        request.setTransportPermits(List.of(cemt));

        printResult(borderCrossingService.evaluate(request));
    }

    // ************************************************************************************
    // SCENARIO 5 — Overload but can continue
    // Just the trailer overload, all documents and permits valid
    // ************************************************************************************
    private void scenario5_TrailerOverload() {
        printScenarioHeader(5, "Samo pretovar — plaća kaznu i nastavlja");
        System.out.println("  Ulazni podaci:");
        System.out.println("  - Strani vozač, svi dokumenti validni");
        System.out.println("  - Masa robe 27000 kg, nosivost 24000 kg (pretovar 3000 kg)");
        System.out.println("  - CEMT dozvola validna");
        System.out.println("  Očekivano: HOLD_FINE, kršenje TRAILER_OVERLOAD, canContinue = true");

        Driver driver = new Driver();
        driver.setName("Pierre");
        driver.setSurname("Dupont");
        driver.setCitizenship("FR");
        driver.setForeignCitizen(true);
        driver.setInterpolWarrant(false);
        driver.setDomesticWarrant(false);
        driver.setPhotoMatches(true);
        driver.setDocumentReportedStolen(false);
        driver.setHasVisa(true);
        driver.setHasSupplementaryDocument(false);
        driver.setFinancialFunds(800.0);
        driver.setPlannedStayDays(4);

        DrivingLicence dl = new DrivingLicence();
        dl.setLicenceNumber("FR-555");
        dl.setExpiryDate(LocalDate.now().plusYears(2));
        dl.setCategory("CE");

        IdentificationDocument id = new IdentificationDocument();
        id.setDocumentNumber("FR-P-555");
        id.setType("PASSPORT");
        id.setIssuingCountry("FR");
        id.setExpiryDate(LocalDate.now().plusYears(4));

        VehicleRegistration vr = new VehicleRegistration();
        vr.setRegistrationNumber("PA-789-FR");
        vr.setExpiryDate(LocalDate.now().plusYears(1));
        vr.setTrailerCapacity(24000.0);
        vr.setTruckWeight(7500.0);

        CMRDocument cmr = new CMRDocument();
        cmr.setOriginCountry("FR");
        cmr.setDestinationCountry("BA");
        cmr.setGoodsWeight(27000.0);
        cmr.setGoodsDescription("Prehrambeni proizvodi");

        TransportPermit cemt = new TransportPermit();
        cemt.setType(PermitType.CEMT);
        cemt.setExpiryDate(LocalDate.now().plusYears(1));
        cemt.setCoveredRoutes("ALL");

        BorderCrossingRequest request = new BorderCrossingRequest();
        request.setDriver(driver);
        request.setDrivingLicence(dl);
        request.setIdentificationDocument(id);
        request.setVehicleRegistration(vr);
        request.setCmrDocument(cmr);
        request.setTransportPermits(List.of(cemt));

        printResult(borderCrossingService.evaluate(request));
    }


    // ************************************************************************************
    // SCENARIO 6 — Expired ID card but valid vehicle and permit
    // ************************************************************************************
    private void scenario6_ExpiredIDValidVehicle() {
        printScenarioHeader(6, "Istekla lična karta — vozilo i dozvole validni");
        System.out.println("  Ulazni podaci:");
        System.out.println("  - Istekla lična karta");
        System.out.println("  - Vozilo, teret i dozvole potpuno validni");
        System.out.println("  Očekivano: FORBID_ENTRY, DriverValidity=FORBIDDEN");

        Driver driver = new Driver();
        driver.setName("Petar");
        driver.setSurname("Petrović");
        driver.setCitizenship("BA");
        driver.setForeignCitizen(false);
        driver.setInterpolWarrant(false);
        driver.setDomesticWarrant(false);
        driver.setPhotoMatches(true);
        driver.setDocumentReportedStolen(false);
        driver.setHasVisa(false);
        driver.setHasSupplementaryDocument(false);
        driver.setFinancialFunds(500.0);
        driver.setPlannedStayDays(2);

        DrivingLicence dl = new DrivingLicence();
        dl.setLicenceNumber("BA-333");
        dl.setExpiryDate(LocalDate.now().plusYears(2));
        dl.setCategory("CE");

        IdentificationDocument id = new IdentificationDocument();
        id.setDocumentNumber("BA-ID-333");
        id.setType("ID_CARD");
        id.setIssuingCountry("BA");
        id.setExpiryDate(LocalDate.of(2023, 6, 1));

        VehicleRegistration vr = new VehicleRegistration();
        vr.setRegistrationNumber("SA-100-BA");
        vr.setExpiryDate(LocalDate.now().plusYears(1));
        vr.setTrailerCapacity(24000.0);
        vr.setTruckWeight(8000.0);

        CMRDocument cmr = new CMRDocument();
        cmr.setOriginCountry("BA");
        cmr.setDestinationCountry("HR");
        cmr.setGoodsWeight(18000.0);
        cmr.setGoodsDescription("Drvna građa");

        TransportPermit cemt = new TransportPermit();
        cemt.setType(PermitType.CEMT);
        cemt.setExpiryDate(LocalDate.now().plusYears(1));
        cemt.setCoveredRoutes("ALL");

        BorderCrossingRequest request = new BorderCrossingRequest();
        request.setDriver(driver);
        request.setDrivingLicence(dl);
        request.setIdentificationDocument(id);
        request.setVehicleRegistration(vr);
        request.setCmrDocument(cmr);
        request.setTransportPermits(List.of(cemt));

        printResult(borderCrossingService.evaluate(request));
    }


    // ************************************************************************************
    // SCENARIO 7 — Insufficient financial funds
    // Foreign driver is planning a 10-day stay but has 500 EUR instead of minimum 750 EUR
    // ************************************************************************************
    private void scenario7_InsufficientFunds() {
        printScenarioHeader(7, "Nedovoljna finansijska sredstva");
        System.out.println("  Ulazni podaci:");
        System.out.println("  - Strani vozač, planira boravak 10 dana");
        System.out.println("  - Ima 500 EUR, treba minimum 750 EUR (10 * 75)");
        System.out.println("  - Vozilo i dozvole validni");
        System.out.println("  Očekivano: FORBID_ENTRY, DriverValidity=FORBIDDEN");

        Driver driver = new Driver();
        driver.setName("Hans");
        driver.setSurname("Mueller");
        driver.setCitizenship("DE");
        driver.setForeignCitizen(true);
        driver.setInterpolWarrant(false);
        driver.setDomesticWarrant(false);
        driver.setPhotoMatches(true);
        driver.setDocumentReportedStolen(false);
        driver.setHasVisa(true);
        driver.setHasSupplementaryDocument(false);
        driver.setFinancialFunds(500.0);
        driver.setPlannedStayDays(10);

        DrivingLicence dl = new DrivingLicence();
        dl.setLicenceNumber("DE-777");
        dl.setExpiryDate(LocalDate.now().plusYears(2));
        dl.setCategory("CE");

        IdentificationDocument id = new IdentificationDocument();
        id.setDocumentNumber("DE-P-777");
        id.setType("PASSPORT");
        id.setIssuingCountry("DE");
        id.setExpiryDate(LocalDate.now().plusYears(3));

        VehicleRegistration vr = new VehicleRegistration();
        vr.setRegistrationNumber("DE-777-AB");
        vr.setExpiryDate(LocalDate.now().plusYears(1));
        vr.setTrailerCapacity(24000.0);
        vr.setTruckWeight(8000.0);

        CMRDocument cmr = new CMRDocument();
        cmr.setOriginCountry("DE");
        cmr.setDestinationCountry("BA");
        cmr.setGoodsWeight(20000.0);
        cmr.setGoodsDescription("Tekstil");

        TransportPermit cemt = new TransportPermit();
        cemt.setType(PermitType.CEMT);
        cemt.setExpiryDate(LocalDate.now().plusYears(1));
        cemt.setCoveredRoutes("ALL");

        BorderCrossingRequest request = new BorderCrossingRequest();
        request.setDriver(driver);
        request.setDrivingLicence(dl);
        request.setIdentificationDocument(id);
        request.setVehicleRegistration(vr);
        request.setCmrDocument(cmr);
        request.setTransportPermits(List.of(cemt));

        printResult(borderCrossingService.evaluate(request));
    }


    private void printResult(BorderCrossingResult result) {
        System.out.println("\n  Zaključivanje po nivoima:");
        System.out.println("    Nivo 1 — DriverValidity : "
                + (result.getDriverValidity() != null
                ? result.getDriverValidity().getStatus() : "nije kreiran"));
        System.out.println("    Nivo 2 — VehicleValidity: "
                + (result.getVehicleValidity() != null
                ? result.getVehicleValidity().getStatus() : "nije kreiran (preskočen)"));
        System.out.println("    Nivo 3 — PermitStatus   : "
                + (result.getPermitStatus() != null
                ? result.getPermitStatus().getStatus() : "nije kreiran (preskočen)"));

        System.out.println("\n  Utvrđena kršenja:");
        if (result.getViolations() == null || result.getViolations().isEmpty()) {
            System.out.println("    Nema kršenja.");
        } else {
            result.getViolations().forEach(v -> {
                System.out.println("    [" + v.getType() + "]");
                System.out.println("      Zakonski osnov : " + v.getLegalBasis());
                System.out.println("      Kazna          : " + v.getFineAmount() + " BAM");
                System.out.println("      Može nastaviti : " + v.getCanContinue());
                System.out.println("      Objašnjenje    : " + v.getExplanation());
            });
        }

        System.out.println("\n  Finalna odluka:");
        if (result.getFinalDecision() != null) {
            System.out.println("    Preporuka    : " + result.getFinalDecision().getRecommendation());
            System.out.println("    Ukupna kazna : " + result.getFinalDecision().getTotalFine() + " BAM");
            if (result.getFinalDecision().getEntryRefusalCertificate() != null)
                System.out.println("    Potvrda      : " + result.getFinalDecision().getEntryRefusalCertificate());
        } else {
            System.out.println("    FinalDecision nije kreiran!");
        }
    }

    private void printScenarioHeader(int number, String title) {
        System.out.println("\n" + "*".repeat(75));
        System.out.println("  SCENARIO " + number + " — " + title);
        System.out.println("*".repeat(75));
    }

    private void printHeader(String title) {
        System.out.println("\n" + "*".repeat(75));
        System.out.println("  " + title);
        System.out.println("*".repeat(75));
    }
}