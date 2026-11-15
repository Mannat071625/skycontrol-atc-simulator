
package com.atc.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("PASSENGER")
public class PassengerAircraft extends Aircraft {

    private int passengerCapacity;

    protected PassengerAircraft() {
        super();
    }

    public PassengerAircraft(String aircraftId, String flightNumber, String airlineName, double fuelLevel, int passengerCapacity) {
        super(aircraftId, flightNumber, airlineName, fuelLevel);
        if (passengerCapacity < 0) {
            throw new IllegalArgumentException("Passenger capacity cannot be negative.");
        }
        this.passengerCapacity = passengerCapacity;
    }

    @Override
    public String getAircraftType() {
        return "Passenger Aircraft";
    }

    @Override
    public double getRequiredRunwayLength() {
        return 2500;
    }

    public int getPassengerCapacity() {
        return passengerCapacity;
    }

    public void boardPassengers(int count) {
        if (count < 0 || count > passengerCapacity) {
            throw new IllegalArgumentException("Invalid passenger count.");
        }

        System.out.println(count + " passengers boarded Flight " + getFlightNumber());
    }
}
