package org.grupo.uno.parking.data.service;

import org.grupo.uno.parking.data.model.Audith;
import org.springframework.data.domain.Page;
import java.time.LocalDateTime;
import java.util.Map;

public interface IAudithService {

    Audith createAudit(String entity, String description, String operation,
                       Map<String, Object> request, Map<String, Object> response, String result);

    Page<Audith> getAllAudits(int page, int size);

    Audith getAuditById(long id);

    Page<Audith> getAuditsByEntity(String entity, int page, int size);

    Page<Audith> getAuditsByStartDate(LocalDateTime startDate, int page, int size);

    Page<Audith> getAuditsByDateRange(LocalDateTime startDate, LocalDateTime endDate, int page, int size);
}
