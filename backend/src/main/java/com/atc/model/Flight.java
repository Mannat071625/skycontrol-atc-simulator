package com.atc.model;

import java.time.LocalDateTime;
import com.atc.enums.FlightStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "flights")
public class Flight {
    @Id
    @Column(nullable = false, unique = true)
    private String flightNumber;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private LocalDateTime scheduledDeparture;

    @Column(nullable = false)
    private LocalDateTime scheduledArrival;

    @Enumerated(EnumType.STRING)
    private FlightStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "aircraft_id", nullable = false)
    private Aircraft aircraft;

    protected Flight() {}

    public Flight(String flightNumber, String source, String destination,
                  LocalDateTime scheduledDeparture, LocalDateTime scheduledArrival,
                  Aircraft aircraft) {
        if (flightNumber == null || flightNumber.isBlank() ||
            source == null || source.isBlank() ||
            destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Flight details cannot be empty");
        }

        if (scheduledDeparture == null || scheduledArrival == null ||
            !scheduledArrival.isAfter(scheduledDeparture)) {
            throw new IllegalArgumentException("Invalid flight schedule");
        }

        if (aircraft == null) {
            throw new IllegalArgumentException("Aircraft cannot be null");
        }

        this.flightNumber = flightNumber;
        this.source = source;
        this.destination = destination;
        this.scheduledDeparture = scheduledDeparture;
        this.scheduledArrival = scheduledArrival;
        this.aircraft = aircraft;
        this.status = FlightStatus.SCHEDULED;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDateTime getScheduledDeparture() {
        return scheduledDeparture;
    }

    public LocalDateTime getScheduledArrival() {
        return scheduledArrival;
    }

    public FlightStatus getStatus() {
        return status;
    }

    public Aircraft getAircraft() {
        return aircraft;
    }

    public void setStatus(FlightStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Flight status cannot be null");
        }
        this.status = status;
    }

    public void displayFlightDetails() {
        System.out.println("Flight Number: " + flightNumber);
        System.out.println("Route: " + source + " -> " + destination);
        System.out.println("Scheduled Departure: " + scheduledDeparture);
        System.out.println("Scheduled Arrival: " + scheduledArrival);
        System.out.println("Status: " + status);
        System.out.println("Aircraft: " + aircraft.getAircraftId());
    }
}