
package com.atc.controller;

import com.atc.enums.WeatherCondition;
import com.atc.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    public Map<String, Object> getWeather() {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("condition", weatherService.getCurrentWeather());
        response.put("visibility", weatherService.getVisibility());
        response.put("windSpeed", weatherService.getWindSpeed());

        return response;
    }

    @PutMapping
    public ResponseEntity<?> updateWeather(@RequestBody Map<String, Object> request) {

        try {
            WeatherCondition condition = WeatherCondition.valueOf(
                request.get("condition").toString().toUpperCase()
            );

            int visibility = Integer.parseInt(
                request.get("visibility").toString()
            );

            double windSpeed = Double.parseDouble(
                request.get("windSpeed").toString()
            );

            weatherService.updateWeather(condition, visibility, windSpeed);

            return ResponseEntity.ok(getWeather());

        } catch (IllegalArgumentException | NullPointerException e) {
            return ResponseEntity.badRequest()
                .body("Invalid weather details. Check condition, visibility and wind speed.");
        }
    }

    @GetMapping("/restrictions")
    public Map<String, Object> getWeatherRestrictions() {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("landingRestricted", weatherService.isLandingRestricted());
        response.put("takeoffRestricted", weatherService.isTakeoffRestricted());
        response.put("emergencyLandingRestricted",
            weatherService.isEmergencyLandingRestricted());

        return response;
    }
}
