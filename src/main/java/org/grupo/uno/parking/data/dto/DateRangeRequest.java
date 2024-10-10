package org.grupo.uno.parking.data.dto;

import jakarta.validation.constraints.NotNull;

public class DateRangeRequest {
    @NotNull
    private String startDate; // Formato esperado: "yyyy-MM-dd'T'HH:mm:ss"

    @NotNull
    private String endDate; // Formato esperado: "yyyy-MM-dd'T'HH:mm:ss"

    // Getters y Setters
    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
