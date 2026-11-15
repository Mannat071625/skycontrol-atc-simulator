
package com.atc.controller;

import com.atc.enums.AircraftStatus;
import com.atc.model.Aircraft;
import com.atc.model.Airport;
import com.atc.model.Gate;
import com.atc.repository.AircraftRepository;
import com.atc.service.ControlTower;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/operations")
public class OperationsController {

    private final Airport airport;
    private final ControlTower controlTower;
    private final AircraftRepository aircraftRepository;

    public OperationsController(Airport airport, ControlTower controlTower,
                                AircraftRepository aircraftRepository) {
        this.airport = airport;
        this.controlTower = controlTower;
        this.aircraftRepository = aircraftRepository;
    }

    @PostMapping("/landing/{aircraftId}")
    public ResponseEntity<?> requestLanding(@PathVariable String aircraftId) {

        Aircraft aircraft = airport.searchAircraft(aircraftId);

        if (aircraft == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Aircraft with ID " + aircraftId + " not found.");
        }

        int queueSizeBefore = controlTower.getRunwayQueueSize();

        try {
            controlTower.requestLanding(aircraft);
            aircraftRepository.save(aircraft);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("aircraftId", aircraft.getAircraftId());
            response.put("flightNumber", aircraft.getFlightNumber());
            response.put("status", aircraft.getStatus());

            if (controlTower.getRunwayQueueSize() > queueSizeBefore) {
                response.put("message", "Landing delayed. Aircraft added to runway queue.");
            } else if (aircraft.getStatus() == AircraftStatus.WAITING ||
                       aircraft.getStatus() == AircraftStatus.AIRBORNE) {
                response.put("message", "Landing restricted due to weather conditions.");
            } else {
                response.put("message", "Landing request processed successfully.");
            }

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/prepare-takeoff/{aircraftId}")
    public ResponseEntity<?> prepareTakeoff(@PathVariable String aircraftId) {

        Aircraft aircraft = airport.searchAircraft(aircraftId);

        if (aircraft == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Aircraft with ID " + aircraftId + " not found.");
        }

        if (aircraft.getStatus() != AircraftStatus.TAXIING) {
            return ResponseEntity.badRequest()
                .body("Aircraft must be taxiing towards a gate before preparing for takeoff.");
        }

        boolean hasAssignedGate = false;

        for (Gate gate : airport.getGates()) {
            if (gate.getAssignedAircraft() == aircraft) {
                hasAssignedGate = true;
                break;
            }
        }

        if (!hasAssignedGate) {
            return ResponseEntity.badRequest()
                .body("Aircraft must have an assigned gate before takeoff.");
        }

        aircraft.setStatus(AircraftStatus.READY_FOR_TAKEOFF);
        aircraftRepository.save(aircraft);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Aircraft is ready for takeoff.");
        response.put("aircraftId", aircraft.getAircraftId());
        response.put("flightNumber", aircraft.getFlightNumber());
        response.put("status", aircraft.getStatus());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/takeoff/{aircraftId}")
    public ResponseEntity<?> requestTakeoff(@PathVariable String aircraftId) {

        Aircraft aircraft = airport.searchAircraft(aircraftId);

        if (aircraft == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Aircraft with ID " + aircraftId + " not found.");
        }

        try {
            controlTower.requestTakeoff(aircraft);
            aircraftRepository.save(aircraft);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Takeoff operation processed.");
            response.put("aircraftId", aircraft.getAircraftId());
            response.put("flightNumber", aircraft.getFlightNumber());
            response.put("status", aircraft.getStatus());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
