package com.localconnect.dto;

import com.localconnect.domain.enums.RequestStatus;
import com.localconnect.domain.enums.ResourceCategory;
import com.localconnect.domain.enums.UrgencyLevel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ServiceRequestResponseDto {
    private Long id;
    private String referenceNumber;
    private Long customerId;
    private String customerPhone;
    private ResourceCategory category;
    private UrgencyLevel urgencyLevel;
    private int quantityRequired;
    private int allocatedQuantity;
    private String pickupLocation;
    private String destinationLocation;
    private LocalDate scheduledDate;
    private LocalTime scheduledTime;
    private String stayType;
    private String goodsType;
    private String specialInstructions;
    private RequestStatus status;
    private int broadcastWindowHours;
    private LocalDateTime broadcastExpiresAt;
    private boolean isExpired;
    private int searchRadiusKm;
    private int offersCount;
    private List<ProviderOfferResponseDto> offers;
    private LocalDateTime createdAt;

    public ServiceRequestResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public ResourceCategory getCategory() {
        return category;
    }

    public void setCategory(ResourceCategory category) {
        this.category = category;
    }

    public UrgencyLevel getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(UrgencyLevel urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public int getQuantityRequired() {
        return quantityRequired;
    }

    public void setQuantityRequired(int quantityRequired) {
        this.quantityRequired = quantityRequired;
    }

    public int getAllocatedQuantity() {
        return allocatedQuantity;
    }

    public void setAllocatedQuantity(int allocatedQuantity) {
        this.allocatedQuantity = allocatedQuantity;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(String destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getStayType() {
        return stayType;
    }

    public void setStayType(String stayType) {
        this.stayType = stayType;
    }

    public String getGoodsType() {
        return goodsType;
    }

    public void setGoodsType(String goodsType) {
        this.goodsType = goodsType;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public int getBroadcastWindowHours() {
        return broadcastWindowHours;
    }

    public void setBroadcastWindowHours(int broadcastWindowHours) {
        this.broadcastWindowHours = broadcastWindowHours;
    }

    public LocalDateTime getBroadcastExpiresAt() {
        return broadcastExpiresAt;
    }

    public void setBroadcastExpiresAt(LocalDateTime broadcastExpiresAt) {
        this.broadcastExpiresAt = broadcastExpiresAt;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }

    public int getSearchRadiusKm() {
        return searchRadiusKm;
    }

    public void setSearchRadiusKm(int searchRadiusKm) {
        this.searchRadiusKm = searchRadiusKm;
    }

    public int getOffersCount() {
        return offersCount;
    }

    public void setOffersCount(int offersCount) {
        this.offersCount = offersCount;
    }

    public List<ProviderOfferResponseDto> getOffers() {
        return offers;
    }

    public void setOffers(List<ProviderOfferResponseDto> offers) {
        this.offers = offers;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
