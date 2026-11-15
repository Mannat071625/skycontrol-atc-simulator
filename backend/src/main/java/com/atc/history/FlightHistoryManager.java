
package com.atc.history;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.atc.repository.FlightHistoryRepository;

public class FlightHistoryManager {
    private final FlightHistoryRepository historyRepository;
    private final ArrayList<FlightRecord> localRecords;

    public FlightHistoryManager() {
        this.historyRepository = null;
        this.localRecords = new ArrayList<>();
    }

    public FlightHistoryManager(FlightHistoryRepository historyRepository) {
        if (historyRepository == null) {
            throw new IllegalArgumentException("Repository cannot be null");
        }
        this.historyRepository = historyRepository;
        this.localRecords = null;
    }

    public void addRecord(FlightRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Flight record cannot be null.");
        }

        if (historyRepository != null) {
            historyRepository.save(record);
        } else {
            localRecords.add(record);
        }

        System.out.println("Flight history record added successfully.");
    }

    public void displayAllRecords() {
        List<FlightRecord> records = getRecords();

        if (records.isEmpty()) {
            System.out.println("No flight history available.");
            return;
        }

        for (FlightRecord record : records) {
            System.out.println("------------------------------");
            record.displayRecord();
        }
    }

    public void searchByFlightNumber(String flightNumber) {
        List<FlightRecord> matchingRecords;

        if (historyRepository != null) {
            matchingRecords = historyRepository.findByFlightNumberIgnoreCase(flightNumber);
        } else {
            matchingRecords = new ArrayList<>();

            for (FlightRecord record : localRecords) {
                if (record.getFlightNumber().equalsIgnoreCase(flightNumber)) {
                    matchingRecords.add(record);
                }
            }
        }

        if (matchingRecords.isEmpty()) {
            System.out.println("No history found for Flight " + flightNumber);
            return;
        }

        for (FlightRecord record : matchingRecords) {
            System.out.println("------------------------------");
            record.displayRecord();
        }
    }

    public void displayLatestRecordsFirst() {
        List<FlightRecord> records = getRecords();

        if (records.isEmpty()) {
            System.out.println("No flight history available.");
            return;
        }

        records.sort(
            Comparator.comparing(FlightRecord::getTimestamp).reversed()
        );

        for (FlightRecord record : records) {
            System.out.println("------------------------------");
            record.displayRecord();
        }
    }

    public ArrayList<FlightRecord> getRecords() {
        if (historyRepository != null) {
            return new ArrayList<>(historyRepository.findAll());
        }
        return new ArrayList<>(localRecords);
    }
}
