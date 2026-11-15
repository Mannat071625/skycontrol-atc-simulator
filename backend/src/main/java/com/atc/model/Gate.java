
package com.atc.model;

import com.atc.enums.GateStatus;

public class Gate {

    private String gateId;
    private GateStatus status;
    private Aircraft assignedAircraft;

    public Gate(String gateId) {
        if (gateId == null || gateId.isBlank()) {
            throw new IllegalArgumentException("Invalid gate ID.");
        }

        this.gateId = gateId;
        this.status = GateStatus.AVAILABLE;
        this.assignedAircraft = null;
    }

    public synchronized boolean assignAircraft(Aircraft aircraft) {

        if (aircraft == null) {
            throw new IllegalArgumentException("Aircraft cannot be null.");
        }

        if (status != GateStatus.AVAILABLE) {
            return false;
        }

        assignedAircraft = aircraft;
        status = GateStatus.OCCUPIED;

        System.out.println("Gate " + gateId + " assigned to Flight " + aircraft.getFlightNumber());

        return true;
    }

    public synchronized void releaseGate() {

        if (status != GateStatus.OCCUPIED) {
            throw new IllegalStateException("Gate is not occupied.");
        }

        System.out.println("Gate " + gateId + " released from Flight " + assignedAircraft.getFlightNumber());

        assignedAircraft = null;
        status = GateStatus.AVAILABLE;
    }

    public synchronized void setUnderMaintenance() {

        if (status == GateStatus.OCCUPIED) {
            throw new IllegalStateException("Cannot maintain an occupied gate.");
        }

        status = GateStatus.UNDER_MAINTENANCE;
    }

    public synchronized void setAvailable() {

        if (status == GateStatus.OCCUPIED) {
            throw new IllegalStateException("Cannot free an occupied gate.");
        }

        status = GateStatus.AVAILABLE;
    }

    public String getGateId() {
        return gateId;
    }

    public synchronized GateStatus getStatus() {
        return status;
    }

    public synchronized Aircraft getAssignedAircraft() {
        return assignedAircraft;
    }

    public synchronized void displayGateDetails() {
        System.out.println("Gate ID: " + gateId);
        System.out.println("Status: " + status);

        if (assignedAircraft != null) {
            System.out.println("Assigned Flight: " + assignedAircraft.getFlightNumber());
        }
    }
}
