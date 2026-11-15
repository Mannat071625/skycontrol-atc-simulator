
package com.atc.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.atc.history.FlightRecord;
import com.atc.service.ControlTower;

@RestController
@RequestMapping("/api/history")
public class FlightHistoryController {

    private final ControlTower controlTower;

    public FlightHistoryController(ControlTower controlTower) {
        this.controlTower = controlTower;
    }

    @GetMapping
    public ResponseEntity<List<FlightRecord>> getAllHistory() {
        return ResponseEntity.ok(
            controlTower.getFlightHistoryRecords()
        );
    }

    @GetMapping("/latest")
    public ResponseEntity<List<FlightRecord>> getLatestHistory() {
        List<FlightRecord> records =
            controlTower.getFlightHistoryRecords();

        records.sort(
            Comparator.comparing(FlightRecord::getTimestamp).reversed()
        );

        return ResponseEntity.ok(records);
    }

    @GetMapping("/{flightNumber}")
    public ResponseEntity<?> searchHistory(
            @PathVariable String flightNumber) {

        List<FlightRecord> matchingRecords =
            controlTower.getFlightHistoryRecords()
                .stream()
                .filter(record ->
                    record.getFlightNumber().equalsIgnoreCase(flightNumber))
                .toList();

        if (matchingRecords.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("No history found for Flight " + flightNumber);
        }

        return ResponseEntity.ok(matchingRecords);
    }
}
