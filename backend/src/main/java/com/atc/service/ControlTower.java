
package com.atc.service;

import java.util.ArrayList;

import com.atc.enums.AircraftStatus;
import com.atc.enums.FlightStatus;
import com.atc.enums.WeatherCondition;
import com.atc.exceptions.WeatherRestrictionException;
import com.atc.history.FlightHistoryManager;
import com.atc.history.FlightRecord;
import com.atc.model.Aircraft;
import com.atc.model.Airport;
import com.atc.model.EmergencyAircraft;
import com.atc.model.Flight;
import com.atc.model.Gate;
import com.atc.model.Runway;
import com.atc.repository.FlightHistoryRepository;
import com.atc.repository.FlightRepository;

public class ControlTower {
    private Airport airport;
    private EmergencyHandler emergencyHandler;
    private WeatherService weatherService;
    private FlightHistoryManager historyManager;
    private RunwayQueueManager runwayQueueManager;
    private GateQueueManager gateQueueManager;
    private FlightScheduler flightScheduler;

    public ControlTower(Airport airport) {
        this(airport, new WeatherService(), null, null);
    }

    public ControlTower(Airport airport, WeatherService weatherService,
                        FlightRepository flightRepository,
                        FlightHistoryRepository flightHistoryRepository) {

        if (airport == null || weatherService == null) {
            throw new IllegalArgumentException("Dependencies cannot be null.");
        }

        this.airport = airport;
        this.weatherService = weatherService;

        this.historyManager = flightHistoryRepository == null
            ? new FlightHistoryManager()
            : new FlightHistoryManager(flightHistoryRepository);

        this.runwayQueueManager = new RunwayQueueManager();
        this.gateQueueManager = new GateQueueManager();

        this.flightScheduler = flightRepository == null
            ? new FlightScheduler()
            : new FlightScheduler(flightRepository);

        this.emergencyHandler = new EmergencyHandler(
            airport,
            historyManager,
            gateQueueManager,
            weatherService
        );
    }

    public void requestLanding(Aircraft aircraft) {
        if (aircraft == null) {
            throw new IllegalArgumentException("Aircraft cannot be null.");
        }

        if (!airport.getRegisteredAircraft().contains(aircraft)) {
            throw new IllegalArgumentException("Aircraft is not registered at this airport.");
        }

        if (aircraft.getStatus() != AircraftStatus.WAITING &&
            aircraft.getStatus() != AircraftStatus.AIRBORNE) {
            throw new IllegalStateException("Aircraft is not eligible for landing.");
        }

        try {
            if (weatherService.isLandingRestricted()) {
                throw new WeatherRestrictionException("Landing denied due to unsafe weather conditions.");
            }
        } catch (WeatherRestrictionException e) {
            System.out.println(e.getMessage());
            return;
        }

        System.out.println("\nLanding request received for Flight " + aircraft.getFlightNumber());

        aircraft.setStatus(AircraftStatus.APPROACHING);

        Runway assignedRunway = airport.assignRunway(aircraft);

        if (assignedRunway == null) {
            aircraft.setStatus(AircraftStatus.AIRBORNE);
            runwayQueueManager.addAircraft(aircraft);
            System.out.println("Landing delayed. Flight " + aircraft.getFlightNumber() + " added to waiting queue.");
            return;
        }

        try {
            aircraft.setStatus(AircraftStatus.LANDING);

            System.out.println("Flight " + aircraft.getFlightNumber() + " is landing on Runway " + assignedRunway.getRunwayId());

            aircraft.setStatus(AircraftStatus.LANDED);

            historyManager.addRecord(
                new FlightRecord(
                    aircraft.getFlightNumber(),
                    aircraft.getAircraftId(),
                    "LANDING",
                    "Aircraft successfully landed on Runway " + assignedRunway.getRunwayId()
                )
            );

        } finally {
            assignedRunway.releaseRunway();
        }

        Gate assignedGate = airport.assignGate(aircraft);

        if (assignedGate != null) {
            aircraft.setStatus(AircraftStatus.TAXIING);

            historyManager.addRecord(
                new FlightRecord(
                    aircraft.getFlightNumber(),
                    aircraft.getAircraftId(),
                    "TAXIING",
                    "Aircraft taxiing towards Gate " + assignedGate.getGateId()
                )
            );

            System.out.println("Flight " + aircraft.getFlightNumber() + " is taxiing towards Gate " + assignedGate.getGateId());

        } else {
            gateQueueManager.addAircraft(aircraft);
            System.out.println("Flight " + aircraft.getFlightNumber() + " has landed but is waiting for a gate.");
        }
    }

