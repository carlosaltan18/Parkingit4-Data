package org.grupo.uno.parking.data.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DateRangeRequest {
    @NotNull
    private String startDate; // Formato esperado: "yyyy-MM-dd'T'HH:mm:ss"

    @NotNull
    private String endDate; // Formato esperado: "yyyy-MM-dd'T'HH:mm:ss"

    public DateRangeRequest(String startDate, String endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
