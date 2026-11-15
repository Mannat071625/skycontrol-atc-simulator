
package com.atc.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CARGO")
public class CargoAircraft extends Aircraft {

    private double cargoCapacity;

    protected CargoAircraft() {
        super();
    }

    public CargoAircraft(String aircraftId, String flightNumber, String airlineName, double fuelLevel, double cargoCapacity) {
        super(aircraftId, flightNumber, airlineName, fuelLevel);
        if (!Double.isFinite(cargoCapacity) || cargoCapacity < 0) {
            throw new IllegalArgumentException("Cargo capacity must be a non-negative finite value.");
        }
        this.cargoCapacity = cargoCapacity;
    }

    @Override
    public String getAircraftType() {
        return "Cargo Aircraft";
    }

    @Override
    public double getRequiredRunwayLength() {
        return 3000;
    }

    public double getCargoCapacity() {
        return cargoCapacity;
    }

    public void loadCargo(double weight) {
        if (!Double.isFinite(weight) || weight < 0 || weight > cargoCapacity) {
            throw new IllegalArgumentException("Invalid cargo weight.");
        }

        System.out.println(weight + " tonnes of cargo loaded on Flight " + getFlightNumber());
    }
}
