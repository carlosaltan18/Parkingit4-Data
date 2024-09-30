package org.grupo.uno.parking.data.service;

import org.grupo.uno.parking.data.dto.AudithDTO;
import org.grupo.uno.parking.data.model.Audith;
import org.grupo.uno.parking.data.repository.AudithRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AudithService {

    private final AudithRepository audithRepository;

    @Autowired
    public AudithService(AudithRepository audithRepository) {
        this.audithRepository = audithRepository;
    }

    public Audith createAudit(String entity, String description, String operation, Map<String, Object> request, Map<String, Object> response, String result) {
        Audith audit = new Audith();
        audit.setEntity(entity);
        audit.setStartDate(LocalDateTime.now());
        audit.setDescription(description);
        audit.setOperation(operation);
        audit.setRequest(request);
        audit.setResponse(response);
        audit.setResult(result);
        return audithRepository.save(audit);
    }

    public List<Audith> getAllAudits() {
        return audithRepository.findAll();
    }

    public Optional<Audith> getAuditById(long id) {
        return audithRepository.findById(id);
    }

    public List<Audith> getAuditsByEntity(String entity) {
        return audithRepository.findAll().stream()
                .filter(audit -> audit.getEntity().equalsIgnoreCase(entity))
                .collect(Collectors.toList());
    }

    public AudithDTO convertToDTO(Audith audit) {
        if (audit == null) {
            return null;
        }
        AudithDTO dto = new AudithDTO();
        dto.setAuditId(audit.getAuditId());
        dto.setEntity(audit.getEntity());
        dto.setStartDate(OffsetDateTime.from(audit.getStartDate()));
        dto.setDescription(audit.getDescription());
        dto.setOperation(audit.getOperation());
        dto.setResult(audit.getResult());
        dto.setRequest(audit.getRequest() != null ? audit.getRequest() : null);
        dto.setResponse(audit.getResponse() != null ? audit.getResponse() : null);
        return dto;
    }

    private OffsetDateTime convertToOffsetDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atOffset(ZoneOffset.UTC);
    }

    public void processAudit(Audith audit) {
        try {
            OffsetDateTime offsetDateTime = convertToOffsetDateTime(audit.getStartDate());
        } catch (DateTimeException e) {
            System.err.println("Error al convertir a OffsetDateTime: " + e.getMessage());
            throw e;
        }
    }

    public List<AudithDTO> getAllAuditDTOs() {
        return getAllAudits().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}

