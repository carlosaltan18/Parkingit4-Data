package org.grupo.uno.parking.data.service;

import jakarta.validation.ValidationException;
import org.grupo.uno.parking.data.dto.AudithDTO;
import org.grupo.uno.parking.data.model.Audith;
import org.grupo.uno.parking.data.repository.AudithRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AudithServiceTest {

    @Mock
    private AudithRepository audithRepository;

    @InjectMocks
    private AudithService audithService;

    private Audith audit;

    @BeforeEach
    void setUp() {
        audit = new Audith();
        audit.setAuditId(1L);
        audit.setEntity("TestEntity");
        audit.setDescription("Test description");
        audit.setOperation("CREATE");
        audit.setStartDate(LocalDateTime.now());
        audit.setRequest(Collections.singletonMap("key", "value"));
        audit.setResponse(Collections.singletonMap("responseKey", "responseValue"));
        audit.setResult("SUCCESS");
    }

    @Test
    void createAudit_success() {
        when(audithRepository.save(any(Audith.class))).thenReturn(audit);

        Audith createdAudit = audithService.createAudit("TestEntity", "Test description", "CREATE",
                Collections.singletonMap("key", "value"), Collections.singletonMap("responseKey", "responseValue"), "SUCCESS");

        assertNotNull(createdAudit);
        assertEquals(audit.getAuditId(), createdAudit.getAuditId());
        verify(audithRepository, times(1)).save(any(Audith.class));
    }

    @Test
    void createAudit_invalidParameters() {
        assertThrows(ValidationException.class, () -> {
            audithService.createAudit("", "Test description", "CREATE",
                    Collections.singletonMap("key", "value"), Collections.singletonMap("responseKey", "responseValue"), "SUCCESS");
        });

        assertThrows(ValidationException.class, () -> {
            audithService.createAudit("TestEntity", "", "CREATE",
                    Collections.singletonMap("key", "value"), Collections.singletonMap("responseKey", "responseValue"), "SUCCESS");
        });

        assertThrows(ValidationException.class, () -> {
            audithService.createAudit("TestEntity", "Test description", "",
                    Collections.singletonMap("key", "value"), Collections.singletonMap("responseKey", "responseValue"), "SUCCESS");
        });
    }

    @Test
    void getAllAudits_success() {
        Page<Audith> page = new PageImpl<>(Collections.singletonList(audit));
        when(audithRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Audith> result = audithService.getAllAudits(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(audithRepository, times(1)).findAll(any(Pageable.class));
    }


    @Test
    void getAuditById_success() {
        when(audithRepository.findById(1L)).thenReturn(Optional.of(audit));

        Audith foundAudit = audithService.getAuditById(1L);

        assertNotNull(foundAudit);
        assertEquals(audit.getAuditId(), foundAudit.getAuditId());
        verify(audithRepository, times(1)).findById(1L);
    }

    @Test
    void getAuditById_notFound() {
        when(audithRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            audithService.getAuditById(1L);
        });

        assertEquals("Auditoría no encontrada con ID: 1", exception.getMessage());
    }

    @Test
    void getAuditsByEntity_success() {
        Page<Audith> page = new PageImpl<>(Collections.singletonList(audit));
        when(audithRepository.findByEntityIgnoreCase(eq("TestEntity"), any())).thenReturn(page);

        Page<Audith> result = audithService.getAuditsByEntity("TestEntity", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(audithRepository, times(1)).findByEntityIgnoreCase(eq("TestEntity"), any());
    }

    @Test
    void getAuditsByEntity_emptyEntity() {
        Exception exception = assertThrows(ValidationException.class, () -> {
            audithService.getAuditsByEntity("", 0, 10);
        });

        assertEquals("La entidad no puede estar vacía.", exception.getMessage());
    }

    @Test
    void getAuditsByStartDate_success() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        Page<Audith> page = new PageImpl<>(Collections.singletonList(audit));
        when(audithRepository.findByStartDateBetween(eq(startDate), any(), any())).thenReturn(page);

        Page<Audith> result = audithService.getAuditsByStartDate(startDate, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(audithRepository, times(1)).findByStartDateBetween(eq(startDate), any(), any());
    }

    @Test
    void getAuditsByStartDate_nullStartDate() {
        Exception exception = assertThrows(ValidationException.class, () -> {
            audithService.getAuditsByStartDate(null, 0, 10);
        });

        assertEquals("La fecha de inicio no puede estar vacía.", exception.getMessage());
    }

    @Test
    void getAuditsByDateRange_success() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();
        Page<Audith> page = new PageImpl<>(Collections.singletonList(audit));
        when(audithRepository.findByStartDateBetween(eq(startDate), eq(endDate), any())).thenReturn(page);

        Page<Audith> result = audithService.getAuditsByDateRange(startDate, endDate, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(audithRepository, times(1)).findByStartDateBetween(eq(startDate), eq(endDate), any());
    }

    @Test
    void getAuditsByDateRange_invalidDates() {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().minusDays(1);

        Exception exception = assertThrows(ValidationException.class, () -> {
            audithService.getAuditsByDateRange(startDate, endDate, 0, 10);
        });

        assertEquals("La fecha de inicio no puede ser posterior a la fecha de fin.", exception.getMessage());
    }

    @Test
    void convertToDTO_success() {
        AudithDTO dto = audithService.convertToDTO(audit);

        assertNotNull(dto);
        assertEquals(audit.getAuditId(), dto.getAuditId());
        assertEquals(audit.getEntity(), dto.getEntity());
        assertEquals(audit.getDescription(), dto.getDescription());
        verify(audithRepository, never()).findById(any());  // No debería hacer llamadas al repositorio
    }

    @Test
    void convertToDTO_nullAudit() {
        AudithDTO dto = audithService.convertToDTO(null);

        assertNull(dto);
    }
}
