package com.localconnect.service;

import com.localconnect.domain.*;
import com.localconnect.domain.enums.AllocationStatus;
import com.localconnect.domain.enums.OfferStatus;
import com.localconnect.domain.enums.RequestStatus;
import com.localconnect.dto.BookingAllocationResponseDto;
import com.localconnect.exception.ConcurrencyConflictException;
import com.localconnect.exception.InvalidStateException;
import com.localconnect.exception.ResourceNotFoundException;
import com.localconnect.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AllocationService {

    private final BookingAllocationRepository allocationRepository;
    private final ProviderOfferRepository offerRepository;
    private final ServiceRequestRepository requestRepository;
    private final ResourceRepository resourceRepository;
    private final DriverRepository driverRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public AllocationService(
            BookingAllocationRepository allocationRepository,
            ProviderOfferRepository offerRepository,
            ServiceRequestRepository requestRepository,
            ResourceRepository resourceRepository,
            DriverRepository driverRepository,
            NotificationService notificationService,
            AuditService auditService) {
        this.allocationRepository = allocationRepository;
        this.offerRepository = offerRepository;
        this.requestRepository = requestRepository;
        this.resourceRepository = resourceRepository;
        this.driverRepository = driverRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    /**
     * Stage 3: Customer final confirms the booking allocation.
     * Transitions allocation to CONFIRMED and releases driver contact details.
     */
    @Transactional
    public BookingAllocationResponseDto confirmAllocation(Long customerId, Long offerId) {
        ProviderOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));

        ServiceRequest request = offer.getRequest();

        if (!request.getCustomer().getId().equals(customerId)) {
            throw new InvalidStateException("Unauthorized to confirm this allocation");
        }

        if (offer.getStatus() != OfferStatus.NOMINATION_CONFIRMED) {
            throw new InvalidStateException(
                    "Offer must be in NOMINATION_CONFIRMED state before final confirmation (current: "
                            + offer.getStatus() + ")");
        }

        Resource resource = resourceRepository.findById(offer.getNominatedResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Nominated resource not found"));

        Driver driver = null;
        if (offer.getNominatedDriverId() != null) {
            driver = driverRepository.findById(offer.getNominatedDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Nominated driver not found"));
        }

        // Concurrency Check: Verify total allocated quantity does not exceed requested
        int currentAllocated = allocationRepository.sumAllocatedQuantityByRequestId(request.getId());
        if (currentAllocated + offer.getOfferedQuantity() > request.getQuantityRequired()) {
            throw new ConcurrencyConflictException("Allocation exceeds remaining requested quantity");
        }

        // Re-verify no conflicts occurred in the interim
        if (allocationRepository.hasResourceConflict(resource.getId(), request.getScheduledDate())) {
            throw new ConcurrencyConflictException("Vehicle schedule conflict detected during confirmation");
        }

        // Create confirmed allocation
        BookingAllocation allocation = new BookingAllocation();
        allocation.setRequest(request);
        allocation.setOffer(offer);
        allocation.setCustomer(request.getCustomer());
        allocation.setProvider(offer.getProvider());
        allocation.setResource(resource);
        allocation.setDriver(driver);
        allocation.setAllocatedQuantity(offer.getOfferedQuantity());
        allocation.setAgreedAmount(offer.getQuotedAmount());
        allocation.setConfirmationCode("LC-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        allocation.setStatus(AllocationStatus.CONFIRMED);

        BookingAllocation saved = allocationRepository.save(allocation);

        // Update offer status to ACCEPTED
        offer.setStatus(OfferStatus.ACCEPTED);
        offerRepository.save(offer);

        // Update request status based on total allocated quantity
        int totalAllocated = currentAllocated + offer.getOfferedQuantity();
        if (totalAllocated >= request.getQuantityRequired()) {
            request.setStatus(RequestStatus.FULLY_ALLOCATED);

            // Mark remaining unselected offers as SUPERSEDED
            List<ProviderOffer> otherOffers = offerRepository.findByRequestIdOrderByCreatedAtAsc(request.getId());
            for (ProviderOffer o : otherOffers) {
                if (o.getStatus() == OfferStatus.SUBMITTED || o.getStatus() == OfferStatus.PROVISIONALLY_SELECTED) {
                    o.setStatus(OfferStatus.SUPERSEDED);
                    offerRepository.save(o);
                }
            }
        } else {
            request.setStatus(RequestStatus.PARTIALLY_ALLOCATED);
        }
        requestRepository.save(request);

        auditService.logEvent(customerId, "BOOKING_ALLOCATION", saved.getId().toString(), "CONFIRM_BOOKING",
                null, "CONFIRMED", "Booking confirmed: Code " + saved.getConfirmationCode() +
                        ", Qty " + saved.getAllocatedQuantity() + ", Amount ₹" + saved.getAgreedAmount());

        // Notify customer with driver full details
        String driverInfo = (driver != null) ? "Driver: " + driver.getFullName() + " (" + driver.getPhone() + ")"
                : "Self-managed";
        notificationService.sendNotification(
                request.getCustomer().getPhone(),
                "BOOKING_CONFIRMED",
                "Booking Confirmed! Code: " + saved.getConfirmationCode() + ". " + driverInfo +
                        ". Vehicle: " + resource.getRegistrationNumber() + ". Pay directly after service completion.");

        // Notify provider/driver
        notificationService.sendNotification(
                offer.getProvider().getUser().getPhone(),
                "ALLOCATION_DISPATCH",
                "New Booking Confirmed! Code: " + saved.getConfirmationCode() + ". Scheduled: " +
                        request.getScheduledDate() + " at " + request.getScheduledTime() + ". Pickup: "
                        + request.getPickupLocation());

        return mapToDto(saved);
    }

    public List<BookingAllocationResponseDto> getCustomerAllocations(Long customerId) {
        return allocationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<BookingAllocationResponseDto> getProviderAllocations(Long providerUserId) {
        return allocationRepository.findByProviderIdOrderByCreatedAtDesc(providerUserId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private BookingAllocationResponseDto mapToDto(BookingAllocation alloc) {
        BookingAllocationResponseDto dto = new BookingAllocationResponseDto();
        dto.setId(alloc.getId());
        dto.setRequestId(alloc.getRequest().getId());
        dto.setRequestReferenceNumber(alloc.getRequest().getReferenceNumber());
        dto.setCustomerId(alloc.getCustomer().getId());
        dto.setProviderId(alloc.getProvider().getId());
        dto.setProviderBusinessName(alloc.getProvider().getBusinessName());
        dto.setResourceTitle(alloc.getResource().getTitle());
        dto.setVehicleRegistrationNumber(alloc.getResource().getRegistrationNumber());
        if (alloc.getDriver() != null) {
            dto.setDriverName(alloc.getDriver().getFullName());
            dto.setDriverPhone(alloc.getDriver().getPhone());
        }
        dto.setAllocatedQuantity(alloc.getAllocatedQuantity());
        dto.setAgreedAmount(alloc.getAgreedAmount());
        dto.setConfirmationCode(alloc.getConfirmationCode());
        dto.setStatus(alloc.getStatus());
        dto.setCreatedAt(alloc.getCreatedAt());
        return dto;
    }
}
