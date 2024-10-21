package org.grupo.uno.parking.data.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.security.RolesAllowed;
import org.grupo.uno.parking.data.dto.RegisterDTO;
import org.grupo.uno.parking.data.exceptions.NoRegistersFoundException;
import org.grupo.uno.parking.data.service.IRegisterService;
import org.grupo.uno.parking.data.service.PdfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/registers")
public class RegisterController {
    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);


    private final IRegisterService registerService;

    private final PdfService pdfService;

    public RegisterController (IRegisterService registerService, PdfService pdfService) {
        this.registerService = registerService;
        this.pdfService = pdfService;
    }

    @RolesAllowed("REGISTER")
    @PostMapping("/entrada")
    public ResponseEntity<RegisterDTO> registroDeEntrada(@RequestBody RegisterDTO registerDTO) {
        if (registerDTO == null || registerDTO.getPlate() == null || registerDTO.getParkingId() == 0) {
            logger.error("El cuerpo de la solicitud es inválido: {}", registerDTO);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            RegisterDTO newRegister = registerService.registroDeEntrada(registerDTO.getPlate(), registerDTO.getParkingId());
            return new ResponseEntity<>(newRegister, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error en registro de entrada: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RolesAllowed("REGISTER")
    @PutMapping("/salida/{plate}")
    public ResponseEntity<RegisterDTO> registroDeSalida(@PathVariable String plate) {
        try {
            RegisterDTO updatedRegister = registerService.registroDeSalida(plate);
            return new ResponseEntity<>(updatedRegister, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.error("Registro no encontrado: ", e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error en registro de salida: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RolesAllowed("REGISTER")
    @GetMapping("")
    public ResponseEntity<Page<RegisterDTO>> getAllRegisters(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            Page<RegisterDTO> registers = registerService.getAllRegisters(page, size);
            return new ResponseEntity<>(registers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error obteniendo todos los registros: ", e);
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
            logger.error("Error guardando el registro: ", e);
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
            logger.error("Registro no encontrado para actualizar: ", e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (DataAccessException e) {
            logger.error("Error actualizando el registro: ", e);
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
            logger.error("Registro no encontrado para eliminar: ", e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (DataAccessException e) {
            logger.error("Error eliminando el registro: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RolesAllowed("REGISTER")
    @GetMapping("/report/{parkingId}/{startDate}/{endDate}")
    public ResponseEntity<List<RegisterDTO>> getRegistersByParkingId(@PathVariable Long parkingId,
                                                                     @PathVariable LocalDate startDate,
                                                                     @PathVariable LocalDate endDate) {
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            List<RegisterDTO> registers = registerService.generateReportByParkingId(parkingId, startDateTime, endDateTime);
            return new ResponseEntity<>(registers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error generando reporte por ID de estacionamiento: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RolesAllowed("REGISTER")
    @PostMapping("/generatePDF/{parkingId}/{startDate}/{endDate}")
    public ResponseEntity<byte[]> getRegistersByParkingIdPDF(@PathVariable Long parkingId,
                                                             @PathVariable LocalDate startDate,
                                                             @PathVariable LocalDate endDate) {
        logger.info("Generating PDF for parkingId: {}", parkingId);
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

            List<RegisterDTO> registers = registerService.generateReportByParkingIdPDF(parkingId, startDateTime, endDateTime);
            if (registers.isEmpty()) {
                throw new NoRegistersFoundException("No registers found for parkingId: " + parkingId);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            String json = objectMapper.writeValueAsString(registers);
            logger.debug("Generated JSON: {}", json);

            byte[] pdfBytes = pdfService.generatePdfFromJson(json);
            logger.info("PDF generated successfully");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "register.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (NoRegistersFoundException e) {
            logger.error("Error generating PDF: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (JsonProcessingException e) {
            logger.error("Error processing JSON: ", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error generating PDF: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}