
package com.atc.controller;

import com.atc.model.Aircraft;
import com.atc.model.Airport;
import com.atc.model.Gate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gates")
public class GateController {

    private final Airport airport;

    public GateController(Airport airport) {
        this.airport = airport;
    }

    @GetMapping
    public List<Map<String, Object>> getAllGates() {
        List<Map<String, Object>> gateList = new ArrayList<>();

        for (Gate gate : airport.getGates()) {
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("gateId", gate.getGateId());
            details.put("status", gate.getStatus());
            details.put("assignedAircraft",
                gate.getAssignedAircraft() == null ? null :
                gate.getAssignedAircraft().getAircraftId());
            gateList.add(details);
        }

        return gateList;
    }

    @PutMapping("/{gateId}/occupy")
    public ResponseEntity<?> occupyGate(
            @PathVariable String gateId,
            @RequestParam String aircraftId) {

        Gate gate = airport.getGates().stream()
            .filter(g -> g.getGateId().equalsIgnoreCase(gateId))
            .findFirst().orElse(null);

        if (gate == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Gate not found.");
        }

        Aircraft aircraft = airport.searchAircraft(aircraftId);

        if (aircraft == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Aircraft not found.");
        }

        try {
            if (!gate.assignAircraft(aircraft)) {
                return ResponseEntity.badRequest()
                    .body("Gate is unavailable.");
            }

            return ResponseEntity.ok(
                Map.of(
                    "message", "Gate occupied successfully.",
                    "gateId", gateId,
                    "aircraftId", aircraftId
                )
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{gateId}/release")
    public ResponseEntity<?> releaseGate(@PathVariable String gateId) {
        Gate gate = airport.getGates().stream()
            .filter(g -> g.getGateId().equalsIgnoreCase(gateId))
            .findFirst().orElse(null);

        if (gate == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Gate not found.");
        }

        try {
            gate.releaseGate();

            return ResponseEntity.ok(
                Map.of(
                    "message", "Gate released successfully.",
                    "gateId", gateId
                )
            );
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
