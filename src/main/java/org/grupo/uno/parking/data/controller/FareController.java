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
import java.util.Optional;

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
            response.put(MESSAGE, "Fare Saved: " + dto.getName());
            logger.info("Fare with name: {} and price: {} add", dto.getName(), dto.getPrice());
            return ResponseEntity.ok(response);
        }catch (AllDataRequiredException e){
            response.put(ERROR, e.getMessage());
            logger.warn("All data is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }catch (Exception e){
            logger.error("Fail add fare with name: {}", dto.getName());
            response.put(ERROR, "An Error ocurred add fare"+ e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @RolesAllowed("FARE")
    @GetMapping("")
    public ResponseEntity<Map<String, String>> getAllFares(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        Map<String, String> response = new HashMap<>();
        try{
            Page<Fare> fares = fareService.getAllFares(page, size);
            response.put(MESSAGE, "fares retrieved successfully");
            response.put("users", fares.getContent().toString());
            response.put("totalPages", String.valueOf(fares.getTotalPages()));
            response.put("currentPage", String.valueOf(fares.getNumber()));
            response.put("totalElements", String.valueOf(fares.getTotalElements()));
            logger.info("Fares get with exit pages: {}, elements: {}",page, fares.getTotalElements());
            return ResponseEntity.ok(response);
        }catch (Exception e){
            logger.error("Fail get fares");
            response.put(ERROR, "An Error ocurred get all fares"+ e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }

    @RolesAllowed("FARE")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, String>> getFareId(@PathVariable Long id){
        Map<String, String> response = new HashMap<>();
        try{
            Optional<Fare> fares = fareService.findFareById(id);
            response.put(MESSAGE, fares.toString());
            logger.info("FARE find {} is correct", id);
            return ResponseEntity.ok(response);
        }catch (Exception e){
            logger.error("Fail get fare");
            response.put(ERROR, "An Error ocurred find fare id "+ e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }

    @RolesAllowed("FARE")
    @GetMapping("/name/{name}")
    public ResponseEntity<Map<String, String>> getFareName(@PathVariable String name){
        Map<String, String> response = new HashMap<>();
        try{
            Optional<Fare> fares = fareService.findByName(name);
            response.put(MESSAGE, fares.toString());
            logger.info("FARE find with name:  {} is correct", name);
            return ResponseEntity.ok(response);
        }catch (Exception e){
            logger.error("Fail get fare with name {}", name);
            response.put(ERROR, "An Error ocurred find fare name "+ e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }

    @RolesAllowed("FARE")
    @DeleteMapping("/deleteFare/{id}")
    public ResponseEntity<Map<String, String>> deleteFare(@PathVariable Long id){
        Map<String, String> response = new HashMap<>();
        try{
            fareService.delete(id);
            response.put(MESSAGE, "dalete fare succesfully");
            logger.info("FARE was delete {}", id);
            return  ResponseEntity.ok(response);
        }catch (IllegalArgumentException | DataAccessException e){
            response.put(MESSAGE, ERROR);
            logger.error("Fail delete fare, id is not found");
            response.put("err", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }catch(Exception e){
            logger.error("Fail delete fare");
            response.put(MESSAGE, ERROR);
            response.put("err", "An Error delete fare  "+ e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }

    @RolesAllowed("FARE")
    @PutMapping("/updateFare/{id}")
    public  ResponseEntity<Map<String, String>> updateFare(@PathVariable Long id, @RequestBody FareDto dto){
        Map<String, String> response = new HashMap<>();
        try{
            fareService.updateFare(dto, id);
            logger.info("Fare {} updated", dto.getName());
            response.put(MESSAGE, "Fare Updated Successfully " + dto );
            return ResponseEntity.ok(response);
        }catch (EntityNotFoundException e){
            logger.error("Fare with id not found");
            response.put(ERROR, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
