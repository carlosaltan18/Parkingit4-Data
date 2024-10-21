package org.grupo.uno.parking.data.service;

import jakarta.persistence.EntityNotFoundException;
import org.grupo.uno.parking.data.dto.ParkingDTO;
import org.grupo.uno.parking.data.model.Parking;
import org.grupo.uno.parking.data.repository.ParkingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;




import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    @InjectMocks
    private ParkingService parkingService;

    @Mock
    private ParkingRepository parkingRepository;

    @Mock
    private AudithService audithService;

    private Parking parking;
    private ParkingDTO parkingDTO;

    @BeforeEach
    void setUp() {
        parking = new Parking();
        parking.setParkingId(1L);
        parking.setName("Test Parking");
        parking.setAddress("123 Test St");
        parking.setPhone("1234567890");
        parking.setSpaces(10);
        parking.setStatus(true);

        parkingDTO = new ParkingDTO();
        parkingDTO.setName("Updated Parking");
        parkingDTO.setAddress("456 Updated St");
        parkingDTO.setPhone("0987654321");
        parkingDTO.setSpaces(15);
        parkingDTO.setStatus(false);
    }

    @Test
    void patchParking_existingParking_updatesParking() {
        when(parkingRepository.findById(anyLong())).thenReturn(Optional.of(parking));

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "New Parking Name");
        updates.put("status", false);

        parkingService.patchParking(1L, updates);
        assertEquals("New Parking Name", parking.getName());
        assertFalse(parking.getStatus());
        verify(parkingRepository, times(1)).save(parking);
    }


    @Test
    void getAllParkings_returnsPagedParkings() {
        List<Parking> parkings = Arrays.asList(parking);
        Page<Parking> parkingPage = new PageImpl<>(parkings);
        when(parkingRepository.findAll(any(Pageable.class))).thenReturn(parkingPage);

        Page<ParkingDTO> result = parkingService.getAllParkings(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Parking", result.getContent().get(0).getName());
    }

    @Test
    void saveParking_validParking_savesParking() {
        when(parkingRepository.save(any(Parking.class))).thenReturn(parking);
        Parking savedParking = parkingService.saveParking(parking);
        assertNotNull(savedParking);
        assertEquals("Test Parking", savedParking.getName());
        verify(parkingRepository, times(1)).save(parking);
    }

    @Test
    void saveParking_invalidParking_throwsIllegalArgumentException() {
        parking.setName(null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            parkingService.saveParking(parking);
        });

        assertEquals("Parking name is required", exception.getMessage());
    }

    @Test
    void updateParking_existingParking_updatesParking() {
        when(parkingRepository.findById(anyLong())).thenReturn(Optional.of(parking));
        parkingService.updateParking(parkingDTO, parking.getParkingId());
        assertEquals("Updated Parking", parking.getName());
        assertEquals("456 Updated St", parking.getAddress());
        assertEquals("0987654321", parking.getPhone());
        assertEquals(15, parking.getSpaces());
        assertFalse(parking.getStatus());
        verify(parkingRepository, times(1)).save(parking);
    }

    @Test
    void updateParking_parkingNotFound_throwsEntityNotFoundException() {
        when(parkingRepository.findById(anyLong())).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            parkingService.updateParking(parkingDTO, 1L);
        });

        assertEquals("Parking with id: 1 does not exist", exception.getMessage());
    }

    @Test
    void patchParking_parkingNotFound_throwsEntityNotFoundException() {
        when(parkingRepository.findById(anyLong())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            parkingService.patchParking(1L, new HashMap<>());
        });
        assertEquals("Parkingwith id: 1 does not exist", exception.getMessage());
    }

    @Test
    void deleteParking_existingParking_deletesParking() {
        when(parkingRepository.findById(anyLong())).thenReturn(Optional.of(parking));
        parkingService.deleteParking(1L);
        verify(parkingRepository, times(1)).delete(parking);
    }

    @Test
    void deleteParking_parkingNotFound_throwsNoSuchElementException() {
        when(parkingRepository.findById(anyLong())).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            parkingService.deleteParking(1L);
        });
        assertEquals("Parking with id: 1 does not exist", exception.getMessage());
    }
    @Test
    void saveParking_success() {

        when(parkingRepository.save(any(Parking.class))).thenReturn(parking);

        Parking savedParking = parkingService.saveParking(parking);
        assertNotNull(savedParking);
        assertEquals("Test Parking", savedParking.getName());
        verify(parkingRepository, times(1)).save(parking);
    }

    @Test
    void updateParking_parkingFound_success() {

        when(parkingRepository.findById(1L)).thenReturn(Optional.of(parking));
        parkingService.updateParking(parkingDTO, 1L);
        assertEquals("Updated Parking", parking.getName());
        verify(parkingRepository, times(1)).save(parking);
    }

    @Test
    void deleteParking_parkingFound_success() {
        when(parkingRepository.findById(1L)).thenReturn(Optional.of(parking));
        parkingService.deleteParking(1L);
        verify(parkingRepository, times(1)).delete(parking);
    }

    @Test
    void getAllParkings_success() {
        List<Parking> parkings = Arrays.asList(parking);
        Page<Parking> parkingPage = new PageImpl<>(parkings);
        when(parkingRepository.findAll(any(Pageable.class))).thenReturn(parkingPage);

        Page<ParkingDTO> result = parkingService.getAllParkings(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Parking", result.getContent().get(0).getName());
    }

    @Test
    void searchParkingByName_parkingFound_success() {
        List<Parking> parkings = Collections.singletonList(parking);
        Page<Parking> parkingPage = new PageImpl<>(parkings);
        when(parkingRepository.findByNameContainingIgnoreCase(eq("Test"), any(Pageable.class))).thenReturn(parkingPage);
        Page<Map<String, Object>> result = parkingService.searchParkingByName("Test", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Parking", result.getContent().get(0).get("name"));
    }


    @Test
    void getActiveParkings_success() {
        List<Parking> activeParkings = Collections.singletonList(parking);
        when(parkingRepository.findByStatus(true)).thenReturn(activeParkings);

        List<Map<String, Object>> result = parkingService.getActiveParkings();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Parking", result.get(0).get("name"));
    }

    @Test
    void patchParking_parkingFound_success() {
        when(parkingRepository.findById(1L)).thenReturn(Optional.of(parking));

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Patched Parking");


        parkingService.patchParking(1L, updates);


        assertEquals("Patched Parking", parking.getName());
        verify(parkingRepository, times(1)).save(parking);
    }



    @Test
    void deleteParking_parkingNotFound_throwsEntityNotFoundException() {
        when(parkingRepository.findById(anyLong())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            parkingService.deleteParking(1L);
        });

        assertEquals("Parking with id: 1 does not exist", exception.getMessage());
    }


}
