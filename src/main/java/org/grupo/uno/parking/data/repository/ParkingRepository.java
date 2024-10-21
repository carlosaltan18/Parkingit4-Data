package org.grupo.uno.parking.data.repository;

import org.grupo.uno.parking.data.model.Parking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingRepository extends JpaRepository<Parking, Long> {
    Page<Parking> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Parking> findByStatus(boolean status);
}
