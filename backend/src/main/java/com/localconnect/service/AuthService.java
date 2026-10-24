package com.localconnect.service;

import com.localconnect.config.JwtService;
import com.localconnect.domain.Provider;
import com.localconnect.domain.User;
import com.localconnect.domain.enums.RoleType;
import com.localconnect.domain.enums.VerificationStatus;
import com.localconnect.dto.AuthRequest;
import com.localconnect.dto.AuthResponse;
import com.localconnect.exception.InvalidStateException;
import com.localconnect.exception.ResourceNotFoundException;
import com.localconnect.repository.ProviderRepository;
import com.localconnect.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public AuthService(
            UserRepository userRepository,
            ProviderRepository providerRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            NotificationService notificationService,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.providerRepository = providerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    public void sendOtp(String phone) {
        // Generates standard 6-digit OTP (for prototype/dev, standard 123456 or random)
        String otp = "123456";
        notificationService.sendNotification(phone, "OTP", "Your LocalConnect verification code is " + otp);
    }

    @Transactional
    public AuthResponse loginOrRegister(AuthRequest request) {
        // Validate OTP (standard simulated OTP check)
        if (request.getOtp() != null && !request.getOtp().equals("123456") && !request.getOtp().equals("000000")) {
            throw new InvalidStateException("Invalid OTP entered. Please try again.");
        }

        User user = userRepository.findByPhone(request.getPhone())
                .orElseGet(() -> {
                    String name = (request.getFullName() != null && !request.getFullName().isBlank())
                            ? request.getFullName()
                            : "User " + request.getPhone().substring(request.getPhone().length() - 4);
                    User newUser = new User(
                            request.getPhone(),
                            name,
                            passwordEncoder.encode("DefaultSecret123!"),
                            RoleType.CUSTOMER);
                    if (request.getPreferredLanguage() != null) {
                        newUser.setPreferredLanguage(request.getPreferredLanguage());
                    }
                    return userRepository.save(newUser);
                });

        auditService.logEvent(user.getId(), "USER", user.getId().toString(), "LOGIN", null, "ACTIVE",
                "User authenticated via mobile OTP");

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse switchRole(Long userId, String targetRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        RoleType role = RoleType.valueOf(targetRole.toUpperCase());

        if (role == RoleType.PROVIDER && !user.getRoles().contains(RoleType.PROVIDER)) {
            // First time switching to Provider: provision basic Provider profile
            user.getRoles().add(RoleType.PROVIDER);
            if (providerRepository.findByUserId(user.getId()).isEmpty()) {
                Provider provider = new Provider();
                provider.setUser(user);
                provider.setBusinessName(user.getFullName() + " Services");
                provider.setOperationalDistrict("Local District");
                provider.setOperationalTaluka("Local Taluka");
                provider.setServiceBase("Town Center");
                provider.setVerificationStatus(VerificationStatus.PENDING_APPROVAL);
                providerRepository.save(provider);
            }
        }

        user.setCurrentRole(role.name());
        userRepository.save(user);

        auditService.logEvent(user.getId(), "USER", user.getId().toString(), "ROLE_SWITCH", null, role.name(),
                "User switched active role to " + role.name());

        return generateAuthResponse(user);
    }

    public AuthResponse getCurrentUserResponse(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return generateAuthResponse(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        Map<String, Object> claims = new HashMap<>();
        Set<String> roleNames = user.getRoles().stream().map(Enum::name).collect(Collectors.toSet());
        claims.put("roles", roleNames);
        claims.put("currentRole", user.getCurrentRole());
        claims.put("userId", user.getId());

        String token = jwtService.generateToken(user.getPhone(), claims);

        return new AuthResponse(
                token,
                user.getId(),
                user.getPhone(),
                user.getFullName(),
                user.getCurrentRole(),
                roleNames,
                user.getPreferredLanguage());
    }
}
