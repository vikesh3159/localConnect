package com.localconnect.dto;

import com.localconnect.domain.enums.VerificationStatus;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class ProviderProfileDto {
    private Long id;
    private Long userId;

    @NotBlank(message = "Business name is required")
    private String businessName;

    @NotBlank(message = "Operational district is required")
    private String operationalDistrict;

    @NotBlank(message = "Operational taluka is required")
    private String operationalTaluka;

    @NotBlank(message = "Service base is required")
    private String serviceBase;

    private BigDecimal ratingAverage;
    private int totalCompletedJobs;
    private VerificationStatus verificationStatus;

    public ProviderProfileDto() {
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

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getOperationalDistrict() {
        return operationalDistrict;
    }

    public void setOperationalDistrict(String operationalDistrict) {
        this.operationalDistrict = operationalDistrict;
    }

    public String getOperationalTaluka() {
        return operationalTaluka;
    }

    public void setOperationalTaluka(String operationalTaluka) {
        this.operationalTaluka = operationalTaluka;
    }

    public String getServiceBase() {
        return serviceBase;
    }

    public void setServiceBase(String serviceBase) {
        this.serviceBase = serviceBase;
    }

    public BigDecimal getRatingAverage() {
        return ratingAverage;
    }

    public void setRatingAverage(BigDecimal ratingAverage) {
        this.ratingAverage = ratingAverage;
    }

    public int getTotalCompletedJobs() {
        return totalCompletedJobs;
    }

    public void setTotalCompletedJobs(int totalCompletedJobs) {
        this.totalCompletedJobs = totalCompletedJobs;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }
}
