package org.grupo.uno.parking.data.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.grupo.uno.parking.data.dto.AudithDTO;
import org.grupo.uno.parking.data.dto.DateRangeRequest;
import org.grupo.uno.parking.data.model.Audith;
import org.grupo.uno.parking.data.service.AudithService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/audith")
public class AudithController {

    private final AudithService audithService;

    private static final Logger logger = LoggerFactory.getLogger(AudithController.class);

    public AudithController(AudithService audithService) {
        this.audithService = audithService;
    }

    private static final String MESSAGE_KEY = "message";
    private static final String AUDITHS_KEY = "audiths";
    private static final String TOTAL_PAGES_KEY = "totalPages";
    private static final String CURRENT_PAGE_KEY = "currentPage";
    private static final String TOTAL_ELEMENTS_KEY = "totalElements";
    private static final String AUDITS_RECOVERED_SUCCESS = "Auditorías recuperadas exitosamente";


    @RolesAllowed("AUDITH")
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getAllAudits(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();
        try {
            Page<Audith> audithPage = audithService.getAllAudits(page, size);
            response.put(MESSAGE_KEY, AUDITS_RECOVERED_SUCCESS);
            response.put(AUDITHS_KEY, audithPage.getContent());
            response.put(TOTAL_PAGES_KEY, audithPage.getTotalPages());
            response.put(CURRENT_PAGE_KEY, audithPage.getNumber());
            response.put(TOTAL_ELEMENTS_KEY, audithPage.getTotalElements());
            logger.info("Get audits, total elements: {}, current page: {}", audithPage.getTotalElements(), audithPage.getNumber());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al recuperar auditorías: {}", e.getMessage(), e);
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
    public ResponseEntity<Map<String, Object>> getAuditsByEntity(@PathVariable("entity") String entity, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();
        try {
            Page<Audith> auditsPage = audithService.getAuditsByEntity(entity, page, size);
            response.put(MESSAGE_KEY, AUDITS_RECOVERED_SUCCESS);
            response.put(AUDITHS_KEY, auditsPage.getContent());
            response.put(TOTAL_PAGES_KEY, auditsPage.getTotalPages());
            response.put(CURRENT_PAGE_KEY, auditsPage.getNumber());
            response.put(TOTAL_ELEMENTS_KEY, auditsPage.getTotalElements());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al recuperar auditorías por entidad: {}", e.getMessage(), e);
            response.put("err", "Error al recuperar auditorías: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @RolesAllowed("AUDITH")
    @PostMapping("/date-range")
    public ResponseEntity<Map<String, Object>> getAuditsByDateRange(@RequestBody DateRangeRequest dateRangeRequest, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();
        try {
            LocalDateTime startDate = LocalDateTime.parse(dateRangeRequest.getStartDate());
            LocalDateTime endDate = LocalDateTime.parse(dateRangeRequest.getEndDate());
            Page<Audith> auditsPage = audithService.getAuditsByDateRange(startDate, endDate, page, size);
            response.put(MESSAGE_KEY, AUDITS_RECOVERED_SUCCESS);
            response.put(AUDITHS_KEY, auditsPage.getContent());
            response.put(TOTAL_PAGES_KEY, auditsPage.getTotalPages());
            response.put(CURRENT_PAGE_KEY, auditsPage.getNumber());
            response.put(TOTAL_ELEMENTS_KEY, auditsPage.getTotalElements());
            return ResponseEntity.ok(response);
        } catch (DateTimeParseException e) {
            logger.error("Error al analizar las fechas: {}", e.getMessage(), e);
            response.put("err", "Error al analizar las fechas: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error recuperando auditorías por rango de fechas: {}", e.getMessage(), e);
            response.put("err", "Error recuperando auditorías: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
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
            logger.error("Error al crear la auditoría: {}", e.getMessage(), e);
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
