package com.localconnect.service;

import com.localconnect.domain.EmergencyAlert;
import com.localconnect.domain.Provider;
import com.localconnect.domain.enums.EmergencyStatus;
import com.localconnect.dto.EmergencyAlertDto;
import com.localconnect.dto.EmergencyDispatchDto;
import com.localconnect.exception.InvalidStateException;
import com.localconnect.exception.ResourceNotFoundException;
import com.localconnect.repository.EmergencyAlertRepository;
import com.localconnect.repository.ProviderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class EmergencyService {

        private final EmergencyAlertRepository emergencyAlertRepository;
        private final ProviderRepository providerRepository;
        private final NotificationService notificationService;
        private final AuditService auditService;

        public EmergencyService(
                        EmergencyAlertRepository emergencyAlertRepository,
                        ProviderRepository providerRepository,
                        NotificationService notificationService,
                        AuditService auditService) {
                this.emergencyAlertRepository = emergencyAlertRepository;
                this.providerRepository = providerRepository;
                this.notificationService = notificationService;
                this.auditService = auditService;
        }

        /**
         * Official Indian Emergency Helplines with strict priority.
         */
        public List<Map<String, String>> getOfficialHelplines() {
                return List.of(
                                Map.of("number", "112", "service", "National Emergency (Police, Fire, Medical)",
                                                "description", "All-in-one Emergency Response Support System (ERSS)",
                                                "priority", "1"),
                                Map.of("number", "108", "service", "Emergency Medical / Ambulance", "description",
                                                "State Government Emergency Medical Relief", "priority", "2"),
                                Map.of("number", "102", "service", "Maternity & Neonatal Ambulance", "description",
                                                "Free ambulance for pregnant women & infants", "priority", "3"),
                                Map.of("number", "101", "service", "Fire Brigade", "description",
                                                "Fire fighting & rescue services", "priority", "4"),
                                Map.of("number", "100", "service", "Police Emergency", "description",
                                                "Immediate police dispatch control room", "priority", "5"),
                                Map.of("number", "1073", "service", "Highway Accident Helpline", "description",
                                                "National highways accident relief & towing", "priority", "6"),
                                Map.of("number", "1091", "service", "Women Helpline", "description",
                                                "24/7 dedicated helpline for women in distress", "priority", "7"));
        }

        /**
         * Records invocation of native telephony link without claiming connection.
         */
        @Transactional
        public void recordHelplineDial(String number, String callerPhone, String location) {
                EmergencyAlert alert = new EmergencyAlert();
                alert.setEmergencyType("STATUTORY_HELPLINE_DIAL");
                alert.setContactPhone(callerPhone != null ? callerPhone : "ANONYMOUS");
                alert.setCallerLocation(location != null ? location : "UNSPECIFIED");
                alert.setDialedHelpline(number);
                alert.setStatus(EmergencyStatus.HELPLINE_DIALED);
                alert.setNotes("User initiated direct phone call to official emergency helpline " + number);

                emergencyAlertRepository.save(alert);
                auditService.logEvent(null, "EMERGENCY_ALERT", alert.getId().toString(), "DIAL_HELPLINE", null,
                                "HELPLINE_DIALED", "Call initiated to " + number);
        }

        /**
         * Broadcasts secondary local community alert (mechanics, private towing, local
         * clinics).
         */
        @Transactional
        public EmergencyAlertDto broadcastCommunityAlert(EmergencyAlertDto dto, Long userId) {
                EmergencyAlert alert = new EmergencyAlert();
                alert.setUserId(userId);
                alert.setEmergencyType(dto.getEmergencyType());
                alert.setContactPhone(dto.getContactPhone());
                alert.setCallerLocation(dto.getCallerLocation());
                alert.setCallerLandmark(dto.getCallerLandmark());
                alert.setNotes(dto.getNotes());
                alert.setStatus(EmergencyStatus.LOCAL_ASSISTANCE_REQUESTED);

                EmergencyAlert saved = emergencyAlertRepository.save(alert);

                auditService.logEvent(userId, "EMERGENCY_ALERT", saved.getId().toString(), "BROADCAST_ALERT",
                                null, "LOCAL_ASSISTANCE_REQUESTED",
                                "Urgent community alert broadcasted near " + saved.getCallerLocation());

                notificationService.sendUrgentAlert(
                                saved.getContactPhone(),
                                "EMERGENCY_ALERT_BROADCASTED",
                                "Your urgent alert has been broadcasted to local responders. For life-threatening emergencies, always dial 112 or 108 directly.");

                return mapToDto(saved);
        }

        /**
         * Responder acknowledges viewing request details (Explicitly: NOT YET
         * DISPATCHED).
         */
        @Transactional
        public EmergencyAlertDto acknowledgeAlert(Long alertId, Long providerUserId) {
                EmergencyAlert alert = emergencyAlertRepository.findById(alertId)
                                .orElseThrow(() -> new ResourceNotFoundException("Emergency alert not found"));

                Provider provider = providerRepository.findByUserId(providerUserId)
                                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

                alert.setAssignedProviderId(provider.getId());
                alert.setStatus(EmergencyStatus.PROVIDER_ACKNOWLEDGED);
                EmergencyAlert saved = emergencyAlertRepository.save(alert);

                auditService.logEvent(providerUserId, "EMERGENCY_ALERT", saved.getId().toString(), "ACKNOWLEDGE_ALERT",
                                "LOCAL_ASSISTANCE_REQUESTED", "PROVIDER_ACKNOWLEDGED",
                                "Provider " + provider.getBusinessName() + " acknowledged alert");

                notificationService.sendNotification(
                                saved.getContactPhone(),
                                "RESPONDER_ACKNOWLEDGED",
                                provider.getBusinessName()
                                                + " has acknowledged your urgent request and is reviewing details. NOT yet dispatched.");

                return mapToDto(saved);
        }

        /**
         * Provider records departure with vehicle plate and ETA (Only now is it
         * CONFIRMED_EN_ROUTE).
         */
        @Transactional
        public EmergencyAlertDto confirmDispatch(Long alertId, Long providerUserId, EmergencyDispatchDto dispatchDto) {
                EmergencyAlert alert = emergencyAlertRepository.findById(alertId)
                                .orElseThrow(() -> new ResourceNotFoundException("Emergency alert not found"));

                if (alert.getStatus() != EmergencyStatus.PROVIDER_ACKNOWLEDGED) {
                        throw new InvalidStateException("Alert must be acknowledged before dispatch can be confirmed");
                }

                alert.setStatus(EmergencyStatus.CONFIRMED_EN_ROUTE);
                alert.setProviderEtaMinutes(dispatchDto.getEtaMinutes());
                alert.setProviderVehiclePlate(dispatchDto.getVehiclePlate());
                if (dispatchDto.getNotes() != null) {
                        alert.setNotes(alert.getNotes() + " | Dispatch Notes: " + dispatchDto.getNotes());
                }

                EmergencyAlert saved = emergencyAlertRepository.save(alert);

                auditService.logEvent(providerUserId, "EMERGENCY_ALERT", saved.getId().toString(), "CONFIRM_DISPATCH",
                                "PROVIDER_ACKNOWLEDGED", "CONFIRMED_EN_ROUTE",
                                "Dispatch verified: Plate " + saved.getProviderVehiclePlate() + ", ETA "
                                                + saved.getProviderEtaMinutes() + " mins");

                notificationService.sendNotification(
                                saved.getContactPhone(),
                                "DISPATCH_CONFIRMED",
                                "Help is on the way! Responder vehicle: " + saved.getProviderVehiclePlate() +
                                                ". Estimated arrival: " + saved.getProviderEtaMinutes() + " minutes.");

                return mapToDto(saved);
        }

        @Transactional
        public EmergencyAlertDto resolveAlert(Long alertId) {
                EmergencyAlert alert = emergencyAlertRepository.findById(alertId)
                                .orElseThrow(() -> new ResourceNotFoundException("Emergency alert not found"));

                alert.setStatus(EmergencyStatus.RESOLVED);
                EmergencyAlert saved = emergencyAlertRepository.save(alert);

                auditService.logEvent(null, "EMERGENCY_ALERT", saved.getId().toString(), "RESOLVE_ALERT", null,
                                "RESOLVED", "Emergency assistance marked resolved");
                return mapToDto(saved);
        }

        public EmergencyAlertDto getAlertById(Long alertId) {
                EmergencyAlert alert = emergencyAlertRepository.findById(alertId)
                                .orElseThrow(() -> new ResourceNotFoundException("Emergency alert not found"));
                return mapToDto(alert);
        }

        private EmergencyAlertDto mapToDto(EmergencyAlert alert) {
                EmergencyAlertDto dto = new EmergencyAlertDto();
                dto.setId(alert.getId());
                dto.setEmergencyType(alert.getEmergencyType());
                dto.setContactPhone(alert.getContactPhone());
                dto.setCallerLocation(alert.getCallerLocation());
                dto.setCallerLandmark(alert.getCallerLandmark());
                dto.setNotes(alert.getNotes());
                dto.setDialedHelpline(alert.getDialedHelpline());
                dto.setStatus(alert.getStatus());
                dto.setProviderEtaMinutes(alert.getProviderEtaMinutes());
                dto.setProviderVehiclePlate(alert.getProviderVehiclePlate());
                dto.setCreatedAt(alert.getCreatedAt());
                return dto;
        }
}
