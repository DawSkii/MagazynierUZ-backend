package org.example.magazynieruz.security;

import org.example.magazynieruz.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserContext {
    public Long getCurrentOrganisationId() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Authentication required");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            User user = (User) principal;

            if (user.getOrganisation() != null) {
                return user.getOrganisation().getId();
            } else {
                throw new IllegalStateException("Logged user (ID: " + user.getUserId() + ") has no organisation");
            }
        }

        throw new IllegalStateException("Unknown user type: " + principal.getClass());
    }
}
