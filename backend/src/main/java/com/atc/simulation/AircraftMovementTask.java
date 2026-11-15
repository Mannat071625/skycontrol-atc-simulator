
package com.atc.simulation;

import com.atc.enums.AircraftStatus;
import com.atc.model.Aircraft;

public class AircraftMovementTask implements Runnable {

    private final Aircraft aircraft;
    private final SimulationClock clock;

    public AircraftMovementTask(Aircraft aircraft, SimulationClock clock) {
        if (aircraft == null || clock == null) {
            throw new IllegalArgumentException("Aircraft and clock cannot be null.");
        }
        this.aircraft = aircraft;
        this.clock = clock;
    }

    @Override
    public void run() {
        try {
            System.out.println("\nFlight " + aircraft.getFlightNumber() + " starting movement simulation.");

            aircraft.setStatus(AircraftStatus.DEPARTING);

            for (int altitude = 1000; altitude <= 5000; altitude += 1000) {
                checkStopRequested();
                aircraft.updatePosition(altitude, altitude / 50.0);
                displayPosition("CLIMBING");
                waitForNextStep();
            }

            checkStopRequested();
            aircraft.setStatus(AircraftStatus.AIRBORNE);

            for (int i = 0; i < 3; i++) {
                checkStopRequested();
                aircraft.updatePosition(5000, 100);
                displayPosition("CRUISING");
                waitForNextStep();
            }

            checkStopRequested();
            aircraft.setStatus(AircraftStatus.APPROACHING);

            for (int altitude = 4000; altitude >= 0; altitude -= 1000) {
                checkStopRequested();
                aircraft.updatePosition(altitude, altitude / 40.0);
                displayPosition("DESCENDING");
                waitForNextStep();
            }

            checkStopRequested();
            aircraft.setStatus(AircraftStatus.LANDED);

            System.out.println("Flight " + aircraft.getFlightNumber() + " completed movement simulation.");

        } catch (InterruptedException e) {
            aircraft.setStatus(AircraftStatus.STOPPED);
            Thread.currentThread().interrupt();
            System.out.println("Movement simulation stopped for Flight " + aircraft.getFlightNumber());
        }
    }

    private void waitForNextStep() throws InterruptedException {
        Thread.sleep(1000);
    }

    private void checkStopRequested() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        clock.awaitIfPaused();

        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }

    private void displayPosition(String phase) {
        System.out.println(
            "Flight: " + aircraft.getFlightNumber() +
            " | Phase: " + phase +
            " | Altitude: " + aircraft.getAltitude() + " ft" +
            " | Speed: " + aircraft.getSpeed() + " knots"
        );
    }
}
