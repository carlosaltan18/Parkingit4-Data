package org.grupo.uno.parking.data.service;


import org.grupo.uno.parking.data.exceptions.ExceptionPdf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class PdfServiceTest {

    private PdfService pdfService;

    @BeforeEach
    void setUp() {
        pdfService = new PdfService();
    }
    @Test
    void generatePdfFromJson_validJson_createsPdfSuccessfully() {

        String json = "[{\"registerId\": 1, \"plate\": \"ABC123\", \"total\": 10.0, " +
                "\"startDate\": \"2023-10-21 12:00:00\", \"endDate\": \"2023-10-21 14:00:00\"}]";

        byte[] pdfBytes = pdfService.generatePdfFromJson(json);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }



    @Test
    void generatePdfFromJson_invalidJson_throwsExceptionPdf() {

        String invalidJson = "[{\"registerId\": 1, \"plate\": \"ABC123\", \"total\": \"invalid\", " +
                "\"startDate\": \"2023-10-21T12:00:00\"}]";

        ExceptionPdf exception = assertThrows(ExceptionPdf.class, () -> {
            pdfService.generatePdfFromJson(invalidJson);
        });

        assertEquals("Failed to parse JSON to RegisterDTO", exception.getMessage());
    }
}
