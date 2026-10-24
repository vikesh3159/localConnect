package com.localconnect.controller;

import com.localconnect.domain.User;
import com.localconnect.dto.RequestRemedyDto;
import com.localconnect.dto.ServiceRequestCreateDto;
import com.localconnect.dto.ServiceRequestResponseDto;
import com.localconnect.service.RequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/requests")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ResponseEntity<ServiceRequestResponseDto> createRequest(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ServiceRequestCreateDto dto) {
        ServiceRequestResponseDto created = requestService.createRequest(user.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ServiceRequestResponseDto>> getMyRequests(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(requestService.getMyRequests(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceRequestResponseDto> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.getRequestById(id));
    }

    @PostMapping("/{id}/remedy")
    public ResponseEntity<ServiceRequestResponseDto> applyRemedy(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody RequestRemedyDto remedyDto) {
        return ResponseEntity.ok(requestService.applyRemedy(user.getId(), id, remedyDto));
    }
}
