package com.localconnect.controller;

import com.localconnect.domain.User;
import com.localconnect.domain.enums.DocumentType;
import com.localconnect.dto.DriverDto;
import com.localconnect.dto.ProviderProfileDto;
import com.localconnect.dto.ResourceDto;
import com.localconnect.service.DocumentService;
import com.localconnect.service.ProviderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/provider")
public class ProviderController {

    private final ProviderService providerService;
    private final DocumentService documentService;

    public ProviderController(ProviderService providerService, DocumentService documentService) {
        this.providerService = providerService;
        this.documentService = documentService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ProviderProfileDto> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(providerService.getProfileByUserId(user.getId()));
    }

    @PutMapping("/profile")
    public ResponseEntity<ProviderProfileDto> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ProviderProfileDto dto) {
        return ResponseEntity.ok(providerService.updateProfile(user.getId(), dto));
    }

    @PostMapping("/resources")
    public ResponseEntity<ResourceDto> addResource(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ResourceDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(providerService.addResource(user.getId(), dto));
    }

    @GetMapping("/resources")
    public ResponseEntity<List<ResourceDto>> getResources(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(providerService.getProviderResources(user.getId()));
    }

    @PostMapping("/drivers")
    public ResponseEntity<DriverDto> addDriver(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody DriverDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(providerService.addDriver(user.getId(), dto));
    }

    @GetMapping("/drivers")
    public ResponseEntity<List<DriverDto>> getDrivers(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(providerService.getProviderDrivers(user.getId()));
    }

    @PostMapping("/documents")
    public ResponseEntity<Map<String, String>> uploadDocument(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> body) {
        DocumentType type = DocumentType.valueOf(body.get("documentType"));
        String fileName = body.get("fileName");
        String filePath = "/secure_storage/" + user.getId() + "_" + fileName;
        Long resourceId = body.get("resourceId") != null ? Long.parseLong(body.get("resourceId")) : null;
        Long driverId = body.get("driverId") != null ? Long.parseLong(body.get("driverId")) : null;

        documentService.uploadDocument(user.getId(), type, fileName, filePath, resourceId, driverId);
        return ResponseEntity.ok(Map.of("message", "Document uploaded successfully and queued for admin verification"));
    }
}
