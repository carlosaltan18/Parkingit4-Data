package org.grupo.uno.parking.data.service;

import org.grupo.uno.parking.data.dto.RegisterDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IRegisterService {

    RegisterDTO RegistroDeEntrada(String plate, long parkingId);

    RegisterDTO RegistroDeSalida(String plate);

    // Cambiado a Page<RegisterDTO> para trabajar con DTOs
    Page<RegisterDTO> getAllRegisters(int page, int size);

    // Cambiado a Optional<RegisterDTO> para trabajar con DTOs
    Optional<RegisterDTO> findById(Long registerId);

    // El método ya estaba utilizando DTO correctamente
    RegisterDTO saveRegister(RegisterDTO registerDTO);

    // El método ya estaba utilizando DTO correctamente
    RegisterDTO updateRegister(RegisterDTO registerDTO, Long registerId);

    // No requiere cambios, pues trabaja con el ID
    void deleteRegister(Long registerId);

    Page<RegisterDTO> generateReportByParkingId(Long parkingId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    List<RegisterDTO> generateReportByParkingIdPDF(Long parkingId, LocalDateTime startDate, LocalDateTime endDate);

}
