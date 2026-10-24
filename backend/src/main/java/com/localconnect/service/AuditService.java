package com.localconnect.service;

import com.localconnect.domain.AuditEvent;
import com.localconnect.repository.AuditEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional
    public void logEvent(Long userId, String entityType, String entityId, String action, String stateBefore,
            String stateAfter, String details) {
        AuditEvent event = new AuditEvent(userId, entityType, entityId, action, stateBefore, stateAfter, details);
        auditEventRepository.save(event);
    }
}
