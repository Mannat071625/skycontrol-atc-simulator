
package com.atc.service;

import java.util.LinkedList;
import java.util.Queue;

import com.atc.model.Aircraft;

public class GateQueueManager {

    private final Queue<Aircraft> waitingQueue;

    public GateQueueManager() {
        waitingQueue = new LinkedList<>();
    }

    public synchronized void addAircraft(Aircraft aircraft) {
        if (aircraft == null) {
            throw new IllegalArgumentException("Aircraft cannot be null.");
        }

        if (waitingQueue.contains(aircraft)) {
            System.out.println("Aircraft is already in the gate waiting queue.");
            return;
        }

        waitingQueue.offer(aircraft);
        System.out.println("Flight " + aircraft.getFlightNumber() + " added to gate waiting queue.");
    }

    public synchronized Aircraft getNextAircraft() {
        return waitingQueue.peek();
    }

    public synchronized Aircraft removeNextAircraft() {
        return waitingQueue.poll();
    }

    public synchronized Aircraft pollNextAircraft() {
        Aircraft aircraft = waitingQueue.poll();

        if (aircraft == null) {
            System.out.println("Gate waiting queue is empty.");
            return null;
        }

        System.out.println("Next aircraft in gate queue: " + aircraft.getFlightNumber());
        return aircraft;
    }

    public synchronized void displayQueue() {
        if (waitingQueue.isEmpty()) {
            System.out.println("Gate waiting queue is empty.");
            return;
        }

        System.out.println("\n========== GATE WAITING QUEUE ==========");

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

public synchronized boolean removeAircraft(String aircraftId) {
    if (aircraftId == null || aircraftId.isBlank()) {
        return false;
    }

    return waitingQueue.removeIf(
        aircraft -> aircraft.getAircraftId().equalsIgnoreCase(aircraftId)
    );
}
}
