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

import java.util.Optional;

@Service
public class RegisterService implements IRegisterService {

    @Autowired
    private RegisterRepository registerRepository;

    @Autowired
    private ParkingRepository parkingRepository;

    @Autowired
    private FareRepository fareRepository;

    @Override
    public Page<Register> getAllRegisters(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return registerRepository.findAll(pageable);
    }

    @Override
    public Optional<Register> findById(Long registerId) {
        return registerRepository.findById(registerId);
    }

    @Override
    public Register saveRegister(RegisterDTO registerDTO) {
        Register register = convertToEntity(registerDTO);
        return registerRepository.save(register);
    }

    @Override
    public void updateRegister(RegisterDTO registerDTO, Long registerId) {
        Optional<Register> optionalRegister = registerRepository.findById(registerId);

        if (optionalRegister.isPresent()) {
            Register register = optionalRegister.get();
            updateRegisterFields(register, registerDTO);
            registerRepository.save(register);
        } else {
            throw new IllegalArgumentException("Register with ID " + registerId + " not found.");
        }
    }

    private void updateRegisterFields(Register register, RegisterDTO registerDTO) {
        register.setName(registerDTO.getName());
        register.setCar(registerDTO.getCar());
        register.setPlate(registerDTO.getPlate());
        register.setStatus(registerDTO.isStatus());
        register.setStartDate(registerDTO.getStartDate());
        register.setEndDate(registerDTO.getEndDate());
        register.setTotal(registerDTO.getTotal());

        Optional<Parking> parking = parkingRepository.findById(registerDTO.getParkingId());
        parking.ifPresent(register::setParking);

        Optional<Fare> fare = fareRepository.findById(registerDTO.getFareId());
        fare.ifPresent(register::setFare);
    }

    @Override
    public void deleteRegister(Long registerId) {
        try {
            registerRepository.deleteById(registerId);
        } catch (DataAccessException e) {
            throw new DataAccessException("Error deleting register with ID " + registerId, e) {};
        }
    }

    private Register convertToEntity(RegisterDTO registerDTO) {
        Register register = new Register();
        register.setName(registerDTO.getName());
        register.setCar(registerDTO.getCar());
        register.setPlate(registerDTO.getPlate());
        register.setStatus(registerDTO.isStatus());
        register.setStartDate(registerDTO.getStartDate());
        register.setEndDate(registerDTO.getEndDate());
        register.setTotal(registerDTO.getTotal());

        // Asignar Parking y Fare desde DTO
        Optional<Parking> parking = parkingRepository.findById(registerDTO.getParkingId());
        parking.ifPresent(register::setParking);

        Optional<Fare> fare = fareRepository.findById(registerDTO.getFareId());
        fare.ifPresent(register::setFare);

        return register;
    }

    private RegisterDTO convertToDTO(Register register) {
        return new RegisterDTO(
                register.getRegisterId(),
                register.getName(),
                register.getCar(),
                register.getPlate(),
                register.isStatus(),
                register.getStartDate(),
                register.getEndDate(),
                register.getParking() != null ? register.getParking().getParkingId() : 0,
                register.getFare() != null ? register.getFare().getFareId() : 0,
                register.getTotal()
        );
    }
}
