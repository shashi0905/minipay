package com.shashi.minipay.payment.controller;

import com.shashi.minipay.payment.dto.request.LoginRequest;
import com.shashi.minipay.payment.dto.request.RegisterRequest;
import com.shashi.minipay.payment.dto.response.AuthResponse;
import com.shashi.minipay.payment.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request.usernameOrEmail(), request.password());
        return ResponseEntity.ok(response);
    }
}
