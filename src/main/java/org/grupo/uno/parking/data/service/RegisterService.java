package org.grupo.uno.parking.data.service;

import org.grupo.uno.parking.data.dto.RegisterDTO;
import org.grupo.uno.parking.data.model.Register;
import org.grupo.uno.parking.data.repository.FareRepository;
import org.grupo.uno.parking.data.repository.ParkingRepository;
import org.grupo.uno.parking.data.repository.RegisterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RegisterService implements IRegisterService {

    private static final Logger logger = LoggerFactory.getLogger(RegisterService.class);

    @Autowired
    private RegisterRepository registerRepository;

    @Autowired
    private ParkingRepository parkingRepository;

    @Autowired
    private FareRepository fareRepository;

    @Autowired
    private AudithService audithService;

    @Override
    public Page<RegisterDTO> getAllRegisters(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Register> registers = registerRepository.findAll(pageable);

        Page<RegisterDTO> registerDTOs = registers.map(this::convertToDTO);

        // Auditar la recuperación de registros
        audithService.createAudit(
                "Register",
                "Retrieved all registers",
                "READ",
                null,
                convertPageToMap(registerDTOs),
                "SUCCESS"
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
    public RegisterDTO saveRegister(RegisterDTO registerDTO) {
        logger.info("Saving new register with details: {}", registerDTO);
        Register register = convertToEntity(registerDTO);
        Register savedRegister = registerRepository.save(register);

        RegisterDTO savedDTO = convertToDTO(savedRegister);

        // Guardar la auditoría de creación
        audithService.createAudit(
                "Register",
                "Registro creado",
                "CREATE",
                convertDTOToMap(registerDTO),
                convertDTOToMap(savedDTO),
                "SUCCESS"
        );

        logger.info("Register saved successfully with ID: {}", savedRegister.getRegisterId());
        return savedDTO;
    }

    @Override
    public RegisterDTO updateRegister(RegisterDTO registerDTO, Long registerId) {
        logger.info("Updating register with ID: {}", registerId);
        Optional<Register> optionalRegister = registerRepository.findById(registerId);

        if (optionalRegister.isPresent()) {
            Register register = optionalRegister.get();
            updateRegisterFields(register, registerDTO);
            Register updatedRegister = registerRepository.save(register);

            RegisterDTO updatedDTO = convertToDTO(updatedRegister);

            // Guardar la auditoría de actualización
            audithService.createAudit(
                    "Register",
                    "Registro actualizado",
                    "UPDATE",
                    convertDTOToMap(registerDTO),
                    convertDTOToMap(updatedDTO),
                    "SUCCESS"
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

                // Guardar la auditoría de eliminación
                audithService.createAudit(
                        "Register",
                        "Registro eliminado",
                        "DELETE",
                        convertEntityToMap(register),
                        null,
                        "SUCCESS"
                );
                logger.info("Register deleted successfully with ID: {}", registerId);
            } else {
                logger.error("Register with ID {} not found.", registerId);
                throw new IllegalArgumentException("Register with ID " + registerId + " not found.");
            }
        } catch (DataAccessException e) {
            logger.error("Error deleting register with ID {}: {}", registerId, e.getMessage());
            throw new DataAccessException("Error deleting register with ID " + registerId, e) {};
        }
    }

    @Override
    public List<RegisterDTO> generateReportByParkingId(Long parkingId) {
        logger.info("Generating report for parking ID: {}", parkingId);

        List<Register> registers = registerRepository.findByParking_ParkingId(parkingId);
        if (registers.isEmpty()) {
            logger.warn("No registers found for parking ID: {}", parkingId);
            throw new IllegalArgumentException("No registers found for parking ID " + parkingId);
        }

        // Convertir los registros a DTO
        List<RegisterDTO> registerDTOs = registers.stream()
                .map(this::convertToDTO)
                .toList();

        // Auditar la generación del reporte
        audithService.createAudit(
                "Register",
                "Generated report for parking ID: " + parkingId,
                "REPORT",
                null,
                convertListToMap(registerDTOs),
                "SUCCESS"
        );

        logger.info("Report generated successfully for parking ID: {}", parkingId);
        return registerDTOs;
    }

    private Register convertToEntity(RegisterDTO registerDTO) {
        logger.debug("Converting RegisterDTO to Register entity: {}", registerDTO);
        Register register = new Register();
        register.setName(registerDTO.getName());
        register.setCar(registerDTO.getCar());
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
            logger.error("Register entity is null.");
            throw new IllegalArgumentException("Register cannot be null");
        }

        logger.debug("Converting Register entity to RegisterDTO: ID: {}", register.getRegisterId());
        return new RegisterDTO(
                register.getRegisterId(),
                register.getName(),
                register.getCar(),
                register.getPlate(),
                register.isStatus(),
                register.getStartDate(),
                register.getEndDate(),
                register.getParking() != null ? register.getParking().getParkingId() : null,
                register.getFare() != null ? register.getFare().getFareId() : null,
                register.getTotal()
        );
    }

    private void updateRegisterFields(Register register, RegisterDTO registerDTO) {
        logger.debug("Updating register fields for ID: {}", register.getRegisterId());
        register.setName(registerDTO.getName());
        register.setCar(registerDTO.getCar());
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
        map.put("name", registerDTO.getName());
        map.put("car", registerDTO.getCar());
        map.put("plate", registerDTO.getPlate());
        map.put("status", registerDTO.isStatus());
        map.put("startDate", registerDTO.getStartDate());
        map.put("endDate", registerDTO.getEndDate());
        map.put("total", registerDTO.getTotal());
        map.put("parkingId", registerDTO.getParkingId());
        map.put("fareId", registerDTO.getFareId());
        return map;
    }

    private Map<String, Object> convertEntityToMap(Register register) {
        Map<String, Object> map = new HashMap<>();
        map.put("registerId", register.getRegisterId());
        map.put("name", register.getName());
        map.put("car", register.getCar());
        map.put("plate", register.getPlate());
        map.put("status", register.isStatus());
        map.put("startDate", register.getStartDate());
        map.put("endDate", register.getEndDate());
        map.put("total", register.getTotal());
        map.put("parkingId", register.getParking() != null ? register.getParking().getParkingId() : null);
        map.put("fareId", register.getFare() != null ? register.getFare().getFareId() : null);
        return map;
    }

    private Map<String, Object> convertPageToMap(Page<RegisterDTO> registerDTOs) {
        Map<String, Object> map = new HashMap<>();
        map.put("registers", registerDTOs.getContent());
        map.put("totalElements", registerDTOs.getTotalElements());
        map.put("totalPages", registerDTOs.getTotalPages());
        map.put("currentPage", registerDTOs.getNumber());
        return map;
    }

    private Map<String, Object> convertListToMap(List<RegisterDTO> registerDTOs) {
        Map<String, Object> map = new HashMap<>();
        map.put("registers", registerDTOs);
        map.put("total", registerDTOs.size());
        return map;
    }
}
