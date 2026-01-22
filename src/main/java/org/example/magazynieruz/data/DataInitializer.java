package org.example.magazynieruz.data;

import lombok.RequiredArgsConstructor;
import org.example.magazynieruz.model.*;
import org.example.magazynieruz.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final OrganisationRepository organisationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LocationRepository locationRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;


    @Override
    public void run(String... args) throws Exception {
        Role userRole = createRoleIfNotExists("ROLE_USER");
        Role adminRole = createRoleIfNotExists("ROLE_ADMIN");

        Organisation org =  createOrganisationIfNotExists("UZ", "1234567890");
        Organisation org2 = createOrganisationIfNotExists("TestCorp", "9876543210");
        
        Warehouse warehouse = createWarehouseIfNotExists("XYZ123", org);
        Location location = createLocationIfNotExists("A1-01-01", warehouse);
        Product product = createProductIfNotExists(1001L, location);

        // Create super admin (no organisation - can manage all organisations)
        createUserIfNotExists("SUPERADMIN", "admin123", null, Set.of(adminRole));
        
        // Create regular user for UZ organisation
        createUserIfNotExists("MAGAZYNIER", "1234", org, Set.of(userRole));
        
        // Create regular user for TestCorp organisation (to demonstrate organisation separation)
        createUserIfNotExists("USER_TESTCORP", "1234", org2, Set.of(userRole));
    }

    private Role createRoleIfNotExists(String roleName) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = new Role(roleName);
            return roleRepository.save(role);
        }
        // Find existing role
        for (Role role : roleRepository.findAll()) {
            if (role.getName().equals(roleName)) {
                return role;
            }
        }
        // Fallback - should not happen
        Role role = new Role(roleName);
        return roleRepository.save(role);
    }

    private Organisation createOrganisationIfNotExists(String organisationName, String TIN) {
        return organisationRepository.findAll().stream()
                .filter(o -> o.getName().equals(organisationName))
                .findFirst()
                .orElseGet(() -> {
                    Organisation org = new Organisation();
                    org.setName(organisationName);
                    org.setTIN(TIN);
                    return organisationRepository.save(org);
                });
    }
    private Warehouse createWarehouseIfNotExists(String warehouseCode, Organisation organisation) {
        return warehouseRepository.findAll().stream()
                .filter(o -> o.getWarehouseCode().equals(warehouseCode))
                .findFirst()
                .orElseGet(() -> {
                    Warehouse w = Warehouse.builder()
                            .warehouseName("default")
                            .warehouseCode("XYZ123")
                            .isActive(true)
                            .organisation(organisation)
                            .address(StructuredAddress.builder()
                                    .street("Default Street")
                                    .houseNumber("1")
                                    .apartmentNumber("1")
                                    .city("Default City")
                                    .postcode("00000")
                                    .latitude(12d)
                                    .longitude(12d)
                                    .build())
                            .build();
                    return warehouseRepository.save(w);
                });
    }


    private Location createLocationIfNotExists(String locationCode, Warehouse warehouse) {
        return locationRepository.findAll().stream()
                .filter(o -> o.getLocationCode().equals(locationCode))
                .findFirst()
                .orElseGet(() -> {
                    Location l = Location.builder()
                            .warehouse(warehouse)
                            .locationCode("A1-01-01")
                            .zoneName("STREFA_A")
                            .locationType(LocationType.BULK)
                            .isActive(true)
                            .isLocked(false)
                            .build();
                    return locationRepository.save(l);
                });
    }

    private Product createProductIfNotExists(Long productId, Location location) {
        return productRepository.findAll().stream()
                .filter(o -> o.getProductId().equals(productId))
                .findFirst()
                .orElseGet(() -> {
                    Product p = Product.builder()
                            .name("Marker permanentny")
                            .description("Czarny, wodoodporny")
                            .price(6.90)
                            .quantity(240)
                            .location(location)
                            .build();
                    return productRepository.save(p);
                });
    }


    private void createUserIfNotExists(String username, String rawPassword, Organisation org, Set<Role> roles) {
        if (!userRepository.existsByUsername(username)) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setOrganisation(org);
            user.setRoles(roles);
            userRepository.save(user);
        }
    }
}