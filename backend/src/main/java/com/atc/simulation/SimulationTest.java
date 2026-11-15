
package com.atc.simulation;

import com.atc.model.Aircraft;
import com.atc.model.PassengerAircraft;

public class SimulationTest {

    
public static void main(String[] args) {

    Aircraft aircraft = new PassengerAircraft(
        "AC901", "AI901", "Air India", 85, 180
    );

    SimulationManager manager = new SimulationManager();

    manager.addAircraft(aircraft);
    manager.startSimulation();

    manager.waitForCompletion();

    System.out.println("\n========== FINAL SIMULATION RESULT ==========");
    System.out.println("Aircraft Status: " + aircraft.getStatus());
    System.out.println("Final Altitude: " + aircraft.getAltitude());
    System.out.println("Final Speed: " + aircraft.getSpeed());
    System.out.println("Simulation Stopped: " + manager.isStopped());
    System.out.println("Clock Running: " + manager.isRunning());
}

}
