
package com.atc.model;

import com.atc.enums.AircraftStatus;
import com.atc.interfaces.FlightOperable;
import jakarta.persistence.*;

@Entity
@Table(name = "aircraft")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "aircraft_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Aircraft implements FlightOperable {

    @Id
    @Column(nullable = false, unique = true)
    private String aircraftId;

    @Column(nullable = false)
    private String flightNumber;

    @Column(nullable = false)
    private String airlineName;

    private double fuelLevel;
    private double altitude;
    private double speed;

    @Enumerated(EnumType.STRING)
    private AircraftStatus status;

    protected Aircraft() {
    }

    public Aircraft(String aircraftId, String flightNumber, String airlineName, double fuelLevel) {
        if (aircraftId == null || aircraftId.isBlank() ||
            flightNumber == null || flightNumber.isBlank() ||
            airlineName == null || airlineName.isBlank()) {
            throw new IllegalArgumentException("Aircraft details cannot be empty.");
        }

        if (!Double.isFinite(fuelLevel) || fuelLevel < 0 || fuelLevel > 100) {
            throw new IllegalArgumentException("Fuel level must be between 0 and 100.");
        }

        this.aircraftId = aircraftId;
        this.flightNumber = flightNumber;
        this.airlineName = airlineName;
        this.fuelLevel = fuelLevel;
        this.altitude = 0;
        this.speed = 0;
        this.status = AircraftStatus.WAITING;
    }

    public abstract String getAircraftType();

    public abstract double getRequiredRunwayLength();

    @Override
    public void requestLanding() {
        System.out.println("Landing request initiated for Flight " + flightNumber);
    }

    @Override
    public void requestTakeoff() {
        System.out.println("Takeoff request initiated for Flight " + flightNumber);
    }

    @Override
    public void reportStatus() {
        System.out.println("Flight " + flightNumber + " Status: " + getStatus());
    }

    public synchronized void updatePosition(double altitude, double speed) {
        if (!Double.isFinite(altitude) || !Double.isFinite(speed) || altitude < 0 || speed < 0) {
            throw new IllegalArgumentException("Invalid altitude or speed.");
        }
        this.altitude = altitude;
        this.speed = speed;
    }

    public synchronized void setFuelLevel(double fuelLevel) {
        if (!Double.isFinite(fuelLevel) || fuelLevel < 0 || fuelLevel > 100) {
            throw new IllegalArgumentException("Fuel level must be between 0 and 100.");
        }
        this.fuelLevel = fuelLevel;
    }

    public synchronized void setStatus(AircraftStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Aircraft status cannot be null.");
        }
        this.status = status;
    }

    public String getAircraftId() {
        return aircraftId;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getAirlineName() {
        return airlineName;
    }

    public synchronized double getFuelLevel() {
        return fuelLevel;
    }

    public synchronized double getAltitude() {
        return altitude;
    }

    public synchronized double getSpeed() {
        return speed;
    }

    public synchronized AircraftStatus getStatus() {
        return status;
    }

    public void displayDetails() {
        System.out.println("Aircraft ID: " + aircraftId);
        System.out.println("Flight Number: " + flightNumber);
        System.out.println("Airline: " + airlineName);
        System.out.println("Aircraft Type: " + getAircraftType());
        System.out.println("Fuel Level: " + getFuelLevel() + "%");
        System.out.println("Altitude: " + getAltitude() + " ft");
        System.out.println("Speed: " + getSpeed() + " knots");
        System.out.println("Status: " + getStatus());
        System.out.println("Required Runway Length: " + getRequiredRunwayLength() + " metres");
    }
}
