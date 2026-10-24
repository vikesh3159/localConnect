package com.localconnect.domain;

import com.localconnect.domain.enums.RequestStatus;
import com.localconnect.domain.enums.ResourceCategory;
import com.localconnect.domain.enums.UrgencyLevel;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "service_requests")
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(nullable = false, unique = true, length = 50)
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ResourceCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UrgencyLevel urgencyLevel = UrgencyLevel.NORMAL;

    @Column(nullable = false)
    private int quantityRequired = 1;

    @Column(nullable = false, length = 200)
    private String pickupLocation;

    @Column(length = 200)
    private String destinationLocation;

    @Column(nullable = false)
    private LocalDate scheduledDate;

    @Column(nullable = false)
    private LocalTime scheduledTime;

    @Column(length = 50)
    private String stayType;

    @Column(length = 50)
    private String goodsType;

    @Column(columnDefinition = "TEXT")
    private String specialInstructions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RequestStatus status = RequestStatus.BROADCASTED;

    private int broadcastWindowHours = 4;

    @Column(nullable = false)
    private LocalDateTime broadcastExpiresAt;

    private int searchRadiusKm = 15;

    @Version
    @Column(name = "opt_lock_version")
    private int version;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public ServiceRequest() {}

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public ResourceCategory getCategory() { return category; }
    public void setCategory(ResourceCategory category) { this.category = category; }

    public UrgencyLevel getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(UrgencyLevel urgencyLevel) { this.urgencyLevel = urgencyLevel; }

    public int getQuantityRequired() { return quantityRequired; }
    public void setQuantityRequired(int quantityRequired) { this.quantityRequired = quantityRequired; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getDestinationLocation() { return destinationLocation; }
    public void setDestinationLocation(String destinationLocation) { this.destinationLocation = destinationLocation; }

    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }

    public LocalTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalTime scheduledTime) { this.scheduledTime = scheduledTime; }

    public String getStayType() { return stayType; }
    public void setStayType(String stayType) { this.stayType = stayType; }

    public String getGoodsType() { return goodsType; }
    public void setGoodsType(String goodsType) { this.goodsType = goodsType; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public int getBroadcastWindowHours() { return broadcastWindowHours; }
    public void setBroadcastWindowHours(int broadcastWindowHours) { this.broadcastWindowHours = broadcastWindowHours; }

    public LocalDateTime getBroadcastExpiresAt() { return broadcastExpiresAt; }
    public void setBroadcastExpiresAt(LocalDateTime broadcastExpiresAt) { this.broadcastExpiresAt = broadcastExpiresAt; }

    public int getSearchRadiusKm() { return searchRadiusKm; }
    public void setSearchRadiusKm(int searchRadiusKm) { this.searchRadiusKm = searchRadiusKm; }

    public int getVersion() { return version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
