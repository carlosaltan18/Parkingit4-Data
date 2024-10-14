package org.grupo.uno.parking.data.repository;

import org.grupo.uno.parking.data.model.Register;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT r FROM Register r WHERE r.plate = :plate AND r.endDate IS NULL")
    Optional<Register> findActiveRegisterByPlate(@Param("plate") String plate);


    @Query("SELECT r FROM Register r WHERE r.parking.id = :parkingId AND r.startDate BETWEEN :startDate AND :endDate AND r.status = true")
    Page<Register> findActiveRegistersByParkingIdAndDateRange(
            @Param("parkingId") Long parkingId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT r FROM Register r WHERE r.parking.parkingId = :parkingId AND r.total > 0 AND r.endDate IS NOT NULL AND r.endDate BETWEEN :startDate AND :endDate")
    List<Register> findActiveRegistersByParkingIdAndDateRangePDF(Long parkingId, LocalDateTime startDate, LocalDateTime endDate);
}
