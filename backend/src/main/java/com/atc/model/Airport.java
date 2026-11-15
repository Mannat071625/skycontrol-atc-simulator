
package com.atc.model;

import java.util.ArrayList;
import com.atc.service.AircraftRegistry;

public class Airport {

    private String airportCode;
    private String airportName;
    private ArrayList<Runway> runways;
    private ArrayList<Gate> gates;
    private AircraftRegistry aircraftRegistry;

    public Airport(String airportCode, String airportName) {
        this.airportCode = airportCode;
        this.airportName = airportName;
        this.runways = new ArrayList<>();
        this.gates = new ArrayList<>();
        this.aircraftRegistry = new AircraftRegistry();
    }

    public void addRunway(Runway runway) {
        if (runway == null) {
            throw new IllegalArgumentException("Runway cannot be null.");
        }

        runways.add(runway);
    }

    public void addGate(Gate gate) {
        if (gate == null) {
            throw new IllegalArgumentException("Gate cannot be null.");
        }

        gates.add(gate);
    }

    public void registerAircraft(Aircraft aircraft) {
        aircraftRegistry.registerAircraft(aircraft);
    }

    public Aircraft searchAircraft(String aircraftId) {
        return aircraftRegistry.searchAircraft(aircraftId);
    }

    public void removeAircraft(String aircraftId) {
        aircraftRegistry.removeAircraft(aircraftId);
    }

    public Runway assignRunway(Aircraft aircraft) {

        for (Runway runway : runways) {
            if (runway.assignAircraft(aircraft)) {
                return runway;
            }
        }

        System.out.println("No suitable runway available for Flight " + aircraft.getFlightNumber());
        return null;
    }

    public Gate assignGate(Aircraft aircraft) {

        for (Gate gate : gates) {
            if (gate.assignAircraft(aircraft)) {
                return gate;
            }
        }

        System.out.println("No available gate for Flight " + aircraft.getFlightNumber());
        return null;
    }

    public ArrayList<Runway> getRunways() {
        return new ArrayList<>(runways);
    }

    public ArrayList<Gate> getGates() {
        return new ArrayList<>(gates);
    }

    public ArrayList<Aircraft> getRegisteredAircraft() {
        ArrayList<Aircraft> aircraftList = new ArrayList<>();

        for (Aircraft aircraft : aircraftRegistry.getAllAircraft()) {
            aircraftList.add(aircraft);
        }

        return aircraftList;
    }

    public String getAirportCode() {
        return airportCode;
    }

    public String getAirportName() {
        return airportName;
    }

    public void displayAirportDetails() {

        System.out.println("\n========== AIRPORT DETAILS ==========");
        System.out.println("Airport: " + airportName);
        System.out.println("Airport Code: " + airportCode);

        System.out.println("\n--- RUNWAYS ---");

        for (Runway runway : runways) {
            runway.displayRunwayDetails();
            System.out.println();
        }

        System.out.println("--- GATES ---");

        for (Gate gate : gates) {
            gate.displayGateDetails();
            System.out.println();
        }

        System.out.println("Registered Aircraft: " + aircraftRegistry.getAircraftCount());
    }
}
