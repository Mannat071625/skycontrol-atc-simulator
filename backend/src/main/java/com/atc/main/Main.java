
package com.atc.main;

import java.time.LocalDateTime;
import java.util.ArrayList;

import com.atc.enums.AircraftStatus;
import com.atc.enums.FlightStatus;
import com.atc.enums.WeatherCondition;
import com.atc.model.Aircraft;
import com.atc.model.Airport;
import com.atc.model.CargoAircraft;
import com.atc.model.EmergencyAircraft;
import com.atc.model.Flight;
import com.atc.model.Gate;
import com.atc.model.PassengerAircraft;
import com.atc.model.Runway;
import com.atc.service.ControlTower;
import com.atc.simulation.RunwayRequestTask;
import com.atc.simulation.SimulationManager;

public class Main {

    public static void main(String[] args) {

        Airport airport = new Airport("DEL", "Indira Gandhi International Airport");

        Runway runway1 = new Runway("RWY-01", 3500);
        Runway runway2 = new Runway("RWY-02", 3000);

        Gate gate1 = new Gate("G-01");
        Gate gate2 = new Gate("G-02");

        airport.addRunway(runway1);
        airport.addRunway(runway2);
        airport.addGate(gate1);
        airport.addGate(gate2);

        PassengerAircraft passenger = new PassengerAircraft("AC101", "AI101", "Air India", 85, 180);
        CargoAircraft cargo = new CargoAircraft("AC202", "AI202", "FedEx", 70, 65);
        EmergencyAircraft emergency = new EmergencyAircraft("AC303", "EX303", "Air Ambulance", 25, "Engine Failure");

        ArrayList<Aircraft> aircraftList = new ArrayList<>();

        aircraftList.add(passenger);
        aircraftList.add(cargo);
        aircraftList.add(emergency);

        for (Aircraft aircraft : aircraftList) {
            airport.registerAircraft(aircraft);
        }

        ControlTower tower = new ControlTower(airport);

        System.out.println("\n========== HASHMAP TEST ==========");

        System.out.println("Aircraft found: " + airport.searchAircraft("AC101").getFlightNumber());
        System.out.println("Aircraft count: " + airport.getRegisteredAircraft().size());
        System.out.println("Aircraft exists: " + (airport.searchAircraft("AC202") != null));

        System.out.println("\n========== AIR TRAFFIC CONTROL SYSTEM ==========");

        airport.displayAirportDetails();

        for (Aircraft aircraft : aircraftList) {
            System.out.println();
            aircraft.displayDetails();
        }

        passenger.boardPassengers(150);
        cargo.loadCargo(40);

        System.out.println("\n========== LANDING TEST ==========");

        tower.requestLanding(passenger);

        System.out.println("\n========== TAKEOFF TEST ==========");

        passenger.setStatus(AircraftStatus.READY_FOR_TAKEOFF);
        tower.requestTakeoff(passenger);

System.out.println("\n========== TAKEOFF WITHOUT GATE TEST ==========");

Aircraft takeoffTestAircraft = new PassengerAircraft(
    "AC999", "AI999", "Test Airline", 80, 150
);

airport.registerAircraft(takeoffTestAircraft);
takeoffTestAircraft.setStatus(AircraftStatus.READY_FOR_TAKEOFF);

try {
    tower.requestTakeoff(takeoffTestAircraft);
} catch (IllegalStateException e) {
    System.out.println("Takeoff rejected: " + e.getMessage());
}
        System.out.println("\n========== GATE RELEASE TEST ==========");

System.out.println("Gates occupied: " +
    airport.getGates().stream()
        .filter(gate -> gate.getAssignedAircraft() != null)
        .count());

        System.out.println("\n========== FLIGHT HISTORY ==========");

        tower.displayFlightHistory();

        System.out.println("\n========== SEARCH FLIGHT HISTORY ==========");

        tower.searchFlightHistory("AI101");

        EmergencyAircraft testAircraft = new EmergencyAircraft(
            "AC404", "EX404", "Test Airline", 80, "Medical Emergency"
        );

        airport.registerAircraft(testAircraft);

        System.out.println("\n========== WEATHER TEST ==========");

        tower.updateWeather(WeatherCondition.THUNDERSTORM, 500, 70);
        tower.displayWeatherReport();

        System.out.println("\n========== LANDING RESTRICTION TEST ==========");

        tower.requestLanding(testAircraft);

        System.out.println("\n========== RUNWAY QUEUE TEST ==========");

        tower.updateWeather(WeatherCondition.CLEAR, 10000, 5);
System.out.println("\n========== RUNWAY OCCUPANCY TEST ==========");

Runway occupiedRunway1 = airport.getRunways().get(0);
Runway occupiedRunway2 = airport.getRunways().get(1);

occupiedRunway1.assignAircraft(passenger);
occupiedRunway2.assignAircraft(cargo);

tower.requestLanding(testAircraft);

tower.displayRunwayQueue();

System.out.println("Waiting aircraft: " + tower.getRunwayQueueSize());

occupiedRunway1.releaseRunway();
occupiedRunway2.releaseRunway();
System.out.println("\n========== PROCESS RUNWAY QUEUE ==========");
tower.processRunwayQueue();
tower.displayRunwayQueue();
System.out.println("Waiting aircraft: " + tower.getRunwayQueueSize());


        System.out.println("\n========== WEATHER DURING QUEUE TEST ==========");

        EmergencyAircraft weatherTestAircraft = new EmergencyAircraft(
            "AC505", "EX505", "Test Airline", 75, "Medical Emergency"
        );

        airport.registerAircraft(weatherTestAircraft);

        Runway occupiedRunway3 = airport.getRunways().get(0);
        Runway occupiedRunway4 = airport.getRunways().get(1);

        occupiedRunway3.assignAircraft(passenger);
        occupiedRunway4.assignAircraft(cargo);

        tower.requestLanding(weatherTestAircraft);

        System.out.println("Queue size before weather change: " + tower.getRunwayQueueSize());

        occupiedRunway3.releaseRunway();
        occupiedRunway4.releaseRunway();

        tower.updateWeather(WeatherCondition.THUNDERSTORM, 500, 70);

        System.out.println("\nProcessing queue under unsafe weather:");

        tower.processRunwayQueue();

        tower.displayRunwayQueue();

        System.out.println("Queue size after weather restriction: " + tower.getRunwayQueueSize());


System.out.println("\n========== QUEUE RETRY AFTER WEATHER IMPROVES ==========");

tower.updateWeather(WeatherCondition.CLEAR, 10000, 5);

tower.processRunwayQueue();

tower.displayRunwayQueue();

System.out.println("Final queue size: " + tower.getRunwayQueueSize());



       
        System.out.println("\n========== GATE WAITING QUEUE TEST ==========");

        EmergencyAircraft gateTestAircraft = new EmergencyAircraft(
            "AC606", "EX606", "Test Airline", 90, "Medical Emergency"
        );

        airport.registerAircraft(gateTestAircraft);

        Gate occupiedGate1 = airport.getGates().get(0);
        Gate occupiedGate2 = airport.getGates().get(1);

        occupiedGate1.assignAircraft(passenger);
        occupiedGate2.assignAircraft(testAircraft);

        tower.requestLanding(gateTestAircraft);

        System.out.println("\nGate queue size: " + tower.getGateQueueSize());
        tower.displayGateQueue();

        System.out.println("\nReleasing first gate:");

        occupiedGate1.releaseGate();
        tower.processGateQueue();

        System.out.println("\nFinal gate queue size: " + tower.getGateQueueSize());


System.out.println("\n========== GATE QUEUE RETRY TEST ==========");

EmergencyAircraft retryAircraft = new EmergencyAircraft(
    "AC550", "EX550", "Test Airline", 85, "Medical Emergency"
);

airport.registerAircraft(retryAircraft);

for (Gate gate : airport.getGates()) {
    if (gate.getAssignedAircraft() != null) {
        gate.releaseGate();
    }
}

occupiedGate1.assignAircraft(passenger);
occupiedGate2.assignAircraft(testAircraft);

tower.requestLanding(retryAircraft);

System.out.println("Queue size before processing: " + tower.getGateQueueSize());

tower.processGateQueue();

System.out.println("Queue size after failed allocation: " + tower.getGateQueueSize());

tower.displayGateQueue();

occupiedGate1.releaseGate();

tower.processGateQueue();

System.out.println("Queue size after gate becomes available: " + tower.getGateQueueSize());

        
System.out.println("\n========== EMERGENCY PRIORITY AND FIFO TEST ==========");

EmergencyAircraft medicalAircraft = new EmergencyAircraft(
    "AC707", "EX707", "Airline A", 60, "Medical Emergency"
);

EmergencyAircraft medicalAircraft2 = new EmergencyAircraft(
    "AC1001", "EX1001", "Airline D", 65, "Medical Emergency"
);

EmergencyAircraft fuelAircraft = new EmergencyAircraft(
    "AC808", "EX808", "Airline B", 20, "Low Fuel"
);

EmergencyAircraft engineAircraft = new EmergencyAircraft(
    "AC909", "EX909", "Airline C", 50, "Engine Failure"
);

airport.registerAircraft(medicalAircraft);
airport.registerAircraft(medicalAircraft2);
airport.registerAircraft(fuelAircraft);
airport.registerAircraft(engineAircraft);

tower.reportEmergency(medicalAircraft);
tower.reportEmergency(medicalAircraft2);
tower.reportEmergency(fuelAircraft);
tower.reportEmergency(engineAircraft);

tower.displayEmergencyQueue();


        System.out.println("\n========== EMERGENCY PROCESSING TEST ==========");
tower.updateWeather(WeatherCondition.THUNDERSTORM, 500, 70);

System.out.println("\n========== EMERGENCY WEATHER TEST ==========");
tower.processEmergencyQueue();

System.out.println("Pending emergencies: " + tower.getPendingEmergencyCount());

tower.updateWeather(WeatherCondition.CLEAR, 10000, 5);
        tower.processEmergencyQueue();

        tower.displayEmergencyQueue();

        System.out.println("Pending emergencies: " + tower.getPendingEmergencyCount());


System.out.println("\n========== EMERGENCY HISTORY TEST ==========");
tower.searchFlightHistory("EX909");

System.out.println("\n========== EMERGENCY GATE TEST ==========");
System.out.println("Pending gate queue: " + tower.getGateQueueSize());
tower.displayGateQueue();

System.out.println("\n========== EMERGENCY GATE ALLOCATION TEST ==========");

for (Gate gate : airport.getGates()) {
    if (gate.getAssignedAircraft() != null) {
        gate.releaseGate();
        break;
    }
}

tower.processGateQueue();

System.out.println("EX909 Status: " +
    airport.searchAircraft("AC909").getStatus());

System.out.println("\n========== UPDATED EMERGENCY HISTORY ==========");
tower.searchFlightHistory("EX909");

System.out.println("\n========== FLIGHT SCHEDULING TEST ==========");

Flight flight1 = new Flight(
    "AI301", "Delhi", "Mumbai",
    LocalDateTime.of(2026, 11, 7, 10, 0),
    LocalDateTime.of(2026, 11, 7, 12, 15),
    passenger
);

Flight flight2 = new Flight(
    "AI302", "Mumbai", "Bangalore",
    LocalDateTime.of(2026, 11, 7, 8, 30),
    LocalDateTime.of(2026, 11, 7, 10, 45),
    cargo
);

tower.scheduleFlight(flight1);
tower.scheduleFlight(flight2);

System.out.println("\n--- ALL SCHEDULED FLIGHTS ---");
tower.displayScheduledFlights();

System.out.println("\n--- FLIGHTS SORTED BY DEPARTURE ---");
tower.displayFlightsByDepartureTime();

System.out.println("\n--- SEARCH FLIGHT ---");
Flight searchedFlight = tower.searchScheduledFlight("AI301");

if (searchedFlight != null) {
    searchedFlight.displayFlightDetails();
}

System.out.println("\n--- UPDATE FLIGHT STATUS ---");
tower.updateScheduledFlightStatus("AI301", FlightStatus.BOARDING);

System.out.println("Total scheduled flights: " + tower.getScheduledFlightCount());

System.out.println("\n========== FLIGHT CONFLICT TEST ==========");

try {
    Flight conflictingFlight = new Flight(
        "AI303", "Delhi", "Chennai",
        LocalDateTime.of(2026, 11, 7, 11, 0),
        LocalDateTime.of(2026, 11, 7, 13, 0),
        passenger
    );

    tower.scheduleFlight(conflictingFlight);

} catch (IllegalArgumentException e) {
    System.out.println("Conflict detected: " + e.getMessage());
}

System.out.println("\n========== NON-OVERLAPPING FLIGHT TEST ==========");

Flight validFlight = new Flight(
    "AI304", "Delhi", "Chennai",
    LocalDateTime.of(2026, 11, 7, 12, 15),
    LocalDateTime.of(2026, 11, 7, 14, 0),
    passenger
);

tower.scheduleFlight(validFlight);

System.out.println("\n========== MULTITHREADING TEST ==========");

Runway testRunway = new Runway("RWY-TEST", 3500);

Thread t1 = new Thread(new RunwayRequestTask(testRunway, passenger), "Thread-1");
Thread t2 = new Thread(new RunwayRequestTask(testRunway, cargo), "Thread-2");
Thread t3 = new Thread(new RunwayRequestTask(testRunway, emergency), "Thread-3");

t1.start();
t2.start();
t3.start();

try {
    t1.join();
    t2.join();
    t3.join();
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    System.out.println("Main thread interrupted.");
}

System.out.println("\nFinal Runway Status: " + testRunway.getStatus());

if (testRunway.getAssignedAircraft() != null) {
    System.out.println("Assigned Flight: " +
        testRunway.getAssignedAircraft().getFlightNumber());
}

System.out.println("\n========== MULTITHREADING RELEASE TEST ==========");

testRunway.releaseRunway();

System.out.println("Runway Status After Release: " + testRunway.getStatus());

boolean reassigned = testRunway.assignAircraft(cargo);

System.out.println("Second Assignment Successful: " + reassigned);
System.out.println("Final Assigned Flight: " +
    testRunway.getAssignedAircraft().getFlightNumber());

    

System.out.println("\n========== SIMULATION STOP TEST ==========");

SimulationManager simulationManager = new SimulationManager();

simulationManager.addAircraft(passenger);
simulationManager.addAircraft(cargo);
simulationManager.addAircraft(emergency);

simulationManager.startSimulation();

try {
    Thread.sleep(3500);

    simulationManager.stopSimulation();
    System.out.println("\n========== SIMULATION STOPPED ==========");

    simulationManager.waitForCompletion();

} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    System.out.println("Main thread interrupted.");
}

System.out.println("\n========== FINAL STOPPED POSITIONS ==========");
passenger.displayDetails();
cargo.displayDetails();
emergency.displayDetails();

System.out.println("\nSimulation Stopped: " + simulationManager.isStopped());
System.out.println("Clock Running: " + simulationManager.isRunning());
System.out.println("Total Simulation Time: " + simulationManager.getElapsedSeconds() + " seconds");

    }
}
