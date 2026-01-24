package org.example.magazynieruz.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.magazynieruz.dto.user.UserRoleResponse;
import org.example.magazynieruz.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User operations for authenticated users")
@SecurityRequirement(name = "bearer-jwt")
public class UserController {

    @GetMapping("/role")
    @Operation(summary = "Get current user's role", description = "Returns the roles of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user roles"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<UserRoleResponse> getCurrentUserRole(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User user) {

            Set<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            
            UserRoleResponse response = new UserRoleResponse(
                    user.getUserId(),
                    user.getUsername(),
                    roles
            );
            
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.status(401).build();
    }
}
