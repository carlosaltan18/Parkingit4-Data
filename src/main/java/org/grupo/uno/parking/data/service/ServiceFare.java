package org.grupo.uno.parking.data.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.grupo.uno.parking.data.dto.FareDto;
import org.grupo.uno.parking.data.exception.AllDataRequiredException;
import org.grupo.uno.parking.data.exceptions.FareExist;
import org.grupo.uno.parking.data.model.Fare;
import org.grupo.uno.parking.data.repository.FareRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@Service
public class ServiceFare implements IServiceFare {

    private static final Logger logger = LoggerFactory.getLogger(ServiceFare.class);
    private final FareRepository fareRepository;
    private final AudithService audithService;

    private static final String MESSAGE1="This Fare doesn't exist";
    private  static final String SUCCESS="Success";

    @Override
    public Page<Fare> getAllFares(int page, int size, String name) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Fare> fares;

        if (name == null || name.isEmpty()) {
            fares = fareRepository.findAll(pageable);
        } else {
            fares = fareRepository.findByNameContainingIgnoreCase(name, pageable);
        }
        // Auditar la recuperación de tarifas
        audithService.createAudit(
                "Fare",
                "Retrieved all fares",
                "READ",
                null,  // Objeto no aplica
                convertToMap(fares), // Respuesta
                SUCCESS
        );

        return fares;
    }

    @Override
    public Optional<Fare> findFareById(Long id) {
        if (id == null) {
            logger.warn("Id not found");
            throw new IllegalArgumentException("Id is necessary");
        }

        Fare fare = fareRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(""));

        // Auditar la búsqueda de tarifa
        audithService.createAudit(
                "Fare",
                "Retrieved fare with ID " + id,
                "READ",
                null,  // Objeto no aplica
                convertToMap(fare),  // Respuesta
                SUCCESS
        );

        return Optional.of(fare);
    }

    @Override
    public void delete(Long idFare) {
        if (!fareRepository.existsById(idFare)) {
            logger.error("Fare not found");
            throw new EntityNotFoundException("This fare doesn't exist");
        }

        try {
            Fare fareToDelete = fareRepository.findById(idFare)
                    .orElseThrow(() -> new EntityNotFoundException(MESSAGE1));
            fareRepository.deleteById(idFare);

            // Auditar la eliminación de la tarifa
            audithService.createAudit(
                    "Fare",
                    "Fare with ID " + idFare + " was deleted",
                    "DELETE",
                    convertToMap(fareToDelete),  // Objeto de la tarifa eliminada
                    null,
                    SUCCESS
            );
        } catch (DataAccessException e) {
            logger.error("Failed deleting fare", e);
        }
    }

    @Override
    public void updateFare(FareDto fareDto, Long id) {
        if (!fareRepository.existsById(id)) {
            logger.warn("Fare not exist with this id");
            throw new EntityNotFoundException(MESSAGE1);
        }

        Fare fare = fareRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MESSAGE1));

        if (fareDto.getName() != null) {
            Optional<Fare> existingFareWithName = fareRepository.findByName(fareDto.getName());
            if (existingFareWithName.isPresent() && !existingFareWithName.get().getFareId().equals(id)) {
                logger.warn("Fare with name {} already exists", fareDto.getName());
                throw new FareExist("Fare with name " + fareDto.getName() + " already exists");
            }
            fare.setName(fareDto.getName());
        }
        if (fareDto.getStartTime() != null) fare.setStartTime(fareDto.getStartTime());
        if (fareDto.getEndTime() != null) fare.setEndTime(fareDto.getEndTime());
        if (fareDto.getPrice() != null) fare.setPrice(fareDto.getPrice());
        if (fareDto.getStatus() != null) fare.setStatus(fareDto.getStatus());

        fareRepository.save(fare);

        // Auditar la actualización de la tarifa
        audithService.createAudit(
                "Fare",
                "Fare with ID " + id + " was updated",
                "UPDATE",
                convertToMap(fare),  // Objeto de la tarifa actualizada
                convertToMap(fare),  // Respuesta, asegurando que no sea null
                "SUCCESS"
        );
    }

    @Override
    public Fare addFare(FareDto fareDto) {
        // Validación de datos requeridos
        if (fareDto.getName() == null || fareDto.getStartTime() == null || fareDto.getEndTime() == null || fareDto.getPrice() == null) {
            logger.warn("All data is required");
            throw new AllDataRequiredException("All data is required");
        }

        // Verificar si la tarifa ya existe
        Optional<Fare> existingFare = fareRepository.findByName(fareDto.getName());
        if (existingFare.isPresent()) {
            logger.warn("Fare with name {} already exists", fareDto.getName());
            throw new FareExist("Fare with name " + fareDto.getName() + " already exists");
        }

        // Crear y guardar la nueva tarifa
        Fare fare = new Fare();
        fare.setName(fareDto.getName());
        fare.setStartTime(fareDto.getStartTime());
        fare.setEndTime(fareDto.getEndTime());
        fare.setPrice(fareDto.getPrice());
        fare.setStatus(true);
        logger.info("Fare created {}", fareDto.getName());

        fare = fareRepository.save(fare);

        // Auditar la creación de la tarifa
        audithService.createAudit(
                "Fare",
                "Fare created with name " + fareDto.getName(),
                "CREATE",
                convertToMap(fare),  // Objeto de la tarifa creada
                convertToMap(fare),  // Respuesta, asegurando que no sea null
                "SUCCESS"
        );

        return fare;
    }

    @Override
    public Optional<Fare> findByName(String name) {
        return fareRepository.findByName(name);
    }

    // Método para convertir Fare a Map
    private Map<String, Object> convertToMap(Fare fare) {
        Map<String, Object> fareMap = new HashMap<>();
        fareMap.put("id", fare.getFareId());
        fareMap.put("name", fare.getName());
        fareMap.put("startTime", fare.getStartTime());
        fareMap.put("endTime", fare.getEndTime());
        fareMap.put("price", fare.getPrice());
        fareMap.put("status", fare.getStatus());
        return fareMap;
    }

    // Método para convertir Page<Fare> a Map
    private Map<String, Object> convertToMap(Page<Fare> fares) {
        Map<String, Object> faresMap = new HashMap<>();
        faresMap.put("content", fares.getContent());
        faresMap.put("totalElements", fares.getTotalElements());
        faresMap.put("totalPages", fares.getTotalPages());
        faresMap.put("size", fares.getSize());
        faresMap.put("number", fares.getNumber());
        return faresMap;
    }
}
