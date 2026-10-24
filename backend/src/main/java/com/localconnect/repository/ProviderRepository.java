package com.localconnect.repository;

import com.localconnect.domain.Provider;
import com.localconnect.domain.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {
    Optional<Provider> findByUserId(Long userId);

    List<Provider> findByVerificationStatus(VerificationStatus status);

    List<Provider> findByOperationalDistrictAndVerificationStatus(String district, VerificationStatus status);
}
