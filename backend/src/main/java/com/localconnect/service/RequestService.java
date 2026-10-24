package com.localconnect.service;

import com.localconnect.domain.BookingAllocation;
import com.localconnect.domain.Provider;
import com.localconnect.domain.ProviderOffer;
import com.localconnect.domain.RequestInvitation;
import com.localconnect.domain.Resource;
import com.localconnect.domain.ServiceRequest;
import com.localconnect.domain.User;
import com.localconnect.domain.enums.OfferStatus;
import com.localconnect.domain.enums.RequestStatus;
import com.localconnect.domain.enums.VerificationStatus;
import com.localconnect.dto.ProviderOfferResponseDto;
import com.localconnect.dto.RequestRemedyDto;
import com.localconnect.dto.ServiceRequestCreateDto;
import com.localconnect.dto.ServiceRequestResponseDto;
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
public class RequestService {

    private final ServiceRequestRepository requestRepository;
    private final RequestInvitationRepository invitationRepository;
    private final ProviderOfferRepository offerRepository;
    private final BookingAllocationRepository allocationRepository;
    private final ProviderRepository providerRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final BroadcastPolicyService policyService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public RequestService(
            ServiceRequestRepository requestRepository,
            RequestInvitationRepository invitationRepository,
            ProviderOfferRepository offerRepository,
            BookingAllocationRepository allocationRepository,
            ProviderRepository providerRepository,
            ResourceRepository resourceRepository,
            UserRepository userRepository,
            BroadcastPolicyService policyService,
            NotificationService notificationService,
            AuditService auditService) {
        this.requestRepository = requestRepository;
        this.invitationRepository = invitationRepository;
        this.offerRepository = offerRepository;
        this.allocationRepository = allocationRepository;
        this.providerRepository = providerRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.policyService = policyService;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    @Transactional
    public ServiceRequestResponseDto createRequest(Long customerId, ServiceRequestCreateDto dto) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = policyService.calculateExpiration(
                dto.getCategory(),
                dto.getUrgencyLevel(),
                dto.getScheduledDate(),
                dto.getScheduledTime(),
                now);

        ServiceRequest request = new ServiceRequest();
        request.setCustomer(customer);
        request.setReferenceNumber("REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        request.setCategory(dto.getCategory());
        request.setUrgencyLevel(dto.getUrgencyLevel());
        request.setQuantityRequired(dto.getQuantityRequired());
        request.setPickupLocation(dto.getPickupLocation());
        request.setDestinationLocation(dto.getDestinationLocation());
        request.setScheduledDate(dto.getScheduledDate());
        request.setScheduledTime(dto.getScheduledTime());
        request.setStayType(dto.getStayType());
        request.setGoodsType(dto.getGoodsType());
        request.setSpecialInstructions(dto.getSpecialInstructions());
        request.setStatus(RequestStatus.BROADCASTED);
        request.setBroadcastWindowHours(policyService.getVehicleTransportTtlHours());
        request.setBroadcastExpiresAt(expiresAt);
        request.setSearchRadiusKm(dto.getSearchRadiusKm() != null ? dto.getSearchRadiusKm() : 15);

        ServiceRequest saved = requestRepository.save(request);

        // Fan out invitations to eligible verified providers
        List<Provider> eligibleProviders = providerRepository.findByVerificationStatus(VerificationStatus.APPROVED);
        int invitedCount = 0;
        for (Provider provider : eligibleProviders) {
            RequestInvitation invitation = new RequestInvitation(saved, provider);
            invitationRepository.save(invitation);
            notificationService.sendNotification(
                    provider.getUser().getPhone(),
                    "BROADCAST_ALERT",
                    "New " + saved.getCategory() + " request near " + saved.getPickupLocation() +
                            ". Quantity: " + saved.getQuantityRequired() + ". Submit your quote before "
                            + expiresAt.toLocalTime());
            invitedCount++;
        }

        auditService.logEvent(customerId, "SERVICE_REQUEST", saved.getId().toString(), "CREATE_BROADCAST", null,
                "BROADCASTED",
                "Request broadcasted to " + invitedCount + " providers. Expiration: " + expiresAt);

        notificationService.sendNotification(
                customer.getPhone(),
                "REQUEST_CONFIRMATION",
                "Your request " + saved.getReferenceNumber() + " has been broadcasted to " + invitedCount +
                        " local providers. Active response window until " + expiresAt);

        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestResponseDto> getMyRequests(Long customerId) {
        return requestRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServiceRequestResponseDto getRequestById(Long requestId) {
        ServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
        return mapToDto(request);
    }

    @Transactional
    public ServiceRequestResponseDto applyRemedy(Long customerId, Long requestId, RequestRemedyDto remedyDto) {
        ServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (!request.getCustomer().getId().equals(customerId)) {
            throw new InvalidStateException("Unauthorized to modify this request");
        }

        String stateBefore = request.getStatus().name();
        LocalDateTime now = LocalDateTime.now();

        switch (remedyDto.getAction().toUpperCase()) {
            case "EXTEND_WINDOW":
                int hours = (remedyDto.getAdditionalHours() != null) ? remedyDto.getAdditionalHours() : 4;
                request.setBroadcastExpiresAt(now.plusHours(hours));
                request.setStatus(RequestStatus.BROADCASTED);
                auditService.logEvent(customerId, "SERVICE_REQUEST", request.getId().toString(), "EXTEND_WINDOW",
                        stateBefore, "BROADCASTED", "Response window extended by " + hours + " hours");
                break;

            case "EXPAND_RADIUS":
                int addRadius = (remedyDto.getAdditionalRadiusKm() != null) ? remedyDto.getAdditionalRadiusKm() : 10;
                request.setSearchRadiusKm(request.getSearchRadiusKm() + addRadius);
                request.setBroadcastExpiresAt(now.plusHours(4));
                request.setStatus(RequestStatus.BROADCASTED);
                auditService.logEvent(customerId, "SERVICE_REQUEST", request.getId().toString(), "EXPAND_RADIUS",
                        stateBefore, "BROADCASTED",
                        "Radius expanded to " + request.getSearchRadiusKm() + " km and rebroadcast");
                break;

            case "MODIFY_DETAILS":
                if (remedyDto.getModifiedQuantity() != null) {
                    request.setQuantityRequired(remedyDto.getModifiedQuantity());
                }
                if (remedyDto.getModifiedDate() != null) {
                    request.setScheduledDate(remedyDto.getModifiedDate());
                }
                if (remedyDto.getModifiedTime() != null) {
                    request.setScheduledTime(remedyDto.getModifiedTime());
                }
                if (remedyDto.getModifiedInstructions() != null) {
                    request.setSpecialInstructions(remedyDto.getModifiedInstructions());
                }
                request.setBroadcastExpiresAt(policyService.calculateExpiration(
                        request.getCategory(), request.getUrgencyLevel(), request.getScheduledDate(),
                        request.getScheduledTime(), now));
                request.setStatus(RequestStatus.BROADCASTED);
                auditService.logEvent(customerId, "SERVICE_REQUEST", request.getId().toString(), "MODIFY_DETAILS",
                        stateBefore, "BROADCASTED", "Request criteria modified and rebroadcast");
                break;

            case "CANCEL_REQUEST":
                request.setStatus(RequestStatus.CANCELLED);
                auditService.logEvent(customerId, "SERVICE_REQUEST", request.getId().toString(), "CANCEL_REQUEST",
                        stateBefore, "CANCELLED", "Request cancelled by customer without platform penalty");
                break;

            default:
                throw new InvalidStateException("Unknown remediation action: " + remedyDto.getAction());
        }

        ServiceRequest updated = requestRepository.save(request);
        return mapToDto(updated);
    }

    public ServiceRequestResponseDto mapToDto(ServiceRequest request) {
        ServiceRequestResponseDto dto = new ServiceRequestResponseDto();
        dto.setId(request.getId());
        dto.setReferenceNumber(request.getReferenceNumber());
        dto.setCustomerId(request.getCustomer().getId());
        dto.setCustomerPhone(request.getCustomer().getPhone());
        dto.setCategory(request.getCategory());
        dto.setUrgencyLevel(request.getUrgencyLevel());
        dto.setQuantityRequired(request.getQuantityRequired());
        dto.setPickupLocation(request.getPickupLocation());
        dto.setDestinationLocation(request.getDestinationLocation());
        dto.setScheduledDate(request.getScheduledDate());
        dto.setScheduledTime(request.getScheduledTime());
        dto.setStayType(request.getStayType());
        dto.setGoodsType(request.getGoodsType());
        dto.setSpecialInstructions(request.getSpecialInstructions());
        dto.setStatus(request.getStatus());
        dto.setBroadcastWindowHours(request.getBroadcastWindowHours());
        dto.setBroadcastExpiresAt(request.getBroadcastExpiresAt());
        dto.setExpired(LocalDateTime.now().isAfter(request.getBroadcastExpiresAt()));
        dto.setSearchRadiusKm(request.getSearchRadiusKm());
        dto.setCreatedAt(request.getCreatedAt());

        int allocated = allocationRepository.sumAllocatedQuantityByRequestId(request.getId());
        dto.setAllocatedQuantity(allocated);

        List<ProviderOffer> offers = offerRepository.findByRequestIdOrderByCreatedAtAsc(request.getId());
        dto.setOffersCount(offers.size());
        dto.setOffers(offers.stream().map(this::mapOfferToDto).collect(Collectors.toList()));

        return dto;
    }

    private ProviderOfferResponseDto mapOfferToDto(ProviderOffer offer) {
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
        if (offer.getStatus() == OfferStatus.ACCEPTED) {
            if (offer.getNominatedResourceId() != null) {
                resourceRepository.findById(offer.getNominatedResourceId())
                        .ifPresent(r -> {
                            dto.setNominatedResourceId(r.getId());
                            dto.setNominatedResourceTitle(r.getTitle());
                            dto.setNominatedVehiclePlate(r.getRegistrationNumber());
                        });
            }
        }

        return dto;
    }
}
