package org.grupo.uno.parking.data.service;

import org.grupo.uno.parking.data.dto.RegisterDTO;
import org.grupo.uno.parking.data.model.Fare;
import org.grupo.uno.parking.data.model.Parking;
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
    private AudithService audithService; // Inyección del servicio de auditoría

    @Override
    public Page<Register> getAllRegisters(int page, int size) {
        logger.info("Fetching all registers - Page: {}, Size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return registerRepository.findAll(pageable);
    }

    @Override
    public Optional<Register> findById(Long registerId) {
        logger.info("Finding register by ID: {}", registerId);
        return registerRepository.findById(registerId);
    }

    @Override
    public Register saveRegister(RegisterDTO registerDTO) {
        logger.info("Saving new register with details: {}", registerDTO);
        Register register = convertToEntity(registerDTO);
        Register savedRegister = registerRepository.save(register);

        // Guardar la auditoría de creación
        audithService.createAudit(
                "Register",
                "Registro creado",
                "CREATE",
                convertDTOToMap(registerDTO),
                convertEntityToMap(savedRegister),
                "SUCCESS"
        );
        logger.info("Register saved successfully with ID: {}", savedRegister.getRegisterId());
        return savedRegister;
    }

    @Override
    public RegisterDTO updateRegister(RegisterDTO registerDTO, Long registerId) {
        logger.info("Updating register with ID: {}", registerId);
        Optional<Register> optionalRegister = registerRepository.findById(registerId);

        if (optionalRegister.isPresent()) {
            Register register = optionalRegister.get();
            updateRegisterFields(register, registerDTO);
            registerRepository.save(register);

            // Guardar la auditoría de actualización
            audithService.createAudit(
                    "Register",
                    "Registro actualizado",
                    "UPDATE",
                    convertDTOToMap(registerDTO),
                    convertEntityToMap(register),
                    "SUCCESS"
            );
            logger.info("Register updated successfully with ID: {}", registerId);
        } else {
            logger.error("Register with ID {} not found.", registerId);
            throw new IllegalArgumentException("Register with ID " + registerId + " not found.");
        }
        return registerDTO;
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

    // Métodos adicionales...

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

        // Asignar Parking si existe
        Optional<Parking> parking = parkingRepository.findById(registerDTO.getParkingId());
        parking.ifPresent(register::setParking);

        // Asignar Fare si existe
        Optional<Fare> fare = fareRepository.findById(registerDTO.getFareId());
        fare.ifPresent(register::setFare);

        return register;
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
}
