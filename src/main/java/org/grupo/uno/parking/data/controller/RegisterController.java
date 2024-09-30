package org.grupo.uno.parking.data.controller;

import org.grupo.uno.parking.data.dto.RegisterDTO;
import org.grupo.uno.parking.data.model.Register;
import org.grupo.uno.parking.data.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/registers")
public class RegisterController {

    @Autowired
    private RegisterService registerService;

    @GetMapping("")
    public ResponseEntity<Page<Register>> getAllRegisters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Register> registers = registerService.getAllRegisters(page, size);
        return new ResponseEntity<>(registers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Register> getRegisterById(@PathVariable Long id) {
        Optional<Register> register = registerService.findById(id);
        return register.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/saveRegister")
    public ResponseEntity<Register> createRegister(@RequestBody RegisterDTO registerDTO) {
        Register newRegister = registerService.saveRegister(registerDTO);
        return new ResponseEntity<>(newRegister, HttpStatus.CREATED);
    }

    @PutMapping("/updateRegister/{id}")
    public ResponseEntity<?> updateRegister(@PathVariable Long id, @RequestBody RegisterDTO registerDTO) {
        try {
            registerService.updateRegister(registerDTO, id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/deleteRegister/{id}")
    public ResponseEntity<?> deleteRegister(@PathVariable Long id) {
        try {
            registerService.deleteRegister(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (DataAccessException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}