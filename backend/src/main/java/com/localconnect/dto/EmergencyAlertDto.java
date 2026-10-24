package com.localconnect.dto;

import com.localconnect.domain.enums.EmergencyStatus;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class EmergencyAlertDto {
    private Long id;

    @NotBlank(message = "Emergency type is required")
    private String emergencyType;

    @NotBlank(message = "Contact phone is required")
    private String contactPhone;

    @NotBlank(message = "Caller location is required")
    private String callerLocation;

    private String callerLandmark;
    private String notes;
    private String dialedHelpline;
    private EmergencyStatus status;
    private Integer providerEtaMinutes;
    private String providerVehiclePlate;
    private LocalDateTime createdAt;

    public EmergencyAlertDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmergencyType() {
        return emergencyType;
    }

    public void setEmergencyType(String emergencyType) {
        this.emergencyType = emergencyType;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getCallerLocation() {
        return callerLocation;
    }

    public void setCallerLocation(String callerLocation) {
        this.callerLocation = callerLocation;
    }

    public String getCallerLandmark() {
        return callerLandmark;
    }

    public void setCallerLandmark(String callerLandmark) {
        this.callerLandmark = callerLandmark;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDialedHelpline() {
        return dialedHelpline;
    }

    public void setDialedHelpline(String dialedHelpline) {
        this.dialedHelpline = dialedHelpline;
    }

    public EmergencyStatus getStatus() {
        return status;
    }

    public void setStatus(EmergencyStatus status) {
        this.status = status;
    }

    public Integer getProviderEtaMinutes() {
        return providerEtaMinutes;
    }

    public void setProviderEtaMinutes(Integer providerEtaMinutes) {
        this.providerEtaMinutes = providerEtaMinutes;
    }

    public String getProviderVehiclePlate() {
        return providerVehiclePlate;
    }

    public void setProviderVehiclePlate(String providerVehiclePlate) {
        this.providerVehiclePlate = providerVehiclePlate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
