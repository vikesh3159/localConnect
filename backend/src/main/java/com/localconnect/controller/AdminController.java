package com.localconnect.controller;

import com.localconnect.domain.ProviderDocument;
import com.localconnect.domain.User;
import com.localconnect.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final DocumentService documentService;

    public AdminController(DocumentService documentService) {
        this.documentService = documentService;
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
}
