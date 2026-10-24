package com.localconnect.controller;

import com.localconnect.domain.Provider;
import com.localconnect.domain.ProviderDocument;
import com.localconnect.domain.User;
import com.localconnect.domain.enums.VerificationStatus;
import com.localconnect.repository.ProviderRepository;
import com.localconnect.repository.UserRepository;
import com.localconnect.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final DocumentService documentService;
    private final ProviderRepository providerRepository;
    private final UserRepository userRepository;

    public AdminController(
            DocumentService documentService,
            ProviderRepository providerRepository,
            UserRepository userRepository) {
        this.documentService = documentService;
        this.providerRepository = providerRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/documents/pending")
    public ResponseEntity<List<ProviderDocument>> getPendingDocuments() {
        return ResponseEntity.ok(documentService.getPendingDocuments());
    }

    @PostMapping("/documents/{id}/review")
    public ResponseEntity<ProviderDocument> reviewDocument(
            @AuthenticationPrincipal User adminUser,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        boolean approved = Boolean.parseBoolean(body.get("approved").toString());
        String rejectionReason = (String) body.get("rejectionReason");
        String adminName = adminUser != null ? adminUser.getFullName() : "Admin";

        return ResponseEntity.ok(documentService.reviewDocument(id, approved, rejectionReason, adminName));
    }

    @GetMapping("/providers")
    public ResponseEntity<List<Map<String, Object>>> getAllProviders() {
        List<Provider> providers = providerRepository.findAll();
        List<Map<String, Object>> result = providers.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("businessName", p.getBusinessName());
            map.put("ownerName", p.getUser() != null ? p.getUser().getFullName() : "N/A");
            map.put("phone", p.getUser() != null ? p.getUser().getPhone() : "N/A");
            map.put("operationalDistrict", p.getOperationalDistrict());
            map.put("operationalTaluka", p.getOperationalTaluka());
            map.put("serviceBase", p.getServiceBase());
            map.put("ratingAverage", p.getRatingAverage());
            map.put("totalCompletedJobs", p.getTotalCompletedJobs());
            map.put("verificationStatus", p.getVerificationStatus().name());
            map.put("createdAt", p.getCreatedAt());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/customers")
    public ResponseEntity<List<Map<String, Object>>> getAllCustomers() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("phone", u.getPhone());
            map.put("fullName", u.getFullName());
            map.put("activeRole", u.getCurrentRole());
            map.put("roles", u.getRoles().stream().map(Enum::name).collect(Collectors.toList()));
            map.put("isActive", u.isActive());
            map.put("preferredLanguage", u.getPreferredLanguage());
            map.put("createdAt", u.getCreatedAt());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/providers/{id}/verify")
    public ResponseEntity<Map<String, Object>> verifyProvider(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found with ID: " + id));
        String statusStr = body.getOrDefault("status", "APPROVED");
        VerificationStatus status = VerificationStatus.valueOf(statusStr.toUpperCase());
        provider.setVerificationStatus(status);
        providerRepository.save(provider);

        return ResponseEntity.ok(Map.of(
                "message", "Provider verification updated to " + status.name(),
                "providerId", id,
                "status", status.name()
        ));
    }
}
