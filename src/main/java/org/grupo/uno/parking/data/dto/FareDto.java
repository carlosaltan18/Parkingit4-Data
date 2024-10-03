package org.grupo.uno.parking.data.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FareDto {
    private long fareId;
    private String name;
  //  @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "El formato de startTime debe ser HH:mm")
    private String startTime;
   // @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "El formato de endTime debe ser HH:mm")
    private String endTime;
    private Double price;
    private Boolean status;
}

