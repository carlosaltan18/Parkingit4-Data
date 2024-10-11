package org.grupo.uno.parking.data.repository;

import org.grupo.uno.parking.data.model.Register;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegisterRepository extends JpaRepository<Register, Long> {
    // Método para encontrar registros por ID de estacionamiento
    List<Register> findByParking_ParkingId(Long parkingId);

    // Método para encontrar un registro por la placa
    Optional<Register> findByPlate(String plate);

    // Método para buscar registros activos en un rango de fechas
    @Query("SELECT r FROM Register r WHERE r.parking.parkingId = :parkingId AND r.total > 0 AND r.endDate IS NOT NULL AND r.endDate BETWEEN :startDate AND :endDate")
    List<Register> findActiveRegistersByParkingIdAndDateRange(Long parkingId, LocalDateTime startDate, LocalDateTime endDate);
}
