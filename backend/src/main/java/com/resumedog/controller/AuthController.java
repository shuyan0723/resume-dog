package com.resumedog.controller;

import com.resumedog.dto.ApiResponse;
import com.resumedog.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证", description = "注册与登录")
public class AuthController {

    private final AuthService authService;

    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            String nickname
    ) {
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
    }

    @PostMapping("/register")
    @Operation(summary = "注册，返回 JWT")
    public ApiResponse<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        String token = authService.register(request.email(), request.password(), request.nickname());
        return ApiResponse.ok(Map.of("token", token));
    }

    @PostMapping("/login")
    @Operation(summary = "登录，返回 JWT")
    public ApiResponse<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());
        return ApiResponse.ok(Map.of("token", token));
    }
}
