package org.grupo.uno.parking.data.controller;


import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.grupo.uno.parking.data.dto.RegisterDTO;
import org.grupo.uno.parking.data.exceptions.NoRegistersFoundException;
import org.grupo.uno.parking.data.service.IRegisterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.grupo.uno.parking.data.service.JwtService;
import org.grupo.uno.parking.data.service.PdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RegisterControllerTest {


    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private IRegisterService registerService;

    @MockBean
    private JwtService jwtService;

    @Mock
    private PdfService pdfService;

    @InjectMocks
    private RegisterController registerController;

    private final LocalDateTime fixedDateTime = LocalDateTime.of(2024, 1, 1, 12, 0);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(registerController).build();
    }


    @Test
    void registroDeEntrada_validRequest_returnsCreated() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO(
                1L,
                "ABC123",
                true,
                fixedDateTime,
                fixedDateTime.plusHours(1),
                1L,
                1L,
                BigDecimal.valueOf(10.00)
        );

        when(registerService.registroDeEntrada("ABC123", 1)).thenReturn(registerDTO);

        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.registerModule(new JavaTimeModule());
        mockMvc.perform(post("/registers/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObjectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plate").value("ABC123"));
    }


    @Test
    void registroDeEntrada_invalidRequest_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/registers/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registroDeSalida_validRequest_returnsOk() throws Exception {
        RegisterDTO updatedRegister = new RegisterDTO(
                1L,
                "ABC123",
                false,
                fixedDateTime,
                fixedDateTime.plusHours(1),
                1L,
                1L,
                BigDecimal.valueOf(10.00)
        );

        when(registerService.registroDeSalida("ABC123")).thenReturn(updatedRegister);

        mockMvc.perform(put("/registers/salida/ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plate").value("ABC123"))
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    void registroDeSalida_notFound_returnsNotFound() throws Exception {
        when(registerService.registroDeSalida("UNKNOWN")).thenThrow(new IllegalArgumentException());

        mockMvc.perform(put("/registers/salida/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllRegisters_returnsOk() throws Exception {

        List<RegisterDTO> registerDTOs = List.of(
                new RegisterDTO(1L, "098CNV", false, LocalDateTime.parse("2024-10-14T13:11:54"), LocalDateTime.parse("2024-10-14T14:16:03"), 1L, 1L, BigDecimal.valueOf(16.00)),
                new RegisterDTO(2L, "iop654", false, LocalDateTime.parse("2024-10-14T18:21:38"), LocalDateTime.parse("2024-10-14T19:24:48"), 1L, 1L, BigDecimal.valueOf(15.75)),
                new RegisterDTO(3L, "OPX299", false, LocalDateTime.parse("2024-10-14T13:38:50"), LocalDateTime.parse("2024-10-14T13:40:19"), 1L, 1L, BigDecimal.valueOf(0.25)),
                new RegisterDTO(4L, "P8X99O", false, LocalDateTime.parse("2024-10-14T18:41:00"), LocalDateTime.parse("2024-10-14T19:41:18"), 1L, 2L, BigDecimal.valueOf(30.00)),
                new RegisterDTO(5L, "465LOP", false, LocalDateTime.parse("2024-10-14T17:09:25"), LocalDateTime.parse("2024-10-14T17:11:12"), 1L, 2L, BigDecimal.valueOf(0.50)),
                new RegisterDTO(7L, "POS450", false, LocalDateTime.parse("2024-10-14T22:38:02"), LocalDateTime.parse("2024-10-14T22:40:31"), 1L, 3L, BigDecimal.valueOf(6.67)),
                new RegisterDTO(9L, "O69DDF", false, LocalDateTime.parse("2024-10-15T09:39:06"), LocalDateTime.parse("2024-10-15T09:55:44"), 1L, 1L, BigDecimal.valueOf(4.00)),
                new RegisterDTO(10L, "234VGF", true, LocalDateTime.parse("2024-10-15T14:16:59"), null, 1L, 0L, null),
                new RegisterDTO(11L, "764MNB", true, LocalDateTime.parse("2024-10-16T13:42:56"), null, 1L, 0L, null),
                new RegisterDTO(12L, "123CSF", true, LocalDateTime.parse("2024-10-16T13:43:21"), null, 1L, 0L, null)
        );

        Page<RegisterDTO> page = new PageImpl<>(registerDTOs, PageRequest.of(0, 10), 50);

        when(registerService.getAllRegisters(0, 10)).thenReturn(page);

        mockMvc.perform(get("/registers?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].registerId").value(1))
                .andExpect(jsonPath("$.content[0].plate").value("098CNV"))
                .andExpect(jsonPath("$.content[0].status").value(false))
                .andExpect(jsonPath("$.content[0].total").value(16.00))
                .andExpect(jsonPath("$.totalElements").value(50))
                .andExpect(jsonPath("$.numberOfElements").value(10))
                .andExpect(jsonPath("$.size").value(10));
    }


    @Test
    void getRegisterById_found_returnsOk() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO(
                1L,
                "ABC123",
                true,
                fixedDateTime,
                fixedDateTime.plusHours(1),
                1L,
                1L,
                BigDecimal.valueOf(10.00)
        );
        when(registerService.findById(1L)).thenReturn(Optional.of(registerDTO));

        mockMvc.perform(get("/registers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plate").value("ABC123"));
    }

    @Test
    void getRegisterById_notFound_returnsNotFound() throws Exception {
        when(registerService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/registers/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void saveRegister_validRequest_returnsCreated() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO(
                1L,
                "ABC123",
                true,
                fixedDateTime,
                fixedDateTime.plusHours(1),
                1L,
                1L,
                BigDecimal.valueOf(10.00)
        );

        when(registerService.saveRegister(registerDTO)).thenReturn(registerDTO);

        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.registerModule(new JavaTimeModule());

        String json = jsonObjectMapper.writeValueAsString(registerDTO);

        mockMvc.perform(post("/registers/saveRegister")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void updateRegister_validRequest_returnsOk() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO(
                1L,
                "ABC123",
                true,
                fixedDateTime,
                fixedDateTime.plusHours(1),
                1L,
                1L,
                BigDecimal.valueOf(10.00)
        );

        RegisterDTO updatedRegister = new RegisterDTO(
                1L,
                "ABC123",
                true,
                fixedDateTime,
                fixedDateTime.plusHours(1),
                1L,
                1L,
                BigDecimal.valueOf(10.00)
        );

        when(registerService.updateRegister(any(RegisterDTO.class), eq(1L))).thenReturn(updatedRegister);

        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.registerModule(new JavaTimeModule());

        String json = jsonObjectMapper.writeValueAsString(registerDTO);

        mockMvc.perform(put("/registers/updateRegister/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plate").value("ABC123"));
    }


    @Test
    void updateRegister_nonExistentId_returnsNotFound() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO(
                1L,
                "ABC123",
                true,
                fixedDateTime,
                fixedDateTime.plusHours(1),
                1L,
                1L,
                BigDecimal.valueOf(10.00)
        );

        when(registerService.updateRegister(any(RegisterDTO.class), eq(1L)))
                .thenThrow(new IllegalArgumentException("Registro no encontrado"));

        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.registerModule(new JavaTimeModule());

        String json = jsonObjectMapper.writeValueAsString(registerDTO);

        mockMvc.perform(put("/registers/updateRegister/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }


    @Test
    void deleteRegister_found_returnsNoContent() throws Exception {
        doNothing().when(registerService).deleteRegister(1L);

        mockMvc.perform(delete("/registers/deleteRegister/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteRegister_notFound_returnsNotFound() throws Exception {
        doThrow(new IllegalArgumentException()).when(registerService).deleteRegister(1L);

        mockMvc.perform(delete("/registers/deleteRegister/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRegistersByParkingId_returnsOk() throws Exception {
        List<RegisterDTO> registers = List.of(
                new RegisterDTO(1L, "ABC123", true,
                        fixedDateTime,
                        fixedDateTime.plusHours(1),
                        1L, 1L,
                        BigDecimal.valueOf(10.00))
        );

        when(registerService.generateReportByParkingId(eq(1L), any(), any())).thenReturn(registers);

        mockMvc.perform(get("/registers/report/1/2024-01-01/2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].plate").value("ABC123"));
    }

    @Test
    void getRegistersByParkingIdPDF_found_returnsPdf() throws Exception {
        List<RegisterDTO> registers = List.of(
                new RegisterDTO(1L, "ABC123", true,
                        fixedDateTime,
                        fixedDateTime.plusHours(1),
                        1L, 1L,
                        BigDecimal.valueOf(10.00))
        );

        when(registerService.generateReportByParkingIdPDF(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(registers);
        when(pdfService.generatePdfFromJson(any())).thenReturn(new byte[0]);

        mockMvc.perform(post("/registers/generatePDF/1/2024-01-01/2024-01-31"))
                .andExpect(status().isOk());
    }

    @Test
    void getRegistersByParkingIdPDF_noRegisters_returnsNotFound() throws Exception {
        when(registerService.generateReportByParkingIdPDF(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class))).thenThrow(new NoRegistersFoundException("No registers found"));

        mockMvc.perform(post("/registers/generatePDF/1/2024-01-01/2024-01-31"))
                .andExpect(status().isNotFound());
    }

    @Test
    void registroDeEntrada_nullRequestBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/registers/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }


    @Test
    void registroDeEntrada_invalidPlate_returnsBadRequest() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO(
                1L,
                null,
                true,
                fixedDateTime,
                fixedDateTime.plusHours(1),
                1L,
                1L,
                BigDecimal.valueOf(10.00)
        );

        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.registerModule(new JavaTimeModule());

        mockMvc.perform(post("/registers/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void registroDeSalida_internalServerError_returnsInternalServerError() throws Exception {
        when(registerService.registroDeSalida("ABC123")).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(put("/registers/salida/ABC123"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAllRegisters_internalServerError_returnsInternalServerError() throws Exception {
        when(registerService.getAllRegisters(anyInt(), anyInt())).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/registers?page=0&size=10"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getRegisterId_returnIsNotFound() throws Exception {
        when(registerService.findById(2L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/registers/2"))
                .andExpect(status().isNotFound());
    }


    @Test
    void saveRegister_isCreated() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO(
                1L,
                "ABC123",
                true,
                fixedDateTime,
                fixedDateTime.plusHours(1),
                1L,
                1L,
                BigDecimal.valueOf(10.00)
        );


        when(registerService.saveRegister(registerDTO))
                .thenThrow(new DataAccessException("Database error") {});

        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.registerModule(new JavaTimeModule());
        String json = jsonObjectMapper.writeValueAsString(registerDTO);

        mockMvc.perform(post("/registers/saveRegister")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }



    @Test
    void updateRegister_notFound_returnsNotFound() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO(
                1L,
                "ABC123",
                true,
                fixedDateTime,
                fixedDateTime.plusHours(1),
                1L,
                1L,
                BigDecimal.valueOf(10.00)
        );

        when(registerService.updateRegister(any(RegisterDTO.class), eq(1L)))
                .thenThrow(new IllegalArgumentException("Not found"));

        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.registerModule(new JavaTimeModule());

        String json = jsonObjectMapper.writeValueAsString(registerDTO);

        mockMvc.perform(put("/registers/updateRegister/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }


    @Test
    void deleteRegister_internalServerError_returnsInternalServerError() throws Exception {
        doThrow(new DataAccessException("Database error") {}).when(registerService).deleteRegister(1L);

        mockMvc.perform(delete("/registers/deleteRegister/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getRegistersByParkingId_internalServerError_returnsInternalServerError() throws Exception {
        when(registerService.generateReportByParkingId(eq(1L), any(), any())).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/registers/report/1/2024-01-01/2024-01-31"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getRegistersByParkingIdPDF_internalServerError_returnsInternalServerError() throws Exception {
        when(registerService.generateReportByParkingIdPDF(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class))).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/registers/generatePDF/1/2024-01-01/2024-01-31"))
                .andExpect(status().isInternalServerError());
    }



}