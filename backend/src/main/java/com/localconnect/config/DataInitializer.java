package com.localconnect.config;

import com.localconnect.domain.*;
import com.localconnect.domain.enums.ResourceCategory;
import com.localconnect.domain.enums.RoleType;
import com.localconnect.domain.enums.VerificationStatus;
import com.localconnect.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;
    private final ResourceRepository resourceRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            ProviderRepository providerRepository,
            ResourceRepository resourceRepository,
            DriverRepository driverRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.providerRepository = providerRepository;
        this.resourceRepository = resourceRepository;
        this.driverRepository = driverRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        // 1. Admin
        User admin = new User("9999999999", "District Admin Officer", passwordEncoder.encode("AdminPass123!"), RoleType.ADMIN);
        admin.setRoles(Set.of(RoleType.ADMIN, RoleType.CUSTOMER));
        userRepository.save(admin);

        // 2. Customer
        User customer = new User("9876543210", "Suresh Deshmukh (Customer)", passwordEncoder.encode("UserPass123!"), RoleType.CUSTOMER);
        userRepository.save(customer);

        // 3. Provider 1: Shree Ganesh Transport
        User providerUser1 = new User("9822001122", "Ganesh Jadhav", passwordEncoder.encode("ProviderPass123!"), RoleType.PROVIDER);
        providerUser1.setRoles(Set.of(RoleType.PROVIDER, RoleType.CUSTOMER));
        userRepository.save(providerUser1);

        Provider provider1 = new Provider();
        provider1.setUser(providerUser1);
        provider1.setBusinessName("Shree Ganesh Transport & Travels");
        provider1.setOperationalDistrict("Akola");
        provider1.setOperationalTaluka("Akola Rural");
        provider1.setServiceBase("Kisan Mandi Chowk");
        provider1.setRatingAverage(BigDecimal.valueOf(4.8));
        provider1.setTotalCompletedJobs(42);
        provider1.setVerificationStatus(VerificationStatus.APPROVED);
        provider1.setVerifiedAt(LocalDateTime.now().minusDays(30));
        providerRepository.save(provider1);

        // Provider 1 Resources
        Resource r1 = new Resource();
        r1.setProvider(provider1);
        r1.setCategory(ResourceCategory.VEHICLE);
        r1.setSubType("car");
        r1.setTitle("Mahindra Bolero (With AC & Driver)");
        r1.setRegistrationNumber("MH-30-AB-1234");
        r1.setCapacity(7);
        r1.setBasePrice(BigDecimal.valueOf(2500.00));
        r1.setPricingUnit("per_day");
        r1.setApprovalStatus(VerificationStatus.APPROVED);
        r1.setActive(true);
        resourceRepository.save(r1);

        Resource r2 = new Resource();
        r2.setProvider(provider1);
        r2.setCategory(ResourceCategory.TRANSPORT);
        r2.setSubType("pickup");
        r2.setTitle("Mahindra Bolero Maxi Truck (1.5 Ton)");
        r2.setRegistrationNumber("MH-30-P-5678");
        r2.setCapacity(1500);
        r2.setBasePrice(BigDecimal.valueOf(1800.00));
        r2.setPricingUnit("per_trip");
        r2.setApprovalStatus(VerificationStatus.APPROVED);
        r2.setActive(true);
        resourceRepository.save(r2);

        // Provider 1 Drivers
        Driver d1 = new Driver();
        d1.setProvider(provider1);
        d1.setFullName("Rajesh Shinde");
        d1.setPhone("9822001123");
        d1.setLicenseNumber("MH3020200001234");
        d1.setLicenseExpiry(LocalDate.now().plusYears(4));
        d1.setVerified(true);
        d1.setActive(true);
        driverRepository.save(d1);

        Driver d2 = new Driver();
        d2.setProvider(provider1);
        d2.setFullName("Raju Patil");
        d2.setPhone("9822001124");
        d2.setLicenseNumber("MH3020190005678");
        d2.setLicenseExpiry(LocalDate.now().plusYears(3));
        d2.setVerified(true);
        d2.setActive(true);
        driverRepository.save(d2);

        // 4. Provider 2: Kisan Homestay & Rooms
        User providerUser2 = new User("9822003344", "Pandurang Patil", passwordEncoder.encode("ProviderPass123!"), RoleType.PROVIDER);
        providerUser2.setRoles(Set.of(RoleType.PROVIDER, RoleType.CUSTOMER));
        userRepository.save(providerUser2);

        Provider provider2 = new Provider();
        provider2.setUser(providerUser2);
        provider2.setBusinessName("Kisan Homestay & Yatri Niwas");
        provider2.setOperationalDistrict("Akola");
        provider2.setOperationalTaluka("Shegaon");
        provider2.setServiceBase("Near Mandir Road");
        provider2.setRatingAverage(BigDecimal.valueOf(4.6));
        provider2.setTotalCompletedJobs(19);
        provider2.setVerificationStatus(VerificationStatus.APPROVED);
        provider2.setVerifiedAt(LocalDateTime.now().minusDays(15));
        providerRepository.save(provider2);

        Resource r3 = new Resource();
        r3.setProvider(provider2);
        r3.setCategory(ResourceCategory.ROOM);
        r3.setSubType("homestay");
        r3.setTitle("Kisan Farm Homestay (Family AC Room)");
        r3.setRegistrationNumber("HS-AK-09");
        r3.setCapacity(4);
        r3.setBasePrice(BigDecimal.valueOf(1200.00));
        r3.setPricingUnit("per_day");
        r3.setApprovalStatus(VerificationStatus.APPROVED);
        r3.setActive(true);
        resourceRepository.save(r3);
    }
}

