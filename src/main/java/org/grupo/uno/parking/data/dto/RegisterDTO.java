package org.grupo.uno.parking.data.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {
    private long registerId;
    private String name;
    private String car;
    private String plate;
    private boolean status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private long parkingId;  // Asegúrate de que es long y no Long
    private long fareId;     // Asegúrate de que es long y no Long
    private BigDecimal total;
}
