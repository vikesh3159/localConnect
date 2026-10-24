package com.localconnect.repository;

import com.localconnect.domain.BookingAllocation;
import com.localconnect.domain.enums.AllocationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingAllocationRepository extends JpaRepository<BookingAllocation, Long> {
    List<BookingAllocation> findByRequestId(Long requestId);

    List<BookingAllocation> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<BookingAllocation> findByProviderIdOrderByCreatedAtDesc(Long providerId);

    Optional<BookingAllocation> findByConfirmationCode(String confirmationCode);

    @Query("SELECT COALESCE(SUM(b.allocatedQuantity), 0) FROM BookingAllocation b WHERE b.request.id = :requestId AND b.status = 'CONFIRMED'")
    int sumAllocatedQuantityByRequestId(@Param("requestId") Long requestId);

    @Query("SELECT COUNT(b) > 0 FROM BookingAllocation b WHERE b.resource.id = :resourceId AND b.request.scheduledDate = :scheduledDate AND b.status = 'CONFIRMED'")
    boolean hasResourceConflict(@Param("resourceId") Long resourceId, @Param("scheduledDate") LocalDate scheduledDate);

    @Query("SELECT COUNT(b) > 0 FROM BookingAllocation b WHERE b.driver.id = :driverId AND b.request.scheduledDate = :scheduledDate AND b.status = 'CONFIRMED'")
    boolean hasDriverConflict(@Param("driverId") Long driverId, @Param("scheduledDate") LocalDate scheduledDate);
}
