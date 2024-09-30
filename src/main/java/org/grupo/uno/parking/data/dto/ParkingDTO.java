package org.grupo.uno.parking.data.dto;

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
    private String name;
    private String address;
    private String phone;
    private int spaces;
    private long userId;
    private Boolean status;
}
