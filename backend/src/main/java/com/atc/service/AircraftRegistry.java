
package com.atc.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.atc.model.Aircraft;

public class AircraftRegistry {

    private HashMap<String, Aircraft> aircraftMap;

    public AircraftRegistry() {
        aircraftMap = new HashMap<>();
    }

    public void registerAircraft(Aircraft aircraft) {
        if (aircraft == null) {
            throw new IllegalArgumentException("Aircraft cannot be null.");
        }

        String aircraftId = aircraft.getAircraftId();

        if (aircraftMap.containsKey(aircraftId)) {
            throw new IllegalArgumentException("Aircraft ID already exists.");
        }

        aircraftMap.put(aircraftId, aircraft);
        System.out.println("Aircraft " + aircraftId + " registered successfully.");
    }

    public Aircraft searchAircraft(String aircraftId) {
        return aircraftMap.get(aircraftId);
    }

    public void removeAircraft(String aircraftId) {
        if (aircraftMap.remove(aircraftId) != null) {
            System.out.println("Aircraft " + aircraftId + " removed successfully.");
        } else {
            System.out.println("Aircraft not found.");
        }
    }

    public boolean containsAircraft(String aircraftId) {
        return aircraftMap.containsKey(aircraftId);
    }

    public void displayAllAircraft() {
        if (aircraftMap.isEmpty()) {
            System.out.println("No aircraft registered.");
            return;
        }

        for (Map.Entry<String, Aircraft> entry : aircraftMap.entrySet()) {
            System.out.println("------------------------------");
            entry.getValue().displayDetails();
        }
    }

    public int getAircraftCount() {
        return aircraftMap.size();
    }
    public ArrayList<Aircraft> getAllAircraft() {
    return new ArrayList<>(aircraftMap.values());
}
}
