package org.grupo.uno.parking.data.service;

import org.grupo.uno.parking.data.model.Parking;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface IParkingService {
    Page<Parking> getAllParkings(int page, int size);

    Optional<Parking> findById(long parkingId);

    Parking saveParking(Parking parking);

    void deleteParking(Long parkingId);
}
