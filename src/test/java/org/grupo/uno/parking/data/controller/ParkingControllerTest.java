package org.grupo.uno.parking.data.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.persistence.EntityNotFoundException;
import org.grupo.uno.parking.data.dto.ParkingDTO;
import org.grupo.uno.parking.data.model.Parking;
import org.grupo.uno.parking.data.service.ParkingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class ParkingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private ParkingService parkingService;

    @InjectMocks
    private ParkingController parkingController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(parkingController).build();
    }

    @Test
     void testGetActiveParkings() throws Exception {
        List<Map<String, Object>> activeParkings = new ArrayList<>();
        Map<String, Object> parking = new HashMap<>();
        parking.put("id", 1);
        parking.put("name", "Parking A");
        parking.put("status", true);
        activeParkings.add(parking);

        when(parkingService.getActiveParkings()).thenReturn(activeParkings);

        mockMvc.perform(get("/parkings/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Parking A"));
    }

    @Test
     void testGetAllParkings() throws Exception {
        List<ParkingDTO> parkingsList = Arrays.asList(
                new ParkingDTO(1l, "Parking A", "Address A", "12345678", 10, true),
                new ParkingDTO(2L, "Parking B", "Address B", "85693214", 5, false)
        );
        Page<ParkingDTO> parkingsPage = new PageImpl<>(parkingsList, PageRequest.of(0, 10), 2);

        when(parkingService.getAllParkings(0, 10)).thenReturn(parkingsPage);

        mockMvc.perform(get("/parkings?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Parking A"))
                .andExpect(jsonPath("$.content[1].name").value("Parking B"));
    }

    @Test
     void testPatchParking() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Updated Parking");

        doNothing().when(parkingService).patchParking(anyLong(), any());

        mockMvc.perform(patch("/parkings/parkingPatch/1")
                        .contentType("application/json")
                        .content("{\"name\": \"Updated Parking\"}"))
                .andExpect(status().isOk());

        verify(parkingService, times(1)).patchParking(1L, updates);
    }

    @Test
     void testGetParkingById_NotFound() throws Exception {
        when(parkingService.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/parkings/1"))
                .andExpect(status().isNotFound());
    }

    @Test
     void testCreateParking() throws Exception {
        ParkingDTO parkingDTO = new ParkingDTO(1l, "New Parking", "New Address", "9876543210", 20, true);
        Parking savedParking = new Parking();

        when(parkingService.saveParking(any())).thenReturn(savedParking);

        mockMvc.perform(post("/parkings/saveParking")
                        .contentType("application/json")
                        .content("{\"name\": \"New Parking\", \"address\": \"New Address\", \"phone\": \"9876543210\", \"spaces\": 20, \"status\": true}"))
                .andExpect(status().isCreated());
    }

    @Test
     void testDeleteParking() throws Exception {
        doNothing().when(parkingService).deleteParking(anyLong());

        mockMvc.perform(delete("/parkings/parkingDelete/1"))
                .andExpect(status().isNoContent());

        verify(parkingService, times(1)).deleteParking(1L);
    }

    @Test
     void testUpdateParking() throws Exception {
        ParkingDTO parkingDTO = new ParkingDTO(1L, "Updated Parking", "Updated Address", "123456789", 15, true);
        Parking updatedParking = new Parking();
        updatedParking.setParkingId(1L);
        updatedParking.setName("Updated Parking");
        doNothing().when(parkingService).updateParking(any(ParkingDTO.class), eq(1L));

        mockMvc.perform(put("/parkings/parkingUpdate/1")
                        .contentType("application/json")
                        .content("{\"name\": \"Updated Parking\", \"address\": \"Updated Address\", \"phone\": \"123456789\", \"spaces\": 15, \"status\": true}"))
                .andExpect(status().isOk());
    }

    @Test
     void testSearchParkingByName() throws Exception {
        List<Map<String, Object>> parkingsList = new ArrayList<>();
        Map<String, Object> parking = new HashMap<>();
        parking.put("id", 1);
        parking.put("name", "Parking A");
        parking.put("status", true);
        parkingsList.add(parking);
        Page<Map<String, Object>> parkingPage = new PageImpl<>(parkingsList, PageRequest.of(0, 10), 1);
        when(parkingService.searchParkingByName("A", 0, 10)).thenReturn(parkingPage);
        mockMvc.perform(get("/parkings/search?name=A&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Parking A"));
    }

    @Test
     void testGetParkingNamesAndStatus() throws Exception {
        List<Map<String, Object>> parkingsList = new ArrayList<>();
        Map<String, Object> parking = new HashMap<>();
        parking.put("id", 1);
        parking.put("name", "Parking A");
        parking.put("status", true);
        parkingsList.add(parking);
        Page<Map<String, Object>> parkingPage = new PageImpl<>(parkingsList, PageRequest.of(0, 10), 1);
        when(parkingService.getParkingNamesAndStatus(0, 10)).thenReturn(parkingPage);
        mockMvc.perform(get("/parkings/namesAndStatus?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Parking A"));
    }

    @Test
    void testPatchParking_InvalidField() throws Exception {
        doThrow(new IllegalArgumentException("Field not recognized: invalidField"))
                .when(parkingService).patchParking(eq(1L), anyMap());

        mockMvc.perform(patch("/parkings/parkingPatch/1")
                        .contentType("application/json")
                        .content("{\"invalidField\": \"Invalid Value\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Field not recognized: invalidField")));
    }

    @Test
    void testDeleteParking_NotFound() throws Exception {
        doThrow(new EntityNotFoundException("Parking with id: 1 does not exist"))
                .when(parkingService).deleteParking(1L);
        mockMvc.perform(delete("/parkings/parkingDelete/1"))
                .andExpect(status().isNotFound());
    }
}