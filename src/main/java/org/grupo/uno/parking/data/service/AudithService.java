package org.grupo.uno.parking.data.service;

import jakarta.validation.ValidationException;
import org.grupo.uno.parking.data.dto.AudithDTO;
import org.grupo.uno.parking.data.model.Audith;
import org.grupo.uno.parking.data.repository.AudithRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AudithService {

    private static final Logger logger = LoggerFactory.getLogger(AudithService.class);

    private final AudithRepository audithRepository;

    @Autowired
    public AudithService(AudithRepository audithRepository) {
        this.audithRepository = audithRepository;
    }

    public Audith createAudit(String entity, String description, String operation,
                              Map<String, Object> request, Map<String, Object> response, String result) {
        validateAuditParameters(entity, description, operation);
        logger.info("Creating audit for entity: {}, operation: {}", entity, operation);

        Audith audit = new Audith();
        audit.setEntity(entity);
        audit.setStartDate(LocalDateTime.now());
        audit.setDescription(description);
        audit.setOperation(operation);
        audit.setRequest(request);
        audit.setResponse(response);
        audit.setResult(result);

        Audith savedAudit = audithRepository.save(audit);
        logger.info("Audit created successfully with ID: {}", savedAudit.getAuditId());
        return savedAudit;
    }

    public Page<Audith> getAllAudits(int page, int size) {
        logger.info("Fetching all audits - Page: {}, Size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return audithRepository.findAll(pageable);
    }

    public Audith getAuditById(long id) {
        logger.info("Fetching audit by ID: {}", id);
        return audithRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Audit not found with ID: {}", id);
                    return new IllegalArgumentException("Auditoría no encontrada con ID: " + id);
                });
    }

    public List<Audith> getAuditsByEntity(String entity) {
        if (!StringUtils.hasText(entity)) {
            logger.error("Entity parameter is empty.");
            throw new ValidationException("La entidad no puede estar vacía.");
        }

        logger.info("Fetching audits for entity: {}", entity);
        return audithRepository.findByEntityIgnoreCase(entity);
    }

    public AudithDTO convertToDTO(Audith audit) {
        if (audit == null) {
            logger.warn("Audit is null, cannot convert to DTO.");
            return null;
        }

        logger.info("Converting audit to DTO for ID: {}", audit.getAuditId());
        AudithDTO dto = new AudithDTO();
        dto.setAuditId(audit.getAuditId());
        dto.setEntity(audit.getEntity());
        dto.setStartDate(convertToOffsetDateTime(audit.getStartDate()));
        dto.setDescription(audit.getDescription());
        dto.setOperation(audit.getOperation());
        dto.setResult(audit.getResult());
        dto.setRequest(audit.getRequest() != null ? audit.getRequest() : null);
        dto.setResponse(audit.getResponse() != null ? audit.getResponse() : null);
        return dto;
    }

    private OffsetDateTime convertToOffsetDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            logger.warn("LocalDateTime is null, cannot convert to OffsetDateTime.");
            return null;
        }
        return localDateTime.atOffset(ZoneOffset.UTC);
    }

    public void processAudit(Audith audit) {
        try {
            logger.info("Processing audit with ID: {}", audit.getAuditId());
            OffsetDateTime offsetDateTime = convertToOffsetDateTime(audit.getStartDate());
            // Procesar la auditoría según sea necesario
        } catch (DateTimeException e) {
            logger.error("Error al convertir a OffsetDateTime: {}", e.getMessage());
            throw e;
        }
    }

    public List<AudithDTO> getAllAuditDTOs(int page, int size) {
        logger.info("Fetching all audit DTOs - Page: {}, Size: {}", page, size);
        Page<Audith> auditPage = getAllAudits(page, size);
        return auditPage.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private void validateAuditParameters(String entity, String description, String operation) {
        if (!StringUtils.hasText(entity)) {
            logger.error("Entity parameter is empty.");
            throw new ValidationException("La entidad no puede estar vacía.");
        }
        if (!StringUtils.hasText(description)) {
            logger.error("Description parameter is empty.");
            throw new ValidationException("La descripción no puede estar vacía.");
        }
        if (!StringUtils.hasText(operation)) {
            logger.error("Operation parameter is empty.");
            throw new ValidationException("La operación no puede estar vacía.");
        }
    }
}
