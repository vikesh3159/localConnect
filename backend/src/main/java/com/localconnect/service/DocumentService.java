package com.localconnect.service;

import com.localconnect.domain.Driver;
import com.localconnect.domain.Provider;
import com.localconnect.domain.ProviderDocument;
import com.localconnect.domain.Resource;
import com.localconnect.domain.enums.DocumentType;
import com.localconnect.domain.enums.VerificationStatus;
import com.localconnect.exception.ResourceNotFoundException;
import com.localconnect.repository.DriverRepository;
import com.localconnect.repository.ProviderDocumentRepository;
import com.localconnect.repository.ProviderRepository;
import com.localconnect.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentService {

    private final ProviderDocumentRepository documentRepository;
    private final ProviderRepository providerRepository;
    private final ResourceRepository resourceRepository;
    private final DriverRepository driverRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public DocumentService(
            ProviderDocumentRepository documentRepository,
            ProviderRepository providerRepository,
            ResourceRepository resourceRepository,
            DriverRepository driverRepository,
            NotificationService notificationService,
            AuditService auditService) {
        this.documentRepository = documentRepository;
        this.providerRepository = providerRepository;
        this.resourceRepository = resourceRepository;
        this.driverRepository = driverRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    @Transactional
    public ProviderDocument uploadDocument(Long providerUserId, DocumentType type, String fileName, String storagePath,
            Long resourceId, Long driverId) {
        Provider provider = providerRepository.findByUserId(providerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        ProviderDocument doc = new ProviderDocument();
        doc.setProvider(provider);
        doc.setDocumentType(type);
        doc.setFileName(fileName);
        doc.setFilePath(storagePath);
        doc.setResourceId(resourceId);
        doc.setDriverId(driverId);
        doc.setVerificationStatus(VerificationStatus.PENDING_APPROVAL);

        ProviderDocument saved = documentRepository.save(doc);
        auditService.logEvent(providerUserId, "DOCUMENT", saved.getId().toString(), "UPLOAD_DOC", null,
                "PENDING_APPROVAL", "Uploaded: " + type);
        return saved;
    }

    public List<ProviderDocument> getPendingDocuments() {
        return documentRepository.findByVerificationStatus(VerificationStatus.PENDING_APPROVAL);
    }

    @Transactional
    public ProviderDocument reviewDocument(Long documentId, boolean approved, String rejectionReason,
            String adminUsername) {
        ProviderDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        doc.setVerificationStatus(approved ? VerificationStatus.APPROVED : VerificationStatus.REJECTED);
        doc.setRejectionReason(approved ? null : rejectionReason);
        doc.setReviewedBy(adminUsername);
        doc.setReviewedAt(LocalDateTime.now());

        ProviderDocument saved = documentRepository.save(doc);

        // If driver license approved, verify driver
        if (approved && doc.getDriverId() != null) {
            driverRepository.findById(doc.getDriverId()).ifPresent(d -> {
                d.setVerified(true);
                driverRepository.save(d);
            });
        }

        // If vehicle RC approved, approve resource
        if (approved && doc.getResourceId() != null) {
            resourceRepository.findById(doc.getResourceId()).ifPresent(r -> {
                r.setApprovalStatus(VerificationStatus.APPROVED);
                resourceRepository.save(r);
            });
        }

        // Check if provider has approved documents, update provider status
        Provider provider = doc.getProvider();
        if (approved && provider.getVerificationStatus() == VerificationStatus.PENDING_APPROVAL) {
            provider.setVerificationStatus(VerificationStatus.APPROVED);
            provider.setVerifiedAt(LocalDateTime.now());
            providerRepository.save(provider);
        }

        auditService.logEvent(null, "DOCUMENT", doc.getId().toString(), "ADMIN_REVIEW", "PENDING_APPROVAL",
                doc.getVerificationStatus().name(),
                "Review by " + adminUsername + (approved ? " - Approved" : " - Rejected: " + rejectionReason));

        notificationService.sendNotification(
                provider.getUser().getPhone(),
                "DOCUMENT_STATUS",
                "Your document (" + doc.getDocumentType() + ") has been "
                        + (approved ? "APPROVED." : "REJECTED. Reason: " + rejectionReason));

        return saved;
    }
}
