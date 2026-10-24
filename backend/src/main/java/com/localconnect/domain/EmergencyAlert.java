package com.localconnect.domain;

import com.localconnect.domain.enums.EmergencyStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_alerts")
public class EmergencyAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false, length = 50)
    private String emergencyType;

    @Column(nullable = false, length = 20)
    private String contactPhone;

    @Column(nullable = false, length = 200)
    private String callerLocation;

    @Column(length = 200)
    private String callerLandmark;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EmergencyStatus status = EmergencyStatus.LOCAL_ASSISTANCE_REQUESTED;

    private Long assignedProviderId;

    private Integer providerEtaMinutes;

    @Column(length = 50)
    private String providerVehiclePlate;

    @Column(length = 20)
    private String dialedHelpline;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public EmergencyAlert() {
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public EmergencyStatus getStatus() {
        return status;
    }

    public void setStatus(EmergencyStatus status) {
        this.status = status;
    }

    public Long getAssignedProviderId() {
        return assignedProviderId;
    }

    public void setAssignedProviderId(Long assignedProviderId) {
        this.assignedProviderId = assignedProviderId;
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

    public String getDialedHelpline() {
        return dialedHelpline;
    }

    public void setDialedHelpline(String dialedHelpline) {
        this.dialedHelpline = dialedHelpline;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
