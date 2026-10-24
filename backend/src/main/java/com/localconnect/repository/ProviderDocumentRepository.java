package com.localconnect.repository;

import com.localconnect.domain.ProviderDocument;
import com.localconnect.domain.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderDocumentRepository extends JpaRepository<ProviderDocument, Long> {
    List<ProviderDocument> findByProviderId(Long providerId);

    List<ProviderDocument> findByVerificationStatus(VerificationStatus status);
}
