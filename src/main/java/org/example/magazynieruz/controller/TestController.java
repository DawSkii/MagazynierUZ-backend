package org.example.magazynieruz.controller;

import org.example.magazynieruz.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for testing and debugging purposes.
 * Contains endpoints for basic connectivity and authentication testing.
 */
@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * Simple test endpoint to verify API connectivity.
     *
     * @return test string
     */
    @GetMapping("/get")
    public String test(){
        return "test";
    }

    /**
     * Test endpoint to verify user authentication and principal extraction.
     *
     * @param loggedUser the authenticated user
     * @return ResponseEntity containing the authenticated user details
     */
    @GetMapping("/usercheck")
    public ResponseEntity<User> testUser(@AuthenticationPrincipal User loggedUser){
     return ResponseEntity.ok(loggedUser);
    }
}
