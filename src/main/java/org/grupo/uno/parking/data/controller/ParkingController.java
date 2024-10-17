
package org.grupo.uno.parking.data.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.EntityNotFoundException;
import org.grupo.uno.parking.data.dto.ParkingDTO;
import org.grupo.uno.parking.data.model.Parking;
import org.grupo.uno.parking.data.service.ParkingService;
import org.grupo.uno.parking.data.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/parkings")
public class ParkingController {

    private final ParkingService parkingService;

    @Autowired
    public ParkingController(ParkingService parkingService, UserRepository userRepository) {
        this.parkingService = parkingService;
    }

    @RolesAllowed("PARKING")
    @GetMapping("/active")
    public ResponseEntity<List<Map<String, Object>>> getActiveParkings() {
        List<Map<String, Object>> activeParkings = parkingService.getActiveParkings();
        return ResponseEntity.ok(activeParkings);
    }

    @RolesAllowed("PARKING")
    @GetMapping("")
    public ResponseEntity<Page<ParkingDTO>> getAllParkings(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        Page<ParkingDTO> parkings = parkingService.getAllParkings(page, size);
        return ResponseEntity.ok(parkings);
    }

    @RolesAllowed("PARKING")
    @PatchMapping("/parkingPatch/{id}")
    public ResponseEntity<Void> patchParking(@PathVariable("id") Long id, @RequestBody Map<String, Object> updates) {
        try {
            parkingService.patchParking(id, updates);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }


    @RolesAllowed("PARKING")
    @GetMapping("/search")
    public ResponseEntity<Page<Map<String, Object>>> searchParkingsByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Map<String, Object>> parkingPage = parkingService.searchParkingByName(name, page, size);
        return ResponseEntity.ok(parkingPage);
    }





    @RolesAllowed("PARKING")
        @GetMapping("/namesAndStatus")
    public ResponseEntity<Page<Map<String, Object>>> getParkingNamesAndStatus(@RequestParam(defaultValue = "0") int page,
                                                                              @RequestParam(defaultValue = "10") int size) {
        Page<Map<String, Object>> parkingNamesAndStatus = parkingService.getParkingNamesAndStatus(page, size);
        return ResponseEntity.ok(parkingNamesAndStatus);
    }


    @RolesAllowed("PARKING")
    @GetMapping("/{id}")
    public ResponseEntity<Parking> getParkingById(@PathVariable("id") long id) {
        Optional<Parking> parking = parkingService.findById(id);
        return parking.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @RolesAllowed("PARKING")
    @PostMapping("/saveParking")
    public ResponseEntity<Parking> createParking(@RequestBody ParkingDTO parkingDTO) {
        Parking parking = new Parking();
        updateParkingFieldsFromDTO(parking, parkingDTO);
        Parking savedParking = parkingService.saveParking(parking);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedParking);
    }

    @RolesAllowed("PARKING")
    @PutMapping("/parkingUpdate/{id}")
    public ResponseEntity<Parking> updateParking(@PathVariable("id") long id, @RequestBody ParkingDTO parkingDTO) {
        try {
            parkingService.updateParking(parkingDTO, id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @RolesAllowed("PARKING")
    @DeleteMapping("/parkingDelete/{id}")
    public ResponseEntity<Void> deleteParking(@PathVariable("id") Long parkingId) {
        try {
            parkingService.deleteParking(parkingId);
            return ResponseEntity.noContent().build();
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void updateParkingFieldsFromDTO(Parking parking, ParkingDTO parkingDTO) {
        parking.setName(parkingDTO.getName());
        parking.setAddress(parkingDTO.getAddress());
        parking.setPhone(parkingDTO.getPhone());
        parking.setSpaces(parkingDTO.getSpaces());
        parking.setStatus(parkingDTO.getStatus());
    }
}




