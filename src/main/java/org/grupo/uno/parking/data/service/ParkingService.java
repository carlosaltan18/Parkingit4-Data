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
import java.util.Map;
import java.util.Optional;

@Service
public class ParkingService implements IParkingService {

    private static final Logger logger = LoggerFactory.getLogger(ParkingService.class);

    private final ParkingRepository parkingRepository;
    private final UserRepository userRepository;
    private final AudithService audithService;

    @Autowired
    public ParkingService(ParkingRepository parkingRepository, UserRepository userRepository, AudithService audithService) {
        this.parkingRepository = parkingRepository;
        this.userRepository = userRepository;
        this.audithService = audithService;
    }

    @Override
    public Page<ParkingDTO> getAllParkings(int page, int size) {
        logger.info("Fetching all parkings - Page: {}, Size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Parking> parkingPage = parkingRepository.findAll(pageable);

        // Convertir cada entidad Parking a un ParkingDTO que incluya la información del usuario
        return parkingPage.map(parking -> {
            ParkingDTO parkingDTO = new ParkingDTO();
            parkingDTO.setName(parking.getName());
            parkingDTO.setAddress(parking.getAddress());
            parkingDTO.setPhone(parking.getPhone());
            parkingDTO.setSpaces(parking.getSpaces());
            parkingDTO.setStatus(parking.getStatus());
            return parkingDTO;
        });
    }
    @Override
    public Page<ParkingDTO> searchParkingByName(String name, int page, int size) {
        logger.info("Searching parkings by name: {} - Page: {}, Size: {}", name, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Parking> parkingPage = parkingRepository.findByNameContainingIgnoreCase(name, pageable);

        // Convertir cada entidad Parking a un ParkingDTO
        return parkingPage.map(parking -> {
            ParkingDTO parkingDTO = new ParkingDTO();
            parkingDTO.setName(parking.getName());
            parkingDTO.setAddress(parking.getAddress());
            parkingDTO.setPhone(parking.getPhone());
            parkingDTO.setSpaces(parking.getSpaces());
            parkingDTO.setStatus(parking.getStatus());
            return parkingDTO;
        });
    }


    @Override
    public Page<Map<String, Object>> getParkingNamesAndStatus(int page, int size) {
        logger.info("Fetching parking names and status - Page: {}, Size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Parking> parkingPage = parkingRepository.findAll(pageable);

        // Convertir solo el id, nombre y el estado a un Map para devolver solo esos campos
        return parkingPage.map(parking -> {
            Map<String, Object> parkingInfo = new HashMap<>();
            parkingInfo.put("parkingId", parking.getParkingId());
            parkingInfo.put("name", parking.getName());
            parkingInfo.put("status", parking.getStatus());
            return parkingInfo;
        });
    }


    @Override
    public Optional<Parking> findById(long parkingId) {
        logger.info("Finding parking by ID: {}", parkingId);
        Optional<Parking> parking = parkingRepository.findById(parkingId);

        audithService.createAudit(
                "Parking",
                "Fetch parking by ID",
                "GET",
                Map.of("parkingId", parkingId),
                Map.of("parking", parking.orElse(null)),
                parking.isPresent() ? "SUCCESS" : "NOT_FOUND"
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
                "Parking",
                "Save new parking",
                "POST",
                Map.of("parking", parking),
                Map.of("savedParking", savedParking),
                "SUCCESS"
        );

        logger.info("Parking saved successfully with ID: {}", savedParking.getParkingId());
        return savedParking;
    }

    public void updateParking(ParkingDTO parkingDTO, long parkingId) {
        logger.info("Updating parking with ID: {}", parkingId);
        Parking parking = parkingRepository.findById(parkingId)
                .orElseThrow(() -> {
                    logger.error("Parking with ID {} does not exist", parkingId);
                    return new EntityNotFoundException("Parking with id: " + parkingId + " does not exist");
                });

        validateParkingDTO(parkingDTO);
        updateParkingFields(parking, parkingDTO);
        parkingRepository.save(parking);

        audithService.createAudit(
                "Parking",
                "Update parking",
                "PUT",
                Map.of("parkingId", parkingId, "parkingDTO", parkingDTO),
                Map.of("updatedParking", parking),
                "SUCCESS"
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
                    logger.error("Parking with ID {} does not exist", parkingId);
                    return new EntityNotFoundException("Parking with id: " + parkingId + " does not exist");
                });

        try {
            parkingRepository.delete(parking);

            audithService.createAudit(
                    "Parking",
                    "Delete parking",
                    "DELETE",
                    Map.of("parkingId", parkingId),
                    Map.of(),
                    "SUCCESS"
            );

            logger.info("Parking deleted successfully with ID: {}", parkingId);
        } catch (DataAccessException e) {
            logger.error("Error deleting parking with ID {}: {}", parkingId, e.getMessage());
            audithService.createAudit(
                    "Parking",
                    "Delete parking",
                    "DELETE",
                    Map.of("parkingId", parkingId),
                    Map.of("error", e.getMessage()),
                    "FAILURE"
            );
            throw e;
        }
    }

    private void validateParking(Parking parking) {
        if (parking.getName() == null || parking.getName().isEmpty()) {
            throw new IllegalArgumentException("Parking name cannot be null or empty.");
        }
        if (parking.getSpaces() <= 0) {
            throw new IllegalArgumentException("Parking must have at least one space.");
        }
    }

    private void validateParkingDTO(ParkingDTO parkingDTO) {
        if (parkingDTO.getName() == null || parkingDTO.getName().isEmpty()) {
            throw new IllegalArgumentException("Parking name cannot be null or empty.");
        }
        if (parkingDTO.getSpaces() <= 0) {
            throw new IllegalArgumentException("Parking must have at least one space.");
        }
    }
}
