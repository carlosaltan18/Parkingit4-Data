package org.grupo.uno.parking.data.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Importa el módulo para manejar fechas y horas
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Converter(autoApply = true)
public class JsonbConverter implements AttributeConverter<Map<String, Object>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // Registra el módulo para manejar LocalDateTime
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);

    private static final Logger LOGGER = Logger.getLogger(JsonbConverter.class.getName());

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }

        // Validar que todos los valores en el mapa son serializables
        validateMap(attribute);

        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Error converting map to JSON for database: {0}", e.getMessage());
            throw new IllegalArgumentException("Failed to convert map to JSON for database", e);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error converting JSON to map for entity. Data: {0}, Error: {1}", new Object[]{dbData, e.getMessage()});
            throw new IllegalArgumentException("Failed to convert JSON to map for entity", e);
        }
    }

    // Método para validar que todos los valores en el mapa son serializables
    private void validateMap(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            try {
                // Intenta serializar el valor
                objectMapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("El valor no se puede serializar: " + entry.getValue(), e);
            }
        }
    }
}
