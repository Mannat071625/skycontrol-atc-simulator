
package com.atc.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Set;

import com.atc.enums.AircraftStatus;
import com.atc.history.FlightHistoryManager;
import com.atc.history.FlightRecord;
import com.atc.model.Airport;
import com.atc.model.EmergencyAircraft;
import com.atc.model.Gate;
import com.atc.model.Runway;

public class EmergencyHandler {

    private final Airport airport;
    private final FlightHistoryManager historyManager;
    private final GateQueueManager gateQueueManager;
    private final PriorityQueue<EmergencyEntry> emergencyQueue;
    private final Set<String> queuedAircraftIds;
    private final WeatherService weatherService;
    private long sequenceCounter;

    private static class EmergencyEntry {
        private final EmergencyAircraft aircraft;
        private final int priority;
        private final long sequenceNumber;

        public EmergencyEntry(EmergencyAircraft aircraft, int priority, long sequenceNumber) {
            this.aircraft = aircraft;
            this.priority = priority;
            this.sequenceNumber = sequenceNumber;
        }
    }

    public EmergencyHandler(Airport airport, FlightHistoryManager historyManager,
                            GateQueueManager gateQueueManager, WeatherService weatherService) {

        if (airport == null || historyManager == null ||
            gateQueueManager == null || weatherService == null) {
            throw new IllegalArgumentException("Dependencies cannot be null.");
        }

        this.airport = airport;
        this.historyManager = historyManager;
        this.gateQueueManager = gateQueueManager;
        this.weatherService = weatherService;

        this.emergencyQueue = new PriorityQueue<>(
            Comparator.comparingInt((EmergencyEntry entry) -> entry.priority)
                .thenComparingLong(entry -> entry.sequenceNumber)
        );

        this.queuedAircraftIds = new HashSet<>();
        this.sequenceCounter = 0;
    }

    private int getEmergencyPriority(EmergencyAircraft aircraft) {
        String type = aircraft.getEmergencyType().toLowerCase(Locale.ROOT);

        if (type.contains("engine failure")) return 1;
        if (type.contains("fuel")) return 2;
        if (type.contains("medical")) return 3;
        if (type.contains("technical")) return 4;
        return 5;
    }

    public synchronized void reportEmergency(EmergencyAircraft aircraft) {
        if (aircraft == null) {
            throw new IllegalArgumentException("Aircraft cannot be null.");
        }

        if (!airport.getRegisteredAircraft().contains(aircraft)) {
            throw new IllegalArgumentException("Aircraft is not registered.");
        }

        if (queuedAircraftIds.contains(aircraft.getAircraftId())) {
            throw new IllegalStateException("Emergency already reported.");
        }

        AircraftStatus status = aircraft.getStatus();

        if (status != AircraftStatus.AIRBORNE &&
            status != AircraftStatus.WAITING &&
            status != AircraftStatus.LANDED &&
            status != AircraftStatus.TAXIING) {
            throw new IllegalStateException(
                "Aircraft cannot report an emergency in its current state."
            );
        }

        aircraft.setStatus(AircraftStatus.EMERGENCY);
        aircraft.reportEmergency();

        int priority = getEmergencyPriority(aircraft);

        emergencyQueue.offer(
            new EmergencyEntry(aircraft, priority, sequenceCounter++)
        );

        queuedAircraftIds.add(aircraft.getAircraftId());

        System.out.println("Emergency Priority: " + priority);
        System.out.println("Emergency added to priority queue.");
    }

    public synchronized void processNextEmergency() {
        if (emergencyQueue.isEmpty()) {
            System.out.println("No pending emergencies.");
            return;
        }

        EmergencyEntry entry = emergencyQueue.poll();
        EmergencyAircraft aircraft = entry.aircraft;

        System.out.println("\n========== EMERGENCY RESPONSE ==========");
        System.out.println("Flight: " + aircraft.getFlightNumber());
        System.out.println("Emergency: " + aircraft.getEmergencyType());

        if (weatherService.isEmergencyLandingRestricted()) {
            aircraft.setStatus(AircraftStatus.EMERGENCY);
            emergencyQueue.offer(entry);
            System.out.println("Emergency landing delayed due to extreme weather conditions.");
            return;
        }

        aircraft.setStatus(AircraftStatus.APPROACHING);

        Runway runway = airport.assignRunway(aircraft);

        if (runway == null) {
            aircraft.setStatus(AircraftStatus.EMERGENCY);
            emergencyQueue.offer(entry);
            System.out.println("Emergency landing delayed. Aircraft remains in priority queue.");
            return;
        }

        try {
            aircraft.setStatus(AircraftStatus.LANDING);

            System.out.println("Emergency landing authorised.");
            System.out.println("Runway assigned: " + runway.getRunwayId());

            aircraft.setStatus(AircraftStatus.LANDED);

            historyManager.addRecord(
                new FlightRecord(
                    aircraft.getFlightNumber(),
                    aircraft.getAircraftId(),
                    "EMERGENCY LANDING",
                    "Emergency aircraft landed on Runway " + runway.getRunwayId()
                )
            );

            System.out.println("Emergency aircraft landed successfully.");
        } finally {
            runway.releaseRunway();
        }

        queuedAircraftIds.remove(aircraft.getAircraftId());

        Gate assignedGate = null;

        for (Gate gate : airport.getGates()) {
            if (gate.getAssignedAircraft() == aircraft) {
                assignedGate = gate;
                break;
            }
        }

        if (assignedGate == null) {
            assignedGate = airport.assignGate(aircraft);
        }

        if (assignedGate != null) {
            aircraft.setStatus(AircraftStatus.TAXIING);

            historyManager.addRecord(
                new FlightRecord(
                    aircraft.getFlightNumber(),
                    aircraft.getAircraftId(),
                    "TAXIING",
                    "Emergency aircraft taxiing towards Gate " + assignedGate.getGateId()
                )
            );

            System.out.println("Emergency aircraft assigned to Gate " + assignedGate.getGateId());
        } else {
            gateQueueManager.addAircraft(aircraft);
            System.out.println("Emergency aircraft is waiting for an available gate.");
        }
    }

    public synchronized int getPendingEmergencyCount() {
        return emergencyQueue.size();
    }

    public synchronized List<EmergencyAircraft> getPendingEmergencies() {
        PriorityQueue<EmergencyEntry> copy = new PriorityQueue<>(emergencyQueue);
        List<EmergencyAircraft> result = new ArrayList<>();

        while (!copy.isEmpty()) {
            result.add(copy.poll().aircraft);
        }

        return result;
    }

    public synchronized void displayEmergencyQueue() {
        System.out.println("\n========== EMERGENCY QUEUE ==========");

        PriorityQueue<EmergencyEntry> copy = new PriorityQueue<>(emergencyQueue);

        if (copy.isEmpty()) {
            System.out.println("No pending emergencies.");
            return;
        }

        while (!copy.isEmpty()) {
            EmergencyEntry entry = copy.poll();

            System.out.println(
                entry.aircraft.getFlightNumber() + " | " +
                entry.aircraft.getEmergencyType() + " | Priority: " +
                entry.priority
            );
        }   
    }
}
