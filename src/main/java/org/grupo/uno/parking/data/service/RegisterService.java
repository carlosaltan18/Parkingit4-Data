package org.grupo.uno.parking.data.service;

import jakarta.persistence.EntityNotFoundException;
import org.grupo.uno.parking.data.dto.RegisterDTO;
import org.grupo.uno.parking.data.model.Fare;
import org.grupo.uno.parking.data.model.Parking;
import org.grupo.uno.parking.data.model.Register;
import org.grupo.uno.parking.data.repository.FareRepository;
import org.grupo.uno.parking.data.repository.ParkingRepository;
import org.grupo.uno.parking.data.repository.RegisterRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class RegisterService implements IRegisterService {

    private static final Logger logger = LoggerFactory.getLogger(RegisterService.class);

    private final RegisterRepository registerRepository;
    private  final ParkingRepository parkingRepository;
    private final  FareRepository fareRepository;
    private  final AudithService audithService;

    private  static final String REGISTER = "Register";
    private  static final  String SUCCESS = "Succes";

    public RegisterService(RegisterRepository registerRepository,
                       ParkingRepository parkingRepository,
                       FareRepository fareRepository,
                       AudithService audithService) {
        this.registerRepository = registerRepository;
        this.parkingRepository = parkingRepository;
        this.fareRepository = fareRepository;
        this.audithService = audithService;
    }



    @Override
    public RegisterDTO registroDeEntrada(String plate, long parkingId) {

        Parking parking = parkingRepository.findById(parkingId)
                .orElseThrow(() -> new EntityNotFoundException("Parking not found"));

        Register register = new Register();
        register.setPlate(plate);
        register.setParking(parking);
        register.setStartDate(LocalDateTime.now());
        register.setStatus(true);

        register = registerRepository.save(register);

        return convertToDTO(register);
    }


    @Override
    public RegisterDTO registroDeSalida(String plate) {
        LocalDateTime endDate = LocalDateTime.now();


        Register register = registerRepository.findActiveRegisterByPlate(plate)
                .orElseThrow(() -> new IllegalArgumentException("Registro activo con placa " + plate + " no encontrado"));

        register.setEndDate(endDate);


        long minutesParked = java.time.Duration.between(register.getStartDate(), endDate).toMinutes();


        List<Fare> fares = fareRepository.findAll();

        Fare selectedFare = null;
        BigDecimal total = BigDecimal.ZERO;


        for (Fare fare : fares) {

            LocalTime startTime = LocalTime.parse(fare.getStartTime());
            LocalTime endTime = LocalTime.parse(fare.getEndTime());
            LocalTime entryTime = register.getStartDate().toLocalTime();


            if (!entryTime.isBefore(startTime) && !entryTime.isAfter(endTime)) {

                total = BigDecimal.valueOf((minutesParked / 60.0) * fare.getPrice());
                selectedFare = fare;
                break;
            }
        }

        if (selectedFare == null) {
            selectedFare = fareRepository.findById(1L)
                    .orElseThrow(() -> new IllegalArgumentException("Tarifa por defecto no encontrada"));
            total = BigDecimal.valueOf((minutesParked / 60.0) * selectedFare.getPrice());
        }

        register.setFare(selectedFare);
        register.setTotal(total);
        register.setStatus(false);

        Register updatedRegister = registerRepository.save(register);

        audithService.createAudit(
                REGISTER,
                "Registro de salida actualizado",
                "UPDATE",
                convertEntityToMap(updatedRegister),
                null,
                SUCCESS
        );

        return convertToDTO(updatedRegister);
    }

    @Override
    public Page<RegisterDTO> getAllRegisters(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Register> registers = registerRepository.findAll(pageable);
        Page<RegisterDTO> registerDTOs = registers.map(this::convertToDTO);

        audithService.createAudit(
                REGISTER,
                "Retrieved all registers",
                "READ",
                null,
                convertPageToMap(registerDTOs),
                SUCCESS
        );

        logger.info("Total registers retrieved: {}", registers.getTotalElements());
        return registerDTOs;
    }

    @Override
    public Optional<RegisterDTO> findById(Long registerId) {
        logger.info("Finding register by ID: {}", registerId);
        return registerRepository.findById(registerId).map(this::convertToDTO);
    }

    @Override
    public RegisterDTO saveRegister(@Valid RegisterDTO registerDTO) {
        validateRegister(registerDTO);
        logger.info("Saving new register with details: {}", registerDTO);
        Register register = convertToEntity(registerDTO);
        Register savedRegister = registerRepository.save(register);

        RegisterDTO savedDTO = convertToDTO(savedRegister);

        audithService.createAudit(
                REGISTER,
                "Registro creado",
                "CREATE",
                convertDTOToMap(registerDTO),
                convertDTOToMap(savedDTO),
                SUCCESS
        );

        logger.info("Register saved successfully with ID: {}", savedRegister.getRegisterId());
        return savedDTO;
    }

    @Override
    public RegisterDTO updateRegister(@Valid RegisterDTO registerDTO, Long registerId) {
        validateRegister(registerDTO);
        logger.info("Updating register with ID: {}", registerId);
        Optional<Register> optionalRegister = registerRepository.findById(registerId);

        if (optionalRegister.isPresent()) {
            Register register = optionalRegister.get();
            updateRegisterFields(register, registerDTO);
            Register updatedRegister = registerRepository.save(register);

            RegisterDTO updatedDTO = convertToDTO(updatedRegister);

            audithService.createAudit(
                    REGISTER,
                    "Registro actualizado",
                    "UPDATE",
                    convertDTOToMap(registerDTO),
                    convertDTOToMap(updatedDTO),
                    SUCCESS
            );
            logger.info("Register updated successfully with ID: {}", registerId);
            return updatedDTO;
        } else {
            logger.error("Register with ID {} not found.", registerId);
            throw new IllegalArgumentException("Register with ID " + registerId + " not found.");
        }
    }

    @Override
    public void deleteRegister(Long registerId) {
        logger.info("Deleting register with ID: {}", registerId);
        try {
            Optional<Register> optionalRegister = registerRepository.findById(registerId);
            if (optionalRegister.isPresent()) {
                Register register = optionalRegister.get();
                registerRepository.deleteById(registerId);

                audithService.createAudit(
                        REGISTER,
                        "Registro eliminado",
                        "DELETE",
                        convertEntityToMap(register),
                        null,
                        SUCCESS
                );
                logger.info("Register deleted successfully with ID: {}", registerId);
            } else {
                logger.error("Register with ID {} not found.", registerId);
                throw new IllegalArgumentException("Register with ID " + registerId + " not found.");
            }
        } catch (DataAccessException e) {
            logger.error("Error deleting register with ID {}: {}", registerId, e.getMessage());
        }
    }

    @Override
    public List<RegisterDTO> generateReportByParkingId(Long parkingId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Generating report for parking ID: {}", parkingId);

        List<Register> registers = registerRepository.findActiveRegistersByParkingIdAndDateRange(parkingId, startDate, endDate);
        if (registers.isEmpty()) {
            logger.warn("No registers found for parking ID: {}", parkingId);
            throw new IllegalArgumentException("No registers found for parking ID " + parkingId);
        }

        List<RegisterDTO> registerDTOs = registers.stream()
                .map(this::convertToDTO)
                .toList();

        audithService.createAudit(
                REGISTER,
                "Generated report for parking ID: " + parkingId,
                "REPORT",
                null,
                convertListToMap(registerDTOs),
                SUCCESS
        );

        logger.info("Report generated successfully for parking ID: {}", parkingId);
        return registerDTOs;
    }

    @Override
    public List<RegisterDTO> generateReportByParkingIdPDF(Long parkingId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Generating PDF report for parking ID: {}", parkingId);

        List<Register> registers = registerRepository.findActiveRegistersByParkingIdAndDateRange(parkingId, startDate, endDate);
        if (registers.isEmpty()) {
            logger.warn("No registers found for parking ID: {}", parkingId);
            throw new IllegalArgumentException("No registers found for parking ID " + parkingId);
        }

        List<RegisterDTO> registerDTOs = registers.stream()
                .map(this::convertToDTO)
                .toList();

        audithService.createAudit(
                REGISTER,
                "Generated PDF report for parking ID: " + parkingId,
                "REPORT",
                null,
                convertListToMap(registerDTOs),
                SUCCESS
        );

        logger.info("PDF report generated successfully for parking ID: {}", parkingId);
        return registerDTOs;
    }

    private void validateRegister(RegisterDTO registerDTO) {
        if (registerDTO.getPlate() == null || registerDTO.getPlate().isEmpty()) {
            throw new IllegalArgumentException("El campo 'plate' no puede estar vacío.");
        }
        if (registerDTO.getStartDate() == null) {
            throw new IllegalArgumentException("La fecha de inicio no puede estar vacía.");
        }
        if (registerDTO.getEndDate() == null) {
            throw new IllegalArgumentException("La fecha de finalización no puede estar vacía.");
        }
        if (!parkingRepository.existsById(registerDTO.getParkingId())) {
            throw new IllegalArgumentException("Parking con ID " + registerDTO.getParkingId() + " no encontrado.");
        }
        if (!fareRepository.existsById(registerDTO.getFareId())) {
            throw new IllegalArgumentException("Fare con ID " + registerDTO.getFareId() + " no encontrado.");
        }
    }

    private Register convertToEntity(RegisterDTO registerDTO) {
        logger.debug("Converting RegisterDTO to Register entity: {}", registerDTO);
        Register register = new Register();
        register.setPlate(registerDTO.getPlate());
        register.setStatus(registerDTO.isStatus());
        register.setStartDate(registerDTO.getStartDate());
        register.setEndDate(registerDTO.getEndDate());
        register.setTotal(registerDTO.getTotal());

        parkingRepository.findById(registerDTO.getParkingId()).ifPresent(register::setParking);
        fareRepository.findById(registerDTO.getFareId()).ifPresent(register::setFare);

        return register;
    }

    private RegisterDTO convertToDTO(Register register) {
        if (register == null) {
            logger.error("El registro no puede ser nulo.");
            throw new IllegalArgumentException("El registro no puede ser nulo.");
        }

        RegisterDTO dto = new RegisterDTO();

        dto.setRegisterId(register.getRegisterId());
        dto.setPlate(register.getPlate());
        dto.setStatus(register.isStatus());
        dto.setStartDate(register.getStartDate());
        dto.setEndDate(register.getEndDate());

        dto.setParkingId(register.getParking() != null ? register.getParking().getParkingId() : 0);

        dto.setFareId(register.getFare() != null ? register.getFare().getFareId() : 0);

        dto.setTotal(register.getTotal());

        return dto;
    }





    private void updateRegisterFields(Register register, RegisterDTO registerDTO) {
        logger.debug("Updating Register entity with new values: {}", registerDTO);
        register.setPlate(registerDTO.getPlate());
        register.setStatus(registerDTO.isStatus());
        register.setStartDate(registerDTO.getStartDate());
        register.setEndDate(registerDTO.getEndDate());
        register.setTotal(registerDTO.getTotal());

        parkingRepository.findById(registerDTO.getParkingId()).ifPresent(register::setParking);
        fareRepository.findById(registerDTO.getFareId()).ifPresent(register::setFare);
    }

    private Map<String, Object> convertDTOToMap(RegisterDTO registerDTO) {
        Map<String, Object> map = new HashMap<>();
        map.put("registerId", registerDTO.getRegisterId());
        map.put("plate", registerDTO.getPlate());
        map.put("status", registerDTO.isStatus());
        map.put("startDate", registerDTO.getStartDate());
        map.put("endDate", registerDTO.getEndDate());
        map.put("parkingId", registerDTO.getParkingId());
        map.put("fareId", registerDTO.getFareId());
        map.put("total", registerDTO.getTotal());
        return map;
    }

    private Map<String, Object> convertEntityToMap(Register register) {
        Map<String, Object> map = new HashMap<>();
        map.put("registerId", register.getRegisterId());
        map.put("plate", register.getPlate());
        map.put("status", register.isStatus());
        map.put("startDate", register.getStartDate());
        map.put("endDate", register.getEndDate());
        map.put("parkingId", register.getParking() != null ? register.getParking().getParkingId() : null);
        map.put("fareId", register.getFare() != null ? register.getFare().getFareId() : null);
        map.put("total", register.getTotal());
        return map;
    }

    private Map<String, Object> convertPageToMap(Page<RegisterDTO> registerDTOs) {
        Map<String, Object> map = new HashMap<>();
        map.put("totalElements", registerDTOs.getTotalElements());
        map.put("totalPages", registerDTOs.getTotalPages());
        map.put("currentPage", registerDTOs.getNumber());
        map.put("registers", registerDTOs.getContent());
        return map;
    }

    private Map<String, Object> convertListToMap(List<RegisterDTO> registerDTOs) {
        Map<String, Object> map = new HashMap<>();
        map.put("registers", registerDTOs);
        return map;
    }
}