package org.grupo.uno.parking.data.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParkingDTO {

    private long parkingId;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Address cannot be blank")
    private String address;

    private String phone;  // Este campo puede ser opcional, por lo que no tiene validación

    @Min(value = 1, message = "The number of spaces must be at least 1")
    private int spaces;

    @NotNull(message = "User ID cannot be null")
    @Min(value = 1, message = "User ID must be a positive number")
    private long userId;

    @NotNull(message = "Status cannot be null")
    private Boolean status;
}
