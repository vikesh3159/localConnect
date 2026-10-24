package com.localconnect.repository;

import com.localconnect.domain.RequestInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestInvitationRepository extends JpaRepository<RequestInvitation, Long> {
    List<RequestInvitation> findByProviderIdOrderBySentAtDesc(Long providerId);

    List<RequestInvitation> findByRequestId(Long requestId);

    Optional<RequestInvitation> findByRequestIdAndProviderId(Long requestId, Long providerId);
}
