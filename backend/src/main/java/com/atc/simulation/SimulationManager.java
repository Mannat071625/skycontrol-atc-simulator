
package com.atc.simulation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.atc.model.Aircraft;

public class SimulationManager {

    private final SimulationClock clock;
    private final Thread clockThread;
    private final List<Thread> movementThreads;
    private final Set<String> aircraftIds;
    private boolean started;
    private volatile boolean stopped;

    public SimulationManager() {
        clock = new SimulationClock();
        clockThread = new Thread(clock, "Simulation-Clock");
        movementThreads = new ArrayList<>();
        aircraftIds = new HashSet<>();
        started = false;
        stopped = false;
    }

    public synchronized void addAircraft(Aircraft aircraft) {
        if (aircraft == null) {
            throw new IllegalArgumentException("Aircraft cannot be null.");
        }

        if (started) {
            throw new IllegalStateException("Simulation has already started.");
        }

        if (!aircraftIds.add(aircraft.getAircraftId())) {
            throw new IllegalArgumentException("Aircraft is already added to the simulation.");
        }

        Thread movementThread = new Thread(
            new AircraftMovementTask(aircraft, clock),
            "Movement-" + aircraft.getFlightNumber()
        );

        movementThreads.add(movementThread);
    }

    public synchronized void startSimulation() {
        if (started) {
            throw new IllegalStateException("Simulation has already started.");
        }

        if (movementThreads.isEmpty()) {
            throw new IllegalStateException("No aircraft added to the simulation.");
        }

        started = true;
        clockThread.start();

        for (Thread thread : movementThreads) {
            thread.start();
        }
    }

    public synchronized void pauseSimulation() {
        if (!started || stopped) {
            throw new IllegalStateException("Simulation is not active.");
        }

        clock.pauseClock();
    }

    public synchronized void resumeSimulation() {
        if (!started || stopped) {
            throw new IllegalStateException("Simulation is not active.");
        }

        clock.resumeClock();
    }

    public synchronized void stopSimulation() {
        if (!started || stopped) {
            throw new IllegalStateException("Simulation is not active.");
        }

        stopped = true;
        clock.stopClock();

        for (Thread thread : movementThreads) {
            thread.interrupt();
        }

        clockThread.interrupt();
    }

    public boolean isPaused() {
        return clock.isPaused();
    }

    public boolean isStopped() {
        return stopped;
    }

    public void waitForCompletion() {
        if (!started) {
            throw new IllegalStateException("Simulation has not started.");
        }

        boolean interrupted = false;

        for (Thread thread : movementThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                interrupted = true;
                stopSimulation();
                break;
            }
        }

        if (!stopped) {
            stopped = true;
            clock.stopClock();
            clockThread.interrupt();
        }

        try {
            clockThread.join();
        } catch (InterruptedException e) {
            interrupted = true;
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    public long getElapsedSeconds() {
        return clock.getElapsedSeconds();
    }

    public boolean isRunning() {
        return started && !stopped && clock.isRunning();
    }
}
