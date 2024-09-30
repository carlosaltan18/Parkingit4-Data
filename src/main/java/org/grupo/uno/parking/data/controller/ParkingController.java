package org.grupo.uno.parking.data.controller;

import jakarta.persistence.EntityNotFoundException;
import org.grupo.uno.parking.data.dto.ParkingDTO;
import org.grupo.uno.parking.data.model.Parking;
import org.grupo.uno.parking.data.service.ParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/parkings")
public class ParkingController {

    private final ParkingService parkingService;

    @Autowired
    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @GetMapping("")
    public ResponseEntity<Page<Parking>> getAllParkings(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        Page<Parking> parkings = parkingService.getAllParkings(page, size);
        return ResponseEntity.ok(parkings);
    }


    @GetMapping("/parking/{id}")
    public ResponseEntity<Parking> getParkingById(@PathVariable("id") long id) {
        Optional<Parking> parking = parkingService.findById(id);
        return parking.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/parking/save")
    public ResponseEntity<Parking> createParking(@RequestBody ParkingDTO parkingDTO) {
        Parking parking = new Parking();
        updateParkingFieldsFromDTO(parking, parkingDTO);
        Parking savedParking = parkingService.saveParking(parking);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedParking);
    }

    @PutMapping("/parking/update/{id}")
    public ResponseEntity<Parking> updateParking(@PathVariable("id") long id, @RequestBody ParkingDTO parkingDTO) {
        try {
            parkingService.updateParking(parkingDTO, id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/parking/delete/{id}")
    public ResponseEntity<Void> deleteParking(@PathVariable("id") long id) {
        try {
            parkingService.deleteParking(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
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
