
package com.atc.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.atc.enums.FlightStatus;
import com.atc.model.Flight;
import com.atc.repository.FlightRepository;

public class FlightScheduler {
    private final FlightRepository flightRepository;
    private final ArrayList<Flight> localFlights;

    public FlightScheduler() {
        this.flightRepository = null;
        this.localFlights = new ArrayList<>();
    }

    public FlightScheduler(FlightRepository flightRepository) {
        if (flightRepository == null) {
            throw new IllegalArgumentException("Repository cannot be null");
        }
        this.flightRepository = flightRepository;
        this.localFlights = null;
    }

    public void addFlight(Flight flight) {
        if (flight == null) {
            throw new IllegalArgumentException("Flight cannot be null");
        }

        List<Flight> flights = getFlights();

        for (Flight existing : flights) {
            if (existing.getFlightNumber().equalsIgnoreCase(flight.getFlightNumber())) {
                throw new IllegalArgumentException("Flight already exists");
            }

            if (existing.getAircraft().getAircraftId().equalsIgnoreCase(
                    flight.getAircraft().getAircraftId())) {

                boolean overlapping =
                    flight.getScheduledDeparture().isBefore(existing.getScheduledArrival()) &&
                    flight.getScheduledArrival().isAfter(existing.getScheduledDeparture());

                if (overlapping) {
                    throw new IllegalArgumentException(
                        "Aircraft is already assigned to another flight during this time."
                    );
                }
            }
        }

        if (flightRepository != null) {
            flightRepository.save(flight);
        } else {
            localFlights.add(flight);
        }

        System.out.println("Flight " + flight.getFlightNumber() + " scheduled successfully.");
    }

    public Flight searchFlight(String flightNumber) {
        if (flightRepository != null) {
            return flightRepository.findById(flightNumber).orElse(null);
        }

        for (Flight flight : localFlights) {
            if (flight.getFlightNumber().equalsIgnoreCase(flightNumber)) {
                return flight;
            }
        }
        return null;
    }

    public void displayAllFlights() {
        List<Flight> flights = getFlights();

        if (flights.isEmpty()) {
            System.out.println("No flights are currently scheduled.");
            return;
        }

        for (Flight flight : flights) {
            System.out.println("------------------------------");
            flight.displayFlightDetails();
        }
    }

    public void displayFlightsByDepartureTime() {
        List<Flight> flights = getFlights();
        flights.sort(Comparator.comparing(Flight::getScheduledDeparture));

        if (flights.isEmpty()) {
            System.out.println("No flights are currently scheduled.");
            return;
        }

        for (Flight flight : flights) {
            System.out.println("------------------------------");
            flight.displayFlightDetails();
        }
    }

    public void updateFlightStatus(String flightNumber, FlightStatus status) {
        Flight flight = searchFlight(flightNumber);

        if (flight == null) {
            System.out.println("Flight not found.");
            return;
        }

        flight.setStatus(status);

        if (flightRepository != null) {
            flightRepository.save(flight);
        }

        System.out.println("Flight status updated successfully.");
    }

    public ArrayList<Flight> getFlights() {
        if (flightRepository != null) {
            return new ArrayList<>(flightRepository.findAll());
        }
        return new ArrayList<>(localFlights);
    }
}
