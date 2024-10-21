package org.grupo.uno.parking.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FareDto {
    private long fareId;
    private String name;
    private String startTime;
    private String endTime;
    private Double price;
    private Boolean status;
}

