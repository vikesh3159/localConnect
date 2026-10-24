package com.localconnect.service;

import com.localconnect.domain.*;
import com.localconnect.domain.enums.OfferStatus;
import com.localconnect.domain.enums.RequestStatus;
import com.localconnect.domain.enums.VerificationStatus;
import com.localconnect.dto.NominationDto;
import com.localconnect.dto.ProviderOfferCreateDto;
import com.localconnect.dto.ProviderOfferResponseDto;
import com.localconnect.exception.ConcurrencyConflictException;
import com.localconnect.exception.InvalidStateException;
import com.localconnect.exception.ResourceNotFoundException;
import com.localconnect.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OfferService {

    private final ProviderOfferRepository offerRepository;
    private final ServiceRequestRepository requestRepository;
    private final ProviderRepository providerRepository;
    private final ResourceRepository resourceRepository;
    private final DriverRepository driverRepository;
    private final BookingAllocationRepository allocationRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public OfferService(
            ProviderOfferRepository offerRepository,
            ServiceRequestRepository requestRepository,
            ProviderRepository providerRepository,
            ResourceRepository resourceRepository,
            DriverRepository driverRepository,
            BookingAllocationRepository allocationRepository,
            NotificationService notificationService,
            AuditService auditService) {
        this.offerRepository = offerRepository;
        this.requestRepository = requestRepository;
        this.providerRepository = providerRepository;
        this.resourceRepository = resourceRepository;
        this.driverRepository = driverRepository;
        this.allocationRepository = allocationRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    /**
     * Stage 1: Provider submits quotation (Privacy-safe: no driver PII exposed)
     */
    @Transactional
    public ProviderOfferResponseDto submitQuotation(Long providerUserId, ProviderOfferCreateDto dto) {
        Provider provider = providerRepository.findByUserId(providerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider profile not found for user"));

        if (provider.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new InvalidStateException("Provider must be verified before submitting quotations");
        }

        ServiceRequest request = requestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (request.getStatus() != RequestStatus.BROADCASTED && request.getStatus() != RequestStatus.OFFERS_RECEIVED) {
            throw new InvalidStateException("Request is no longer open for quotations");
        }

        if (LocalDateTime.now().isAfter(request.getBroadcastExpiresAt())) {
            throw new InvalidStateException("Broadcast response window has expired");
        }

        ProviderOffer offer = new ProviderOffer();
        offer.setRequest(request);
        offer.setProvider(provider);
        offer.setOfferedQuantity(dto.getOfferedQuantity());
        offer.setQuotedAmount(dto.getQuotedAmount());
        offer.setIncludesDriver(dto.isIncludesDriver());
        offer.setStatus(OfferStatus.SUBMITTED);
        offer.setExpiresAt(request.getBroadcastExpiresAt());

        ProviderOffer saved = offerRepository.save(offer);

        // Update request status to OFFERS_RECEIVED if first offer
        if (request.getStatus() == RequestStatus.BROADCASTED) {
            request.setStatus(RequestStatus.OFFERS_RECEIVED);
            requestRepository.save(request);
        }

        auditService.logEvent(providerUserId, "PROVIDER_OFFER", saved.getId().toString(), "SUBMIT_QUOTE",
                null, "SUBMITTED",
                "Quotation submitted: Qty " + saved.getOfferedQuantity() + " @ ₹" + saved.getQuotedAmount());

        notificationService.sendNotification(
                request.getCustomer().getPhone(),
                "NEW_OFFER",
                "New quote received from " + provider.getBusinessName() + " for request "
                        + request.getReferenceNumber());

        return mapToDto(saved);
    }

    /**
     * Stage 2: Customer provisionally selects an offer (Atomic 15-minute
     * reservation hold)
     */
    @Transactional
    public ProviderOfferResponseDto selectOfferProvisionally(Long customerId, Long offerId) {
        ProviderOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));

        ServiceRequest request = offer.getRequest();

        if (!request.getCustomer().getId().equals(customerId)) {
            throw new InvalidStateException("Unauthorized to select this offer");
        }

        if (offer.getStatus() != OfferStatus.SUBMITTED) {
            throw new InvalidStateException(
                    "Offer is not available for selection (current status: " + offer.getStatus() + ")");
        }

        LocalDateTime now = LocalDateTime.now();
        offer.setStatus(OfferStatus.PROVISIONALLY_SELECTED);
        offer.setNominationDeadline(now.plusMinutes(15)); // 15-minute provisional reservation hold
        ProviderOffer updated = offerRepository.save(offer);

        request.setStatus(RequestStatus.PROVISIONALLY_HOLDING);
        requestRepository.save(request);

        auditService.logEvent(customerId, "PROVIDER_OFFER", offer.getId().toString(), "PROVISIONAL_SELECT",
                "SUBMITTED", "PROVISIONALLY_SELECTED",
                "15-minute provisional hold placed until " + offer.getNominationDeadline());

        // Urgent 15-minute alert to provider
        notificationService.sendUrgentAlert(
                offer.getProvider().getUser().getPhone(),
                "NOMINATION_REQUIRED",
                "Customer selected your offer for " + request.getReferenceNumber() +
                        "! Nominate vehicle and driver within 15 minutes (Deadline: "
                        + offer.getNominationDeadline().toLocalTime() + ")");

        return mapToDto(updated);
    }

    /**
     * Stage 2b: Provider nominates vehicle and driver with automated conflict
     * validation
     */
    @Transactional
    public ProviderOfferResponseDto nominateResource(Long providerUserId, Long offerId, NominationDto nominationDto) {
        Provider provider = providerRepository.findByUserId(providerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        ProviderOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));

        if (!offer.getProvider().getId().equals(provider.getId())) {
            throw new InvalidStateException("Unauthorized to nominate resources for this offer");
        }

        if (offer.getStatus() != OfferStatus.PROVISIONALLY_SELECTED) {
            throw new InvalidStateException(
                    "Offer is not in provisional selection state (status: " + offer.getStatus() + ")");
        }

        // Check if 15-minute deadline expired
        if (LocalDateTime.now().isAfter(offer.getNominationDeadline())) {
            offer.setStatus(OfferStatus.NOMINATION_FAILED);
            offerRepository.save(offer);
            throw new InvalidStateException("15-minute nomination deadline has expired");
        }

        ServiceRequest request = offer.getRequest();
        LocalDate scheduledDate = request.getScheduledDate();

        // Automated Validation 1: Verify Vehicle / Resource
        Resource resource = resourceRepository.findById(nominationDto.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));

        if (!resource.getProvider().getId().equals(provider.getId())) {
            throw new InvalidStateException("Resource does not belong to provider");
        }
        if (resource.getApprovalStatus() != VerificationStatus.APPROVED || !resource.isActive()) {
            throw new InvalidStateException("Resource is not approved or active");
        }

        // Automated Validation 2: Check Vehicle Schedule Conflicts
        if (allocationRepository.hasResourceConflict(resource.getId(), scheduledDate)) {
            offer.setStatus(OfferStatus.NOMINATION_FAILED);
            offerRepository.save(offer);
            throw new ConcurrencyConflictException("Vehicle " + resource.getTitle()
                    + " has an active conflicting confirmed booking on " + scheduledDate);
        }

        // Automated Validation 3: Verify Driver
        Driver driver = driverRepository.findById(nominationDto.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        if (!driver.getProvider().getId().equals(provider.getId())) {
            throw new InvalidStateException("Driver does not belong to provider roster");
        }
        if (!driver.isVerified() || !driver.isActive()) {
            throw new InvalidStateException("Driver is not verified or active");
        }
        if (driver.getLicenseExpiry().isBefore(scheduledDate)) {
            throw new InvalidStateException("Driver commercial license will be expired on scheduled trip date");
        }

        // Automated Validation 4: Check Driver Schedule Conflicts
        if (allocationRepository.hasDriverConflict(driver.getId(), scheduledDate)) {
            offer.setStatus(OfferStatus.NOMINATION_FAILED);
            offerRepository.save(offer);
            throw new ConcurrencyConflictException("Driver " + driver.getFullName()
                    + " has an active conflicting confirmed booking on " + scheduledDate);
        }

        // Validation passed: update offer to NOMINATION_CONFIRMED
        offer.setNominatedResourceId(resource.getId());
        offer.setNominatedDriverId(driver.getId());
        offer.setStatus(OfferStatus.NOMINATION_CONFIRMED);
        ProviderOffer saved = offerRepository.save(offer);

        auditService.logEvent(providerUserId, "PROVIDER_OFFER", offer.getId().toString(), "NOMINATION_PASS",
                "PROVISIONALLY_SELECTED", "NOMINATION_CONFIRMED", "Vehicle (" + resource.getTitle() + ") and Driver ("
                        + driver.getFullName() + ") validated successfully");

        notificationService.sendNotification(
                request.getCustomer().getPhone(),
                "NOMINATION_CONFIRMED",
                "Provider has nominated vehicle and verified driver for request " + request.getReferenceNumber()
                        + ". Please confirm booking.");

        return mapToDto(saved);
    }

    public List<ProviderOfferResponseDto> getOffersForRequest(Long requestId) {
        return offerRepository.findByRequestIdOrderByCreatedAtAsc(requestId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ProviderOfferResponseDto> getOffersForProvider(Long providerUserId) {
        Provider provider = providerRepository.findByUserId(providerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));
        return offerRepository.findByProviderIdOrderByCreatedAtDesc(provider.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ProviderOfferResponseDto mapToDto(ProviderOffer offer) {
        ProviderOfferResponseDto dto = new ProviderOfferResponseDto();
        dto.setId(offer.getId());
        dto.setRequestId(offer.getRequest().getId());
        dto.setProviderId(offer.getProvider().getId());
        dto.setProviderBusinessName(offer.getProvider().getBusinessName());
        dto.setProviderRating(offer.getProvider().getRatingAverage());
        dto.setOfferedQuantity(offer.getOfferedQuantity());
        dto.setQuotedAmount(offer.getQuotedAmount());
        dto.setIncludesDriver(offer.isIncludesDriver());
        dto.setStatus(offer.getStatus());
        dto.setNominationDeadline(offer.getNominationDeadline());
        dto.setExpiresAt(offer.getExpiresAt());
        dto.setCreatedAt(offer.getCreatedAt());

        // Privacy rule: Only reveal nominated resource & driver details when confirmed
        if (offer.getStatus() == OfferStatus.ACCEPTED || offer.getStatus() == OfferStatus.NOMINATION_CONFIRMED) {
            if (offer.getNominatedResourceId() != null) {
                resourceRepository.findById(offer.getNominatedResourceId())
                        .ifPresent(r -> {
                            dto.setNominatedResourceId(r.getId());
                            dto.setNominatedResourceTitle(r.getTitle());
                            dto.setNominatedVehiclePlate(r.getRegistrationNumber());
                        });
            }
            if (offer.getStatus() == OfferStatus.ACCEPTED && offer.getNominatedDriverId() != null) {
                driverRepository.findById(offer.getNominatedDriverId())
                        .ifPresent(d -> {
                            dto.setAssignedDriverName(d.getFullName());
                            dto.setAssignedDriverPhone(d.getPhone());
                        });
            }
        }

        return dto;
    }
}
