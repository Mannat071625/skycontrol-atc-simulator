
package com.atc.controller;

import com.atc.model.Airport;
import com.atc.model.Runway;
import com.atc.model.Gate;
import com.atc.model.Aircraft;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/airport")
public class AirportController {

    private final Airport airport;

    public AirportController(Airport airport) {
        this.airport = airport;
    }

    @GetMapping
    public Map<String, Object> getAirportDetails() {

        Map<String, Object> response = new LinkedHashMap<>();

        ArrayList<Map<String, Object>> runwayDetails = new ArrayList<>();

        for (Runway runway : airport.getRunways()) {
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("runwayId", runway.getRunwayId());
            details.put("length", runway.getLength());
            details.put("status", runway.getStatus());
            runwayDetails.add(details);
        }

        ArrayList<Map<String, Object>> gateDetails = new ArrayList<>();

        for (Gate gate : airport.getGates()) {
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("gateId", gate.getGateId());
            details.put("status", gate.getStatus());
            gateDetails.add(details);
        }

        ArrayList<Map<String, Object>> aircraftDetails = new ArrayList<>();

        for (Aircraft aircraft : airport.getRegisteredAircraft()) {
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("aircraftId", aircraft.getAircraftId());
            details.put("flightNumber", aircraft.getFlightNumber());
            details.put("airlineName", aircraft.getAirlineName());
            details.put("aircraftType", aircraft.getAircraftType());
            details.put("status", aircraft.getStatus());
            aircraftDetails.add(details);
        }

        response.put("airportCode", airport.getAirportCode());
        response.put("airportName", airport.getAirportName());
        response.put("totalRunways", runwayDetails.size());
        response.put("totalGates", gateDetails.size());
        response.put("totalAircraft", aircraftDetails.size());
        response.put("runways", runwayDetails);
        response.put("gates", gateDetails);
        response.put("aircraft", aircraftDetails);

        return response;
    }
}
