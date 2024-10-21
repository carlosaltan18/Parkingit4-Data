package org.grupo.uno.parking.data.service;

import jakarta.validation.Valid;
import org.grupo.uno.parking.data.dto.RegisterDTO;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IRegisterService {


    RegisterDTO registroDeEntrada(String plate, long parkingId);

    RegisterDTO registroDeSalida(String plate);

    Page<RegisterDTO> getAllRegisters(int page, int size);

    Optional<RegisterDTO> findById(Long registerId);

    RegisterDTO saveRegister(@Valid RegisterDTO registerDTO);

    RegisterDTO updateRegister(@Valid RegisterDTO registerDTO, Long registerId);

    void deleteRegister(Long registerId);

    List<RegisterDTO> generateReportByParkingId(Long parkingId, LocalDateTime startDate, LocalDateTime endDate);

    List<RegisterDTO> generateReportByParkingIdPDF(Long parkingId, LocalDateTime startDate, LocalDateTime endDate);
}