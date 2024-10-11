package org.grupo.uno.parking.data.repository;

import org.grupo.uno.parking.data.model.Audith;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AudithRepository extends JpaRepository<Audith, Long> {

    Page<Audith> findByEntity(String entity, Pageable pageable);

    Page<Audith> findByStartDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<Audith> findByEntityIgnoreCase(String entity, Pageable pageable);

    Optional<Audith> findById(Long id);

    Page<Audith> findByOperation(String operation, Pageable pageable);
}
