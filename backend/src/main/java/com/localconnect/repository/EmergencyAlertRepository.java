package com.localconnect.repository;

import com.localconnect.domain.EmergencyAlert;
import com.localconnect.domain.enums.EmergencyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyAlertRepository extends JpaRepository<EmergencyAlert, Long> {
    List<EmergencyAlert> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<EmergencyAlert> findByStatusOrderByCreatedAtDesc(EmergencyStatus status);
}
