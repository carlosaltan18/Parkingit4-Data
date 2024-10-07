package org.grupo.uno.parking.data.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO implements Serializable {

    private long registerId;
    private String name;
    private String car;
    private String plate;
    private boolean status;

    // Cambiado de LocalDateTime a OffsetDateTime, si así lo tienes configurado en el modelo
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Usamos solo los IDs en lugar de las entidades completas
    private long parkingId;
    private long fareId;

    private BigDecimal total;
}
