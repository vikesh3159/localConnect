package com.localconnect.service;

import com.localconnect.domain.enums.ResourceCategory;
import com.localconnect.domain.enums.UrgencyLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Service
public class BroadcastPolicyService {

    @Value("${localconnect.policy.vehicle-transport-ttl-hours:4}")
    private int vehicleTransportTtlHours;

    @Value("${localconnect.policy.room-ttl-hours:4}")
    private int roomTtlHours;

    @Value("${localconnect.policy.urgent-ttl-minutes:30}")
    private int urgentTtlMinutes;

    @Value("${localconnect.policy.emergency-ttl-minutes:15}")
    private int emergencyTtlMinutes;

    @Value("${localconnect.policy.night-pause-start-hour:21}")
    private int nightPauseStartHour; // 9:00 PM

    @Value("${localconnect.policy.night-pause-end-hour:9}")
    private int nightPauseEndHour; // 9:00 AM

    /**
     * Calculates the exact broadcast expiration timestamp according to the approved
     * rules:
     * - Urgent/Emergency: Active 24/7 (15-30m window).
     * - Normal Vehicle/Transport/Rooms: 4-hour window during active hours.
     * - Overnight requests (submitted between 9:00 PM and 9:00 AM): clock starts at
     * 9:00 AM next morning.
     */
    public LocalDateTime calculateExpiration(
            ResourceCategory category,
            UrgencyLevel urgencyLevel,
            LocalDate scheduledDate,
            LocalTime scheduledTime,
            LocalDateTime now) {
        // 1. Emergency requests have special fast-response window (15 minutes, 24/7)
        if (category == ResourceCategory.EMERGENCY || urgencyLevel == UrgencyLevel.EMERGENCY) {
            return now.plusMinutes(emergencyTtlMinutes);
        }

        // 2. Urgent requests: Check if need is within 3 hours
        LocalDateTime scheduledDateTime = LocalDateTime.of(scheduledDate, scheduledTime);
        boolean isImmediateNeed = ChronoUnit.HOURS.between(now, scheduledDateTime) <= 3
                && ChronoUnit.HOURS.between(now, scheduledDateTime) >= 0;

        if (urgencyLevel == UrgencyLevel.URGENT || isImmediateNeed) {
            return now.plusMinutes(urgentTtlMinutes);
        }

        // 3. Normal TTL calculation with overnight pause
        int windowHours = (category == ResourceCategory.ROOM) ? roomTtlHours : vehicleTransportTtlHours;

        // Check if created during overnight pause window (>= 21:00 or < 09:00)
        int currentHour = now.getHour();
        if (currentHour >= nightPauseStartHour) {
            // Submitted after 9 PM: active window begins 9:00 AM next morning
            return now.toLocalDate().plusDays(1).atTime(nightPauseEndHour, 0).plusHours(windowHours);
        } else if (currentHour < nightPauseEndHour) {
            // Submitted before 9 AM: active window begins 9:00 AM today
            return now.toLocalDate().atTime(nightPauseEndHour, 0).plusHours(windowHours);
        } else {
            // Submitted during daytime: add windowHours. If crosses 9 PM, pause remaining
            // until 9 AM next morning
            LocalDateTime simpleExpiration = now.plusHours(windowHours);
            if (simpleExpiration.getHour() >= nightPauseStartHour) {
                long hoursBeforeNight = ChronoUnit.HOURS.between(now, now.toLocalDate().atTime(nightPauseStartHour, 0));
                long remainingHours = windowHours - hoursBeforeNight;
                return now.toLocalDate().plusDays(1).atTime(nightPauseEndHour, 0).plusHours(remainingHours);
            }
            return simpleExpiration;
        }
    }

    public int getVehicleTransportTtlHours() {
        return vehicleTransportTtlHours;
    }

    public int getRoomTtlHours() {
        return roomTtlHours;
    }

    public int getUrgentTtlMinutes() {
        return urgentTtlMinutes;
    }

    public int getEmergencyTtlMinutes() {
        return emergencyTtlMinutes;
    }
}
