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
    private String plate;
    private boolean status;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private long parkingId;
    private long fareId;

    private BigDecimal total;
}
