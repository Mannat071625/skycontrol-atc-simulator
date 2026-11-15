package com.atc.service;

import com.atc.enums.WeatherCondition;

public class WeatherService {
    private WeatherCondition currentWeather;
    private int visibility;
    private double windSpeed;

    public WeatherService() {
        currentWeather = WeatherCondition.CLEAR;
        visibility = 10000;
        windSpeed = 5.0;
    }

    public void updateWeather(WeatherCondition condition, int visibility, double windSpeed) {
        if (condition == null) {
            throw new IllegalArgumentException("Weather condition cannot be null");
        }

        if (visibility < 0 || windSpeed < 0) {
            throw new IllegalArgumentException("Visibility and wind speed cannot be negative");
        }

        this.currentWeather = condition;
        this.visibility = visibility;
        this.windSpeed = windSpeed;

        System.out.println("Weather conditions updated successfully.");
    }

    public boolean isLandingRestricted() {
        return currentWeather == WeatherCondition.FOG ||
               currentWeather == WeatherCondition.THUNDERSTORM ||
               currentWeather == WeatherCondition.STRONG_WIND ||
               visibility < 1000 ||
               windSpeed > 50;
    }
public boolean isEmergencyLandingRestricted() {
    return currentWeather == WeatherCondition.THUNDERSTORM ||
           (currentWeather == WeatherCondition.STRONG_WIND && windSpeed > 80) ||
           visibility < 200;
}
    public boolean isTakeoffRestricted() {
        return currentWeather == WeatherCondition.THUNDERSTORM ||
               currentWeather == WeatherCondition.STRONG_WIND ||
               visibility < 500 ||
               windSpeed > 60;
    }

    public void displayWeatherReport() {
        System.out.println("========== WEATHER REPORT ==========");
        System.out.println("Condition: " + currentWeather);
        System.out.println("Visibility: " + visibility + " metres");
        System.out.println("Wind Speed: " + windSpeed + " km/h");
        System.out.println("Landing Restricted: " + (isLandingRestricted() ? "YES" : "NO"));
        System.out.println("Takeoff Restricted: " + (isTakeoffRestricted() ? "YES" : "NO"));
    }

    public WeatherCondition getCurrentWeather() {
        return currentWeather;
    }

    public int getVisibility() {
        return visibility;
    }

    public double getWindSpeed() {
        return windSpeed;
    }
}