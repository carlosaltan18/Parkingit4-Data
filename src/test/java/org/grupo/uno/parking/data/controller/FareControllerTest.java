package org.grupo.uno.parking.data.controller;



import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import java.util.*;

import jakarta.persistence.EntityNotFoundException;
import org.grupo.uno.parking.data.dto.FareDto;
import org.grupo.uno.parking.data.exception.AllDataRequiredException;
import org.grupo.uno.parking.data.model.Fare;
import org.grupo.uno.parking.data.service.ServiceFare;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import static org.junit.jupiter.api.Assertions.*;

class FareControllerTest {

    @InjectMocks
    private FareController fareController;

    @Mock
    private ServiceFare fareService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddFare_Success() {
        FareDto fareDto = new FareDto();
        fareDto.setName("Test Fare");
        fareDto.setPrice(100.0);
        fareDto.setStartTime("10:00");
        fareDto.setEndTime("22:00");

        ResponseEntity<Map<String, String>> response = fareController.addFare(fareDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Fare saved: Test Fare", response.getBody().get("message"));
    }

    @Test
    void testAddFare_AllDataRequiredException() {
        FareDto fareDto = new FareDto(); // Missing required data

        when(fareService.addFare(any())).thenThrow(new AllDataRequiredException("All data is required"));

        ResponseEntity<Map<String, String>> response = fareController.addFare(fareDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("All data is required", response.getBody().get("Error"));
    }

    @Test
    void testGetAllFares_Success() {
        Page<Fare> farePage = new PageImpl<>(Collections.singletonList(new Fare()), PageRequest.of(0, 10), 1);

        when(fareService.getAllFares(0, 10, null)).thenReturn(farePage);

        ResponseEntity<Map<String, Object>> response = fareController.getAllFares(0, 10, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Fares retrieved successfully", response.getBody().get("message"));
        assertEquals(1, ((List<?>) response.getBody().get("fares")).size());
    }

    @Test
    void testGetAllFares_Exception() {
        when(fareService.getAllFares(anyInt(), anyInt(), any())).thenThrow(new RuntimeException("Error retrieving fares"));

        ResponseEntity<Map<String, Object>> response = fareController.getAllFares(0, 10, null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred while retrieving fares: Error retrieving fares", response.getBody().get("Error"));
    }

    @Test
    void testGetFareById_Success() {
        Fare fare = new Fare();
        fare.setFareId(1L);
        fare.setName("Test Fare");

        when(fareService.findFareById(anyLong())).thenReturn(Optional.of(fare));

        ResponseEntity<Map<String, Object>> response = fareController.getFareById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(fare, response.getBody().get("message"));
    }

    @Test
    void testGetFareById_EntityNotFoundException() {
        when(fareService.findFareById(anyLong())).thenThrow(new EntityNotFoundException("Fare not found for ID: 1"));

        ResponseEntity<Map<String, Object>> response = fareController.getFareById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Fare not found for ID: 1", response.getBody().get("Error"));
    }

    @Test
    void testDeleteFare_Success() {
        doNothing().when(fareService).delete(anyLong());

        ResponseEntity<Map<String, String>> response = fareController.deleteFare(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "El estado no es 200 OK");
        assertEquals("Fare deleted successfully", response.getBody().get("Message"));
    }

    @Test
    void testDeleteFare_Exception() {
        doThrow(new DataAccessException("Error deleting fare") {}).when(fareService).delete(anyLong());

        ResponseEntity<Map<String, String>> response = fareController.deleteFare(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred while deleting fare: Error deleting fare", response.getBody().get("Error"));
    }

    @Test
    void testUpdateFare_Success() {
        FareDto fareDto = new FareDto();
        fareDto.setName("Updated Fare");

        doNothing().when(fareService).updateFare(any(), anyLong());

        ResponseEntity<Map<String, String>> response = fareController.updateFare(1L, fareDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Fare updated successfully: Updated Fare", response.getBody().get("message"));
    }

    @Test
    void testUpdateFare_EntityNotFoundException() {
        FareDto fareDto = new FareDto();

        doThrow(new EntityNotFoundException("Fare not found: 1")).when(fareService).updateFare(any(), anyLong());

        ResponseEntity<Map<String, String>> response = fareController.updateFare(1L, fareDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Fare not found: Fare not found: 1", response.getBody().get("Error"));
    }
    @Test
    void testGetFareByName_Success() {
        Fare fare = new Fare();
        fare.setFareId(1L);
        fare.setName("Test Fare");

        when(fareService.findByName("Test Fare")).thenReturn(Optional.of(fare));

        ResponseEntity<Map<String, Object>> response = fareController.getFareByName("Test Fare");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(fare, response.getBody().get("message"));
    }

    @Test
    void testGetFareByName_EntityNotFoundException() {
        when(fareService.findByName("Nonexistent Fare")).thenThrow(new EntityNotFoundException("Fare not found for name: Nonexistent Fare"));

        ResponseEntity<Map<String, Object>> response = fareController.getFareByName("Nonexistent Fare");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Fare not found for name: Nonexistent Fare", response.getBody().get("Error"));
    }

    @Test
    void testUpdateFare_InvalidDataException() {
        FareDto fareDto = new FareDto();
        fareDto.setName(""); // Invalid name

        doThrow(new AllDataRequiredException("All data is required")).when(fareService).updateFare(any(), anyLong());

        ResponseEntity<Map<String, String>> response = fareController.updateFare(1L, fareDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("All data is required", response.getBody().get("Error"));
    }

    @Test
    void testDeleteFare_EntityNotFoundException() {
        doThrow(new EntityNotFoundException("Fare not found for ID: 1")).when(fareService).delete(anyLong());

        ResponseEntity<Map<String, String>> response = fareController.deleteFare(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("An error occurred while deleting fare: Fare not found for ID: 1", response.getBody().get("Error"));
    }

    @Test
    void testAddFare_Exception() {
        FareDto fareDto = new FareDto();
        fareDto.setName("Test Fare");
        fareDto.setPrice(100.0);
        fareDto.setStartTime("10:00");
        fareDto.setEndTime("22:00");

        when(fareService.addFare(any())).thenThrow(new RuntimeException("Generic error"));

        ResponseEntity<Map<String, String>> response = fareController.addFare(fareDto);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred while adding fare: Generic error", response.getBody().get("Error"));
    }

    @Test
    void testGetAllFares_EmptyResult() {
        Page<Fare> farePage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(fareService.getAllFares(0, 10, null)).thenReturn(farePage);

        ResponseEntity<Map<String, Object>> response = fareController.getAllFares(0, 10, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Fares retrieved successfully", response.getBody().get("message"));
        assertTrue(((List<?>) response.getBody().get("fares")).isEmpty());
    }
}