package org.grupo.uno.parking.data.service;

import org.grupo.uno.parking.data.dto.ParkingDTO;
import org.grupo.uno.parking.data.model.Parking;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IParkingService {
    void patchParking(Long parkingId, Map<String, Object> updates);

    Page<ParkingDTO> getAllParkings(int page, int size);

    List<Map<String, Object>> getActiveParkings();

    Page<Map<String, Object>> searchParkingByName(String name, int page, int size);

    Page<Map<String, Object>> getParkingNamesAndStatus(int page, int size);

    Optional<Parking> findById(long parkingId);

    Parking saveParking(Parking parking);

    void deleteParking(Long parkingId);
}
