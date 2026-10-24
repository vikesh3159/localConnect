package com.localconnect.repository;

import com.localconnect.domain.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    List<Driver> findByProviderIdAndIsActiveTrue(Long providerId);

    Optional<Driver> findByIdAndProviderId(Long id, Long providerId);

    List<Driver> findByProviderIdAndIsVerifiedTrueAndIsActiveTrue(Long providerId);
}
