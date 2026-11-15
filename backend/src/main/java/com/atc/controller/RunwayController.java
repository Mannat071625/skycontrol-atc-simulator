
package com.atc.controller;

import com.atc.model.Aircraft;
import com.atc.model.Airport;
import com.atc.model.Runway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/runways")
public class RunwayController {

    private final Airport airport;

    public RunwayController(Airport airport) {
        this.airport = airport;
    }

    @GetMapping
    public List<Map<String, Object>> getAllRunways() {
        synchronized (airport) {
            List<Map<String, Object>> runwayList = new ArrayList<>();

            for (Runway runway : airport.getRunways()) {
                Map<String, Object> details = new LinkedHashMap<>();
                details.put("runwayId", runway.getRunwayId());
                details.put("length", runway.getLength());
                details.put("status", runway.getStatus());
                details.put("assignedAircraft",
                    runway.getAssignedAircraft() == null ? null :
                    runway.getAssignedAircraft().getAircraftId());
                runwayList.add(details);
            }

            return runwayList;
        }
    }

    @PutMapping("/{runwayId}/occupy")
    public ResponseEntity<?> occupyRunway(
            @PathVariable String runwayId,
            @RequestParam String aircraftId) {

        synchronized (airport) {
            Runway runway = airport.getRunways().stream()
                .filter(r -> r.getRunwayId().equalsIgnoreCase(runwayId))
                .findFirst().orElse(null);

            if (runway == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Runway not found.");
            }

            Aircraft aircraft = airport.searchAircraft(aircraftId);

            if (aircraft == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Aircraft not found.");
            }

            for (Runway existing : airport.getRunways()) {
                Aircraft assigned = existing.getAssignedAircraft();

                if (assigned != null &&
                    assigned.getAircraftId().equalsIgnoreCase(aircraft.getAircraftId())) {
                    return ResponseEntity.badRequest().body(
                        "Aircraft " + aircraft.getAircraftId() +
                        " is already assigned to runway " + existing.getRunwayId() + "."
                    );
                }
            }

            try {
                if (!runway.assignAircraft(aircraft)) {
                    return ResponseEntity.badRequest()
                        .body("Runway is unavailable or aircraft requires a longer runway.");
                }

                return ResponseEntity.ok(
                    Map.of("message", "Runway occupied successfully.",
                           "runwayId", runway.getRunwayId(),
                           "aircraftId", aircraft.getAircraftId())
                );
            } catch (IllegalArgumentException | IllegalStateException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
    }

    @PutMapping("/{runwayId}/release")
    public ResponseEntity<?> releaseRunway(@PathVariable String runwayId) {
        synchronized (airport) {
            Runway runway = airport.getRunways().stream()
                .filter(r -> r.getRunwayId().equalsIgnoreCase(runwayId))
                .findFirst().orElse(null);

            if (runway == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Runway not found.");
            }

            try {
                runway.releaseRunway();

                return ResponseEntity.ok(
                    Map.of("message", "Runway released successfully.",
                           "runwayId", runway.getRunwayId())
                );
            } catch (IllegalStateException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
    }
}
