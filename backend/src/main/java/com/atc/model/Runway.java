
package com.atc.model;

import com.atc.enums.RunwayStatus;

public class Runway {

    private String runwayId;
    private double length;
    private RunwayStatus status;
    private Aircraft assignedAircraft;

    public Runway(String runwayId, double length) {
        if (runwayId == null || runwayId.isBlank() || length <= 0) {
            throw new IllegalArgumentException("Invalid runway details.");
        }

        this.runwayId = runwayId;
        this.length = length;
        this.status = RunwayStatus.AVAILABLE;
        this.assignedAircraft = null;
    }

    public synchronized boolean assignAircraft(Aircraft aircraft) {

        if (aircraft == null) {
            throw new IllegalArgumentException("Aircraft cannot be null.");
        }

        if (status != RunwayStatus.AVAILABLE) {
            return false;
        }

        if (aircraft.getRequiredRunwayLength() > length) {
            return false;
        }

        assignedAircraft = aircraft;
        status = RunwayStatus.OCCUPIED;

        System.out.println("Runway " + runwayId + " assigned to Flight " + aircraft.getFlightNumber());

        return true;
    }

    public synchronized void releaseRunway() {

        if (status != RunwayStatus.OCCUPIED) {
            throw new IllegalStateException("Runway is not currently occupied.");
        }

        System.out.println("Runway " + runwayId + " released from Flight " + assignedAircraft.getFlightNumber());

        assignedAircraft = null;
        status = RunwayStatus.AVAILABLE;
    }

   public synchronized void setUnderMaintenance() {

    if (status == RunwayStatus.OCCUPIED) {
        throw new IllegalStateException("Cannot maintain an occupied runway.");
    }

    status = RunwayStatus.UNDER_MAINTENANCE;
}

    public synchronized void setAvailable() {

        if (status == RunwayStatus.OCCUPIED) {
            throw new IllegalStateException("Cannot make an occupied runway available.");
        }

        status = RunwayStatus.AVAILABLE;
    }

    public String getRunwayId() {
        return runwayId;
    }

    public double getLength() {
        return length;
    }

    public synchronized RunwayStatus getStatus() {
        return status;
    }

    public synchronized Aircraft getAssignedAircraft() {
        return assignedAircraft;
    }

    public synchronized void displayRunwayDetails() {
        System.out.println("Runway ID: " + runwayId);
        System.out.println("Length: " + length + " metres");
        System.out.println("Status: " + status);

        if (assignedAircraft != null) {
            System.out.println("Assigned Flight: " + assignedAircraft.getFlightNumber());
        }
    }
}
