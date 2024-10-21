package org.grupo.uno.parking.data.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.data.domain.PageRequest.of;

import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;
import org.grupo.uno.parking.data.dto.FareDto;
import org.grupo.uno.parking.data.exception.AllDataRequiredException;
import org.grupo.uno.parking.data.exceptions.FareExist;
import org.grupo.uno.parking.data.model.Fare;
import org.grupo.uno.parking.data.repository.FareRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

class ServiceFareTest {

    @Mock
    private FareRepository fareRepository;

    @Mock
    private AudithService audithService;

    @InjectMocks
    private ServiceFare serviceFare;

    private Fare fare;
    private FareDto fareDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        fare = new Fare();
        fare.setFareId(1L);
        fare.setName("Test Fare");
        fare.setStartTime("08:00");
        fare.setEndTime("20:00");
        fare.setPrice(10.0);
        fare.setStatus(true);

        fareDto = new FareDto();
        fareDto.setName("Test Fare");
        fareDto.setStartTime("08:00");
        fareDto.setEndTime("20:00");
        fareDto.setPrice(10.0);
        fareDto.setStatus(true);
    }

    @Test
    void getAllFares_returnsAllFares() {
        Pageable pageable = of(0, 10);
        Page<Fare> farePage = mock(Page.class);
        when(fareRepository.findAll(pageable)).thenReturn(farePage);
        Page<Fare> result = serviceFare.getAllFares(0, 10, null);
        assertNotNull(result);
        verify(fareRepository).findAll(pageable);
    }

    @Test
    void findFareById_existingId_returnsFare() {
        when(fareRepository.findById(1L)).thenReturn(Optional.of(fare));
        Optional<Fare> result = serviceFare.findFareById(1L);
        assertTrue(result.isPresent());
        assertEquals(fare, result.get());
    }

    @Test
    void findFareById_nonExistingId_throwsException() {
        when(fareRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> {
            serviceFare.findFareById(1L);
        });
    }

    @Test
    void delete_existingFare_deletesFare() {
        when(fareRepository.existsById(1L)).thenReturn(true);
        when(fareRepository.findById(1L)).thenReturn(Optional.of(fare));
        serviceFare.delete(1L);

        verify(fareRepository).deleteById(1L);
    }

    @Test
    void delete_nonExistingFare_throwsException() {
        when(fareRepository.existsById(1L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> {
            serviceFare.delete(1L);
        });
    }

    @Test
    void updateFare_existingFare_updatesFare() {
        when(fareRepository.existsById(1L)).thenReturn(true);
        when(fareRepository.findById(1L)).thenReturn(Optional.of(fare));
        serviceFare.updateFare(fareDto, 1L);
        verify(fareRepository).save(fare);
        assertEquals("Test Fare", fare.getName());
    }

    @Test
    void updateFare_nonExistingFare_throwsException() {
        when(fareRepository.existsById(1L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> {
            serviceFare.updateFare(fareDto, 1L);
        });
    }

    @Test
    void addFare_validFare_createsFare() {
        when(fareRepository.findByName(fareDto.getName())).thenReturn(Optional.empty());
        when(fareRepository.save(any(Fare.class))).thenReturn(fare);
        Fare result = serviceFare.addFare(fareDto);
        assertNotNull(result);
        assertEquals(fare.getName(), result.getName());
        verify(fareRepository).save(any(Fare.class));
    }

    @Test
    void addFare_existingFare_throwsException() {
        when(fareRepository.findByName(fareDto.getName())).thenReturn(Optional.of(fare));
        assertThrows(FareExist.class, () -> {
            serviceFare.addFare(fareDto);
        });
    }

    @Test
    void addFare_missingData_throwsException() {
        fareDto.setName(null);
        assertThrows(AllDataRequiredException.class, () -> {
            serviceFare.addFare(fareDto);
        });
    }
}
