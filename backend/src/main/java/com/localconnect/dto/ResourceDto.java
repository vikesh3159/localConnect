package com.localconnect.dto;

import com.localconnect.domain.enums.ResourceCategory;
import com.localconnect.domain.enums.VerificationStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ResourceDto {
    private Long id;
    private Long providerId;
    private String providerBusinessName;

    @NotNull(message = "Category is required")
    private ResourceCategory category;

    @NotBlank(message = "Sub-type is required")
    private String subType;

    @NotBlank(message = "Title is required")
    private String title;

    private String registrationNumber;

    @Min(value = 1, message = "Capacity must be at least 1")
    private int capacity = 1;

    @NotNull(message = "Base price is required")
    private BigDecimal basePrice;

    private String pricingUnit = "per_day";
    private VerificationStatus approvalStatus;
    private boolean active = true;

    public ResourceDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public ResourceCategory getCategory() {
        return category;
    }

    public void setCategory(ResourceCategory category) {
        this.category = category;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public String getPricingUnit() {
        return pricingUnit;
    }

    public void setPricingUnit(String pricingUnit) {
        this.pricingUnit = pricingUnit;
    }

    public VerificationStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(VerificationStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
