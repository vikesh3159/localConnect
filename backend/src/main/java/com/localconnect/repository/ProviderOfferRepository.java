package com.localconnect.repository;

import com.localconnect.domain.ProviderOffer;
import com.localconnect.domain.enums.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProviderOfferRepository extends JpaRepository<ProviderOffer, Long> {
    List<ProviderOffer> findByRequestIdOrderByCreatedAtAsc(Long requestId);

    List<ProviderOffer> findByProviderIdOrderByCreatedAtDesc(Long providerId);

    @Query("SELECT o FROM ProviderOffer o WHERE o.status = 'PROVISIONALLY_SELECTED' AND o.nominationDeadline <= :now")
    List<ProviderOffer> findExpiredProvisionalHolds(@Param("now") LocalDateTime now);

    @Query("SELECT o FROM ProviderOffer o WHERE o.status = 'SUBMITTED' AND o.expiresAt <= :now")
    List<ProviderOffer> findExpiredOffers(@Param("now") LocalDateTime now);
}
