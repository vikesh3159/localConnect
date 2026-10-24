package com.localconnect.domain;

import com.localconnect.domain.enums.OfferStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "provider_offers")
public class ProviderOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private ServiceRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @Column(nullable = false)
    private int offeredQuantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quotedAmount;

    @Column(nullable = false)
    private boolean includesDriver = true;

    private Long nominatedResourceId;
    private Long nominatedDriverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OfferStatus status = OfferStatus.SUBMITTED;

    private LocalDateTime nominationDeadline;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public ProviderOffer() {
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

    public ServiceRequest getRequest() {
        return request;
    }

    public void setRequest(ServiceRequest request) {
        this.request = request;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
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

    public Long getNominatedResourceId() {
        return nominatedResourceId;
    }

    public void setNominatedResourceId(Long nominatedResourceId) {
        this.nominatedResourceId = nominatedResourceId;
    }

    public Long getNominatedDriverId() {
        return nominatedDriverId;
    }

    public void setNominatedDriverId(Long nominatedDriverId) {
        this.nominatedDriverId = nominatedDriverId;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
