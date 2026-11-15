package com.atc.simulation;

import com.atc.model.Aircraft;
import com.atc.model.Runway;

public class RunwayRequestTask implements Runnable {

    private Runway runway;
    private Aircraft aircraft;

    public RunwayRequestTask(Runway runway, Aircraft aircraft) {
        this.runway = runway;
        this.aircraft = aircraft;
    }

    @Override
    public void run() {
        System.out.println(
            Thread.currentThread().getName() +
            " requesting runway for Flight " + aircraft.getFlightNumber()
        );

        boolean assigned = runway.assignAircraft(aircraft);

        if (assigned) {
            System.out.println(
                Thread.currentThread().getName() +
                ": Runway successfully assigned to " + aircraft.getFlightNumber()
            );
        } else {
            System.out.println(
                Thread.currentThread().getName() +
                ": Runway unavailable for " + aircraft.getFlightNumber()
            );
        }
    }
}