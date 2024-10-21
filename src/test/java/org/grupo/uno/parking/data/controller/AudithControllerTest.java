package org.grupo.uno.parking.data.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.grupo.uno.parking.data.dto.DateRangeRequest;
import org.grupo.uno.parking.data.model.Audith;
import org.grupo.uno.parking.data.service.AudithService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AudithControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private AudithService audithService;

    @InjectMocks
    private AudithController audithController;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(audithController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void getAllAudits_success_returnsOk() throws Exception {
        Page<Audith> mockPage = Mockito.mock(Page.class);
        when(mockPage.getContent()).thenReturn(new ArrayList<>());
        when(mockPage.getTotalPages()).thenReturn(1);
        when(mockPage.getNumber()).thenReturn(0);
        when(mockPage.getTotalElements()).thenReturn(0L);
        when(audithService.getAllAudits(0, 10)).thenReturn(mockPage);

        mockMvc.perform(get("/audith")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Auditorías recuperadas exitosamente"))
                .andExpect(jsonPath("$.audiths").isArray())
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getAllAudits_failure_returnsInternalServerError() throws Exception {
        when(audithService.getAllAudits(0, 10)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/audith")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json("{\"err\":\"Error al recuperar auditorías: Database error\"}"));  // Ajusta aquí
    }


    @Test
    void getAuditById_success_returnsOk() throws Exception {
        Audith mockAudit = new Audith();
        mockAudit.setAuditId(1L);
        mockAudit.setEntity("Test Entity");
        mockAudit.setDescription("Test Description");

        when(audithService.getAuditById(anyLong())).thenReturn(mockAudit);

        mockMvc.perform(get("/audith/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auditId").value(1L))
                .andExpect(jsonPath("$.entity").value("Test Entity"))
                .andExpect(jsonPath("$.description").value("Test Description"));
    }


    @Test
    void getAuditsByEntity_success_returnsOk() throws Exception {
        Page<Audith> mockPage = Mockito.mock(Page.class);
        when(mockPage.getContent()).thenReturn(new ArrayList<>());
        when(mockPage.getTotalPages()).thenReturn(1);
        when(mockPage.getNumber()).thenReturn(0);
        when(mockPage.getTotalElements()).thenReturn(0L);
        when(audithService.getAuditsByEntity("TestEntity", 0, 10)).thenReturn(mockPage);

        mockMvc.perform(get("/audith/entity/TestEntity")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Auditorías recuperadas exitosamente"))
                .andExpect(jsonPath("$.audiths").isArray())
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getAuditsByEntity_failure_returnsInternalServerError() throws Exception {
        when(audithService.getAuditsByEntity("TestEntity", 0, 10)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/audith/entity/TestEntity")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json("{\"err\":\"Error al recuperar auditorías: Database error\"}"));
    }


    @Test
    void getAuditsByDateRange_success_returnsOk() throws Exception {
        DateRangeRequest request = new DateRangeRequest("2024-01-01T00:00:00", "2024-01-31T23:59:59");
        Page<Audith> mockPage = Mockito.mock(Page.class);

        when(mockPage.getContent()).thenReturn(new ArrayList<>());
        when(mockPage.getTotalPages()).thenReturn(1);
        when(mockPage.getNumber()).thenReturn(0);
        when(mockPage.getTotalElements()).thenReturn(0L);
        when(audithService.getAuditsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class), anyInt(), anyInt()))
                .thenReturn(mockPage);

        mockMvc.perform(post("/audith/date-range")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Auditorías recuperadas exitosamente"))
                .andExpect(jsonPath("$.audiths").isArray())
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getAuditsByDateRange_dateParseException_returnsBadRequest() throws Exception {
        DateRangeRequest request = new DateRangeRequest("invalid-date", "2024-01-31T23:59:59");

        mockMvc.perform(post("/audith/date-range")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.err").value("Error al analizar las fechas: Text 'invalid-date' could not be parsed at index 0"));
    }

    @Test
    void getAuditsByDateRange_failure_returnsInternalServerError() throws Exception {
        DateRangeRequest request = new DateRangeRequest("2024-01-01T00:00:00", "2024-01-31T23:59:59");
        when(audithService.getAuditsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/audith/date-range")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json("{\"err\":\"Error recuperando auditorías: Database error\"}"));
    }

    @Test
    void createManualAudit_success_returnsOk() throws Exception {
        Audith mockAudit = new Audith();
        mockAudit.setEntity("Test Entity");
        mockAudit.setDescription("Test Description");
        when(audithService.createAudit(any(), any(), any(), any(), any(), any())).thenReturn(mockAudit);

        mockMvc.perform(post("/audith/manual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockAudit)))
                .andExpect(status().isOk())
                .andExpect(content().string("Auditoría creada exitosamente"));
    }

    @Test
    void createManualAudit_failure_returnsInternalServerError() throws Exception {
        Audith mockAudit = new Audith();
        mockAudit.setEntity("Test Entity");
        mockAudit.setDescription("Test Description");
        when(audithService.createAudit(any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/audith/manual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockAudit)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error al crear la auditoría: Database error")); // Updated expected value
    }

}
