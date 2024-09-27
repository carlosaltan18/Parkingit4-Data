package org.grupo.uno.parking.data.repository;

import org.grupo.uno.parking.data.model.Fare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FareRepository extends JpaRepository<Fare, Long> {
    @Query("SELECT f FROM Fare f WHERE f.name = :name")
    Optional<Fare> findByName(@Param("name") String name);
}
