package org.grupo.uno.parking.data.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.grupo.uno.parking.data.dto.RegisterDTO;
import org.grupo.uno.parking.data.model.Fare;
import org.grupo.uno.parking.data.model.Parking;
import org.grupo.uno.parking.data.model.Register;
import org.grupo.uno.parking.data.repository.FareRepository;
import org.grupo.uno.parking.data.repository.ParkingRepository;
import org.grupo.uno.parking.data.repository.RegisterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

class RegisterServiceTest {

    @Mock
    private RegisterRepository registerRepository;

    @Mock
    private ParkingRepository parkingRepository;

    @Mock
    private FareRepository fareRepository;

    @Mock
    private AudithService audithService;

    @InjectMocks
    private RegisterService registerService;

    private RegisterDTO registerDTO;
    private Register register;
    private Parking parking;
    private Fare fare;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        registerDTO = new RegisterDTO();
        registerDTO.setRegisterId(1);
        registerDTO.setPlate("ABC123");
        registerDTO.setParkingId(1);
        registerDTO.setFareId(1);
        registerDTO.setStartDate(LocalDateTime.now());
        registerDTO.setEndDate(LocalDateTime.now().plusHours(1));
        registerDTO.setTotal(BigDecimal.valueOf(10.0));
        registerDTO.setStatus(true);

        parking = new Parking();
        parking.setParkingId(1);

        fare = new Fare();
        fare.setFareId(1l);
        fare.setPrice(Double.valueOf(10.0));
        fare.setStartTime("00:00");
        fare.setEndTime("23:59");

        register = new Register();
        register.setRegisterId(1);
        register.setPlate("ABC123");
        register.setParking(parking);
        register.setStartDate(LocalDateTime.now());
        register.setStatus(true);
    }

    @Test
    void registroDeEntrada_createsRegisterSuccessfully() {
        when(parkingRepository.findById(1L)).thenReturn(Optional.of(parking));
        when(registerRepository.save(any(Register.class))).thenReturn(register);

        RegisterDTO result = registerService.registroDeEntrada("ABC123", 1L);

        assertNotNull(result);
        assertEquals("ABC123", result.getPlate());
        verify(registerRepository).save(any(Register.class));
    }

    @Test
    void registroDeSalida_updatesRegisterSuccessfully() {
        when(registerRepository.findActiveRegisterByPlate("ABC123")).thenReturn(Optional.of(register));
        when(fareRepository.findAll()).thenReturn(Arrays.asList(fare));
        when(registerRepository.save(any(Register.class))).thenReturn(register);

        RegisterDTO result = registerService.registroDeSalida("ABC123");

        assertNotNull(result);
        assertEquals(register.getTotal(), result.getTotal());
        verify(registerRepository).save(any(Register.class));
    }

    @Test
    void getAllRegisters_returnsAllRegisters() {
        Page<Register> page = new PageImpl<>(Arrays.asList(register));
        when(registerRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<RegisterDTO> result = registerService.getAllRegisters(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(registerRepository).findAll(any(PageRequest.class));
    }

    @Test
    void findById_registerFound() {
        when(registerRepository.findById(1L)).thenReturn(Optional.of(register));

        Optional<RegisterDTO> result = registerService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("ABC123", result.get().getPlate());
    }

    @Test
    void saveRegister_createsRegisterSuccessfully() {
        when(parkingRepository.existsById(anyLong())).thenReturn(true);
        when(fareRepository.existsById(anyLong())).thenReturn(true);
        when(registerRepository.save(any(Register.class))).thenReturn(register);

        RegisterDTO result = registerService.saveRegister(registerDTO);

        assertNotNull(result);
        assertEquals("ABC123", result.getPlate());
        verify(registerRepository).save(any(Register.class));
    }

    @Test
    void updateRegister_updatesRegisterSuccessfully() {

        when(registerRepository.findById(1L)).thenReturn(Optional.of(register));
        when(registerRepository.save(any(Register.class))).thenReturn(register);
        when(parkingRepository.existsById(registerDTO.getParkingId())).thenReturn(true);
        when(fareRepository.existsById(registerDTO.getFareId())).thenReturn(true);

        RegisterDTO result = registerService.updateRegister(registerDTO, 1L);

        assertNotNull(result);
        assertEquals("ABC123", result.getPlate());
        verify(registerRepository).save(any(Register.class));
    }

    @Test
    void deleteRegister_deletesRegisterSuccessfully() {
        when(registerRepository.findById(1L)).thenReturn(Optional.of(register));

        registerService.deleteRegister(1L);

        verify(registerRepository).deleteById(1L);
    }

    @Test
    void generateReportByParkingId_returnsReportSuccessfully() {
        when(registerRepository.findActiveRegistersByParkingIdAndDateRange(anyLong(), any(), any()))
                .thenReturn(Arrays.asList(register));

        var result = registerService.generateReportByParkingId(1L, LocalDateTime.now().minusDays(1), LocalDateTime.now());

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(audithService).createAudit(anyString(), anyString(), anyString(), any(), any(), anyString());
    }

    @Test
    void generateReportByParkingIdPDF_returnsPDFReportSuccessfully() {
        when(registerRepository.findActiveRegistersByParkingIdAndDateRange(anyLong(), any(), any()))
                .thenReturn(Arrays.asList(register));

        var result = registerService.generateReportByParkingIdPDF(1L, LocalDateTime.now().minusDays(1), LocalDateTime.now());

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(audithService).createAudit(anyString(), anyString(), anyString(), any(), any(), anyString());
    }
}
