package org.grupo.uno.parking.data.controller;

import jakarta.annotation.security.RolesAllowed;
import org.grupo.uno.parking.data.dto.RegisterDTO;
import org.grupo.uno.parking.data.model.Register;
import org.grupo.uno.parking.data.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/registers")
public class RegisterController {

    @Autowired
    private RegisterService registerService;

    @RolesAllowed("REGISTER")
    @GetMapping
    public ResponseEntity<Page<Register>> getAllRegisters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Register> registers = registerService.getAllRegisters(page, size);
        return ResponseEntity.ok(registers);
    }

    @RolesAllowed("REGISTER")
    @GetMapping("/{id}")
    public ResponseEntity<Register> getRegisterById(@PathVariable Long id) {
        return registerService.findById(id)
                .map(register -> ResponseEntity.ok(register))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @RolesAllowed("REGISTER")
    @PostMapping("/saveRegister")
    public ResponseEntity<Register> createRegister(@RequestBody RegisterDTO registerDTO) {
        Register newRegister = registerService.saveRegister(registerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newRegister);
    }

    @RolesAllowed("REGISTER")
    @PutMapping("/updateRegister/{id}")
    public ResponseEntity<RegisterDTO> updateRegister(@PathVariable Long id, @RequestBody RegisterDTO registerDTO) {
        try {
            RegisterDTO updatedRegister = registerService.updateRegister(registerDTO, id);
            return ResponseEntity.ok(updatedRegister);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @RolesAllowed("REGISTER")
    @DeleteMapping("/deleteRegister/{id}")
    public ResponseEntity<String> deleteRegister(@PathVariable Long id) {
        try {
            registerService.deleteRegister(id);
            return ResponseEntity.noContent().build();
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting register: " + e.getMessage());
        }
    }
}
