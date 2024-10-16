package org.grupo.uno.parking.data.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.grupo.uno.parking.data.service.ServiceFare;
import org.grupo.uno.parking.data.dto.FareDto;
import org.grupo.uno.parking.data.exception.AllDataRequiredException;
import org.grupo.uno.parking.data.model.Fare;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fare")
@AllArgsConstructor
@Controller
public class FareController {
    private static final String MESSAGE = "message";
    private static final String ERROR = "Error";
    private final ServiceFare fareService;
    private static final Logger logger = LoggerFactory.getLogger(FareController.class);

    @RolesAllowed("FARE")
    @PostMapping("/saveFare")
    public ResponseEntity<Map<String, String>> addFare(@RequestBody FareDto dto){
        Map<String, String> response = new HashMap<>();
        try{
            fareService.addFare(dto);
            response.put(MESSAGE, "Fare saved: " + dto.getName());
            logger.info("Fare added: name={}, price={}", dto.getName(), dto.getPrice());
            return ResponseEntity.ok(response);
        } catch (AllDataRequiredException e) {
            response.put(ERROR, e.getMessage());
            logger.warn("All data is required: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error adding fare: {}", e.getMessage());
            response.put(ERROR, "An error occurred while adding fare: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @RolesAllowed("FARE")
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getAllFares(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {

        Map<String, Object> response = new HashMap<>();
        try {
            Page<Fare> fares = fareService.getAllFares(page, size, name);

            response.put(MESSAGE, "Fares retrieved successfully");
            response.put("fares", fares.getContent());
            response.put("totalPages", fares.getTotalPages());
            response.put("currentPage", fares.getNumber());
            response.put("totalElements", fares.getTotalElements());
            logger.info("Fares retrieved: totalElements={}, currentPage={}", fares.getTotalElements(), fares.getNumber());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving fares: {}", e.getMessage());
            response.put(ERROR, "An error occurred while retrieving fares: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @RolesAllowed("FARE")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getFareById(@PathVariable Long id){
        Map<String, Object> response = new HashMap<>();
        try {
            Fare fare = fareService.findFareById(id).orElseThrow(() -> new EntityNotFoundException("Fare not found for ID: " + id));
            response.put(MESSAGE, fare);
            logger.info("Fare found for ID: {}", id);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Fare not found: {}", e.getMessage());
            response.put(ERROR, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Error finding fare: {}", e.getMessage());
            response.put(ERROR, "An error occurred while finding fare: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @RolesAllowed("FARE")
    @GetMapping("/name/{name}")
    public ResponseEntity<Map<String, Object>> getFareByName(@PathVariable String name){
        Map<String, Object> response = new HashMap<>();
        try {
            Fare fare = fareService.findByName(name).orElseThrow(() -> new EntityNotFoundException("Fare not found for name: " + name));
            response.put(MESSAGE, fare);
            logger.info("Fare found for name: {}", name);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Fare not found for name: {}", e.getMessage());
            response.put(ERROR, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Error finding fare: {}", e.getMessage());
            response.put(ERROR, "An error occurred while finding fare: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @RolesAllowed("FARE")
    @DeleteMapping("/deleteFare/{id}")
    public ResponseEntity<Map<String, String>> deleteFare(@PathVariable Long id){
        Map<String, String> response = new HashMap<>();
        try {
            fareService.delete(id);
            response.put(MESSAGE, "Fare deleted successfully");
            logger.info("Fare deleted: ID={}", id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | DataAccessException e) {
            logger.error("Error deleting fare: ID={} - {}", id, e.getMessage());
            response.put(ERROR, "An error occurred while deleting fare: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @RolesAllowed("FARE")
    @PutMapping("/updateFare/{id}")
    public ResponseEntity<Map<String, String>> updateFare(@PathVariable Long id, @RequestBody FareDto dto){
        Map<String, String> response = new HashMap<>();
        try {
            fareService.updateFare(dto, id);
            logger.info("Fare updated: ID={}, name={}", id, dto.getName());
            response.put(MESSAGE, "Fare updated successfully: " + dto.getName());
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Error updating fare: {}", e.getMessage());
            response.put(ERROR, "Fare not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