    public void requestTakeoff(Aircraft aircraft) {
        if (aircraft == null) {
            throw new IllegalArgumentException("Aircraft cannot be null.");
        }

        if (!airport.getRegisteredAircraft().contains(aircraft)) {
            throw new IllegalArgumentException("Aircraft is not registered at this airport.");
        }

        if (aircraft.getStatus() != AircraftStatus.READY_FOR_TAKEOFF) {
            throw new IllegalStateException("Aircraft is not ready for takeoff.");
        }

        boolean hasAssignedGate = false;

        for (Gate gate : airport.getGates()) {
            if (gate.getAssignedAircraft() == aircraft) {
                hasAssignedGate = true;
                break;
            }
        }

        if (!hasAssignedGate) {
            throw new IllegalStateException("Aircraft must be assigned a gate before takeoff.");
        }

        try {
            if (weatherService.isTakeoffRestricted()) {
                throw new WeatherRestrictionException("Takeoff denied due to unsafe weather conditions.");
            }
        } catch (WeatherRestrictionException e) {
            System.out.println(e.getMessage());
            return;
        }

        System.out.println("\nTakeoff request received for Flight " + aircraft.getFlightNumber());

        Runway assignedRunway = airport.assignRunway(aircraft);

        if (assignedRunway == null) {
            runwayQueueManager.addAircraft(aircraft);
            System.out.println("Takeoff delayed for Flight " + aircraft.getFlightNumber() + ". Added to waiting queue.");
            return;
        }

        try {
            aircraft.setStatus(AircraftStatus.DEPARTING);

            System.out.println("Flight " + aircraft.getFlightNumber() + " is departing from Runway " + assignedRunway.getRunwayId());

            aircraft.setStatus(AircraftStatus.AIRBORNE);

            historyManager.addRecord(
                new FlightRecord(
                    aircraft.getFlightNumber(),
                    aircraft.getAircraftId(),
                    "TAKEOFF",
                    "Aircraft successfully departed from Runway " + assignedRunway.getRunwayId()
                )
            );

            System.out.println("Flight " + aircraft.getFlightNumber() + " has successfully taken off.");

        } finally {
            assignedRunway.releaseRunway();
        }

        releaseGateAndProcessQueue(aircraft);
    }

    private void releaseGateAndProcessQueue(Aircraft aircraft) {
        for (Gate gate : airport.getGates()) {
            if (gate.getAssignedAircraft() == aircraft) {
                gate.releaseGate();
                break;
            }
        }

        processGateQueue();
    }

    public synchronized void processRunwayQueue() {
        Aircraft aircraft = runwayQueueManager.pollNextAircraft();

        if (aircraft == null) {
            return;
        }

        if (aircraft.getStatus() == AircraftStatus.AIRBORNE) {
            requestLanding(aircraft);

            if (aircraft.getStatus() != AircraftStatus.TAXIING &&
                aircraft.getStatus() != AircraftStatus.LANDED) {
                runwayQueueManager.addAircraft(aircraft);
            }

        } else if (aircraft.getStatus() == AircraftStatus.READY_FOR_TAKEOFF) {
            requestTakeoff(aircraft);

            if (aircraft.getStatus() != AircraftStatus.AIRBORNE) {
                runwayQueueManager.addAircraft(aircraft);
            }

        } else {
            System.out.println("Aircraft " + aircraft.getAircraftId() + " is not eligible for runway allocation.");
        }
    }

