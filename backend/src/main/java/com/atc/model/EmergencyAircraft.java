
package com.atc.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("EMERGENCY")
public class EmergencyAircraft extends Aircraft {

    private String emergencyType;

    protected EmergencyAircraft() {
        super();
    }

    public EmergencyAircraft(String aircraftId, String flightNumber, String airlineName, double fuelLevel, String emergencyType) {
        super(aircraftId, flightNumber, airlineName, fuelLevel);
        if (emergencyType == null || emergencyType.isBlank()) {
            throw new IllegalArgumentException("Emergency type cannot be empty.");
        }
        this.emergencyType = emergencyType;
    }

    @Override
    public String getAircraftType() {
        return "Emergency Aircraft";
    }

    @Override
    public double getRequiredRunwayLength() {
        return 2500;
    }

    public String getEmergencyType() {
        return emergencyType;
    }

    public void reportEmergency() {
        System.out.println("EMERGENCY: " + emergencyType + " reported by Flight " + getFlightNumber());
    }
}
