package org.grupo.uno.parking.data.repository;

import org.grupo.uno.parking.data.model.Fare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FareRepository extends JpaRepository<Fare, Long> {

    // Buscar tarifa por nombre
    @Query("SELECT f FROM Fare f WHERE f.name = :name")
    Optional<Fare> findByName(String name);

    @Query("SELECT f FROM Fare f WHERE :duration BETWEEN f.startTime AND f.endTime")
    List<Fare> findFareByDuration(@Param("duration") long duration);

}
