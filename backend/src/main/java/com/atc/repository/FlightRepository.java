package com.atc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.atc.model.Flight;

public interface FlightRepository extends JpaRepository<Flight, String> {
    boolean existsByFlightNumberIgnoreCase(String flightNumber);
}