package com.localconnect.dto;

import com.localconnect.domain.enums.OfferStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProviderOfferResponseDto {
    private Long id;
    private Long requestId;
    private Long providerId;
    private String providerBusinessName;
    private BigDecimal providerRating;
    private int offeredQuantity;
    private BigDecimal quotedAmount;
    private boolean includesDriver;
    private OfferStatus status;
    private LocalDateTime nominationDeadline;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    // Nominated resource summary (details unmasked only after confirmed allocation)
    private Long nominatedResourceId;
    private String nominatedResourceTitle;
    private String nominatedVehiclePlate; // masked unless confirmed
    private String assignedDriverName; // masked unless confirmed
    private String assignedDriverPhone; // masked unless confirmed

    public ProviderOfferResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public String getProviderBusinessName() {
        return providerBusinessName;
    }

    public void setProviderBusinessName(String providerBusinessName) {
        this.providerBusinessName = providerBusinessName;
    }

    public BigDecimal getProviderRating() {
        return providerRating;
    }

    public void setProviderRating(BigDecimal providerRating) {
        this.providerRating = providerRating;
    }

    public int getOfferedQuantity() {
        return offeredQuantity;
    }

    public void setOfferedQuantity(int offeredQuantity) {
        this.offeredQuantity = offeredQuantity;
    }

    public BigDecimal getQuotedAmount() {
        return quotedAmount;
    }

    public void setQuotedAmount(BigDecimal quotedAmount) {
        this.quotedAmount = quotedAmount;
    }

    public boolean isIncludesDriver() {
        return includesDriver;
    }

    public void setIncludesDriver(boolean includesDriver) {
        this.includesDriver = includesDriver;
    }

    public OfferStatus getStatus() {
        return status;
    }

    public void setStatus(OfferStatus status) {
        this.status = status;
    }

    public LocalDateTime getNominationDeadline() {
        return nominationDeadline;
    }

    public void setNominationDeadline(LocalDateTime nominationDeadline) {
        this.nominationDeadline = nominationDeadline;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getNominatedResourceId() {
        return nominatedResourceId;
    }

    public void setNominatedResourceId(Long nominatedResourceId) {
        this.nominatedResourceId = nominatedResourceId;
    }

    public String getNominatedResourceTitle() {
        return nominatedResourceTitle;
    }

    public void setNominatedResourceTitle(String nominatedResourceTitle) {
        this.nominatedResourceTitle = nominatedResourceTitle;
    }

    public String getNominatedVehiclePlate() {
        return nominatedVehiclePlate;
    }

    public void setNominatedVehiclePlate(String nominatedVehiclePlate) {
        this.nominatedVehiclePlate = nominatedVehiclePlate;
    }

    public String getAssignedDriverName() {
        return assignedDriverName;
    }

    public void setAssignedDriverName(String assignedDriverName) {
        this.assignedDriverName = assignedDriverName;
    }

    public String getAssignedDriverPhone() {
        return assignedDriverPhone;
    }

    public void setAssignedDriverPhone(String assignedDriverPhone) {
        this.assignedDriverPhone = assignedDriverPhone;
    }
}
