package com.localconnect.controller;

import com.localconnect.domain.User;
import com.localconnect.dto.BookingAllocationResponseDto;
import com.localconnect.service.AllocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/allocations")
public class AllocationController {

    private final AllocationService allocationService;

    public AllocationController(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    /**
     * Stage 3: Customer final confirms booking allocation.
     * Generates confirmation code, locks inventory, and reveals driver credentials.
     */
    @PostMapping("/confirm/{offerId}")
    public ResponseEntity<BookingAllocationResponseDto> confirmAllocation(
            @AuthenticationPrincipal User user,
            @PathVariable Long offerId
    ) {
        return ResponseEntity.ok(allocationService.confirmAllocation(user.getId(), offerId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingAllocationResponseDto>> getMyAllocations(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(allocationService.getCustomerAllocations(user.getId()));
    }

    @GetMapping("/provider")
    public ResponseEntity<List<BookingAllocationResponseDto>> getProviderAllocations(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(allocationService.getProviderAllocations(user.getId()));
    }
}

