
package com.atc.history;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "flight_history")
public class FlightRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String flightNumber;

    @Column(nullable = false)
    private String aircraftId;

    @Column(nullable = false)
    private String event;

    private String details;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    protected FlightRecord() {}

    public FlightRecord(String flightNumber, String aircraftId, String event, String details) {
        if (flightNumber == null || flightNumber.isBlank() ||
            aircraftId == null || aircraftId.isBlank() ||
            event == null || event.isBlank()) {
            throw new IllegalArgumentException("Flight record details cannot be empty.");
        }

        this.flightNumber = flightNumber;
        this.aircraftId = aircraftId;
        this.event = event;
        this.details = details == null ? "" : details;
        this.timestamp = LocalDateTime.now();
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getAircraftId() {
        return aircraftId;
    }

    public String getEvent() {
        return event;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void displayRecord() {
        System.out.println("Flight Number: " + flightNumber);
        System.out.println("Aircraft ID: " + aircraftId);
        System.out.println("Event: " + event);
        System.out.println("Details: " + details);
        System.out.println("Timestamp: " + timestamp);
    }
}
