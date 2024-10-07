package org.grupo.uno.parking.data.repository;

import org.grupo.uno.parking.data.model.Register;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegisterRepository extends JpaRepository<Register, Long> {
    // Método para encontrar registros por ID de estacionamiento
    List<Register> findByParking_ParkingId(Long parkingId);
}
