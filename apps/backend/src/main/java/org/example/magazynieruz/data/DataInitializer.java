package org.example.magazynieruz.data;

import org.example.magazynieruz.model.Organisation;
import org.example.magazynieruz.model.Role;
import org.example.magazynieruz.model.User;
import org.example.magazynieruz.repository.OrganisationRepository;
import org.example.magazynieruz.repository.RoleRepository;
import org.example.magazynieruz.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final OrganisationRepository organisationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, OrganisationRepository organisationRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.organisationRepository = organisationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        createRoleIfNotExists("ROLE_USER");
        createRoleIfNotExists("ROLE_ADMIN");

        Organisation org =  createOrganisationIfNotExists("UZ", "1234567890");

        createUserIfNotExists("MAGAZYNIER","1234",org,null);
    }

    private void createRoleIfNotExists(String roleName) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = new Role(roleName);
            roleRepository.save(role);
        }
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