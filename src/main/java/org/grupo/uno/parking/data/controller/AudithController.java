package org.grupo.uno.parking.data.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.grupo.uno.parking.data.dto.AudithDTO;
import org.grupo.uno.parking.data.dto.DateRangeRequest;
import org.grupo.uno.parking.data.model.Audith;
import org.grupo.uno.parking.data.service.AudithService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hibernate.validator.internal.metadata.core.ConstraintHelper.MESSAGE;

@RestController
@RequestMapping("/audith")
public class AudithController {

    @Autowired
    private AudithService audithService;

    private static final Logger logger = LoggerFactory.getLogger(AudithController.class);

    @RolesAllowed("AUDITH")
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getAllAudits(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();
        try {
            Page<Audith> audithPage = audithService.getAllAudits(page, size);
            response.put(MESSAGE, "Auditorías recuperadas exitosamente");
            response.put("audiths", audithPage.getContent());
            response.put("totalPages", audithPage.getTotalPages());
            response.put("currentPage", audithPage.getNumber());
            response.put("totalElements", audithPage.getTotalElements());
            logger.info("Get audits, total elements: {}, current page: {}", audithPage.getTotalElements(), audithPage.getNumber());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("err", "Error al recuperar auditorías: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @RolesAllowed("AUDITH")
    @GetMapping("/{id}")
    public ResponseEntity<AudithDTO> getAuditById(@PathVariable("id") long id) {
        Audith audit = audithService.getAuditById(id);
        return ResponseEntity.ok(convertToDto(audit));
    }

    @RolesAllowed("AUDITH")
    @GetMapping("/entity/{entity}")
    public ResponseEntity<List<AudithDTO>> getAuditsByEntity(@PathVariable("entity") String entity) {
        List<Audith> audits = audithService.getAuditsByEntity(entity);
        List<AudithDTO> auditDTOs = audits.stream()
                .map(this::convertToDto)
                .toList();
        return ResponseEntity.ok(auditDTOs);
    }

    @RolesAllowed("AUDITH")
    @PostMapping("/date-range")
    public ResponseEntity<List<AudithDTO>> getAuditsByDateRange(@RequestBody DateRangeRequest dateRangeRequest) {
        try {
            // Asegúrate de que el formato de fecha sea el esperado
            LocalDateTime startDate = LocalDateTime.parse(dateRangeRequest.getStartDate());
            LocalDateTime endDate = LocalDateTime.parse(dateRangeRequest.getEndDate());
            List<Audith> audits = audithService.getAuditsByDateRange(startDate, endDate);
            List<AudithDTO> auditDTOs = audits.stream()
                    .map(this::convertToDto)
                    .toList();
            return ResponseEntity.ok(auditDTOs);
        } catch (DateTimeParseException e) {
            logger.error("Error al analizar las fechas: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Error recuperando auditorías por rango de fechas: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }





    @RolesAllowed("AUDITH")
    @PostMapping("/manual")
    public ResponseEntity<String> createManualAudit(@Valid @RequestBody Audith auditRequest) {
        try {
            audithService.createAudit(
                    auditRequest.getEntity(),
                    auditRequest.getDescription(),
                    auditRequest.getOperation(),
                    auditRequest.getRequest(),
                    auditRequest.getResponse(),
                    auditRequest.getResult()
            );
            return ResponseEntity.ok("Auditoría creada exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al crear la auditoría: " + e.getMessage());
        }
    }

    private OffsetDateTime convertToOffsetDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atOffset(ZoneOffset.UTC);
    }

    private AudithDTO convertToDto(Audith audit) {
        if (audit == null) {
            return null;
        }
        AudithDTO dto = new AudithDTO();
        dto.setAuditId(audit.getAuditId());
        dto.setEntity(audit.getEntity());
        dto.setStartDate(convertToOffsetDateTime(audit.getStartDate()));
        dto.setDescription(audit.getDescription());
        dto.setOperation(audit.getOperation());
        dto.setResult(audit.getResult());
        dto.setRequest(audit.getRequest());
        dto.setResponse(audit.getResponse());
        return dto;
    }
}