    public void displayRunwayQueue() {
        runwayQueueManager.displayQueue();
    }

    public void removeFromRunwayQueue(String aircraftId) {
        runwayQueueManager.removeAircraft(aircraftId);
    }

    public int getRunwayQueueSize() {
        return runwayQueueManager.getQueueSize();
    }

    public void updateWeather(WeatherCondition condition, int visibility, double windSpeed) {
        weatherService.updateWeather(condition, visibility, windSpeed);
    }

    public void displayWeatherReport() {
        weatherService.displayWeatherReport();
    }

    public void reportEmergency(EmergencyAircraft aircraft) {
        emergencyHandler.reportEmergency(aircraft);
    }

    public void processEmergencyQueue() {
        emergencyHandler.processNextEmergency();
    }

    public void displayEmergencyQueue() {
        emergencyHandler.displayEmergencyQueue();
    }

    public int getPendingEmergencyCount() {
        return emergencyHandler.getPendingEmergencyCount();
    }

    public void displayFlightHistory() {
        historyManager.displayAllRecords();
    }

    public void searchFlightHistory(String flightNumber) {
        historyManager.searchByFlightNumber(flightNumber);
    }

    public void displayLatestFlightHistory() {
        historyManager.displayLatestRecordsFirst();
    }

    public synchronized void processGateQueue() {
        Aircraft nextAircraft = gateQueueManager.pollNextAircraft();

        if (nextAircraft == null) {
            return;
        }

        Gate assignedGate = airport.assignGate(nextAircraft);

        if (assignedGate != null) {
            nextAircraft.setStatus(AircraftStatus.TAXIING);

            historyManager.addRecord(
                new FlightRecord(
                    nextAircraft.getFlightNumber(),
                    nextAircraft.getAircraftId(),
                    "TAXIING",
                    "Aircraft taxiing towards Gate " + assignedGate.getGateId()
                )
            );

            System.out.println(
                "Flight " + nextAircraft.getFlightNumber() +
                " assigned to Gate " + assignedGate.getGateId() +
                " from waiting queue."
            );

        } else {
            gateQueueManager.addAircraft(nextAircraft);
            System.out.println(
                "No gate available. Flight " + nextAircraft.getFlightNumber() +
                " remains in the waiting queue."
            );
        }
    }

    public void displayGateQueue() {
        gateQueueManager.displayQueue();
    }

    public int getGateQueueSize() {
        return gateQueueManager.getQueueSize();
    }

    public void scheduleFlight(Flight flight) {
        flightScheduler.addFlight(flight);
    }

    public Flight searchScheduledFlight(String flightNumber) {
        return flightScheduler.searchFlight(flightNumber);
    }

    public void displayScheduledFlights() {
        flightScheduler.displayAllFlights();
    }

    public void displayFlightsByDepartureTime() {
        flightScheduler.displayFlightsByDepartureTime();
    }

    public void updateScheduledFlightStatus(String flightNumber, FlightStatus status) {
        flightScheduler.updateFlightStatus(flightNumber, status);
    }

    public int getScheduledFlightCount() {
        return flightScheduler.getFlights().size();
    }

    public ArrayList<Flight> getScheduledFlights() {
        return flightScheduler.getFlights();
    }

    public EmergencyHandler getEmergencyHandler() {
        return emergencyHandler;
    }

    public ArrayList<FlightRecord> getFlightHistoryRecords() {
        return historyManager.getRecords();
    }

    public java.util.List<Aircraft> getRunwayWaitingAircraft() {
        return runwayQueueManager.getWaitingAircraft();
    }

    public java.util.List<Aircraft> getGateWaitingAircraft() {
        return gateQueueManager.getWaitingAircraft();
    }

    public boolean removeFromGateQueue(String aircraftId) {
        return gateQueueManager.removeAircraft(aircraftId);
    }
}
