package com.localconnect.repository;

import com.localconnect.domain.Resource;
import com.localconnect.domain.enums.ResourceCategory;
import com.localconnect.domain.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByProviderId(Long providerId);

    List<Resource> findByProviderIdAndIsActiveTrue(Long providerId);

    @Query("SELECT r FROM Resource r WHERE r.category = :category AND r.approvalStatus = 'APPROVED' AND r.isActive = true")
    List<Resource> findActiveByCategory(@Param("category") ResourceCategory category);

    @Query("SELECT r FROM Resource r WHERE r.category = :category AND r.subType = :subType AND r.approvalStatus = 'APPROVED' AND r.isActive = true")
    List<Resource> findActiveByCategoryAndSubType(@Param("category") ResourceCategory category,
            @Param("subType") String subType);

    List<Resource> findByApprovalStatus(VerificationStatus status);
}
