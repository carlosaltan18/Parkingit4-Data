package org.grupo.uno.parking.data.service;


import org.grupo.uno.parking.data.dto.RegisterDTO;
import org.grupo.uno.parking.data.model.Register;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface IRegisterService {


    Page<Register> getAllRegisters(int page, int size);

    Optional<Register> findById(Long registerId);

    Register saveRegister(RegisterDTO registerDTO);

    void updateRegister(RegisterDTO registerDTO, Long registerId);

    void deleteRegister(Long registerId);
}
