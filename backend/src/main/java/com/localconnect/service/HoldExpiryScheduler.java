package com.localconnect.service;

import com.localconnect.domain.ProviderOffer;
import com.localconnect.domain.ServiceRequest;
import com.localconnect.domain.enums.OfferStatus;
import com.localconnect.domain.enums.RequestStatus;
import com.localconnect.repository.ProviderOfferRepository;
import com.localconnect.repository.ServiceRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HoldExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(HoldExpiryScheduler.class);

    private final ProviderOfferRepository offerRepository;
    private final ServiceRequestRepository requestRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public HoldExpiryScheduler(
            ProviderOfferRepository offerRepository,
            ServiceRequestRepository requestRepository,
            NotificationService notificationService,
            AuditService auditService) {
        this.offerRepository = offerRepository;
        this.requestRepository = requestRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    /**
     * Checks for expired 15-minute provisional reservation holds every 30 seconds.
     */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void processExpiredProvisionalHolds() {
        LocalDateTime now = LocalDateTime.now();
        List<ProviderOffer> expiredHolds = offerRepository.findExpiredProvisionalHolds(now);

        for (ProviderOffer offer : expiredHolds) {
            log.info("Provisional hold expired for Offer ID {} (Deadline: {})", offer.getId(),
                    offer.getNominationDeadline());
            offer.setStatus(OfferStatus.NOMINATION_FAILED);
            offerRepository.save(offer);

            ServiceRequest request = offer.getRequest();
            if (request.getStatus() == RequestStatus.PROVISIONALLY_HOLDING) {
                request.setStatus(RequestStatus.OFFERS_RECEIVED);
                requestRepository.save(request);
            }

            auditService.logEvent(null, "PROVIDER_OFFER", offer.getId().toString(), "HOLD_EXPIRED",
                    "PROVISIONALLY_SELECTED", "NOMINATION_FAILED",
                    "15-minute nomination deadline elapsed without valid driver nomination");

            notificationService.sendNotification(
                    request.getCustomer().getPhone(),
                    "HOLD_RELEASED",
                    "A provider quote for " + request.getReferenceNumber()
                            + " expired during driver assignment. Your request remains open to other offers.");
        }
    }

    /**
     * Checks for expired broadcast windows every minute.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processExpiredBroadcasts() {
        LocalDateTime now = LocalDateTime.now();
        List<ServiceRequest> expiredRequests = requestRepository.findExpiredRequests(now);

        for (ServiceRequest request : expiredRequests) {
            log.info("Broadcast window expired for Request ID {} ({})", request.getId(), request.getReferenceNumber());
            String stateBefore = request.getStatus().name();
            request.setStatus(RequestStatus.EXPIRED);
            requestRepository.save(request);

            auditService.logEvent(null, "SERVICE_REQUEST", request.getId().toString(), "BROADCAST_EXPIRED",
                    stateBefore, "EXPIRED", "Broadcast window closed at " + request.getBroadcastExpiresAt());

            notificationService.sendNotification(
                    request.getCustomer().getPhone(),
                    "BROADCAST_EXPIRED",
                    "Response window closed for request " + request.getReferenceNumber() +
                            ". Open My Requests to extend time, expand search radius, or edit details.");
        }
    }
}
