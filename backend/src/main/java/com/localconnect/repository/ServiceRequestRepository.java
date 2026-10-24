package com.localconnect.repository;

import com.localconnect.domain.ServiceRequest;
import com.localconnect.domain.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    Optional<ServiceRequest> findByReferenceNumber(String referenceNumber);

    @Query("SELECT r FROM ServiceRequest r WHERE r.status IN ('BROADCASTED', 'OFFERS_RECEIVED') AND r.broadcastExpiresAt <= :now")
    List<ServiceRequest> findExpiredRequests(@Param("now") LocalDateTime now);
}
