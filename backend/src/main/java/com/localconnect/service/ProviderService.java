package com.localconnect.service;

import com.localconnect.domain.*;
import com.localconnect.domain.enums.ResourceCategory;
import com.localconnect.domain.enums.VerificationStatus;
import com.localconnect.dto.DriverDto;
import com.localconnect.dto.ProviderProfileDto;
import com.localconnect.dto.ResourceDto;
import com.localconnect.exception.InvalidStateException;
import com.localconnect.exception.ResourceNotFoundException;
import com.localconnect.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProviderService {

    private final ProviderRepository providerRepository;
    private final ResourceRepository resourceRepository;
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public ProviderService(
            ProviderRepository providerRepository,
            ResourceRepository resourceRepository,
            DriverRepository driverRepository,
            UserRepository userRepository,
            AuditService auditService) {
        this.providerRepository = providerRepository;
        this.resourceRepository = resourceRepository;
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ProviderProfileDto updateProfile(Long userId, ProviderProfileDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Provider provider = providerRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Provider p = new Provider();
                    p.setUser(user);
                    return p;
                });

        provider.setBusinessName(dto.getBusinessName());
        provider.setOperationalDistrict(dto.getOperationalDistrict());
        provider.setOperationalTaluka(dto.getOperationalTaluka());
        provider.setServiceBase(dto.getServiceBase());

        Provider saved = providerRepository.save(provider);
        auditService.logEvent(userId, "PROVIDER", saved.getId().toString(), "UPDATE_PROFILE", null,
                saved.getVerificationStatus().name(), "Provider profile updated");
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public ProviderProfileDto getProfileByUserId(Long userId) {
        Provider provider = providerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider profile not found"));
        return mapToDto(provider);
    }

    @Transactional(readOnly = true)
    public ProviderProfileDto getProfileById(Long id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));
        return mapToDto(provider);
    }

    @Transactional
    public ResourceDto addResource(Long userId, ResourceDto dto) {
        Provider provider = providerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        Resource resource = new Resource();
        resource.setProvider(provider);
        resource.setCategory(dto.getCategory());
        resource.setSubType(dto.getSubType());
        resource.setTitle(dto.getTitle());
        resource.setRegistrationNumber(dto.getRegistrationNumber());
        resource.setCapacity(dto.getCapacity());
        resource.setBasePrice(dto.getBasePrice());
        resource.setPricingUnit(dto.getPricingUnit() != null ? dto.getPricingUnit() : "per_day");
        // Mandatory verification gate: starts as PENDING_APPROVAL
        resource.setApprovalStatus(VerificationStatus.PENDING_APPROVAL);
        resource.setActive(true);

        Resource saved = resourceRepository.save(resource);
        auditService.logEvent(userId, "RESOURCE", saved.getId().toString(), "REGISTER_RESOURCE", null,
                "PENDING_APPROVAL", "Resource registered: " + saved.getTitle());
        return mapResourceToDto(saved);
    }

    @Transactional
    public DriverDto addDriver(Long userId, DriverDto dto) {
        Provider provider = providerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        Driver driver = new Driver();
        driver.setProvider(provider);
        driver.setFullName(dto.getFullName());
        driver.setPhone(dto.getPhone());
        driver.setLicenseNumber(dto.getLicenseNumber());
        driver.setLicenseExpiry(dto.getLicenseExpiry());
        driver.setVerified(false); // requires document verification
        driver.setActive(true);

        Driver saved = driverRepository.save(driver);
        auditService.logEvent(userId, "DRIVER", saved.getId().toString(), "ADD_DRIVER", null, "UNVERIFIED",
                "Driver added: " + saved.getFullName());
        return mapDriverToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ResourceDto> getProviderResources(Long userId) {
        Provider provider = providerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));
        return resourceRepository.findByProviderId(provider.getId()).stream()
                .map(this::mapResourceToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DriverDto> getProviderDrivers(Long userId) {
        Provider provider = providerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));
        return driverRepository.findByProviderIdAndIsActiveTrue(provider.getId()).stream()
                .map(this::mapDriverToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResourceDto> searchActiveResources(ResourceCategory category, String subType) {
        List<Resource> resources;
        if (subType != null && !subType.isBlank()) {
            resources = resourceRepository.findActiveByCategoryAndSubType(category, subType);
        } else {
            resources = resourceRepository.findActiveByCategory(category);
        }
        return resources.stream().map(this::mapResourceToDto).collect(Collectors.toList());
    }

    private ProviderProfileDto mapToDto(Provider p) {
        ProviderProfileDto dto = new ProviderProfileDto();
        dto.setId(p.getId());
        dto.setUserId(p.getUser().getId());
        dto.setBusinessName(p.getBusinessName());
        dto.setOperationalDistrict(p.getOperationalDistrict());
        dto.setOperationalTaluka(p.getOperationalTaluka());
        dto.setServiceBase(p.getServiceBase());
        dto.setRatingAverage(p.getRatingAverage());
        dto.setTotalCompletedJobs(p.getTotalCompletedJobs());
        dto.setVerificationStatus(p.getVerificationStatus());
        return dto;
    }

    private ResourceDto mapResourceToDto(Resource r) {
        ResourceDto dto = new ResourceDto();
        dto.setId(r.getId());
        dto.setProviderId(r.getProvider().getId());
        dto.setProviderBusinessName(r.getProvider().getBusinessName());
        dto.setCategory(r.getCategory());
        dto.setSubType(r.getSubType());
        dto.setTitle(r.getTitle());
        dto.setRegistrationNumber(r.getRegistrationNumber());
        dto.setCapacity(r.getCapacity());
        dto.setBasePrice(r.getBasePrice());
        dto.setPricingUnit(r.getPricingUnit());
        dto.setApprovalStatus(r.getApprovalStatus());
        dto.setActive(r.isActive());
        return dto;
    }

    private DriverDto mapDriverToDto(Driver d) {
        DriverDto dto = new DriverDto();
        dto.setId(d.getId());
        dto.setProviderId(d.getProvider().getId());
        dto.setFullName(d.getFullName());
        dto.setPhone(d.getPhone());
        dto.setLicenseNumber(d.getLicenseNumber());
        dto.setLicenseExpiry(d.getLicenseExpiry());
        dto.setVerified(d.isVerified());
        dto.setActive(d.isActive());
        return dto;
    }
}
