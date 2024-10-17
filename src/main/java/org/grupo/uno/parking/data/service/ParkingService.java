package org.grupo.uno.parking.data.service;

import jakarta.persistence.EntityNotFoundException;
import org.grupo.uno.parking.data.dto.ParkingDTO;
import org.grupo.uno.parking.data.model.Parking;
import org.grupo.uno.parking.data.repository.ParkingRepository;
import org.grupo.uno.parking.data.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ParkingService implements IParkingService {

    private static final Logger logger = LoggerFactory.getLogger(ParkingService.class);

    // Definir constantes para evitar duplicación de literales
    private static final String PARKING = "Parking";
    private static final String PARKING_NOT_FOUND = "Parking with ID {} does not exist";
    private static final String DOES_NOT_EXIST = " does not exist";
    private static final String PARKING_ID = "parkingId";
    private static final String SUCCESS = "SUCCESS";
    private static final String TEXTO_WITH = "with id: ";

    private final ParkingRepository parkingRepository;
    private final AudithService audithService;

    @Autowired
    public ParkingService(ParkingRepository parkingRepository, UserRepository userRepository, AudithService audithService) {
        this.parkingRepository = parkingRepository;
        this.audithService = audithService;
    }

    @Override
    public void patchParking(Long parkingId, Map<String, Object> updates) {
        logger.info("Patching parking with ID: {}", parkingId);

        Parking parking = parkingRepository.findById(parkingId)
                .orElseThrow(() -> {
                    logger.error(PARKING_NOT_FOUND, parkingId);
                    return new EntityNotFoundException(PARKING + TEXTO_WITH + parkingId + DOES_NOT_EXIST);
                });

        // Aplicar actualizaciones usando setters
        updates.forEach((key, value) -> {
            switch (key) {
                case "name":
                    parking.setName((String) value);
                    break;
                case "status":
                    parking.setStatus((Boolean) value);
                    break;
                default:
                    logger.error("Field {} not recognized", key);
                    throw new IllegalArgumentException("Field not recognized: " + key);
            }
        });

        parkingRepository.save(parking);

        audithService.createAudit(
                PARKING,
                "Patch parking",
                "PATCH",
                Map.of(PARKING_ID, parkingId, "updates", updates),
                Map.of("patchedParking", parking),
                SUCCESS
        );

        logger.info("Parking patched successfully with ID: {}", parkingId);
    }

    @Override
    public Page<ParkingDTO> getAllParkings(int page, int size) {
        logger.info("Fetching all parkings - Page: {}, Size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Parking> parkingPage = parkingRepository.findAll(pageable);

        return parkingPage.map(this::convertToDTO);
    }

    private ParkingDTO convertToDTO(Parking parking) {
        ParkingDTO parkingDTO = new ParkingDTO();
        parkingDTO.setName(parking.getName());
        parkingDTO.setAddress(parking.getAddress());
        parkingDTO.setPhone(parking.getPhone());
        parkingDTO.setSpaces(parking.getSpaces());
        parkingDTO.setStatus(parking.getStatus());
        return parkingDTO;
    }

    @Override
    public List<Map<String, Object>> getActiveParkings() {
        logger.info("Fetching all active parkings");

        List<Parking> activeParkings = parkingRepository.findByStatus(true);

        // Reemplazo de Stream.collect(Collectors.toList()) con Stream.toList()
        return activeParkings.stream().map(this::mapParkingInfo).toList();
    }

    private Map<String, Object> mapParkingInfo(Parking parking) {
        Map<String, Object> parkingInfo = new HashMap<>();
        parkingInfo.put("id", parking.getParkingId());
        parkingInfo.put("name", parking.getName());
        parkingInfo.put("status", parking.getStatus());
        return parkingInfo;
    }

    @Override
    public Page<Map<String, Object>> searchParkingByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Parking> parkingPage = parkingRepository.findByNameContainingIgnoreCase(name, pageable);

        logger.info("Querying for parkings with name containing '{}', found: {}", name, parkingPage.getTotalElements());

        if (parkingPage.isEmpty()) {
            logger.warn("No parkings found with name containing: {}", name);
        }

        return parkingPage.map(this::mapParkingInfo);
    }

    @Override
    public Page<Map<String, Object>> getParkingNamesAndStatus(int page, int size) {
        logger.info("Fetching parking names and status - Page: {}, Size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Parking> parkingPage = parkingRepository.findAll(pageable);

        return parkingPage.map(this::mapParkingInfo);
    }

    @Override
    public Optional<Parking> findById(long parkingId) {
        logger.info("Finding parking by ID: {}", parkingId);
        Optional<Parking> parking = parkingRepository.findById(parkingId);

        audithService.createAudit(
                PARKING,
                "Fetch parking by ID",
                "GET",
                Map.of(PARKING_ID, parkingId),
                Map.of("parking", parking.orElse(null)),
                parking.isPresent() ? SUCCESS : "NOT_FOUND"
        );

        if (parking.isPresent()) {
            logger.info("Parking found: {}", parking.get());
        } else {
            logger.warn("Parking with ID {} not found.", parkingId);
        }
        return parking;
    }

    @Override
    public Parking saveParking(Parking parking) {
        validateParking(parking);

        logger.info("Saving new parking with details: {}", parking);
        Parking savedParking = parkingRepository.save(parking);

        audithService.createAudit(
                PARKING,
                "Save new parking",
                "POST",
                Map.of("parking", parking),
                Map.of("savedParking", savedParking),
                SUCCESS
        );

        logger.info("Parking saved successfully with ID: {}", savedParking.getParkingId());
        return savedParking;
    }

    public void updateParking(ParkingDTO parkingDTO, long parkingId) {
        logger.info("Updating parking with ID: {}", parkingId);
        Parking parking = parkingRepository.findById(parkingId)
                .orElseThrow(() -> {
                    logger.error(PARKING_NOT_FOUND, parkingId);
                    return new EntityNotFoundException(PARKING + " with id: " + parkingId + DOES_NOT_EXIST);
                });

        validateParkingDTO(parkingDTO);
        updateParkingFields(parking, parkingDTO);
        parkingRepository.save(parking);

        audithService.createAudit(
                PARKING,
                "Update parking",
                "PUT",
                Map.of(PARKING_ID, parkingId, "parkingDTO", parkingDTO),
                Map.of("updatedParking", parking),
                SUCCESS
        );
        logger.info("Parking updated successfully with ID: {}", parkingId);
    }

    private void updateParkingFields(Parking parking, ParkingDTO parkingDTO) {
        logger.debug("Updating parking fields for ID: {}", parking.getParkingId());
        parking.setName(parkingDTO.getName());
        parking.setAddress(parkingDTO.getAddress());
        parking.setPhone(parkingDTO.getPhone());
        parking.setSpaces(parkingDTO.getSpaces());
        parking.setStatus(parkingDTO.getStatus());
    }

    @Override
    public void deleteParking(Long parkingId) {
        logger.info("Deleting parking with ID: {}", parkingId);
        Parking parking = parkingRepository.findById(parkingId)
                .orElseThrow(() -> {
                    logger.error(PARKING_NOT_FOUND, parkingId);
                    return new EntityNotFoundException(PARKING + " with id: " + parkingId + DOES_NOT_EXIST);
                });
        try {
            parkingRepository.delete(parking);
            audithService.createAudit(
                    PARKING,
                    "Delete parking",
                    "DELETE",
                    Map.of(PARKING_ID, parkingId),
                    Map.of(),
                    SUCCESS
            );
            logger.info("Parking deleted successfully with ID: {}", parkingId);
        } catch (DataAccessException e) {
            logger.error("Error deleting parking with ID {}: {}", parkingId, e.getMessage());
        }
    }

    private void validateParking(Parking parking) {
        if (parking.getName() == null || parking.getName().isBlank()) {
            throw new IllegalArgumentException("Parking name is required");
        }
    }

    private void validateParkingDTO(ParkingDTO parkingDTO) {
        if (parkingDTO.getName() == null || parkingDTO.getName().isBlank()) {
            throw new IllegalArgumentException("Parking name is required");
        }
    }
}
