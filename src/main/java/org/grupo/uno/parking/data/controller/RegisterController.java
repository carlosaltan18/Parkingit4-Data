package org.grupo.uno.parking.data.controller;

import jakarta.annotation.security.RolesAllowed;
import org.grupo.uno.parking.data.dto.RegisterDTO;
import org.grupo.uno.parking.data.service.IRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/registers")
public class RegisterController {

    @Autowired
    private IRegisterService registerService;

    @RolesAllowed("REGISTER")
    @GetMapping("")
    public ResponseEntity<Page<RegisterDTO>> getAllRegisters(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            Page<RegisterDTO> registers = registerService.getAllRegisters(page, size);
            return new ResponseEntity<>(registers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RolesAllowed("REGISTER")
    @GetMapping("/{id}")
    public ResponseEntity<RegisterDTO> getRegisterById(@PathVariable Long id) {
        return registerService.findById(id)
                .map(registerDTO -> new ResponseEntity<>(registerDTO, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RolesAllowed("REGISTER")
    @PostMapping("/saveRegister")
    public ResponseEntity<RegisterDTO> saveRegister(@RequestBody RegisterDTO registerDTO) {
        try {
            RegisterDTO savedRegister = registerService.saveRegister(registerDTO);
            return new ResponseEntity<>(savedRegister, HttpStatus.CREATED);
        } catch (DataAccessException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RolesAllowed("REGISTER")
    @PutMapping("/updateRegister/{id}")
    public ResponseEntity<RegisterDTO> updateRegister(
            @PathVariable Long id, @RequestBody RegisterDTO registerDTO) {
        try {
            RegisterDTO updatedRegister = registerService.updateRegister(registerDTO, id);
            return new ResponseEntity<>(updatedRegister, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (DataAccessException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RolesAllowed("REGISTER")
    @DeleteMapping("/deleteRegister/{id}")
    public ResponseEntity<Void> deleteRegister(@PathVariable Long id) {
        try {
            registerService.deleteRegister(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (DataAccessException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Nuevo endpoint para obtener registros por ID de estacionamiento
    @RolesAllowed("REGISTER")
    @GetMapping("/report/{parkingId}")
    public ResponseEntity<List<RegisterDTO>> getRegistersByParkingId(@PathVariable Long parkingId) {
        try {
            List<RegisterDTO> registers = registerService.generateReportByParkingId(parkingId);
            return new ResponseEntity<>(registers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
