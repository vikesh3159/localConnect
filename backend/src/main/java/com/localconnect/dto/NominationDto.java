package com.localconnect.dto;

import jakarta.validation.constraints.NotNull;

public class NominationDto {

    @NotNull(message = "Resource ID is required")
    private Long resourceId;

    @NotNull(message = "Driver ID is required for vehicle/transport allocations")
    private Long driverId;

    public NominationDto() {
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }
}
