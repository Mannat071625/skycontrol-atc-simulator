
package com.atc.config;

import com.atc.model.Airport;
import com.atc.model.Runway;
import com.atc.model.Gate;
import com.atc.model.Aircraft;
import com.atc.repository.AircraftRepository;
import com.atc.repository.FlightRepository;
import com.atc.repository.FlightHistoryRepository;
import com.atc.service.ControlTower;
import com.atc.service.WeatherService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AirportConfig {

    @Bean
    public Airport airport(AircraftRepository aircraftRepository) {
        Airport airport = new Airport(
            "DEL",
            "Indira Gandhi International Airport"
        );

        airport.addRunway(new Runway("RWY-01", 3500));
        airport.addRunway(new Runway("RWY-02", 3000));

        airport.addGate(new Gate("G-01"));
        airport.addGate(new Gate("G-02"));

        for (Aircraft aircraft : aircraftRepository.findAll()) {
            airport.registerAircraft(aircraft);
        }

        return airport;
    }

    @Bean
    public WeatherService weatherService() {
        return new WeatherService();
    }

    @Bean
    public ControlTower controlTower(
            Airport airport,
            WeatherService weatherService,
            FlightRepository flightRepository,
            FlightHistoryRepository flightHistoryRepository) {

        return new ControlTower(
            airport,
            weatherService,
            flightRepository,
            flightHistoryRepository
        );
    }
}
