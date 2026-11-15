
package com.atc.service;

import java.util.LinkedList;
import java.util.Queue;

import com.atc.model.Aircraft;

public class RunwayQueueManager {

    private final Queue<Aircraft> waitingQueue;

    public RunwayQueueManager() {
        waitingQueue = new LinkedList<>();
    }

    public synchronized void addAircraft(Aircraft aircraft) {
        if (aircraft == null) {
            throw new IllegalArgumentException("Aircraft cannot be null.");
        }

        if (waitingQueue.contains(aircraft)) {
            System.out.println("Aircraft is already in the waiting queue.");
            return;
        }

        waitingQueue.offer(aircraft);
        System.out.println("Flight " + aircraft.getFlightNumber() + " added to runway waiting queue.");
    }

    public synchronized Aircraft getNextAircraft() {
        Aircraft aircraft = waitingQueue.peek();

        if (aircraft == null) {
            System.out.println("No aircraft waiting for runway allocation.");
            return null;
        }

        System.out.println("Next aircraft in queue: " + aircraft.getFlightNumber());
        return aircraft;
    }

    public synchronized Aircraft pollNextAircraft() {
    Aircraft aircraft = waitingQueue.poll();

    if (aircraft == null) {
        System.out.println("No aircraft waiting for runway allocation.");
        return null;
    }

    System.out.println("Next aircraft in queue: " + aircraft.getFlightNumber());
    return aircraft;
}

    public synchronized void removeNextAircraft() {
        waitingQueue.poll();
    }

    public synchronized void removeAircraft(String aircraftId) {
        boolean removed = waitingQueue.removeIf(
            aircraft -> aircraft.getAircraftId().equals(aircraftId)
        );

        if (removed) {
            System.out.println("Aircraft " + aircraftId + " removed from waiting queue.");
        } else {
            System.out.println("Aircraft not found in waiting queue.");
        }
    }

    public synchronized void displayQueue() {
        if (waitingQueue.isEmpty()) {
            System.out.println("Runway waiting queue is empty.");
            return;
        }

        System.out.println("\n========== RUNWAY WAITING QUEUE ==========");

        for (Aircraft aircraft : waitingQueue) {
            System.out.println(
                "Aircraft ID: " + aircraft.getAircraftId() +
                " | Flight: " + aircraft.getFlightNumber()
            );
        }
    }

    public synchronized int getQueueSize() {
        return waitingQueue.size();
    }

    public synchronized boolean isQueueEmpty() {
        return waitingQueue.isEmpty();
    }

    public synchronized java.util.List<Aircraft> getWaitingAircraft() {
    return new java.util.ArrayList<>(waitingQueue);
}
}
