package org.grupo.uno.parking.data.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import org.grupo.uno.parking.data.dto.RegisterDTO;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
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

            // Título del PDF
            Paragraph title = new Paragraph("Registro de Vehículos")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // Crear una tabla para los registros
            Table table = new Table(NUMBER_OF_COLUMNS);
            table.setWidth(UnitValue.createPercentValue(100)); // Ancho de la tabla al 100%

            // Estilos de encabezados
            for (String header : new String[]{"Register ID", "Name", "Car", "Plate", "Total", "Start Date", "End Date"}) {
                Cell headerCell = new Cell().add(new Paragraph(header))
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setPadding(5);
                table.addHeaderCell(headerCell);
            }

            // Formateador para la fecha
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // Agregar los registros a la tabla
            for (RegisterDTO register : registers) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(register.getRegisterId()))));
                table.addCell(new Cell().add(new Paragraph(register.getName())));
                table.addCell(new Cell().add(new Paragraph(register.getCar())));
                table.addCell(new Cell().add(new Paragraph(register.getPlate())));
                table.addCell(new Cell().add(new Paragraph(register.getTotal().toString())));
                table.addCell(new Cell().add(new Paragraph(register.getStartDate().format(formatter))));
                table.addCell(new Cell().add(new Paragraph(register.getEndDate().format(formatter))));
            }

            document.add(table);

            Paragraph footer = new Paragraph("Generado el " + LocalDateTime.now().format(formatter))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20);
            document.add(footer);
        } catch (Exception e) {
            throw new RuntimeException("Error while generating PDF", e);
        }

        return byteArrayOutputStream.toByteArray();
    }
}