package com.localconnect.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EmergencyDispatchDto {

    @NotNull(message = "ETA in minutes is required")
    @Min(value = 1, message = "ETA must be at least 1 minute")
    private Integer etaMinutes;

    @NotBlank(message = "Vehicle registration plate is required for dispatch")
    private String vehiclePlate;

    private String notes;

    public EmergencyDispatchDto() {
    }

    public Integer getEtaMinutes() {
        return etaMinutes;
    }

    public void setEtaMinutes(Integer etaMinutes) {
        this.etaMinutes = etaMinutes;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
