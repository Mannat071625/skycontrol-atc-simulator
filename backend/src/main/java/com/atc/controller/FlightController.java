
package com.atc.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.atc.dto.FlightRequest;
import com.atc.enums.FlightStatus;
import com.atc.model.Aircraft;
import com.atc.model.Airport;
import com.atc.model.Flight;
import com.atc.service.ControlTower;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final Airport airport;
    private final ControlTower controlTower;

    public FlightController(Airport airport, ControlTower controlTower) {
        this.airport = airport;
        this.controlTower = controlTower;
    }

    @PostMapping
    public ResponseEntity<?> scheduleFlight(@Valid @RequestBody FlightRequest request) {
        try {
            Aircraft aircraft = airport.searchAircraft(request.getAircraftId());

            if (aircraft == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Aircraft not found.");
            }

            Flight flight = new Flight(
                request.getFlightNumber(),
                request.getSource(),
                request.getDestination(),
                request.getScheduledDeparture(),
                request.getScheduledArrival(),
                aircraft
            );

            controlTower.scheduleFlight(flight);

            return ResponseEntity.status(HttpStatus.CREATED).body(flight);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Flight>> getAllFlights(
            @RequestParam(required = false) String sortBy) {

        List<Flight> flights = controlTower.getScheduledFlights();

        if ("departure".equalsIgnoreCase(sortBy)) {
            flights.sort(Comparator.comparing(Flight::getScheduledDeparture));
        }

        return ResponseEntity.ok(flights);
    }

    @GetMapping("/{flightNumber}")
    public ResponseEntity<?> getFlight(@PathVariable String flightNumber) {
        Flight flight = controlTower.searchScheduledFlight(flightNumber);

        if (flight == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Flight not found.");
        }

        return ResponseEntity.ok(flight);
    }

    @PutMapping("/{flightNumber}/status")
    public ResponseEntity<?> updateFlightStatus(
            @PathVariable String flightNumber,
            @RequestParam FlightStatus status) {

        Flight flight = controlTower.searchScheduledFlight(flightNumber);

        if (flight == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Flight not found.");
        }

        try {
            controlTower.updateScheduledFlightStatus(flightNumber, status);
            return ResponseEntity.ok(flight);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
