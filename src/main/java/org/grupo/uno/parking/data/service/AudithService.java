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

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AudithService implements IAudithService {

    private static final Logger logger = LoggerFactory.getLogger(AudithService.class);
    private static final int MAX_LENGTH = 255;

    private final AudithRepository audithRepository;

    @Autowired
    public AudithService(AudithRepository audithRepository) {
        this.audithRepository = audithRepository;
    }

    @Override
    public Audith createAudit(String entity, String description, String operation,
                              Map<String, Object> request, Map<String, Object> response, String result) {
        validateAuditParameters(entity, description, operation);
        logger.info("Creating audit for entity: {}, operation: {}", entity, operation);

        description = truncateIfNecessary(description);
        request = truncateMapValuesIfNecessary(request);
        response = truncateMapValuesIfNecessary(response);
        result = truncateIfNecessary(result);

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

    @Override
    public Page<Audith> getAllAudits(int page, int size) {
        logger.info("Fetching all audits - Page: {}, Size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return audithRepository.findAll(pageable);
    }

    @Override
    public Audith getAuditById(long id) {
        logger.info("Fetching audit by ID: {}", id);
        return audithRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Audit not found with ID: {}", id);
                    return new IllegalArgumentException("Auditoría no encontrada con ID: " + id);
                });
    }

    @Override
    public Page<Audith> getAuditsByEntity(String entity, int page, int size) {
        if (!StringUtils.hasText(entity)) {
            logger.error("Entity parameter is empty.");
            throw new ValidationException("La entidad no puede estar vacía.");
        }

        logger.info("Fetching audits for entity: {} - Page: {}, Size: {}", entity, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return audithRepository.findByEntityIgnoreCase(entity, pageable);
    }

    @Override
    public Page<Audith> getAuditsByStartDate(LocalDateTime startDate, int page, int size) {
        if (startDate == null) {
            logger.error("Start date is null.");
            throw new ValidationException("La fecha de inicio no puede estar vacía.");
        }

        LocalDateTime endDate = LocalDateTime.now();
        logger.info("Fetching audits between {} and {} - Page: {}, Size: {}", startDate, endDate, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return audithRepository.findByStartDateBetween(startDate, endDate, pageable);
    }

    @Override
    public Page<Audith> getAuditsByDateRange(LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        if (startDate == null || endDate == null) {
            logger.error("Start date or end date is null.");
            throw new ValidationException("Las fechas de inicio y fin no pueden estar vacías.");
        }

        if (startDate.isAfter(endDate)) {
            logger.error("Start date {} cannot be after end date {}", startDate, endDate);
            throw new ValidationException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        logger.info("Fetching audits between {} and {} - Page: {}, Size: {}", startDate, endDate, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return audithRepository.findByStartDateBetween(startDate, endDate, pageable);
    }

    public List<AudithDTO> getAllAuditDTOs(int page, int size) {
        logger.info("Fetching all audit DTOs - Page: {}, Size: {}", page, size);
        Page<Audith> auditPage = getAllAudits(page, size);
        return auditPage.stream()
                .map(this::convertToDTO)
                .toList();
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

    private String truncateIfNecessary(String value) {
        if (value != null && value.length() > MAX_LENGTH) {
            logger.warn("Truncating value: {} to maximum length of {}", value, MAX_LENGTH);
            return value.substring(0, MAX_LENGTH);
        }
        return value;
    }

    private Map<String, Object> truncateMapValuesIfNecessary(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return map;
        }
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> truncateIfNecessary(String.valueOf(entry.getValue()))
                ));
    }
}

