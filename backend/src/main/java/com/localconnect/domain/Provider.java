package com.localconnect.domain;

import com.localconnect.domain.enums.VerificationStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "providers")
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 150)
    private String businessName;

    @Column(nullable = false, length = 100)
    private String operationalDistrict;

    @Column(nullable = false, length = 100)
    private String operationalTaluka;

    @Column(nullable = false, length = 150)
    private String serviceBase;

    @Column(precision = 3, scale = 2)
    private BigDecimal ratingAverage = BigDecimal.valueOf(0.00);

    private int totalCompletedJobs = 0;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING_APPROVAL;

    private LocalDateTime verifiedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Provider() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
