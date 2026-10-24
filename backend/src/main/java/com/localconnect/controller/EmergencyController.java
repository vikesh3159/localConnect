package com.localconnect.controller;

import com.localconnect.domain.User;
import com.localconnect.dto.EmergencyAlertDto;
import com.localconnect.dto.EmergencyDispatchDto;
import com.localconnect.service.EmergencyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/emergency")
public class EmergencyController {

    private final EmergencyService emergencyService;

    public EmergencyController(EmergencyService emergencyService) {
        this.emergencyService = emergencyService;
    }

    /**
     * Primary Statutory Emergency Directory (112, 108, 101, 100, 1073)
     */
    @GetMapping("/helplines")
    public ResponseEntity<List<Map<String, String>>> getHelplines() {
        return ResponseEntity.ok(emergencyService.getOfficialHelplines());
    }

    /**
     * Records when user taps direct phone dialer
     */
    @PostMapping("/dial")
    public ResponseEntity<Map<String, String>> recordDial(@RequestBody Map<String, String> body) {
        emergencyService.recordHelplineDial(body.get("number"), body.get("phone"), body.get("location"));
        return ResponseEntity.ok(Map.of("message", "Helpline dial recorded"));
    }

    /**
     * Secondary Community Assistance Alert
     */
    @PostMapping("/broadcast")
    public ResponseEntity<EmergencyAlertDto> broadcastCommunityAlert(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody EmergencyAlertDto dto) {
        Long userId = user != null ? user.getId() : null;
        EmergencyAlertDto created = emergencyService.broadcastCommunityAlert(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Local Responder acknowledges viewing alert (Message: NOT YET DISPATCHED)
     */
    @PostMapping("/{id}/ack")
    public ResponseEntity<EmergencyAlertDto> acknowledgeAlert(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(emergencyService.acknowledgeAlert(id, user.getId()));
    }

    /**
     * Responder inputs departure plate and ETA -> Only now CONFIRMED_EN_ROUTE
     */
    @PostMapping("/{id}/dispatch")
    public ResponseEntity<EmergencyAlertDto> confirmDispatch(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody EmergencyDispatchDto dispatchDto) {
        return ResponseEntity.ok(emergencyService.confirmDispatch(id, user.getId(), dispatchDto));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<EmergencyAlertDto> resolveAlert(@PathVariable Long id) {
        return ResponseEntity.ok(emergencyService.resolveAlert(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmergencyAlertDto> getAlertById(@PathVariable Long id) {
        return ResponseEntity.ok(emergencyService.getAlertById(id));
    }
}
