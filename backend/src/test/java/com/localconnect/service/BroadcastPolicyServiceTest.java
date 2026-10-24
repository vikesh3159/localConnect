package com.localconnect.service;

import com.localconnect.domain.enums.ResourceCategory;
import com.localconnect.domain.enums.UrgencyLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class BroadcastPolicyServiceTest {

    private BroadcastPolicyService policyService;

    @BeforeEach
    void setUp() {
        policyService = new BroadcastPolicyService();
        ReflectionTestUtils.setField(policyService, "vehicleTransportTtlHours", 4);
        ReflectionTestUtils.setField(policyService, "roomTtlHours", 4);
        ReflectionTestUtils.setField(policyService, "urgentTtlMinutes", 30);
        ReflectionTestUtils.setField(policyService, "emergencyTtlMinutes", 15);
        ReflectionTestUtils.setField(policyService, "nightPauseStartHour", 21);
        ReflectionTestUtils.setField(policyService, "nightPauseEndHour", 9);
    }

    @Test
    void testStandardVehicleTtlDuringDay() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 15, 10, 0); // 10:00 AM
        LocalDate scheduledDate = LocalDate.of(2026, 11, 20);
        LocalTime scheduledTime = LocalTime.of(8, 0);

        LocalDateTime expiresAt = policyService.calculateExpiration(
                ResourceCategory.VEHICLE, UrgencyLevel.NORMAL, scheduledDate, scheduledTime, now);

        assertEquals(LocalDateTime.of(2026, 11, 15, 14, 0), expiresAt); // Exactly 4 hours later (2:00 PM)
    }

    @Test
    void testUrgentRequestTtl() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 15, 10, 0);
        LocalDate scheduledDate = LocalDate.of(2026, 11, 15);
        LocalTime scheduledTime = LocalTime.of(12, 0); // departure in 2 hours -> Urgent

        LocalDateTime expiresAt = policyService.calculateExpiration(
                ResourceCategory.VEHICLE, UrgencyLevel.NORMAL, scheduledDate, scheduledTime, now);

        assertEquals(LocalDateTime.of(2026, 11, 15, 10, 30), expiresAt); // 30-min urgent window
    }

    @Test
    void testEmergencyFastTtl() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 15, 23, 30); // 11:30 PM
        LocalDate scheduledDate = LocalDate.of(2026, 11, 15);
        LocalTime scheduledTime = LocalTime.of(23, 45);

        LocalDateTime expiresAt = policyService.calculateExpiration(
                ResourceCategory.EMERGENCY, UrgencyLevel.EMERGENCY, scheduledDate, scheduledTime, now);

        assertEquals(LocalDateTime.of(2026, 11, 15, 23, 45), expiresAt); // 15-min emergency window active 24/7
    }

    @Test
    void testOvernightPauseLogic() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 15, 22, 15); // 10:15 PM (after 9 PM)
        LocalDate scheduledDate = LocalDate.of(2026, 11, 25);
        LocalTime scheduledTime = LocalTime.of(10, 0);

        LocalDateTime expiresAt = policyService.calculateExpiration(
                ResourceCategory.VEHICLE, UrgencyLevel.NORMAL, scheduledDate, scheduledTime, now);

        // Active countdown starts at 9:00 AM next day + 4 hours = 1:00 PM next day
        assertEquals(LocalDateTime.of(2026, 11, 16, 13, 0), expiresAt);
    }
}
