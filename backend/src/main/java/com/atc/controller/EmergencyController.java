
package com.atc.controller;

import com.atc.model.Aircraft;
import com.atc.model.Airport;
import com.atc.model.EmergencyAircraft;
import com.atc.repository.AircraftRepository;
import com.atc.service.ControlTower;
import com.atc.service.EmergencyHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emergencies")
public class EmergencyController {

    private final Airport airport;
    private final EmergencyHandler emergencyHandler;
    private final AircraftRepository aircraftRepository;

    public EmergencyController(Airport airport, ControlTower controlTower,
                               AircraftRepository aircraftRepository) {
        this.airport = airport;
        this.emergencyHandler = controlTower.getEmergencyHandler();
        this.aircraftRepository = aircraftRepository;
    }

    @PostMapping
    public ResponseEntity<?> reportEmergency(@RequestBody Map<String, String> request) {
        String aircraftId = request.get("aircraftId");

        if (aircraftId == null || aircraftId.isBlank()) {
            return ResponseEntity.badRequest().body("Aircraft ID is required.");
        }

        Aircraft aircraft = airport.searchAircraft(aircraftId);

        if (aircraft == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Aircraft with ID " + aircraftId + " not found.");
        }

        if (!(aircraft instanceof EmergencyAircraft)) {
            return ResponseEntity.badRequest()
                .body("Only emergency aircraft can report an emergency.");
        }

        try {
            emergencyHandler.reportEmergency((EmergencyAircraft) aircraft);
            aircraftRepository.save(aircraft);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Emergency reported successfully.");
            response.put("aircraftId", aircraft.getAircraftId());
            response.put("flightNumber", aircraft.getFlightNumber());
            response.put("status", aircraft.getStatus());
            response.put("pendingEmergencies",
                emergencyHandler.getPendingEmergencyCount());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/process")
    public ResponseEntity<?> processNextEmergency() {
        List<EmergencyAircraft> pending = emergencyHandler.getPendingEmergencies();

        if (pending.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("No pending emergencies.");
        }

        EmergencyAircraft aircraft = pending.get(0);

        emergencyHandler.processNextEmergency();
        aircraftRepository.save(aircraft);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Emergency processing attempted.");
        response.put("aircraftId", aircraft.getAircraftId());
        response.put("status", aircraft.getStatus());
        response.put("pendingEmergencies",
            emergencyHandler.getPendingEmergencyCount());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public List<Map<String, Object>> getPendingEmergencies() {
        List<Map<String, Object>> emergencyList = new ArrayList<>();

        for (EmergencyAircraft aircraft : emergencyHandler.getPendingEmergencies()) {
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("aircraftId", aircraft.getAircraftId());
            details.put("flightNumber", aircraft.getFlightNumber());
            details.put("emergencyType", aircraft.getEmergencyType());
            details.put("status", aircraft.getStatus());
            emergencyList.add(details);
        }

        return emergencyList;
    }
}
