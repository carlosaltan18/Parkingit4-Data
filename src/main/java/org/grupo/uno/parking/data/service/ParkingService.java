package org.grupo.uno.parking.data.service;

import jakarta.persistence.EntityNotFoundException;
import org.grupo.uno.parking.data.dto.ParkingDTO;
import org.grupo.uno.parking.data.model.Parking;
import org.grupo.uno.parking.data.model.User;
import org.grupo.uno.parking.data.repository.ParkingRepository;
import org.grupo.uno.parking.data.repository.UserRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ParkingService implements IParkingService {

    private final ParkingRepository parkingRepository;
    private final UserRepository userRepository;

    public ParkingService(ParkingRepository parkingRepository, UserRepository userRepository) {
        this.parkingRepository = parkingRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Page<Parking> getAllParkings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return parkingRepository.findAll(pageable);
    }

    @Override
    public Optional<Parking> findById(long parkingId) {
        return parkingRepository.findById(parkingId);
    }

    @Override
    public Parking saveParking(Parking parking) {
        return parkingRepository.save(parking);
    }

    public void updateParking(ParkingDTO parkingDTO, long parkingId) {
        Parking parking = parkingRepository.findById(parkingId)
                .orElseThrow(() -> new EntityNotFoundException("Parking with id: " + parkingId + " does not exist"));

        // Actualizamos los campos utilizando el DTO
        updateParkingFields(parking, parkingDTO);
        parkingRepository.save(parking);
    }

    private void updateParkingFields(Parking parking, ParkingDTO parkingDTO) {
        parking.setName(parkingDTO.getName());
        parking.setAddress(parkingDTO.getAddress());
        parking.setPhone(parkingDTO.getPhone());
        parking.setSpaces(parkingDTO.getSpaces());
        parking.setStatus(parkingDTO.getStatus());
        if (parkingDTO.getUserId() != 0) {
            User user = userRepository.findById(parkingDTO.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("User with id: " + parkingDTO.getUserId() + " does not exist"));
            parking.setUserId(user);
        }
    }

    @Override
    public void deleteParking(Long parkingId) {
        Parking parking = parkingRepository.findById(parkingId)
                .orElseThrow(() -> new EntityNotFoundException("Parking with id: " + parkingId + " does not exist"));

        try {
            parkingRepository.delete(parking);
        } catch (DataAccessException e) {
            throw new DataAccessException("Error deleting parking with id: " + parkingId, e) {};
        }
    }
}
