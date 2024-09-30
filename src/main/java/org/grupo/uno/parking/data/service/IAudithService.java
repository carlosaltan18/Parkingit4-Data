package org.grupo.uno.parking.data.service;

import org.grupo.uno.parking.data.dto.AudithDTO;
import org.grupo.uno.parking.data.model.Audith;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IAudithService {
    void recordAudit(String entity, String description, String operation, Map<String, Object> request, Map<String, Object> response, String result);

    List<AudithDTO> getAllAudits();

    Optional<AudithDTO> getAuditById(long id);

    AudithDTO convertToDTO(Audith audit);
}
