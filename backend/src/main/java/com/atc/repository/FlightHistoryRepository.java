
package com.atc.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.atc.history.FlightRecord;

public interface FlightHistoryRepository extends JpaRepository<FlightRecord, Long> {
    List<FlightRecord> findByFlightNumberIgnoreCase(String flightNumber);
    List<FlightRecord> findAllByOrderByTimestampDesc();
}
