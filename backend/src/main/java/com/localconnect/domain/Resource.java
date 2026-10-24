package com.localconnect.domain;

import com.localconnect.domain.enums.ResourceCategory;
import com.localconnect.domain.enums.VerificationStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ResourceCategory category;

    @Column(nullable = false, length = 50)
    private String subType;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 50)
    private String registrationNumber;

    @Column(nullable = false)
    private int capacity = 1;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice = BigDecimal.ZERO;

    @Column(nullable = false, length = 30)
    private String pricingUnit = "per_day";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VerificationStatus approvalStatus = VerificationStatus.PENDING_APPROVAL;

    @Column(nullable = false)
    private boolean isActive = true;

    @Version
    @Column(name = "opt_lock_version")
    private int version;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Resource() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Provider getProvider() { return provider; }
    public void setProvider(Provider provider) { this.provider = provider; }

    public ResourceCategory getCategory() { return category; }
    public void setCategory(ResourceCategory category) { this.category = category; }

    public String getSubType() { return subType; }
    public void setSubType(String subType) { this.subType = subType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public String getPricingUnit() { return pricingUnit; }
    public void setPricingUnit(String pricingUnit) { this.pricingUnit = pricingUnit; }

    public VerificationStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(VerificationStatus approvalStatus) { this.approvalStatus = approvalStatus; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getVersion() { return version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
