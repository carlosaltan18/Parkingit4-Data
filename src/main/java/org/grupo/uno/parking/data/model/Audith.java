package org.grupo.uno.parking.data.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.grupo.uno.parking.data.converter.JsonbConverter;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Data
@Table(name = "audith")
public class Audith {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private long auditId;

    private String entity;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime startDate;

    private String description;
    private String operation;
    private String result;

    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> request;

    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> response;
}
