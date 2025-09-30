package com.mogou.controller;

import com.mogou.dto.*;
import com.mogou.service.AuthService;
import com.mogou.service.SchoolRegistrationService;
import com.mogou.service.CompanyRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SchoolRegistrationService schoolRegistrationService;
    private final CompanyRegistrationService companyRegistrationService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/register/school")
    public ResponseEntity<AuthResponse> registerSchool(@Valid @RequestBody SchoolRegistrationRequest request) {
        return ResponseEntity.ok(schoolRegistrationService.registerSchool(request));
    }

    @PostMapping("/register/student")
    public ResponseEntity<AuthResponse> registerStudent(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/register/company")
    public ResponseEntity<AuthResponse> registerCompany(@Valid @RequestBody CompanyRegistrationRequest request) {
        return ResponseEntity.ok(companyRegistrationService.registerCompany(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth service is working");
    }
    
    @PostMapping("/test-school")
    public ResponseEntity<Map<String, Object>> testSchool(@RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("received", data);
        response.put("message", "Test endpoint working");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register/school-simple")
    public ResponseEntity<Map<String, Object>> registerSchoolSimple(@RequestBody SimpleSchoolRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Simple school registration received");
        response.put("data", request);
        return ResponseEntity.ok(response);
    }
}