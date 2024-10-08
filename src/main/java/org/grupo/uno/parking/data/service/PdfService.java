package org.grupo.uno.parking.data.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import org.grupo.uno.parking.data.dto.RegisterDTO;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    private static final int NUMBER_OF_COLUMNS = 7;

    public byte[] generatePdfFromJson(String json) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        List<RegisterDTO> registers;
        try {
            registers = objectMapper.readValue(json, new TypeReference<List<RegisterDTO>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON to RegisterDTO list", e);
        }

        // Crear un ByteArrayOutputStream para guardar el PDF
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Crear un PdfWriter y un PdfDocument
        try (PdfWriter pdfWriter = new PdfWriter(byteArrayOutputStream);
             PdfDocument pdfDocument = new PdfDocument(pdfWriter);
             Document document = new Document(pdfDocument)) {

            // Crear una tabla para los registros
            Table table = new Table(NUMBER_OF_COLUMNS);
            table.addHeaderCell(new Cell().add(new Paragraph("Register ID")));
            table.addHeaderCell(new Cell().add(new Paragraph("Name")));
            table.addHeaderCell(new Cell().add(new Paragraph("Car")));
            table.addHeaderCell(new Cell().add(new Paragraph("Plate")));
            table.addHeaderCell(new Cell().add(new Paragraph("Total")));
            table.addHeaderCell(new Cell().add(new Paragraph("Start Date"))); // Nueva columna
            table.addHeaderCell(new Cell().add(new Paragraph("End Date"))); // Nueva columna

            // Formateador para la fecha
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // Agregar los registros a la tabla
            for (RegisterDTO register : registers) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(register.getRegisterId()))));
                table.addCell(new Cell().add(new Paragraph(register.getName())));
                table.addCell(new Cell().add(new Paragraph(register.getCar())));
                table.addCell(new Cell().add(new Paragraph(register.getPlate())));
                table.addCell(new Cell().add(new Paragraph(register.getTotal().toString())));
                table.addCell(new Cell().add(new Paragraph(register.getStartDate().format(formatter)))); // Hora de inicio
                table.addCell(new Cell().add(new Paragraph(register.getEndDate().format(formatter)))); // Hora de fin
            }

            document.add(table);
        } catch (Exception e) {
            throw new RuntimeException("Error while generating PDF", e);
        }

        return byteArrayOutputStream.toByteArray(); // Retornar el PDF en forma de byte[]
    }
}