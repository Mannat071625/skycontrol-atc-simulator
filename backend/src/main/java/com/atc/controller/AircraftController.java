
package com.atc.controller;

import com.atc.dto.AircraftRequest;
import com.atc.model.Aircraft;
import com.atc.model.Airport;
import com.atc.model.PassengerAircraft;
import com.atc.model.CargoAircraft;
import com.atc.model.EmergencyAircraft;
import com.atc.repository.AircraftRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/aircraft")
public class AircraftController {

    private final Airport airport;
    private final AircraftRepository aircraftRepository;

    public AircraftController(Airport airport, AircraftRepository aircraftRepository) {
        this.airport = airport;
        this.aircraftRepository = aircraftRepository;
    }

    @PostMapping
    public ResponseEntity<?> registerAircraft(@Valid @RequestBody AircraftRequest request) {

        Aircraft aircraft;

        String type = request.getAircraftType().toUpperCase();

        switch (type) {
            case "PASSENGER":
                if (request.getPassengerCapacity() == null) {
                    return ResponseEntity.badRequest().body("Passenger capacity is required.");
                }
                aircraft = new PassengerAircraft(
                    request.getAircraftId(),
                    request.getFlightNumber(),
                    request.getAirlineName(),
                    request.getFuelLevel(),
                    request.getPassengerCapacity()
                );
                break;

            case "CARGO":
                if (request.getCargoCapacity() == null) {
                    return ResponseEntity.badRequest().body("Cargo capacity is required.");
                }
                aircraft = new CargoAircraft(
                    request.getAircraftId(),
                    request.getFlightNumber(),
                    request.getAirlineName(),
                    request.getFuelLevel(),
                    request.getCargoCapacity()
                );
                break;

            case "EMERGENCY":
                if (request.getEmergencyType() == null || request.getEmergencyType().isBlank()) {
                    return ResponseEntity.badRequest().body("Emergency type is required.");
                }
                aircraft = new EmergencyAircraft(
                    request.getAircraftId(),
                    request.getFlightNumber(),
                    request.getAirlineName(),
                    request.getFuelLevel(),
                    request.getEmergencyType()
                );
                break;

            default:
                return ResponseEntity.badRequest().body("Invalid aircraft type.");
        }

        if (aircraftRepository.existsById(aircraft.getAircraftId())) {
            return ResponseEntity.badRequest().body("Aircraft ID already exists.");
        }

        aircraftRepository.save(aircraft);
        airport.registerAircraft(aircraft);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Aircraft registered successfully.");
        response.put("aircraftId", aircraft.getAircraftId());
        response.put("flightNumber", aircraft.getFlightNumber());
        response.put("aircraftType", aircraft.getAircraftType());
        response.put("status", aircraft.getStatus());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<Map<String, Object>> getAllAircraft() {

        List<Map<String, Object>> aircraftList = new ArrayList<>();

        for (Aircraft aircraft : aircraftRepository.findAll()) {
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("aircraftId", aircraft.getAircraftId());
            details.put("flightNumber", aircraft.getFlightNumber());
            details.put("airlineName", aircraft.getAirlineName());
            details.put("aircraftType", aircraft.getAircraftType());
            details.put("fuelLevel", aircraft.getFuelLevel());
            details.put("status", aircraft.getStatus());
            aircraftList.add(details);
        }

        return aircraftList;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAircraftById(@PathVariable String id) {

        Aircraft aircraft = aircraftRepository.findById(id).orElse(null);

        if (aircraft == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Aircraft with ID " + id + " not found.");
        }

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("aircraftId", aircraft.getAircraftId());
        details.put("flightNumber", aircraft.getFlightNumber());
        details.put("airlineName", aircraft.getAirlineName());
        details.put("aircraftType", aircraft.getAircraftType());
        details.put("fuelLevel", aircraft.getFuelLevel());
        details.put("status", aircraft.getStatus());

        return ResponseEntity.ok(details);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAircraft(@PathVariable String id) {

        if (!aircraftRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Aircraft with ID " + id + " not found.");
        }

        aircraftRepository.deleteById(id);
        airport.removeAircraft(id);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Aircraft removed successfully.");
        response.put("aircraftId", id);

        return ResponseEntity.ok(response);
    }
}
