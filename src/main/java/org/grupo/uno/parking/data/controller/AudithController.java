package org.grupo.uno.parking.data.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.grupo.uno.parking.data.dto.AudithDTO;
import org.grupo.uno.parking.data.model.Audith;
import org.grupo.uno.parking.data.service.AudithService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/audith")
public class AudithController {


    @Autowired
    AudithService audithService;

    @Autowired
    public AudithController(AudithService audithService) {
        this.audithService = audithService;
    }

    @RolesAllowed("AUDITH")
    @GetMapping("")
    public ResponseEntity<List<AudithDTO>> getAllAudits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<AudithDTO> auditDTOs = audithService.getAllAuditDTOs(page, size);
        return ResponseEntity.ok(auditDTOs);
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
