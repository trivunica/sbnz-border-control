package com.ftn.sbnz.service;

import com.ftn.sbnz.model.enums.TransportPermitType;
import com.ftn.sbnz.model.models.*;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/border")
@CrossOrigin(origins = "http://localhost:4200")
public class BorderCrossingController {

    @Autowired
    private BorderCrossingService borderCrossingService;

    @Autowired
    private CepService cepService;


    @PostMapping("/evaluate")
    public ResponseEntity<BorderCrossingResult> evaluate(
            @RequestBody BorderCrossingRequest request) {
        return ResponseEntity.ok(borderCrossingService.evaluate(request));
    }


    @PostMapping("/crossing-event")
    public ResponseEntity<Void> registerCrossing(
            @RequestBody CrossingEvent event) {
        cepService.registerCrossing(event);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/alarms")
    public ResponseEntity<List<CepAlarm>> getAlarms() {
        return ResponseEntity.ok(cepService.getActiveAlarms());
    }


    @GetMapping("/alarms/{plateNumber}")
    public ResponseEntity<List<CepAlarm>> getAlarmsForVehicle(
            @PathVariable String plateNumber) {
        return ResponseEntity.ok(cepService.getAlarmsForVehicle(plateNumber));
    }

}
