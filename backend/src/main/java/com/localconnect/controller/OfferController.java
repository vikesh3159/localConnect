package com.localconnect.controller;

import com.localconnect.domain.User;
import com.localconnect.dto.NominationDto;
import com.localconnect.dto.ProviderOfferCreateDto;
import com.localconnect.dto.ProviderOfferResponseDto;
import com.localconnect.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/offers")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    /**
     * Stage 1: Provider submits quotation (driver details are withheld)
     */
    @PostMapping
    public ResponseEntity<ProviderOfferResponseDto> submitQuotation(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ProviderOfferCreateDto dto) {
        ProviderOfferResponseDto created = offerService.submitQuotation(user.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/request/{requestId}")
    public ResponseEntity<List<ProviderOfferResponseDto>> getOffersForRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(offerService.getOffersForRequest(requestId));
    }

    @GetMapping("/provider")
    public ResponseEntity<List<ProviderOfferResponseDto>> getOffersForProvider(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(offerService.getOffersForProvider(user.getId()));
    }

    /**
     * Stage 2: Customer provisionally selects offer (places 15-minute reservation
     * hold)
     */
    @PostMapping("/{id}/select")
    public ResponseEntity<ProviderOfferResponseDto> selectOfferProvisionally(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(offerService.selectOfferProvisionally(user.getId(), id));
    }

    /**
     * Stage 2b: Provider nominates specific vehicle and driver (automated
     * validation checks)
     */
    @PostMapping("/{id}/nominate")
    public ResponseEntity<ProviderOfferResponseDto> nominateResource(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody NominationDto nominationDto) {
        return ResponseEntity.ok(offerService.nominateResource(user.getId(), id, nominationDto));
    }
}
