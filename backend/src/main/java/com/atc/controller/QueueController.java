
package com.atc.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.atc.model.Aircraft;
import com.atc.service.ControlTower;

@RestController
@RequestMapping("/api/queues")
public class QueueController {

    private final ControlTower controlTower;

    public QueueController(ControlTower controlTower) {
        this.controlTower = controlTower;
    }

    @GetMapping("/runway")
    public ResponseEntity<List<Aircraft>> getRunwayQueue() {
        return ResponseEntity.ok(
            controlTower.getRunwayWaitingAircraft()
        );
    }

    @GetMapping("/gate")
    public ResponseEntity<List<Aircraft>> getGateQueue() {
        return ResponseEntity.ok(
            controlTower.getGateWaitingAircraft()
        );
    }

    @GetMapping("/runway/size")
    public ResponseEntity<Map<String, Integer>> getRunwayQueueSize() {
        return ResponseEntity.ok(
            Map.of("queueSize", controlTower.getRunwayQueueSize())
        );
    }

    @GetMapping("/gate/size")
    public ResponseEntity<Map<String, Integer>> getGateQueueSize() {
        return ResponseEntity.ok(
            Map.of("queueSize", controlTower.getGateQueueSize())
        );
    }

    @PostMapping("/runway/process")
    public ResponseEntity<?> processRunwayQueue() {
        int before = controlTower.getRunwayQueueSize();

        if (before == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Runway waiting queue is empty.");
        }

        controlTower.processRunwayQueue();

        return ResponseEntity.ok(
            Map.of(
                "message", "Runway queue processing attempted.",
                "previousQueueSize", before,
                "currentQueueSize", controlTower.getRunwayQueueSize()
            )
        );
    }

    @PostMapping("/gate/process")
    public ResponseEntity<?> processGateQueue() {
        int before = controlTower.getGateQueueSize();

        if (before == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Gate waiting queue is empty.");
        }

        controlTower.processGateQueue();

        return ResponseEntity.ok(
            Map.of(
                "message", "Gate queue processing attempted.",
                "previousQueueSize", before,
                "currentQueueSize", controlTower.getGateQueueSize()
            )
        );
    }

    @DeleteMapping("/runway/{aircraftId}")
    public ResponseEntity<?> removeFromRunwayQueue(
            @PathVariable String aircraftId) {

        List<Aircraft> queue = controlTower.getRunwayWaitingAircraft();

        boolean exists = queue.stream().anyMatch(
            aircraft -> aircraft.getAircraftId().equalsIgnoreCase(aircraftId)
        );

        if (!exists) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Aircraft not found in runway waiting queue.");
        }

        controlTower.removeFromRunwayQueue(aircraftId);

        return ResponseEntity.ok(
            Map.of(
                "message", "Aircraft removed from runway waiting queue.",
                "aircraftId", aircraftId,
                "remainingQueueSize", controlTower.getRunwayQueueSize()
            )
        );
    }

    @DeleteMapping("/gate/{aircraftId}")
public ResponseEntity<?> removeFromGateQueue(
        @PathVariable String aircraftId) {

    boolean removed = controlTower.removeFromGateQueue(aircraftId);

    if (!removed) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("Aircraft not found in gate waiting queue.");
    }

    return ResponseEntity.ok(
        Map.of(
            "message", "Aircraft removed from gate waiting queue.",
            "aircraftId", aircraftId,
            "remainingQueueSize", controlTower.getGateQueueSize()
        )
    );
}
}
