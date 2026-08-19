package com.vehisales.platform.api;

import com.vehisales.platform.api.dto.LoginRequest;
import com.vehisales.platform.api.dto.LoginResponse;
import com.vehisales.platform.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        String username = request == null ? "" : request.username();
        String password = request == null ? "" : request.password();
        return new LoginResponse(adminAuthService.login(username, password), username);
    }
}
