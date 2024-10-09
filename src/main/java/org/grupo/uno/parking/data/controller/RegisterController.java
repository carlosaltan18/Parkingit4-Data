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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/registers")
public class RegisterController {
    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @Autowired
    private IRegisterService registerService;
    @Autowired
    private PdfService pdfService;

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

    @RolesAllowed("REGISTER")
    @PostMapping("/generatePDF/{parkingId}")
    public ResponseEntity<byte[]> getRegistersByParkingIdPDF(@PathVariable Long parkingId) {
        logger.info("Generating PDF for parkingId: {}", parkingId);
        try {
            List<RegisterDTO> registers = registerService.generateReportByParkingIdPDF(parkingId);
            if (registers == null) {
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
